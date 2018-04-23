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

package com.openexchange.mail.conversion;

import static com.openexchange.mail.mime.utils.MimeMessageUtility.shouldRetry;
import java.io.InputStream;
import com.openexchange.conversion.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.virtual.osgi.Services;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.crypto.CryptographicAwareMailAccessFactory;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.session.Session;

/**
 * {@link MailPartDataSource} - A generic {@link DataSource} for mail parts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailPartDataSource implements DataSource {

    /**
     * Common required arguments for uniquely determining a mail part:
     * <ul>
     * <li>com.openexchange.mail.conversion.fullname</li>
     * <li>com.openexchange.mail.conversion.mailid</li>
     * <li>com.openexchange.mail.conversion.sequenceid</li>
     * </ul>
     */
    protected static final String[] ARGS = {
        "com.openexchange.mail.conversion.fullname", "com.openexchange.mail.conversion.mailid",
        "com.openexchange.mail.conversion.sequenceid" };

    /**
     * Initializes a new {@link MailPartDataSource}
     */
    protected MailPartDataSource() {
        super();
    }

    protected final MailPart getMailPart(final int accountId, final String fullname, final String mailId, final String sequenceId, final Session session) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, accountId);
            CryptographicAwareMailAccessFactory cryptoMailAccessFactory = Services.getServiceLookup().getOptionalService(CryptographicAwareMailAccessFactory.class);
            if(cryptoMailAccessFactory != null) {
                mailAccess = cryptoMailAccessFactory.createAccess(
                    (MailAccess<IMailFolderStorage, IMailMessageStorage>) mailAccess,
                    session,
                    null);
            }
            mailAccess.connect();
            return loadPart(fullname, mailId, sequenceId, mailAccess);
        } catch (final OXException e) {
            if ((null != mailAccess) && shouldRetry(e)) {
                // Re-connect
                mailAccess = MailAccess.reconnect(mailAccess);
                return loadPart(fullname, mailId, sequenceId, mailAccess);
            }
            throw e;
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    private MailPart loadPart(final String fullname, final String mailId, final String sequenceId, MailAccess<?, ?> mailAccess) throws OXException {
        final MailPart mailPart = mailAccess.getMessageStorage().getAttachment(fullname, mailId, sequenceId);
        mailPart.loadContent();
        return mailPart;
    }

    @Override
    public String[] getRequiredArguments() {
        return new String[] { ARGS[0], ARGS[1], ARGS[2] };
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }
}
