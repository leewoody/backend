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

import static org.junit.Assert.assertEquals;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Contact;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug16515Test extends AbstractAJAXSession {

    private AJAXClient client;
    private TimeZone tz;
    private Contact contact;

    private final String FILE_AS_VALUE = "I'm the file_as field of Herbert Meier";

    public Bug16515Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = getClient().getValues().getTimeZone();
        contact = createContact();
    }

    @Test
    public void testFileAs() throws Exception {
        GetRequest getContactReq = new GetRequest(contact.getParentFolderID(), contact.getObjectID(), tz);
        GetResponse getContactResp = getClient().execute(getContactReq);
        final Contact toCompare = getContactResp.getContact();

        assertEquals("File as has changed after creating contact.", contact.getFileAs(), toCompare.getFileAs());
    }

    @After
    public void tearDown() throws Exception {
        try {
            DeleteRequest deleteContactReq = new DeleteRequest(contact, false);
            getClient().execute(deleteContactReq);
        } finally {
            super.tearDown();
        }
    }

    public Contact createContact() throws Exception {
        final Contact contact = new Contact();
        contact.setTitle("Herr");
        contact.setSurName("Meier");
        contact.setGivenName("Herbert");
        contact.setDisplayName("Herbert Meier");
        contact.setStreetBusiness("Franz-Meier Weg 17");
        contact.setCityBusiness("Test Stadt");
        contact.setStateBusiness("NRW");
        contact.setCountryBusiness("Deutschland");
        contact.setTelephoneBusiness1("+49112233445566");
        contact.setCompany("Internal Test AG");
        contact.setEmail1("hebert.meier@open-xchange.com");
        contact.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        contact.setFileAs(FILE_AS_VALUE);

        InsertRequest insertContactReq = new InsertRequest(contact);
        InsertResponse insertContactResp = getClient().execute(insertContactReq);
        insertContactResp.fillObject(contact);

        return contact;
    }
}
