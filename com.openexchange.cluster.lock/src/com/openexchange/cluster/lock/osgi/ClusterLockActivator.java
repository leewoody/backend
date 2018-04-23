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

package com.openexchange.cluster.lock.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.cluster.lock.internal.ClusterLockServiceDatabaseImpl;
import com.openexchange.cluster.lock.internal.ClusterLockServiceHazelcastImpl;
import com.openexchange.cluster.lock.internal.Unregisterer;
import com.openexchange.cluster.lock.internal.groupware.ClusterLockCreateTableTask;
import com.openexchange.cluster.lock.internal.groupware.CreateClusterLockTable;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;

/**
 * {@link ClusterLockActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ClusterLockActivator extends HousekeepingActivator implements Unregisterer {

    private ServiceTracker<HazelcastInstance, HazelcastInstance> tracker;

    /**
     * Initializes a new {@link ClusterLockActivator}.
     */
    public ClusterLockActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastConfigurationService.class, DatabaseService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final HazelcastConfigurationService hzConfigService = getService(HazelcastConfigurationService.class);
        final boolean enabled = hzConfigService.isEnabled();

        if (false == enabled) {
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new ClusterLockCreateTableTask(this)));
            registerService(CreateTableService.class, new CreateClusterLockTable(), null);
            registerService(ClusterLockService.class, new ClusterLockServiceDatabaseImpl(this));
        } else {
            registerService(ClusterLockService.class, new ClusterLockServiceHazelcastImpl(this, this));
            tracker = track(HazelcastInstance.class, new HazelcastClusterLockServiceTracker(context));
            openTrackers();
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterService(ClusterLockService.class);
        super.stopBundle();
    }

    @Override
    public <S> void registerService(Class<S> clazz, S service) {
        super.registerService(clazz, service);
    }

    @Override
    public <S> void unregisterService(S service) {
        super.unregisterService(service);
    }

    @Override
    public void unregister() {
        ServiceTracker<HazelcastInstance, HazelcastInstance> tracker = this.tracker;
        if (null != tracker) {
            tracker.close();
            this.tracker = null;
        }
    }

    @Override
    public void propagateNotActive(HazelcastInstanceNotActiveException notActiveException) {
        final BundleContext context = this.context;
        if (null != context) {
            context.registerService(HazelcastInstanceNotActiveException.class, notActiveException, null);
        }
    }

}
