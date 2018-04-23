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

package com.openexchange.oauth.json.oauthaccount;

import java.util.LinkedHashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.DefaultOAuthAccount;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.json.Services;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OAuthScopeRegistry;

/**
 * Parses the JSON representation of an OAuth account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AccountParser {

    /**
     * Initializes a new {@link AccountParser}.
     */
    private AccountParser() {
        super();
    }

    /**
     * Parses the OAuth account from specified JSON representation of an OAuth account.
     *
     * @param accountJSON The JSON representation of the OAuth account
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The parsed OAuth account
     * @throws OXException If an OPen-Xchange error occurs
     * @throws JSONException If an error occurs while parsing/reading JSON data
     */
    public static DefaultOAuthAccount parse(JSONObject accountJSON, int userId, int contextId) throws OXException, JSONException {
        DefaultOAuthAccount account = new DefaultOAuthAccount();

        if (accountJSON.hasAndNotNull(AccountField.ID.getName())) {
            account.setId(accountJSON.getInt(AccountField.ID.getName()));
        }
        if (accountJSON.hasAndNotNull(AccountField.DISPLAY_NAME.getName())) {
            account.setDisplayName(accountJSON.getString(AccountField.DISPLAY_NAME.getName()));
        }
        if (accountJSON.hasAndNotNull(AccountField.TOKEN.getName())) {
            account.setToken(accountJSON.getString(AccountField.TOKEN.getName()));
        }
        if (accountJSON.hasAndNotNull(AccountField.SECRET.getName())) {
            account.setSecret(accountJSON.getString(AccountField.SECRET.getName()));
        }
        if (accountJSON.hasAndNotNull(AccountField.SERVICE_ID.getName())) {
            String serviceId = accountJSON.getString(AccountField.SERVICE_ID.getName());
            OAuthServiceMetaDataRegistry registry = Services.getService(OAuthService.class).getMetaDataRegistry();
            account.setMetaData(registry.getService(serviceId, userId, contextId));
        }
        if (accountJSON.hasAndNotNull(AccountField.ENABLED_SCOPES.getName()) && account.getMetaData() != null) {
            JSONArray enabledScopesArray = accountJSON.getJSONArray(AccountField.ENABLED_SCOPES.getName());
            int length = enabledScopesArray.length();
            if (length > 0) {
                OAuthScopeRegistry scopeRegistry = Services.optService(OAuthScopeRegistry.class);
                if (null == scopeRegistry) {
                    throw ServiceExceptionCode.absentService(OAuthScopeRegistry.class);
                }

                API api = account.getMetaData().getAPI();
                Set<OAuthScope> enabledScopes = new LinkedHashSet<>(length);
                for (int i = 0; i < length; i++) {
                    String scope = enabledScopesArray.getString(i);
                    enabledScopes.add(scopeRegistry.getScope(api, OXScope.valueOf(scope)));
                }
                account.setEnabledScopes(enabledScopes);
            }
        }

        return account;
    }

}
