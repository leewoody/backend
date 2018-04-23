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

package com.openexchange.subscribe.osgi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.subscribe.CompositeSubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;


/**
 * {@link OSGiSubscriptionSourceDiscoveryCollector}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class OSGiSubscriptionSourceDiscoveryCollector implements ServiceTrackerCustomizer, SubscriptionSourceDiscoveryService {

    private final BundleContext context;
    private final ServiceTracker tracker;
    private final List<ServiceReference> references = new ArrayList<ServiceReference>();

    private final CompositeSubscriptionSourceDiscoveryService delegate = new CompositeSubscriptionSourceDiscoveryService();

    public OSGiSubscriptionSourceDiscoveryCollector(final BundleContext context) {
        this.context = context;
        this.tracker = new ServiceTracker(context, SubscriptionSourceDiscoveryService.class.getName(), this);
        tracker.open();
    }

    public void close() {
        delegate.clear();
        for(final ServiceReference reference : references) {
            context.ungetService(reference);
        }
        tracker.close();
    }

    @Override
    public Object addingService(final ServiceReference reference) {
        final SubscriptionSourceDiscoveryService service = (SubscriptionSourceDiscoveryService) context.getService(reference);
        if(service.getClass() == getClass()) {
            context.ungetService(reference);
            return service;
        }
        delegate.addSubscriptionSourceDiscoveryService(service);
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference reference, final Object service) {

    }

    @Override
    public void removedService(final ServiceReference reference, final Object service) {
        delegate.removeSubscriptionSourceDiscoveryService((SubscriptionSourceDiscoveryService) service);
        references.remove(reference);
        context.ungetService(reference);
    }


    @Override
    public SubscriptionSource getSource(final Context context, final int subscriptionId) throws OXException {
        return delegate.getSource(context, subscriptionId);
    }

    @Override
    public SubscriptionSource getSource(final String identifier) {
        return delegate.getSource(identifier);
    }

    @Override
    public List<SubscriptionSource> getSources() {
        return delegate.getSources();
    }

    @Override
    public List<SubscriptionSource> getSources(final int folderModule) {
        return delegate.getSources(folderModule);
    }

    @Override
    public boolean knowsSource(final String identifier) {
        return delegate.knowsSource(identifier);
    }

    @Override
    public SubscriptionSourceDiscoveryService filter(final int user, final int context) throws OXException {
        return delegate.filter(user, context);
    }

    public void addSubscriptionSourceDiscoveryService(final SubscriptionSourceDiscoveryService service) {
        delegate.addSubscriptionSourceDiscoveryService(service);
    }

    public void removeSubscriptionSourceDiscoveryService(final SubscriptionSourceDiscoveryService service) {
        delegate.removeSubscriptionSourceDiscoveryService(service);
    }


}
