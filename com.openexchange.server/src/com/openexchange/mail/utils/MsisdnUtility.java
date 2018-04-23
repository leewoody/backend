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

package com.openexchange.mail.utils;

import java.util.Set;
import javax.mail.internet.InternetAddress;
import com.openexchange.contact.ContactService;
import com.openexchange.groupware.contact.ContactUtil;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.MsisdnCheck;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * Utility class to check and handle actions if MSISDN is enabled
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.2.2
 */
public class MsisdnUtility {

    /**
     * logger
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MsisdnUtility.class);

    /**
     * Prevent instantiation of a new {@link MsisdnUtility}.
     */
    private MsisdnUtility() {
        super();
    }

    /**
     * Adds the MSISDN number to the given address set.
     *
     * @param addresses - current address set to add the MSISDN number into
     * @param session - session to get the current contact and receive the number.
     */
    public static void addMsisdnAddress(Set<InternetAddress> addresses, Session session) {
        final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
        if (null != contactService) {
            try {
                final Contact contact = contactService.getUser(session, session.getUserId());
                final Set<String> set = ContactUtil.gatherTelephoneNumbers(contact);
                for (final String number : set) {
                    try {
                        addresses.add(new QuotedInternetAddress(MsisdnCheck.cleanup(number)));
                    } catch (final Exception e) {
                        // Ignore invalid number
                        LOG.debug("Ignoring invalid number: {}", number, e);
                    }
                }
            } catch (final Exception e) {
                LOG.warn("Could not check for valid MSISDN numbers.", e);
            }
        }
    }

}
