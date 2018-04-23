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

package com.openexchange.mailaccount.json.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.jslob.storage.registry.JSlobStorageRegistry;
import com.openexchange.mailaccount.Constants;
import com.openexchange.mailaccount.CredentialsProviderRegistry;
import com.openexchange.mailaccount.CredentialsProviderService;
import com.openexchange.mailaccount.internal.MailAccountOAuthAccountDeleteListener;
import com.openexchange.mailaccount.internal.MailAccountOAuthAccountReauthorizedListener;
import com.openexchange.mailaccount.json.MailAccountActionProvider;
import com.openexchange.mailaccount.json.MailAccountOAuthConstants;
import com.openexchange.mailaccount.json.actions.AbstractMailAccountAction;
import com.openexchange.mailaccount.json.factory.MailAccountActionFactory;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthAccountReauthorizedListener;
import com.openexchange.oauth.provider.resourceserver.scope.AbstractScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;


/**
 * {@link MailAccountJSONActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountJSONActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link MailAccountJSONActivator}.
     */
    public MailAccountJSONActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;

        MailAccountActionProviderTracker providerTracker = new MailAccountActionProviderTracker(context);
        track(MailAccountActionProvider.class, providerTracker);

        CredentialsProviderTracker credentialsProviderTracker = new CredentialsProviderTracker(context);
        track(CredentialsProviderService.class, credentialsProviderTracker);
        CredentialsProviderRegistry.getInstance().applyListing(credentialsProviderTracker);

        track(JSlobStorageRegistry.class, new ServiceTrackerCustomizer<JSlobStorageRegistry, JSlobStorageRegistry>() {

            @Override
            public JSlobStorageRegistry addingService(ServiceReference<JSlobStorageRegistry> reference) {
                final JSlobStorageRegistry storageRegistry = context.getService(reference);
                AbstractMailAccountAction.setJSlobStorageRegistry(storageRegistry);
                return storageRegistry;
            }

            @Override
            public void modifiedService(ServiceReference<JSlobStorageRegistry> reference, JSlobStorageRegistry service) {
                // nothing
            }

            @Override
            public void removedService(ServiceReference<JSlobStorageRegistry> reference, JSlobStorageRegistry service) {
                AbstractMailAccountAction.setJSlobStorageRegistry(null);
                context.ungetService(reference);
            }
        });
        trackService(Dispatcher.class);
        openTrackers();

        registerModule(new MailAccountActionFactory(providerTracker), Constants.getModule());

        registerService(OAuthAccountDeleteListener.class, new MailAccountOAuthAccountDeleteListener());
        registerService(OAuthAccountReauthorizedListener.class, new MailAccountOAuthAccountReauthorizedListener());

        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(MailAccountOAuthConstants.OAUTH_READ_SCOPE, OAuthScopeDescription.READ_ONLY) {

            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.MULTIPLE_MAIL_ACCOUNTS.getCapabilityName());
            }
        });
        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(MailAccountOAuthConstants.OAUTH_WRITE_SCOPE, OAuthScopeDescription.WRITABLE) {

            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.MULTIPLE_MAIL_ACCOUNTS.getCapabilityName());
            }
        });
    }

    @Override
    protected void stopBundle() throws Exception {
        CredentialsProviderRegistry.getInstance().applyListing(null);
        super.stopBundle();
    }

}
