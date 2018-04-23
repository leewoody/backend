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

package com.openexchange.ajax.subscribe.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.Subscription;

/**
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AllSubscriptionsTest extends AbstractSubscriptionTest {

    public AllSubscriptionsTest() {
        super();
    }

    @Test
    public void testBasicOXMFAllRequest() throws OXException, IOException, SAXException, JSONException {
        FolderObject folder = createDefaultContactFolder();
        DynamicFormDescription formDescription = generateFormDescription();
        Subscription subscription = generateOXMFSubscription(formDescription);
        subscription.setFolderId(folder.getObjectID());

        subMgr.newAction(subscription);
        assertFalse("Precondition: Creation of test file should work", subMgr.getLastResponse().hasError());
        List<String> columns = Arrays.asList("id", "folder", "source");
        JSONArray all = subMgr.allAction(folder.getObjectID(), columns);

        assertFalse("Should be able to handle all request", subMgr.getLastResponse().hasError());
        assertEquals("Should only have one result", 1, all.length());
        JSONArray elements = all.getJSONArray(0);
        assertEquals("Should have three elements", 3, elements.length());
        assertEquals("Should return the same ID", subscription.getId(), elements.getInt(0));
        assertEquals("Should return the same folder", subscription.getFolderId(), elements.getString(1));
        assertEquals("Should return the same source ID", subscription.getSource().getId(), elements.getString(2));
    }

    @Test
    public void testAllRequestForUser() throws OXException, IOException, SAXException, JSONException {
        FolderObject folder = createDefaultContactFolder();
        DynamicFormDescription formDescription = generateFormDescription();
        Subscription subscription = generateOXMFSubscription(formDescription);
        subscription.setFolderId(folder.getObjectID());

        subMgr.newAction(subscription);
        assertFalse("Precondition: Creation of test file should work", subMgr.getLastResponse().hasError());
        List<String> columns = Arrays.asList("id", "folder", "source");
        JSONArray all = subMgr.allAction(folder.getObjectID(), columns);
        assertFalse("Should be able to handle all request", subMgr.getLastResponse().hasError());

        boolean containsSubscription = false;
        checkID: for (int i = 0; i < all.length(); i++) {
            JSONArray actual = all.getJSONArray(i);
            if (actual.getInt(0) == subscription.getId()) {
                containsSubscription = true;
                break checkID;
            }
        }

        assertTrue("Subscription is not contained in all request.", containsSubscription);
    }

}
