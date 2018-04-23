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

package com.openexchange.ajax.osgi;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_ACTION;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Stack;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.SessionServletInterceptor;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.ajax.login.handler.KerberosTicketReload;
import com.openexchange.ajax.session.MissingKerberosTicketInterceptor;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.osgi.DependentServiceRegisterer;
import com.openexchange.osgi.Tools;
import com.openexchange.sessiond.SessiondService;

/**
 * Registers the Kerberos ticket reload login server action.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.0
 */
public final class KerberosAJAXActivator implements BundleActivator {

    private final Stack<ServiceTracker<?, ?>> trackers = new Stack<ServiceTracker<?, ?>>();

    private ServiceRegistration<SessionServletInterceptor> registration;

    public KerberosAJAXActivator() {
        super();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        registration = context.registerService(SessionServletInterceptor.class, new MissingKerberosTicketInterceptor(), null);
        Dictionary<String, String> d = new Hashtable<String, String>();
        d.put(PARAMETER_ACTION, "ticketReload");
        DependentServiceRegisterer<LoginRequestHandler> registerer = new DependentServiceRegisterer<LoginRequestHandler>(context, LoginRequestHandler.class, KerberosTicketReload.class, d, SessiondService.class, KerberosService.class, EventAdmin.class);
        trackers.push(new ServiceTracker<Object, Object>(context, registerer.getFilter(), registerer));
        Tools.open(trackers);

    }

    @Override
    public void stop(BundleContext context) {
        Tools.close(trackers);
        registration.unregister();
    }
}
