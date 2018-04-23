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

package com.openexchange.nosql.cassandra.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.datastax.driver.core.Cluster.Initializer;
import com.datastax.driver.core.Configuration;
import com.datastax.driver.core.Host.StateListener;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.Policies;
import com.datastax.driver.core.policies.RetryPolicy;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CassandraServiceInitializer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
class CassandraServiceInitializer implements Initializer {

    private final LeanConfigurationService leanConfigurationService;
    private final ServiceLookup services;

    /**
     * Initialises a new {@link CassandraServiceInitializer}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    public CassandraServiceInitializer(ServiceLookup services) {
        super();
        this.services = services;
        leanConfigurationService = services.getService(LeanConfigurationService.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.datastax.driver.core.Cluster.Initializer#getClusterName()
     */
    @Override
    public String getClusterName() {
        return leanConfigurationService.getProperty(CassandraProperty.clusterName);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.datastax.driver.core.Cluster.Initializer#getContactPoints()
     */
    @Override
    public List<InetSocketAddress> getContactPoints() {
        int port = leanConfigurationService.getIntProperty(CassandraProperty.port);
        String cps = leanConfigurationService.getProperty(CassandraProperty.clusterContactPoints);
        String[] cpsSplit = Strings.splitByComma(cps);

        List<InetSocketAddress> contactPoints = new ArrayList<>();
        for (String contactPoint : cpsSplit) {
            contactPoints.add(new InetSocketAddress(contactPoint, port));
        }
        return contactPoints;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.datastax.driver.core.Cluster.Initializer#getConfiguration()
     */
    @Override
    public Configuration getConfiguration() {
        // Retry Policies
        String rp = leanConfigurationService.getProperty(CassandraProperty.retryPolicy);
        boolean logRetryPolicy = leanConfigurationService.getBooleanProperty(CassandraProperty.logRetryPolicy);
        RetryPolicy retryPolicy = logRetryPolicy ? CassandraRetryPolicy.valueOf(rp).getLoggingRetryPolicy() : CassandraRetryPolicy.valueOf(rp).getRetryPolicy();

        // Load Balancing Policies
        String lbPolicy = leanConfigurationService.getProperty(CassandraProperty.loadBalancingPolicy);
        LoadBalancingPolicy loadBalancingPolicy = CassandraLoadBalancingPolicy.createLoadBalancingPolicy(lbPolicy);

        // Build policies
        Policies policies = Policies.builder().withRetryPolicy(retryPolicy).withLoadBalancingPolicy(loadBalancingPolicy).build();

        // Build pooling options
        PoolingOptions poolingOptions = new PoolingOptions();
        int heartbeatInterval = leanConfigurationService.getIntProperty(CassandraProperty.poolingHeartbeat);
        int minLocal = leanConfigurationService.getIntProperty(CassandraProperty.minimumLocalConnectionsPerNode);
        int maxLocal = leanConfigurationService.getIntProperty(CassandraProperty.maximumLocalConnectionsPerNode);
        int minRemote = leanConfigurationService.getIntProperty(CassandraProperty.minimumRemoteConnectionsPerNode);
        int maxRemote = leanConfigurationService.getIntProperty(CassandraProperty.maximumRemoteConnectionsPerNode);
        int idleTimeoutSeconds = leanConfigurationService.getIntProperty(CassandraProperty.idleConnectionTrashTimeout);
        int localMaxRequests = leanConfigurationService.getIntProperty(CassandraProperty.maximumRequestsPerLocalConnection);
        int remoteMaxRequests = leanConfigurationService.getIntProperty(CassandraProperty.maximumRequestsPerRemoteConnection);
        int maxQueueSize = leanConfigurationService.getIntProperty(CassandraProperty.acquisitionQueueMaxSize);
        poolingOptions.setHeartbeatIntervalSeconds(heartbeatInterval);
        poolingOptions.setConnectionsPerHost(HostDistance.LOCAL, minLocal, maxLocal);
        poolingOptions.setConnectionsPerHost(HostDistance.REMOTE, minRemote, maxRemote);
        poolingOptions.setIdleTimeoutSeconds(idleTimeoutSeconds);
        poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, localMaxRequests);
        poolingOptions.setMaxRequestsPerConnection(HostDistance.REMOTE, remoteMaxRequests);
        poolingOptions.setMaxQueueSize(maxQueueSize);

        // Build socket options
        SocketOptions socketOptions = new SocketOptions();
        {
            int connectTimeout = leanConfigurationService.getIntProperty(CassandraProperty.connectTimeout);
            socketOptions.setConnectTimeoutMillis(connectTimeout);
        }
        {
            int readTimeout = leanConfigurationService.getIntProperty(CassandraProperty.readTimeout);
            socketOptions.setConnectTimeoutMillis(readTimeout);
        }

        // Build configuration
        return Configuration.builder().withPolicies(policies).withPoolingOptions(poolingOptions).withSocketOptions(socketOptions).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.datastax.driver.core.Cluster.Initializer#getInitialListeners()
     */
    @Override
    public Collection<StateListener> getInitialListeners() {
        StateListener sl = new MBeanHostStateListener(services);
        return Collections.singletonList(sl);
    }
}
