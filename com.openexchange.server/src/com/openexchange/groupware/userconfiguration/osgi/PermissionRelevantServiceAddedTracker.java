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

package com.openexchange.groupware.userconfiguration.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.service.PermissionAvailabilityService;
import com.openexchange.passwordchange.PasswordChangeService;

/**
 * The {@link PermissionRelevantServiceAddedTracker} is used to track services which availability have got impact for the user interface (e.
 * g. {@link PasswordChangeService}). If the service is available it the availability will be registered for the
 * {@link PermissionAvailabilityService} to analyze it for the user interface.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class PermissionRelevantServiceAddedTracker<S> extends ServiceTracker<S, S> {

    private volatile ServiceRegistration<PermissionAvailabilityService> serviceRegistration;

    /**
     * Initializes a new {@link PermissionRelevantServiceAddedTracker}.
     *
     * @param context - the context
     * @param clazz - the class to track
     */
    public PermissionRelevantServiceAddedTracker(BundleContext context, Class<S> clazz) {
        super(context, clazz, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public S addingService(ServiceReference<S> reference) {
        final S service = context.getService(reference);
        serviceRegistration = context.registerService(PermissionAvailabilityService.class, new PermissionAvailabilityService(Permission.EDIT_PASSWORD), null);
        return service;
    }

    @Override
    public void removedService(ServiceReference<S> reference, S service) {
        final ServiceRegistration<PermissionAvailabilityService> serviceRegistration = this.serviceRegistration;
        if (null != serviceRegistration) {
            serviceRegistration.unregister();
            this.serviceRegistration = null;
        }
        super.removedService(reference, service);
    }

}
