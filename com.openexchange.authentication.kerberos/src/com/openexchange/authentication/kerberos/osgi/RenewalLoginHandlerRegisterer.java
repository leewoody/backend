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

package com.openexchange.authentication.kerberos.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.authentication.kerberos.impl.DelegationTicketLifecycle;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.kerberos.KerberosUtils;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.timer.TimerService;

/**
 * {@link RenewalLoginHandlerRegisterer}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class RenewalLoginHandlerRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RenewalLoginHandlerRegisterer.class);

    private final BundleContext context;
    private KerberosService kerberosService;
    private TimerService timerService;
    private ServiceRegistration<?> registration;
    private DelegationTicketLifecycle starter;

    public RenewalLoginHandlerRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public synchronized Object addingService(ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        final boolean needsRegistration;
        {
            if (obj instanceof KerberosService) {
                kerberosService = (KerberosService) obj;
            }
            if (obj instanceof TimerService) {
                timerService = (TimerService) obj;
            }
            needsRegistration = null != kerberosService && null != timerService && registration == null;
        }
        if (needsRegistration) {
            LOG.info("Registering delegation ticket renewal service.");
            final Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
            properties.put(EventConstants.EVENT_TOPIC, new String[] { SessiondEventConstants.TOPIC_REMOVE_SESSION, SessiondEventConstants.TOPIC_REMOVE_CONTAINER, KerberosUtils.TOPIC_TICKET_READDED });
            starter = new DelegationTicketLifecycle(kerberosService, timerService);
            registration = context.registerService(new String[] { LoginHandlerService.class.getName() , EventHandler.class.getName() }, starter, properties);
        }
        return obj;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Nothing to do.
    }

    @Override
    public synchronized void removedService(ServiceReference<Object> reference, Object service) {
        ServiceRegistration<?> unregister = null;
        {
            if (service instanceof TimerService) {
                timerService = null;
            }
            if (service instanceof KerberosService) {
                kerberosService = null;
            }
            if (registration != null && (timerService == null || kerberosService == null)) {
                unregister = registration;
                registration = null;
            }
        }
        if (null != unregister) {
            LOG.info("Unregistering delegation ticket renewal service.");
            unregister.unregister();
            starter.stopAll();
        }
        context.ungetService(reference);
    }
}
