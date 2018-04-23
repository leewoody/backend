/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.mail.json.actions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.mail.MessageRemovedException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.IOs;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.server.ServiceLookup;

/**
 * {@link GetMultipleMessagesAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetMultipleMessagesAction extends AbstractMailAction {

    /**
     * Initializes a new {@link GetMultipleMessagesAction}.
     *
     * @param services
     */
    public GetMultipleMessagesAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException, JSONException {
        List<IdFolderPair> pairs;

        // Parse pairs
        {
            String value = req.getParameter("body");
            if (isEmpty(value)) {
                String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
                String[] ids;
                {
                    String parameterId = AJAXServlet.PARAMETER_ID;
                    String sIds = req.getParameter(parameterId);
                    if (null == sIds) {
                        JSONArray jArray = (JSONArray) req.getRequest().requireData();
                        int length = jArray.length();
                        ids = new String[length];
                        for (int i = 0; i < length; i++) {
                            ids[i] = jArray.getJSONObject(i).getString(parameterId);
                        }
                    } else {
                        ids = Strings.splitByComma(sIds);
                    }
                }
                pairs = new ArrayList<IdFolderPair>(ids.length);
                for (int i = 0; i < ids.length; i++) {
                    pairs.add(new IdFolderPair(ids[i], folderPath));
                }
            } else {
                JSONArray jsonArray = new JSONArray(value);
                int len = jsonArray.length();
                pairs = new ArrayList<IdFolderPair>(len);
                for (int i = 0; i < len; i++) {
                    JSONObject tuple = jsonArray.getJSONObject(i);
                    // Identifier
                    String id = tuple.getString(DataFields.ID);
                    // Folder
                    String folderId = tuple.optString(FolderChildFields.FOLDER_ID, null);
                    if (null == folderId) {
                        folderId = tuple.optString(AJAXServlet.PARAMETER_FOLDERID, null);
                    }
                    IdFolderPair pair = new IdFolderPair(id, folderId);
                    pairs.add(pair);
                }
            }
        }

        // Sort them
        Collections.sort(pairs);

        // Do it...
        AJAXRequestData ajaxRequestData = req.getRequest();
        if (ajaxRequestData.setResponseHeader("Content-Type", "application/zip")) {
            // Write directly... set HTTP response headers
            {
                StringBuilder sb = new StringBuilder(512);
                sb.append("attachment");
                DownloadUtility.appendFilenameParameter("mails.zip", "application/zip", ajaxRequestData.getUserAgent(), sb);
                ajaxRequestData.setResponseHeader("Content-Disposition", sb.toString());
            }

            try {
                performMultipleFolder(req, pairs, ajaxRequestData.optOutputStream());
                return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
            } catch (IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        }

        // Store to .tmp folder
        ThresholdFileHolder thresholdFileHolder = new ThresholdFileHolder();
        boolean error = true;
        try {
            performMultipleFolder(req, pairs, thresholdFileHolder.asOutputStream());
            error = false;
        } finally {
            // Close on error
            if (error) {
                Streams.close(thresholdFileHolder);
            }
        }

        // Set meta information
        req.getRequest().setFormat("file");
        thresholdFileHolder.setContentType("application/zip");
        thresholdFileHolder.setName("mails.zip");

        // Return AJAX result
        return new AJAXRequestResult(thresholdFileHolder, "file");
    }

    private void performMultipleFolder(MailRequest req, List<IdFolderPair> pairs, OutputStream stream) throws OXException {
        final MailServletInterface mailInterface = getMailInterface(req);

        // Initialize ZIP'ing
        ZipArchiveOutputStream zipOutput = null;
        try {
            int size = pairs.size();
            Set<String> names = new HashSet<String>(size);
            String ext = ".eml";

            for (int i = 0; i < size; i++) {
                final IdFolderPair pair = pairs.get(i);
                final MailMessage message = mailInterface.getMessage(pair.folderId, pair.identifier, false);
                if (null != message) {
                    // Initialize ZIP output stream (if not already done)
                    if (zipOutput == null) {
                        zipOutput = new ZipArchiveOutputStream(stream);
                        zipOutput.setEncoding("UTF-8");
                        zipOutput.setUseLanguageEncodingFlag(true);
                    }

                    // Determine proper name for message-associated entry
                    String name;
                    {
                        String subject = message.getSubject();
                        name = (isEmpty(subject) ? "mail" + (i + 1) : MailServletInterface.saneForFileName(subject)) + ext;
                        int reslen = name.lastIndexOf('.');
                        int count = 1;
                        while (false == names.add(name)) {
                            // Name already contained
                            name = name.substring(0, reslen);
                            name = new StringBuilder(name).append("_(").append(count++).append(')').append(ext).toString();
                        }
                    }

                    // Add ZIP entry to output stream
                    ZipArchiveEntry entry;
                    {
                        int num = 1;
                        while (true) {
                            try {
                                final int pos = name.indexOf(ext);
                                final String entryName = name.substring(0, pos) + (num > 1 ? "_(" + num + ")" : "") + ext;
                                entry = new ZipArchiveEntry(entryName);
                                zipOutput.putArchiveEntry(entry);
                                break;
                            } catch (final java.util.zip.ZipException e) {
                                final String eMessage = e.getMessage();
                                if (eMessage == null || !eMessage.startsWith("duplicate entry")) {
                                    throw e;
                                }
                                num++;
                            }
                        }
                    }

                    // Transfer bytes from the message to the ZIP stream
                    long before = zipOutput.getBytesWritten();
                    message.writeTo(zipOutput);
                    long entrySize = zipOutput.getBytesWritten() - before;
                    entry.setSize(entrySize);

                    // Complete the entry
                    zipOutput.closeArchiveEntry();
                }
            }
        } catch (IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            OXException oxe = MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            if (IOs.isConnectionReset(e)) {
                /*-
                 * A "java.io.IOException: Connection reset by peer" is thrown when the other side has abruptly aborted the connection in midst of a transaction.
                 *
                 * That can have many causes which are not controllable from the Middleware side. E.g. the end-user decided to shutdown the client or change the
                 * server abruptly while still interacting with your server, or the client program has crashed, or the enduser's Internet connection went down,
                 * or the enduser's machine crashed, etc, etc.
                 */
                oxe.markLightWeight();
            }
            throw oxe;
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            // Complete the ZIP file
            Streams.close(zipOutput);
        }
    }

    private final class IdFolderPair implements Comparable<IdFolderPair> {

        final String identifier;
        final String folderId;

        IdFolderPair(final String identifier, final String folderId) {
            super();
            this.identifier = identifier;
            this.folderId = folderId;
        }

        @Override
        public int compareTo(IdFolderPair o) {
            int retval = folderId.compareTo(o.folderId);
            if (0 == retval) {
                retval = identifier.compareTo(o.identifier);
            }
            return retval;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            if (identifier != null) {
                builder.append("identifier=").append(identifier).append(", ");
            }
            if (folderId != null) {
                builder.append("folderId=").append(folderId);
            }
            builder.append("]");
            return builder.toString();
        }
    }

}
