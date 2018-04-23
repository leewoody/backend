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

package com.openexchange.contact.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.internal.operands.ColumnOperand;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link SearchTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SearchTest extends ContactStorageTest {

    @Test
    public void testSearchByUID() throws Exception {
        /*
         * create contact
         */
        final String folderId = "500011";
        final Contact contact = new Contact();
        contact.setCreatedBy(getUserID());
        contact.setDisplayName("Horst Horstensen");
        contact.setGivenName("Horst");
        contact.setSurName("Horstensen");
        contact.setEmail1("horst.horstensen@example.com");
        contact.setUid(UUID.randomUUID().toString());
        getStorage().create(getSession(), folderId, contact);
        super.rememberForCleanUp(contact);
        /*
         * search contact
         */
        final SingleSearchTerm term = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
        term.addOperand(new ColumnOperand("uid"));
        term.addOperand(new ConstantOperand<String>(contact.getUid()));
        final SearchIterator<Contact> result = getStorage().search(getSession(), term, ContactField.values());
        /*
         * verify search result
         */
        assertNotNull("got no search result", result);
        assertTrue("got no search result", 0 < result.size());
        Contact foundContact = findContact(contact.getUid(), result);
        assertNotNull("contact not in search results", foundContact);
        assertEquals("display name wrong", contact.getDisplayName(), foundContact.getDisplayName());
        assertEquals("surname wrong", contact.getSurName(), foundContact.getSurName());
        assertEquals("givenname wrong", contact.getGivenName(), foundContact.getGivenName());
        assertEquals("email1 wrong", contact.getEmail1(), foundContact.getEmail1());
    }

}
