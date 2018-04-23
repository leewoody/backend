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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageStatusSupport;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFolderStatus;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ExamineAction}
 *
 * @author <a href="mailto:joshua.wirtz@open-xchange.com">Joshua Wirtz</a>
 */
public class ExamineAction extends AbstractMailAction {

    /**
     * Initializes a new {@link ExamineAction}.
     *
     * @param services The service look-up
     */
    public ExamineAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException, JSONException {
        String folder = req.checkParameter("folder");
        ServerSession session = req.getSession();
        FullnameArgument fullnameArgument = MailFolderUtility.prepareMailFolderParam(folder);

        MailServletInterface mailInterface = getMailInterface(req);
        mailInterface.openFor(folder);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = mailInterface.getMailAccess();
        {
            MailConfig mailConfig = mailAccess.getMailConfig();
            MailCapabilities capabilities = mailConfig.getCapabilities();

            if (!capabilities.hasFolderValidity()) {
                throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
            }

            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();

            IMailFolderStorageStatusSupport validityFolderStorage = folderStorage.supports(IMailFolderStorageStatusSupport.class);
            if (null == validityFolderStorage) {
                throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
            }

            if (!validityFolderStorage.isStatusSupported()) {
                throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
            }

            MailFolderStatus status = validityFolderStorage.getFolderStatus(fullnameArgument.getFullname());
            JSONObject jStatus = new JSONObject(6)
                .put("validity", status.getValidity())
                .put("total", status.getTotal())
                .put("unread", status.getUnread())
                .put("next", status.getNextId());

            return new AJAXRequestResult(jStatus, "json");
        }
    }

}
