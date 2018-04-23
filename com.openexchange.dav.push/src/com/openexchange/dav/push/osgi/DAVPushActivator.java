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

package com.openexchange.dav.push.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVServlet;
import com.openexchange.dav.push.DAVPushEventHandler;
import com.openexchange.dav.push.DAVPushUtility;
import com.openexchange.dav.push.apn.DAVApnOptionsProvider;
import com.openexchange.dav.push.apn.DAVApnPushMessageGenerator;
import com.openexchange.dav.push.gcm.DavPushGateway;
import com.openexchange.dav.push.mixins.PushKey;
import com.openexchange.dav.push.mixins.PushTransports;
import com.openexchange.dav.push.mixins.SubscribeURL;
import com.openexchange.dav.push.mixins.SupportedTransportSet;
import com.openexchange.dav.push.mixins.Topic;
import com.openexchange.dav.push.mixins.Version;
import com.openexchange.dav.push.subscribe.PushSubscribeFactory;
import com.openexchange.dav.push.subscribe.PushSubscribePerformer;
import com.openexchange.exception.OXException;
import com.openexchange.login.Interface;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushMessageGenerator;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.transport.apn.ApnOptionsProvider;
import com.openexchange.webdav.protocol.helpers.PropertyMixin;

/**
 * {@link DAVPushActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class DAVPushActivator extends HousekeepingActivator implements Reloadable  {

    private volatile PushSubscribeFactory factory;
    private ServiceRegistration<ApnOptionsProvider> optionsProviderRegistration;
    private ServiceRegistration<EventHandler> eventHandlerRegistration;
    private List<ServiceRegistration<PushNotificationTransport>> pushTransportRegistrations;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, ConfigurationService.class, PushNotificationService.class, PushSubscriptionRegistry.class, ConfigViewFactory.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        try {
            getLogger(DAVPushActivator.class).info("starting bundle {}", context.getBundle());
            /*
             * register push subscribe servlet
             */
            PushSubscribePerformer performer = new PushSubscribePerformer(this);
            this.factory = performer.getFactory();
            getService(HttpService.class).registerServlet("/servlet/dav/subscribe", new DAVServlet(performer, Interface.CALDAV), null, null);
            /*
             * register push message generators
             */
            registerService(PushMessageGenerator.class, new DAVApnPushMessageGenerator(DAVPushUtility.CLIENT_CARDDAV));
            registerService(PushMessageGenerator.class, new DAVApnPushMessageGenerator(DAVPushUtility.CLIENT_CALDAV));
            /*
             * register OSGi mixins
             */
            registerService(PropertyMixin.class, new PushKey());
            registerService(PropertyMixin.class, new PushTransports(performer.getFactory()));
            registerService(PropertyMixin.class, new SubscribeURL());
            registerService(PropertyMixin.class, new SupportedTransportSet(performer.getFactory()));
            registerService(PropertyMixin.class, new Topic());
            registerService(PropertyMixin.class, new Version());
            /*
             * initial initialization
             */
            reinit(getService(ConfigurationService.class));
        } catch (Exception e) {
            getLogger(DAVPushActivator.class).error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(DAVPushActivator.class).info("stopping bundle {}", context.getBundle());
        reinit(null);
        super.stopBundle();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            reinit(configService);
        } catch (Exception e) {
            getLogger(DAVPushActivator.class).error("error during initialisation", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest(DAVApnOptionsProvider.getPropertiesOfInterest()).build();
    }

    private void reinit(ConfigurationService configService) throws OXException {
        /*
         * unregister previous options provider and transports
         */
        ServiceRegistration<ApnOptionsProvider> optionsProviderRegistration = this.optionsProviderRegistration;
        if (null != optionsProviderRegistration) {
            optionsProviderRegistration.unregister();
            this.optionsProviderRegistration = null;
        }
        List<ServiceRegistration<PushNotificationTransport>> pushTransportRegistrations = this.pushTransportRegistrations;
        if (null != pushTransportRegistrations && 0 < pushTransportRegistrations.size()) {
            for (ServiceRegistration<PushNotificationTransport> pushTransportRegistration : pushTransportRegistrations) {
                pushTransportRegistration.unregister();
            }
            pushTransportRegistrations = null;
        }
        ServiceRegistration<EventHandler> eventHandlerRegistration = this.eventHandlerRegistration;
        if (null != eventHandlerRegistration) {
            eventHandlerRegistration.unregister();
            this.eventHandlerRegistration = null;
        }
        /*
         * re-init factory & register options provider and transports
         */
        if (null != configService) {
            boolean registerEventHandler = false;
            factory.reinit(configService);
            DAVApnOptionsProvider optionsProvider = factory.getApnOptionsProvider();
            if (null != optionsProvider && 0 < optionsProvider.getAvailableOptions().size()) {
                optionsProviderRegistration = context.registerService(ApnOptionsProvider.class, optionsProvider, null);
                this.optionsProviderRegistration = optionsProviderRegistration;
                registerEventHandler = true;
            }
            List<DavPushGateway> pushGateways = factory.getGateways();
            if (null != pushGateways && 0 < pushGateways.size()) {
                pushTransportRegistrations = new ArrayList<ServiceRegistration<PushNotificationTransport>>(pushGateways.size());
                for (DavPushGateway pushGateway : pushGateways) {
                    pushTransportRegistrations.add(context.registerService(PushNotificationTransport.class, pushGateway, null));
                }
                this.pushTransportRegistrations = pushTransportRegistrations;
                registerEventHandler = true;
            }
            if (registerEventHandler) {
                Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
                serviceProperties.put(EventConstants.EVENT_TOPIC, DAVPushEventHandler.TOPICS);
                eventHandlerRegistration = context.registerService(EventHandler.class, new DAVPushEventHandler(getService(PushNotificationService.class)), serviceProperties);
                this.eventHandlerRegistration = eventHandlerRegistration;
            }
        }
    }

}
