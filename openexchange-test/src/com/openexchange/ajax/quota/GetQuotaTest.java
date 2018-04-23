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

package com.openexchange.ajax.quota;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Random;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;

/**
 * {@link GetQuotaTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GetQuotaTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link GetQuotaTest}.
     *
     * @param name The test name
     */
    public GetQuotaTest() {
        super();
    }

    @Test
    public void testGetQuota() throws Exception {
        /*
         * get quota from all available modules
         */
        GetQuotaRequest request = new GetQuotaRequest(null, null);
        GetQuotaResponse response = getClient().execute(request);
        JSONObject jsonModules = (JSONObject) response.getData();
        assertNotNull("No response data", jsonModules);
        Set<String> modules = jsonModules.keySet();
        if (null != modules && 0 < modules.size()) {
            /*
             * get quotas from one specific random module
             */
            Random random = new Random();
            String randomModule = modules.toArray(new String[modules.size()])[random.nextInt(modules.size())];
            JSONObject jsonModule = jsonModules.getJSONObject(randomModule);
            assertTrue("No display_name found", jsonModule.hasAndNotNull("display_name"));
            assertTrue("No accounts array found", jsonModule.hasAndNotNull("accounts"));
            request = new GetQuotaRequest(randomModule, null);
            response = getClient().execute(request);
            JSONArray jsonAccounts = (JSONArray) response.getData();
            assertNotNull("No response data", jsonAccounts);
            if (0 < jsonAccounts.length()) {
                /*
                 * get quota from one specific random account
                 */
                JSONObject randomAccount = jsonAccounts.getJSONObject(random.nextInt(jsonAccounts.length()));
                assertTrue("No account_id found", randomAccount.hasAndNotNull("account_id"));
                assertTrue("No account_name found", randomAccount.hasAndNotNull("account_name"));
                assertTrue("No quota or countquota found", randomAccount.hasAndNotNull("quota") || randomAccount.hasAndNotNull("countquota"));
                request = new GetQuotaRequest(randomModule, randomAccount.getString("account_id"));
                response = getClient().execute(request);
                JSONObject jsonAccount = (JSONObject) response.getData();
                assertTrue("No account_id found", jsonAccount.hasAndNotNull("account_id"));
                assertTrue("No account_name found", jsonAccount.hasAndNotNull("account_name"));
                assertTrue("No quota or countquota found", jsonAccount.hasAndNotNull("quota") || randomAccount.hasAndNotNull("countquota"));
            }
        }
    }

}
