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

package com.openexchange.ajax.contact;

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.contact.action.SearchByBirthdayRequest;
import com.openexchange.ajax.framework.CommonSearchResponse;
import com.openexchange.groupware.container.Contact;

/**
 * {@link BirthdayAndAnniversaryTest}
 * 
 * Checks the requests to get upcoming birthdays and anniversaries.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class BirthdayAndAnniversaryTest extends AbstractManagedContactTest {

    @Test
    public void testSearchByBirthday() throws Exception {
        /*
         * create contacts
         */
        Contact contact1 = super.generateContact("M\u00e4rz");
        contact1.setBirthday(D("1988-03-03 00:00"));
        contact1 = cotm.newAction(contact1);
        Contact contact2 = super.generateContact("Juli");
        contact2.setBirthday(D("1977-07-07 00:00:00"));
        contact2 = cotm.newAction(contact2);
        Contact contact3 = super.generateContact("Oktober");
        contact3.setBirthday(D("1910-10-10 00:00:00"));
        contact3 = cotm.newAction(contact3);
        /*
         * search birthdays in different timeframes
         */
        String parentFolderID = String.valueOf(contact1.getParentFolderID());
        int[] columns = { Contact.OBJECT_ID, Contact.BIRTHDAY, Contact.FOLDER_ID };
        SearchByBirthdayRequest request;
        CommonSearchResponse response;
        List<Contact> contacts;

        request = new SearchByBirthdayRequest(D("2013-01-01 00:00:00"), D("2013-09-01 00:00:00"), parentFolderID, columns, true);
        response = getClient().execute(request);
        contacts = cotm.transform((JSONArray) response.getResponse().getData(), columns);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 2, contacts.size());

        request = new SearchByBirthdayRequest(D("2013-01-01 00:00:00"), D("2014-01-01 00:00:00"), parentFolderID, columns, true);
        response = getClient().execute(request);
        contacts = cotm.transform((JSONArray) response.getResponse().getData(), columns);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 3, contacts.size());

        request = new SearchByBirthdayRequest(D("2013-06-01 00:00:00"), D("2014-01-01 00:00:00"), parentFolderID, columns, true);
        response = getClient().execute(request);
        contacts = cotm.transform((JSONArray) response.getResponse().getData(), columns);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 2, contacts.size());

        request = new SearchByBirthdayRequest(D("2013-03-04 00:00:00"), D("2013-07-06 00:00:00"), parentFolderID, columns, true);
        response = getClient().execute(request);
        contacts = cotm.transform((JSONArray) response.getResponse().getData(), columns);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 0, contacts.size());

        request = new SearchByBirthdayRequest(D("2085-03-03 00:00:00"), D("2085-03-03 01:01:00"), parentFolderID, columns, true);
        response = getClient().execute(request);
        contacts = cotm.transform((JSONArray) response.getResponse().getData(), columns);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 1, contacts.size());

    }

}
