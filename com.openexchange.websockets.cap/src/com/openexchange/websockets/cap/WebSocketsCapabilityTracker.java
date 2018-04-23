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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.websockets.cap;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.websockets.WebSocketService;


/**
 * {@link WebSocketsCapabilityTracker} - Advertises <code>"websocket"</code> capability if {@link WebSocketService} is available.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketsCapabilityTracker implements ServiceTrackerCustomizer<Object, Object> {

    private static final String CAPABILITY_WEBSOCKET = "websocket";

    private final BundleContext context;
    private final Class<?>[] neededServices;
    private boolean capabilityDeclared;
    private boolean webSocketServiceAvailable;
    private CapabilityService capabilityService;
    private ConfigViewFactory configViewFactory;
    private ServiceRegistration<CapabilityChecker> checkerRegistration;

    /**
     * Initializes a new {@link WebSocketsCapabilityTracker}.
     */
    public WebSocketsCapabilityTracker(BundleContext context) {
        super();
        this.context = context;
        neededServices = new Class<?>[] { WebSocketService.class, CapabilityService.class, ConfigViewFactory.class };
        capabilityDeclared = false;
        webSocketServiceAvailable = false;
    }

    /**
     * Creates the appropriate filter for this service tracker
     *
     * @return The filter
     * @throws InvalidSyntaxException If the syntax of the generated filter is not correct.
     */
    public Filter getFilter() throws InvalidSyntaxException {
        StringBuilder sb = new StringBuilder(16 << neededServices.length).append("(|(");
        for (final Class<?> clazz : neededServices) {
            sb.append(Constants.OBJECTCLASS);
            sb.append('=');
            sb.append(clazz.getName());
            sb.append(")(");
        }
        sb.setCharAt(sb.length() - 1, ')');
        return context.createFilter(sb.toString());
    }

    @Override
    public synchronized Object addingService(ServiceReference<Object> reference) {
        Object service = context.getService(reference);

        if (service instanceof WebSocketService) {
            webSocketServiceAvailable = true;
            if (shouldDeclareCapability()) {
                declareCapability(configViewFactory);
            }
        } else if (service instanceof CapabilityService) {
            capabilityService = (CapabilityService) service;
            if (shouldDeclareCapability()) {
                declareCapability(configViewFactory);
            }
        } else if (service instanceof ConfigViewFactory) {
            configViewFactory = (ConfigViewFactory) service;
            if (shouldDeclareCapability()) {
                declareCapability(configViewFactory);
            }
        } else {
            // Discard...
            context.ungetService(reference);
            return null;
        }

        return service;
    }

    private boolean shouldDeclareCapability() {
        return !capabilityDeclared && webSocketServiceAvailable && null != configViewFactory && null != capabilityService;
    }

    private void declareCapability(final ConfigViewFactory configViewFactory) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, CAPABILITY_WEBSOCKET);
        checkerRegistration = context.registerService(CapabilityChecker.class, new CapabilityChecker() {

            @Override
            public boolean isEnabled(String capability, Session ses) throws OXException {
                if (CAPABILITY_WEBSOCKET.equals(capability)) {
                    final ServerSession session = ServerSessionAdapter.valueOf(ses);
                    if (session.isAnonymous() || session.getUser().isGuest()) {
                        return false;
                    }

                    ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());
                    ComposedConfigProperty<Boolean> property = view.property("com.openexchange.websockets.enabled", boolean.class);

                    if (null == property || !property.isDefined()) {
                        // Default is "true"
                        return true;
                    }

                    return property.get().booleanValue();
                }

                return true;
            }
        }, properties);

        capabilityService.declareCapability(CAPABILITY_WEBSOCKET);
        capabilityDeclared = true;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Ignore
    }

    @Override
    public synchronized void removedService(ServiceReference<Object> reference, Object service) {
        if (service instanceof WebSocketService) {
            if (null != capabilityService && capabilityDeclared) {
                undeclareCapability();
            }
            webSocketServiceAvailable = false;
        } else if (service instanceof CapabilityService) {
            if (capabilityDeclared) {
                undeclareCapability();
            }
            this.capabilityService = null;
        } else if (service instanceof ConfigViewFactory) {
            if (null != capabilityService && capabilityDeclared) {
                undeclareCapability();
            }
            this.configViewFactory = null;
        }

        // Unget anyway
        context.ungetService(reference);
    }

    private void undeclareCapability() {
        ServiceRegistration<CapabilityChecker> checkerRegistration = this.checkerRegistration;
        if (null != checkerRegistration) {
            this.checkerRegistration = null;
            checkerRegistration.unregister();
        }

        capabilityService.undeclareCapability(CAPABILITY_WEBSOCKET);
        capabilityDeclared = false;
    }

}
