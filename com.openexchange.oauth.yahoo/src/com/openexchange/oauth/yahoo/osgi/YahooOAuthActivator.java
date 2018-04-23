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

package com.openexchange.oauth.yahoo.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.oauth.scope.OAuthScopeRegistry;
import com.openexchange.oauth.yahoo.YahooOAuthScope;
import com.openexchange.oauth.yahoo.YahooService;
import com.openexchange.oauth.yahoo.access.YahooAccessEventHandler;
import com.openexchange.oauth.yahoo.internal.OAuthServiceMetaDataYahooImpl;
import com.openexchange.oauth.yahoo.internal.YahooServiceImpl;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link YahooOAuthActivator}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class YahooOAuthActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(YahooOAuthActivator.class);

    private volatile OAuthService oauthService;
    private volatile OAuthServiceMetaDataYahooImpl oAuthMetaData;

    /** Gets OAuthService */
    public OAuthService getOauthService() {
        return oauthService;
    }

    /** Sets OAuthService */
    public void setOauthService(final OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    /** Gets OAuthServiceMetaDataYahooImpl */
    public OAuthServiceMetaDataYahooImpl getOAuthMetaData() {
        return oAuthMetaData;
    }

    /** Sets OAuthServiceMetaDataYahooImpl */
    public void setOAuthMetaData(final OAuthServiceMetaDataYahooImpl oauthMetaData) {
        this.oAuthMetaData = oauthMetaData;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, OAuthService.class, DeferringURLService.class, ThreadPoolService.class, OAuthScopeRegistry.class, OAuthAccessRegistryService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServices(this);
        oauthService = getService(OAuthService.class);
        oAuthMetaData = new OAuthServiceMetaDataYahooImpl(this);
        registerService(OAuthServiceMetaData.class, oAuthMetaData);
        registerService(Reloadable.class, oAuthMetaData);
        LOG.info("OAuthServiceMetaData for Yahoo was started");

        final YahooService yahooService = new YahooServiceImpl(this);
        registerService(YahooService.class, yahooService);
        // Register the delete listener
        registerService(OAuthAccountDeleteListener.class, (OAuthAccountDeleteListener) yahooService);

        // Register the scope
        OAuthScopeRegistry scopeRegistry = getService(OAuthScopeRegistry.class);
        scopeRegistry.registerScopes(oAuthMetaData.getAPI(), YahooOAuthScope.values());

        /*
         * Register event handler
         */
        final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.TOPIC_LAST_SESSION);
        registerService(EventHandler.class, new YahooAccessEventHandler(), serviceProperties);

        // Register the update task
        // track(DatabaseService.class, new DatabaseUpdateTaskServiceTracker(context));

        LOG.info("YahooService was started.");
    }

}
