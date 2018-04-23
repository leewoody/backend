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

package com.openexchange.messaging.json.actions.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.SimMessageAccess.Call;
import com.openexchange.messaging.json.MessagingMessageParser;
import com.openexchange.messaging.json.MessagingMessageWriter;

/**
 * {@link GetTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class GetTest extends AbstractMessagingActionTest {

    // Success Cases

         @Test
     public void testGet() throws OXException {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");
        req.putParameter("id", "theID");

        final AJAXRequestResult result = perform(req);

        assertNotNull(result);
        final Object resultObject = result.getResultObject();
        assertNotNull(resultObject);
        assertTrue(JSONObject.class.isInstance(resultObject));

        final Call call = getMessagingAccessCall("com.openexchange.test1", 12);

        assertEquals("getMessage", call.getName());

        final Object[] args = call.getArgs();
        assertEquals("theFolderID", args[0]);
        assertEquals("theID", args[1]);
        assertEquals(false, args[2]);
    }

         @Test
     public void testPeek() throws OXException {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");
        req.putParameter("peek", "true");
        req.putParameter("id", "theID");

        final AJAXRequestResult result = perform(req);

        final Call call = getMessagingAccessCall("com.openexchange.test1", 12);

        assertEquals("getMessage", call.getName());

        final Object[] args = call.getArgs();
        assertEquals("theFolderID", args[0]);
        assertEquals("theID", args[1]);
        assertEquals(true, args[2]);
    }

    // Error Cases

         @Test
     public void testMissingMessagingService() throws OXException {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");
        req.putParameter("id", "theID");

        assertFails(req);
    }

         @Test
     public void testMissingAccountID() throws OXException {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("id", "theID");

        assertFails(req);
    }

         @Test
     public void testMissingFolder() throws OXException {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("account", "12");
        req.putParameter("id", "theID");

        assertFails(req);
    }

         @Test
     public void testMissingID() throws OXException {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");

        assertFails(req);
    }

         @Test
     public void testInvalidPeekParameter() throws OXException {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");
        req.putParameter("id", "theID");
        req.putParameter("peek", "aboo");

        assertFails(req);
    }

    @Override
    protected GetAction getAction() {
        return new GetAction(registry, new MessagingMessageWriter(), new MessagingMessageParser());
    }

}
