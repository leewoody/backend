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

package com.openexchange.messaging.json;

import static com.openexchange.json.JSONAssertion.assertValidates;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.json.JSONAssertion;
import com.openexchange.messaging.ContentDisposition;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.StringMessageHeader;
import com.openexchange.messaging.generic.internet.MimeContentDisposition;

/**
 * {@link ContentDispositionWriterTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContentDispositionWriterTest {

         @Test
     public void testWriteContentType() throws OXException, JSONException {
        final ContentDisposition contentDisp = new MimeContentDisposition();
        contentDisp.setDisposition(ContentDisposition.ATTACHMENT);
        contentDisp.setFilenameParameter("foo.dat");

        final ContentDispositionWriter writer = new ContentDispositionWriter();

        final SimEntry<String, Collection<MessagingHeader>> entry = entry( contentDisp );

        assertTrue(writer.handles(entry));
        assertEquals("Content-Disposition", writer.writeKey(entry));

        final Object value = writer.writeValue(entry, null);
        assertNotNull(value);

        final JSONObject jsonCDisp = (JSONObject) value;

        final JSONAssertion assertion = new JSONAssertion()
            .isObject()
                .hasKey("type").withValue(ContentDisposition.ATTACHMENT)
                .hasKey("params").withValueObject()
                    .hasKey("filename").withValue("foo.dat")
                .objectEnds()
            .objectEnds();

        assertValidates(assertion, jsonCDisp);

    }

         @Test
     public void testWriteBasicHeader() throws OXException, JSONException {
        final MessagingHeader contentDisp = new StringMessageHeader("Content-Disposition", ContentDisposition.ATTACHMENT+";filename=foo.dat");

        final ContentDispositionWriter writer = new ContentDispositionWriter();

        final SimEntry<String, Collection<MessagingHeader>> entry = entry( contentDisp );

        assertTrue(writer.handles(entry));
        assertEquals("Content-Disposition", writer.writeKey(entry));

        final Object value = writer.writeValue(entry, null);
        assertNotNull(value);

        final JSONObject jsonCDisp = (JSONObject) value;

        final JSONAssertion assertion = new JSONAssertion()
        .isObject()
            .hasKey("type").withValue(ContentDisposition.ATTACHMENT)
            .hasKey("params").withValueObject()
                .hasKey("filename").withValue("foo.dat")
            .objectEnds()
        .objectEnds();

        assertValidates(assertion, jsonCDisp);

    }

    private SimEntry<String, Collection<MessagingHeader>> entry(final MessagingHeader header) {
        return new SimEntry<String, Collection<MessagingHeader>>(header.getName(), Arrays.asList(header));
    }
}
