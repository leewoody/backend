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

package com.openexchange.webdav.xml.contact;

import static org.junit.Assert.fail;
import java.util.Date;
import java.util.Locale;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.webdav.xml.ContactTest;
import com.openexchange.webdav.xml.XmlServlet;

public class UpdateTest extends ContactTest {

    public static final String CONTENT_TYPE = "image/png";

    public static final byte[] image = { -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 1, 3, 0, 0, 0, 37, -37, 86, -54, 0, 0, 0, 6, 80, 76, 84, 69, -1, -1, -1, -1, -1, -1, 85, 124, -11, 108, 0, 0, 0, 1, 116, 82, 78, 83, 0, 64, -26, -40, 102, 0, 0, 0, 1, 98, 75, 71, 68, 0, -120, 5, 29, 72, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 11, 18, 0, 0, 11, 18, 1, -46, -35, 126, -4, 0, 0, 0, 10, 73, 68, 65, 84, 120, -38, 99, 96, 0, 0, 0, 2, 0, 1, -27, 39, -34, -4, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 };

    @Test
    public void testUpdateContact() throws Exception {
        Contact contactObj = createContactObject("testUpdateContact");
        final int objectId = insertContact(webCon, contactObj, getHostURI(), login, password);

        contactObj = createContactObject("testUpdateContact");
        contactObj.setEmail1(null);

        updateContact(webCon, contactObj, objectId, contactFolderId, getHostURI(), login, password);
        final Contact loadContact = loadContact(getWebConversation(), objectId, contactFolderId, getHostURI(), getLogin(), getPassword());
        compareObject(contactObj, loadContact);
        deleteContact(getWebConversation(), objectId, contactFolderId, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testUpdateContactWithImage() throws Exception {
        Contact contactObj = createContactObject("testUpdateContactWithImage");
        final int objectId = insertContact(webCon, contactObj, getHostURI(), login, password);

        contactObj = createContactObject("testUpdateContactWithImage");
        contactObj.setEmail1(null);
        contactObj.setImageContentType(CONTENT_TYPE);
        contactObj.setImage1(image);

        updateContact(webCon, contactObj, objectId, contactFolderId, getHostURI(), login, password);
        final Contact loadContact = loadContact(getWebConversation(), objectId, contactFolderId, getHostURI(), getLogin(), getPassword());
        contactObj.removeImage1();
        compareObject(contactObj, loadContact);
        deleteContact(getWebConversation(), objectId, contactFolderId, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testUpdateConcurentConflict() throws Exception {
        Contact contactObj = createContactObject("testUpdateContactConcurentConflict");
        final int objectId = insertContact(webCon, contactObj, getHostURI(), login, password);

        contactObj = createContactObject("testUpdateContactConcurentConflict2");

        try {
            updateContact(webCon, contactObj, objectId, contactFolderId, new Date(0), getHostURI(), login, password);
            fail("expected concurent modification exception!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.MODIFICATION_STATUS);
        }

        final int[][] objectIdAndFolderId = { { objectId, contactFolderId } };
        deleteContact(webCon, objectIdAndFolderId, getHostURI(), login, password);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        Contact contactObj = createContactObject("testUpdateContactNotFound");
        final int objectId = insertContact(webCon, contactObj, getHostURI(), login, password);

        contactObj = createContactObject("testUpdateContactNotFound");

        try {
            updateContact(webCon, contactObj, (objectId + 1000), contactFolderId, new Date(0), getHostURI(), login, password);
            fail("expected object not found exception!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
        }

        final int[][] objectIdAndFolderId = { { objectId, contactFolderId } };
        deleteContact(webCon, objectIdAndFolderId, getHostURI(), login, password);
    }

}
