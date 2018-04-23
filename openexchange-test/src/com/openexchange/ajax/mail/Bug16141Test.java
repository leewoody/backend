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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.configuration.AJAXConfig;

/**
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 *
 */
public class Bug16141Test extends AbstractAJAXSession {

    private AJAXClient client;

    private String folder;

    private UserValues values;

    private final String[][] ids = null;

    private String testMailDir;

    private String address;

    public Bug16141Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        values = getClient().getValues();
        folder = values.getInboxFolder();
        address = getClient().getValues().getSendAddress();
        testMailDir = AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR);
    }

    @Test
    public void testMailImport() throws Exception {
        InputStream[] is = createABunchOfMails();

        final ImportMailRequest importReq = new ImportMailRequest(folder, MailFlag.SEEN.getValue(), is);
        final ImportMailResponse importResp = getClient().execute(importReq);
        JSONArray json = (JSONArray) importResp.getData();

        int err = 0;
        for (int i = 0; i < json.length(); i++) {
            JSONObject jo = json.getJSONObject(i);
            if (jo.has("Error")) {
                err++;
            }
        }

        if (err != 1) {
            fail("Number of corrupt mails is wrong");
        }

        if (json.length() - err != 3) {
            fail("Import did not run til end.");
        }
    }

    private InputStream[] createABunchOfMails() {
        List<InputStream> retval = new ArrayList<InputStream>(4);
        for (String fileName : new String[] { "bug16141_1.eml", "bug16141_2.eml", "bug16141_3.eml", "bug16141_4.eml" }) {
            try {
                retval.add(getMailAndReplaceAddress(fileName));
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
        return retval.toArray(new InputStream[retval.size()]);
    }

    private InputStream getMailAndReplaceAddress(String fileName) throws IOException {
        InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(testMailDir, fileName)), "UTF-8");
        char[] buf = new char[512];
        int length;
        StringBuilder sb = new StringBuilder();
        while ((length = isr.read(buf)) != -1) {
            sb.append(buf, 0, length);
        }
        return new ByteArrayInputStream(TestMails.replaceAddresses(sb.toString(), address).getBytes(com.openexchange.java.Charsets.UTF_8));
    }

    @After
    public void tearDown() throws Exception {
        try {
        if (ids != null) {
            getClient().execute(new DeleteRequest(ids));
        }
        } finally {
            super.tearDown();
        }
    }

}
