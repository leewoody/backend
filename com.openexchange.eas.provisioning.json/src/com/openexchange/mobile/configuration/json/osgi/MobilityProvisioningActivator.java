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

package com.openexchange.mobile.configuration.json.osgi;

import static com.openexchange.mobile.configuration.json.osgi.MobilityProvisioningServiceRegistry.getInstance;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.mobile.configuration.json.action.ActionService;
import com.openexchange.mobile.configuration.json.servlet.MobilityProvisioningServlet;
import com.openexchange.osgi.HousekeepingActivator;

/**
 *
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 *
 */
public class MobilityProvisioningActivator extends HousekeepingActivator {

    private static transient final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobilityProvisioningActivator.class);
    private final static String SERVLET_PATH_APPENDIX = "mobilityprovisioning";
    private volatile String alias;

	public MobilityProvisioningActivator() {
		super();
	}

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[] { ConfigurationService.class, HttpService.class, DispatcherPrefixService.class };
	}

	@Override
	protected void handleAvailability(final Class<?> clazz) {
	    LOG.info("Re-available service: {}", clazz.getName());
		getInstance().addService(clazz, this.<Object>getService(clazz));
	}

	@Override
	protected void handleUnavailability(final Class<?> clazz) {
	    LOG.warn("Absent service: {}", clazz.getName());
		getInstance().removeService(clazz);

	}

    @Override
    protected void startBundle() throws Exception {
        try {

            final MobilityProvisioningServiceRegistry registry = getInstance();
            registry.clearRegistry();
            registry.clearActionServices();
            final Class<?>[] classes = getNeededServices();
            for (int i = 0; i < classes.length; i++) {
                final Object service = getService(classes[i]);
                if (null != service) {
                    registry.addService(classes[i], service);
                }
            }

            String alias = getService(DispatcherPrefixService.class).getPrefix() + SERVLET_PATH_APPENDIX;
            getService(HttpService.class).registerServlet(alias, new MobilityProvisioningServlet(), null, null);
            this.alias = alias;

            track(ActionService.class, new ActionServiceListener(context));
            openTrackers();
        } catch (final Throwable t) {
            LOG.error("", t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }

    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            final HttpService service = getService(HttpService.class);
            if (null != service) {
                String alias = this.alias;
                if (null != alias) {
                    service.unregister(alias);
                    this.alias = null;
                }
            }
            /*
             * Close service trackers
             */
            cleanUp();
            getInstance().clearRegistry();
            getInstance().clearActionServices();
        } catch (final Throwable t) {
            LOG.error("", t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }
    }
}
