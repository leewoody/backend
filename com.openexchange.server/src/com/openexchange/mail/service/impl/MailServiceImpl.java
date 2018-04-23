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

package com.openexchange.mail.service.impl;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link MailServiceImpl} - The mail service implementation
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailServiceImpl implements MailService {

    /**
     * Initializes a new {@link MailServiceImpl}
     */
    public MailServiceImpl() {
        super();
    }

    @Override
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess(final Session session, final int accountId) throws OXException {
        return MailAccess.getInstance(session, accountId);
    }

    @Override
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess(final int userId, final int contextId, final int accountId) throws OXException {
        return MailAccess.getInstance(userId, contextId, accountId);
    }

    @Override
    public MailTransport getMailTransport(final Session session, final int accountId) throws OXException {
        return MailTransport.getInstance(session, accountId);
    }

    @Override
    public MailConfig getMailConfig(Session session, int accountId) throws OXException {
        return MailAccess.getInstance(session, accountId).getMailConfig();
    }

    @Override
    public TransportConfig getTransportConfig(Session session, int accountId) throws OXException {
        return MailTransport.getInstance(session, accountId).getTransportConfig();
    }

    @Override
    public String getMailLoginFor(int userId, int contextId, int accountId) throws OXException {
        // Get the user
        User user = UserStorage.getInstance().getUser(userId, contextId);

        // Get the mail account
        MailAccountStorageService service = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
        if (null == service) {
            throw ServiceExceptionCode.absentService(MailAccountStorageService.class);
        }
        MailAccount mailAccount = service.getMailAccount(accountId, userId, contextId);

        // Return login
        return MailConfig.getMailLogin(mailAccount, user.getLoginInfo(), userId, contextId);
    }

}
