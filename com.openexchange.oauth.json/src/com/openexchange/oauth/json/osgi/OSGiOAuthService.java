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

package com.openexchange.oauth.json.osgi;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.API;
import com.openexchange.oauth.HostInfo;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.session.Session;

/**
 * {@link OSGiOAuthService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OSGiOAuthService extends AbstractOSGiDelegateService<OAuthService> implements OAuthService {

    /**
     * Initializes a new {@link OSGiOAuthService}.
     */
    public OSGiOAuthService() {
        super(OAuthService.class);
    }

    private OAuthService getService0() throws OXException {
        return super.getService();
    }

    @Override
    public OAuthAccount createAccount(final String serviceMetaData, final Map<String, Object> arguments, final int user, final int contextId, Set<OAuthScope> scopes) throws OXException {
        return getService0().createAccount(serviceMetaData, arguments, user, contextId, scopes);
    }

    @Override
    public OAuthAccount createAccount(final String serviceMetaData, final OAuthInteractionType type, final Map<String, Object> arguments, final int user, final int contextId, Set<OAuthScope> scopes) throws OXException {
        return getService0().createAccount(serviceMetaData, type, arguments, user, contextId, scopes);
    }

    @Override
    public void deleteAccount(final int accountId, final int user, final int contextId) throws OXException {
        getService0().deleteAccount(accountId, user, contextId);
    }

    @Override
    public OAuthAccount getAccount(final int accountId, final Session session, final int user, final int contextId) throws OXException {
        return getService0().getAccount(accountId, session, user, contextId);
    }

    @Override
    public List<OAuthAccount> getAccounts(final Session session, final int user, final int contextId) throws OXException {
        return getService0().getAccounts(session, user, contextId);
    }

    @Override
    public List<OAuthAccount> getAccounts(final String serviceMetaData, final Session session, final int user, final int contextId) throws OXException {
        return getService0().getAccounts(serviceMetaData, session, user, contextId);
    }

    @Override
    public OAuthServiceMetaDataRegistry getMetaDataRegistry() {
        final OAuthService delegatee = optService();
        if (null == delegatee) {
            throw new IllegalStateException("OAuthService is absent.");
        }
        return delegatee.getMetaDataRegistry();
    }

    @Override
    public OAuthInteraction initOAuth(final String serviceMetaData, final String callbackUrl, final HostInfo host, final Session session, Set<OAuthScope> scopes) throws OXException {
        return getService0().initOAuth(serviceMetaData, callbackUrl, host, session, scopes);
    }

    @Override
    public void updateAccount(final int accountId, final Map<String, Object> arguments, final int user, final int contextId) throws OXException {
        getService0().updateAccount(accountId, arguments, user, contextId);
    }

    @Override
    public OAuthAccount updateAccount(final int accountId, final String serviceMetaData, final OAuthInteractionType type, final Map<String, Object> arguments, final int user, final int contextId, Set<OAuthScope> scopes) throws OXException {
        return getService0().updateAccount(accountId, serviceMetaData, type, arguments, user, contextId, scopes);
    }

    @Override
    public OAuthAccount getDefaultAccount(API api, Session session) throws OXException {
        return getService0().getDefaultAccount(api, null);
    }
}
