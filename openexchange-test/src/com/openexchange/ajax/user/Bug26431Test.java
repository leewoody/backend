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

package com.openexchange.ajax.user;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.user.actions.GetAttributeRequest;
import com.openexchange.ajax.user.actions.GetAttributeResponse;
import com.openexchange.ajax.user.actions.SetAttributeRequest;
import com.openexchange.ajax.user.actions.SetAttributeResponse;
import com.openexchange.tools.RandomString;

/**
 * {@link Bug26431Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug26431Test extends AbstractAJAXSession {

    private static final String ATTRIBUTE_NAME = "testForBug26431";

    private AJAXClient client;
    private int userId;

    public Bug26431Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        userId = client.getValues().getUserId();
    }

    @Test
    public void testOmittedValue() throws Exception {
        String value = RandomString.generateChars(64);
        SetAttributeResponse response = client.execute(new SetAttributeRequest(userId, ATTRIBUTE_NAME, value, false));
        assertTrue("Setting a test attribute failed.", response.isSuccess());
        response = client.execute(new SetAttributeRequest(userId, ATTRIBUTE_NAME, null, false));
        assertTrue("Removing the attribute failed.", response.isSuccess());
        GetAttributeResponse response2 = client.execute(new GetAttributeRequest(userId, ATTRIBUTE_NAME));
        assertNull("Removing the attribute failed.", response2.getValue());
    }

    @Test
    public void testNullValue() throws Exception {
        String value = RandomString.generateChars(64);
        SetAttributeResponse response = client.execute(new SetAttributeRequest(userId, ATTRIBUTE_NAME, value, false));
        assertTrue("Setting a test attribute failed.", response.isSuccess());
        response = client.execute(new SetAttributeRequest(userId, ATTRIBUTE_NAME, JSONObject.NULL, false));
        assertTrue("Removing the attribute failed.", response.isSuccess());
        GetAttributeResponse response2 = client.execute(new GetAttributeRequest(userId, ATTRIBUTE_NAME));
        assertNull("Removing the attribute failed.", response2.getValue());
    }
}
