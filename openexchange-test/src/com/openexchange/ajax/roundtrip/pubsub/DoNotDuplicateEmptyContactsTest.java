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

package com.openexchange.ajax.roundtrip.pubsub;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.publish.tests.PublicationTestManager;
import com.openexchange.ajax.subscribe.test.SubscriptionTestManager;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.publish.Publication;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;
import com.openexchange.subscribe.Subscription;

/**
 * {@link DoNotDuplicateEmptyContactsTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class DoNotDuplicateEmptyContactsTest extends OXMFContactLifeCycleTest {

    public DoNotDuplicateEmptyContactsTest() {
        super();
    }

    @Test
    public void testShouldNotDuplicateEmptyContacts() throws OXException, OXException, OXException, IOException, SAXException, JSONException {
        Contact emptyContact = new Contact();
        emptyContact.setParentFolderID(pubFolder.getObjectID());
        cotm.newAction(emptyContact);

        // prepare pubsub
        PublicationTestManager pubMgr = getPublishManager();
        SubscriptionTestManager subMgr = getSubscribeManager();
        SimPublicationTargetDiscoveryService pubDiscovery = new SimPublicationTargetDiscoveryService();
        pubMgr.setPublicationTargetDiscoveryService(pubDiscovery);
        Publication publication = generatePublication("contacts", String.valueOf(pubFolder.getObjectID()), pubDiscovery);

        Contact[] contacts;

        // create publication
        pubMgr.newAction(publication);

        // create subscription for that url
        DynamicFormDescription formDescription = publication.getTarget().getFormDescription();
        formDescription.add(FormElement.input("url", "URL"));
        Subscription subscription = generateOXMFSubscription(formDescription);
        subscription.setFolderId(subFolder.getObjectID());
        subMgr.setFormDescription(formDescription);
        String pubUrl = (String) publication.getConfiguration().get("url");
        subscription.getConfiguration().put("url", pubUrl);

        subMgr.newAction(subscription);

        // refresh and check subscription
        subMgr.refreshAction(subscription.getId());
        contacts = cotm.allAction(subFolder.getObjectID());
        assertEquals("Should only contain one contact after first publication", 1, contacts.length);

        // refresh and check subscription again
        subMgr.refreshAction(subscription.getId());
        contacts = cotm.allAction(subFolder.getObjectID());
        assertEquals("Should only contain one contact even after refreshing", 1, contacts.length);
    }
}