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

package com.openexchange.ajax.mail;

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.xml.sax.SAXException;
import com.openexchange.ajax.mail.actions.ForwardRequest;
import com.openexchange.ajax.mail.actions.ForwardResponse;
import com.openexchange.ajax.mail.actions.ReplyAllRequest;
import com.openexchange.ajax.mail.actions.ReplyAllResponse;
import com.openexchange.ajax.mail.actions.ReplyRequest;
import com.openexchange.ajax.mail.actions.ReplyResponse;
import com.openexchange.exception.OXException;
import com.openexchange.test.ContactTestManager;

/**
 * {@link AbstractReplyTest} - test for the Reply/ReplyAll/Forward family of requests.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractReplyTest extends AbstractMailTest {

    protected ContactTestManager contactManager;

    public AbstractReplyTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        this.contactManager = new ContactTestManager(getClient());
    }

    @After
    public void tearDown() throws Exception {
        try {
            clearFolder(getInboxFolder());
            clearFolder(getSentFolder());
            contactManager.cleanUp();
        } finally {
            super.tearDown();
        }
    }

    protected boolean contains(List<String> from, String string) {
        for (String str2 : from) {
            if (str2.contains(string)) {
                return true;
            }
        }
        return false;
    }

    protected JSONObject getReplyEMail(TestMail testMail) throws OXException, IOException, SAXException, JSONException {
        ReplyRequest reply = new ReplyRequest(testMail.getFolder(), testMail.getId());
        reply.setFailOnError(true);
        ReplyResponse response = getClient().execute(reply);
        return (JSONObject) response.getData();
    }

    protected JSONObject getReplyAllEMail(TestMail testMail) throws OXException, IOException, SAXException, JSONException {
        ReplyRequest reply = new ReplyAllRequest(testMail.getFolder(), testMail.getId());
        reply.setFailOnError(true);
        ReplyAllResponse response = (ReplyAllResponse) getClient().execute(reply);
        return (JSONObject) response.getData();
    }

    protected JSONObject getForwardMail(TestMail testMail) throws OXException, IOException, SAXException, JSONException {
        ReplyRequest reply = new ForwardRequest(testMail.getFolder(), testMail.getId());
        reply.setFailOnError(true);
        ForwardResponse response = (ForwardResponse) getClient().execute(reply);
        return (JSONObject) response.getData();
    }

    public static void assertNullOrEmpty(String msg, Collection coll) {
        if (coll == null) {
            return;
        }
        if (coll.size() == 0) {
            return;
        }
        fail(msg);
    }

}
