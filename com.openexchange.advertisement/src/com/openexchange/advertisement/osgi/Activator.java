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

package com.openexchange.advertisement.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.LoggerFactory;
import com.openexchange.advertisement.AdvertisementConfigService;
import com.openexchange.advertisement.AdvertisementPackageService;
import com.openexchange.advertisement.internal.AdvertisementPackageServiceImpl;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.FailureAwareCapabilityChecker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.reseller.ResellerService;
import com.openexchange.session.Session;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { CapabilityService.class, ResellerService.class, ConfigurationService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        LoggerFactory.getLogger(Activator.class).info("starting bundle com.openexchange.advertisement");

        final AdvertisementPackageServiceImpl packageService = new AdvertisementPackageServiceImpl(getService(ResellerService.class), getService(ConfigurationService.class));

        // Register capability
        {
            final String sCapability = "ads";
            Dictionary<String, Object> properties = new Hashtable<>(2);
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
            registerService(CapabilityChecker.class, new FailureAwareCapabilityChecker() {

                @Override
                public FailureAwareCapabilityChecker.Result checkEnabled(String capability, Session session) throws OXException {
                    if (sCapability.equals(capability)) {
                        AdvertisementConfigService confService = packageService.getScheme(session.getContextId());
                        if (confService == null) {
                            return FailureAwareCapabilityChecker.Result.DISABLED;
                        }
                        if (confService.isAvailable(session)) {
                            return FailureAwareCapabilityChecker.Result.ENABLED;
                        }
                        return FailureAwareCapabilityChecker.Result.DISABLED;
                    }

                    return FailureAwareCapabilityChecker.Result.ENABLED;
                }
            }, properties);
            getService(CapabilityService.class).declareCapability(sCapability);
        }

        registerService(AdvertisementPackageService.class, packageService);
        registerService(Reloadable.class, packageService);
        final BundleContext context = this.context;
        track(AdvertisementConfigService.class, new ServiceTrackerCustomizer<AdvertisementConfigService, AdvertisementConfigService>() {

            @Override
            public AdvertisementConfigService addingService(ServiceReference<AdvertisementConfigService> reference) {
                AdvertisementConfigService service = context.getService(reference);
                boolean added = packageService.addServiceAndReload(service);
                if (added) {
                    return service;
                }

                context.ungetService(reference);
                return null;
            }

            @Override
            public void modifiedService(ServiceReference<AdvertisementConfigService> reference, AdvertisementConfigService service) {
                // Nothing to do
            }

            @Override
            public void removedService(ServiceReference<AdvertisementConfigService> reference, AdvertisementConfigService service) {
                packageService.removeServiceAndReload(service);
                context.ungetService(reference);
            }

        });
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        LoggerFactory.getLogger(Activator.class).info("stopping bundle com.openexchange.advertisement");

        super.stopBundle();
    }

}
