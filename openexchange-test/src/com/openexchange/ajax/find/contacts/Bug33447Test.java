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

package com.openexchange.ajax.find.contacts;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.find.Module;
import com.openexchange.find.contacts.ContactsFacetType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.util.DisplayItems;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug33447Test}
 *
 * address book search does not show results from folders other than the global address book
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug33447Test extends ContactsFindTest {

    @Test
    public void testSearchContactFromPersonalContactsFolder() throws Exception {
        Map<String, String> options = new HashMap<String, String>();
        options.put("admin", Boolean.FALSE.toString());
        Contact contact = cotm.newAction(randomContact());
        String prefix = contact.getEmail1().substring(0, 8);
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, Module.CONTACTS.getIdentifier(), options);
        AutocompleteResponse autocompleteResponse = getClient().execute(autocompleteRequest);

        FacetValue foundFacetValue = findByDisplayName(autocompleteResponse.getFacets(), DisplayItems.convert(contact).getDisplayName());
        assertNotNull("no facet value found for: " + contact.getEmail1(), foundFacetValue);
        ActiveFacet activeFacet = createActiveFacet(ContactsFacetType.CONTACT, foundFacetValue.getId(), foundFacetValue.getFilter());
        List<PropDocument> documents = query(Module.CONTACTS, Collections.singletonList(activeFacet), options);
        assertTrue("Contact not found", null != documents && 0 < documents.size());
    }

}
