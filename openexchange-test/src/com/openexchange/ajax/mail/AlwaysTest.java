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

package com.openexchange.ajax.mail;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * @author marcus
 */
public class AlwaysTest extends AbstractAJAXSession {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AlwaysTest.class);

    /**
     * Random number generator.
     */
    private static final Random rand = new Random(System.currentTimeMillis());

    /**
     * Number of mails to read in each mail folder.
     * <ul>
     * <li><code>-1</code> list all mails
     * <li><code>0</code> list no mails
     * </ul>
     */
    private final static int MAX = -1;

    /**
     * This attributes of mails are requested when a mail folder is listed.
     */
    private static final int[] listAttributes = new int[] { MailListField.ID.getField(), MailListField.FOLDER_ID.getField(), MailListField.THREAD_LEVEL.getField(), MailListField.ATTACHMENT.getField(), MailListField.FROM.getField(), MailListField.SUBJECT.getField(), MailListField.RECEIVED_DATE.getField(), MailListField.SIZE.getField(), MailListField.FLAGS.getField(), MailListField.PRIORITY.getField(), CommonObject.COLOR_LABEL };

    private AJAXClient client;

    public AlwaysTest() {
        super();
    }

    @Test
    public void testFolderListing() throws Throwable {
        final FolderObject imapRoot = getIMAPRootFolder();
        recListFolder(imapRoot.getFullName(), "");
    }

    public void recListFolder(final String folderId, final String rights) throws Exception {
        LOG.trace("Listing {}", folderId);
        if (rights.length() > 0) {
            listMails(folderId, MAX);
        }
        final Map<String, String> subRights = getIMAPRights(client, folderId);
        for (final Entry<String, String> entry : subRights.entrySet()) {
            recListFolder(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @param folder
     * @param max
     * @throws Exception 
     */
    private void listMails(final String folderId, final int max) throws Exception {
        MailMessage[] mails = mtm.listMails(folderId, MailListField.ID.getAllFields(), MailListField.ID.getField(), Order.DESCENDING, true, Collections.EMPTY_LIST);
        assertFalse(mtm.getLastResponse().hasError());
        for (MailMessage mail : mails) {
            TestMail mailObj = mtm.get(mail.getFolder(), mail.getMailId());
            assertNotNull(mailObj);
            assertFalse(mtm.getLastResponse().hasError());
        }
    }

    public static Map<String, String> getIMAPRights(final AJAXClient client, final String parent) throws IOException, SAXException, JSONException, OXException {
        final ListResponse listR = client.execute(new ListRequest(EnumAPI.OX_OLD, parent, new int[] { FolderObject.OBJECT_ID, FolderObject.OWN_RIGHTS }, false));
        final Map<String, String> retval = new HashMap<String, String>();
        for (final Object[] row : listR) {
            retval.put(row[0].toString(), row[1].toString());
        }
        return retval;
    }

    public FolderObject getIMAPRootFolder() throws OXException, IOException, SAXException, JSONException, OXException {
        final ListResponse listR = getClient().execute(new ListRequest(EnumAPI.OX_OLD, String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID)));
        FolderObject defaultIMAPFolder = null;
        final Iterator<FolderObject> iter = listR.getFolder();
        while (iter.hasNext()) {
            final FolderObject fo = iter.next();
            if (fo.containsFullName() && fo.getFullName().equals(MailFolder.DEFAULT_FOLDER_ID)) {
                defaultIMAPFolder = fo;
                break;
            }
        }
        assertTrue("Can't find IMAP root folder.", defaultIMAPFolder != null && defaultIMAPFolder.hasSubfolders());
        return defaultIMAPFolder;
    }
}
