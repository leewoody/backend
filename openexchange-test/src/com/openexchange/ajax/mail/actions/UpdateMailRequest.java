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

package com.openexchange.ajax.mail.actions;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link UpdateMailRequest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class UpdateMailRequest extends AbstractMailRequest<UpdateMailResponse> {

    private String folderID;

    private String mailID;

    private int flags;

    private int color;

    private boolean removeFlags;

    private boolean failOnError;

    private boolean messageId;

    public UpdateMailRequest setMessageId(final boolean messageId) {
        this.messageId = messageId;
        return this;
    }

    public boolean doesFailOnError() {
        return failOnError;
    }

    public void setFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }

    public String getFolderID() {
        return folderID;
    }

    public void setFolderID(final String folderID) {
        this.folderID = folderID;
    }

    public String getMailID() {
        return mailID;
    }

    public void setMailID(final String mailID) {
        this.mailID = mailID;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(final int flags) {
        this.flags = flags;
    }

    public int getColor() {
        return color;
    }

    public void setColor(final int color) {
        this.color = color;
    }

    public boolean doesRemoveFlags() {
        return removeFlags;
    }

    public void removeFlags() {
        this.removeFlags = true;
    }

    public boolean doesUpdateFlags() {
        return !removeFlags;
    }

    public void updateFlags() {
        this.removeFlags = false;
    }

    /**
     * Initializes a new {@link UpdateMailRequest}.
     */
    public UpdateMailRequest(final String folderID) {
        super();
        this.folderID = folderID;
        flags = -1;
        color = -1;
    }

    /**
     * Initializes a new {@link UpdateMailRequest}.
     */
    public UpdateMailRequest(final String folderID, final String mailID) {
        super();
        this.folderID = folderID;
        this.mailID = mailID;
        flags = -1;
        color = -1;
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONObject json = new JSONObject();
        if (color >= 0) {
            json.put("color_label", color);
        }
        if (flags >= 0) {
            json.put("flags", flags);
            json.put("value", !removeFlags);
        }
        return json;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        final List<Parameter> list = new LinkedList<Parameter>();

        list.add(new Parameter(Mail.PARAMETER_ACTION, Mail.ACTION_UPDATE));
        list.add(new Parameter(Mail.PARAMETER_FOLDERID, folderID));
        if (null != mailID) {
            list.add(new Parameter(messageId ? Mail.PARAMETER_MESSAGE_ID : Mail.PARAMETER_ID, mailID));
        }
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends UpdateMailResponse> getParser() {
        return new AbstractAJAXParser<UpdateMailResponse>(failOnError) {

            @Override
            protected UpdateMailResponse createResponse(final Response response) throws JSONException {
                return new UpdateMailResponse(response);
            }
        };
    }

}
