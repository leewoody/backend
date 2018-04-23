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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.Contact;

public class NewTest extends AbstractContactTest {

    @Test
    public void testNew() throws Exception {
        final Contact contactObj = createContactObject("testNew");
        insertContact(contactObj);
    }

    @Test
    public void testNewWithDistributionList() throws Exception {
        final Contact contactEntry = createContactObject("internal contact");
        contactEntry.setEmail1("internalcontact@x.de");
        final int contactId = insertContact(contactEntry);
        contactEntry.setObjectID(contactId);

        createContactWithDistributionList("testNewWithDistributionList", contactEntry);
    }

    @Test
    public void testNewContactWithAttachment() throws Exception {
        final Contact contactObj = createContactObject("testNewContactWithAttachment");
        final int objectId = insertContact(contactObj);
        contactObj.setObjectID(objectId);

        final AttachmentMetadata attachmentObj = new AttachmentImpl();
        attachmentObj.setFilename(System.currentTimeMillis() + "test1.txt");
        attachmentObj.setModuleId(Types.CONTACT);
        attachmentObj.setAttachedId(objectId);
        attachmentObj.setFolderId(contactFolderId);
        attachmentObj.setRtfFlag(false);
        attachmentObj.setFileMIMEType("plain/text");

        InputStream byteArrayInputStream = new ByteArrayInputStream("t1".getBytes());
        AttachRequest request1 = new AttachRequest(contactObj, System.currentTimeMillis() + "test1.txt", byteArrayInputStream, "plain/text");
        getClient().execute(request1);
        contactObj.setNumberOfAttachments(1);

        byteArrayInputStream = new ByteArrayInputStream("t2".getBytes());
        AttachRequest request2 = new AttachRequest(contactObj, System.currentTimeMillis() + "test1.txt", byteArrayInputStream, "plain/text");
        getClient().execute(request2);
        contactObj.setNumberOfAttachments(2);

        final Contact loadContact = loadContact(objectId, contactFolderId);
        compareObject(contactObj, loadContact);
    }
}
