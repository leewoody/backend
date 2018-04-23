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

package com.openexchange.nosql.cassandra.mbean.impl;

import javax.management.NotCompliantMBeanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Session.State;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.management.AnnotatedDynamicStandardMBean;
import com.openexchange.nosql.cassandra.CassandraService;
import com.openexchange.nosql.cassandra.impl.CassandraServiceImpl;
import com.openexchange.nosql.cassandra.mbean.CassandraNodeMBean;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CassandraNodeMBeanImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CassandraNodeMBeanImpl extends AnnotatedDynamicStandardMBean implements CassandraNodeMBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraNodeMBeanImpl.class);

    private Host host;
    private int connections;
    private int inFlightQueries;
    private int maxLoad;
    private String hostState;
    private String cassandraVersion;
    private int trashedConnections;

    /**
     * Initialises a new {@link CassandraNodeMBeanImpl}.
     * 
     * @throws NotCompliantMBeanException
     */
    public CassandraNodeMBeanImpl(ServiceLookup services, Host host) throws NotCompliantMBeanException {
        super(services, CassandraNodeMBean.NAME, CassandraNodeMBean.class);
        this.host = host;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.nosql.cassandra.impl.AbstractCassandraMBean#refresh()
     */
    @Override
    protected void refresh() {
        try {
            CassandraService cassandraService = getService(CassandraService.class);
            Cluster cluster = cassandraService.getCluster();
            LoadBalancingPolicy loadBalancingPolicy = cluster.getConfiguration().getPolicies().getLoadBalancingPolicy();
            PoolingOptions poolingOptions = cluster.getConfiguration().getPoolingOptions();

            Session session = ((CassandraServiceImpl) cassandraService).getSession();
            State state = session.getState();
            HostDistance distance = loadBalancingPolicy.distance(host);

            connections = state.getOpenConnections(host);
            trashedConnections = state.getTrashedConnections(host);
            inFlightQueries = state.getInFlightQueries(host);
            maxLoad = connections * poolingOptions.getMaxRequestsPerConnection(distance);
            hostState = host.getState();
            cassandraVersion = host.getCassandraVersion().toString();
        } catch (OXException e) {
            LOGGER.error("Could not refresh the statistics for the Cassandra node '{}' in datacenter '{}' in rack '{}'.", host.getAddress().getHostName(), host.getDatacenter(), host.getRack(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.nosql.cassandra.CassandraNodeMBean#getNodeName()
     */
    @Override
    public String getNodeName() {
        return host.getAddress().getHostName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.nosql.cassandra.CassandraNodeMBean#getConnections()
     */
    @Override
    public int getConnections() {
        return connections;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.nosql.cassandra.CassandraNodeMBean#getInFlightQueries()
     */
    @Override
    public int getInFlightQueries() {
        return inFlightQueries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.nosql.cassandra.CassandraNodeMBean#getMaxLoad()
     */
    @Override
    public int getMaxLoad() {
        return maxLoad;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.nosql.cassandra.CassandraNodeMBean#getState()
     */
    @Override
    public String getState() {
        return hostState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.nosql.cassandra.CassandraNodeMBean#getCassandraVersion()
     */
    @Override
    public String getCassandraVersion() {
        return cassandraVersion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.nosql.cassandra.CassandraNodeMBean#getTrashedConnections()
     */
    @Override
    public int getTrashedConnections() {
        return trashedConnections;
    }
}
