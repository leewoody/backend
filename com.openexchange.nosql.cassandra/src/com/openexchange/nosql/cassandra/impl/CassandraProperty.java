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

import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.openexchange.config.lean.Property;
import com.openexchange.nosql.cassandra.CassandraService;

/**
 * {@link CassandraProperty}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum CassandraProperty implements Property {
    /**
     * Defines the name of the Cassandra cluster. Technically this name does not correlate
     * with the name configured in the real Cassandra cluster, but it's rather used to distinguish
     * exposed JMX metrics when multiple Cluster instances live in the same JVM
     */
    clusterName("ox"),
    /**
     * Defines the Cassandra seed node(s) as a comma separated list
     */
    clusterContactPoints("127.0.0.1"),
    /**
     * Defines the port on which the Cassandra server is running.
     * <p/>
     * Defaults to <code>9042</code>
     */
    port(9042),
    /**
     * Defines load balancing policy to use for the cluster. There are three
     * load balancing policies to choose from:
     * <ul>
     * <li>{@link CassandraLoadBalancingPolicy#RoundRobin}</li>
     * <li>{@link CassandraLoadBalancingPolicy#DCAwareRoundRobin}</li>
     * <li>{@link CassandraLoadBalancingPolicy#DCTokenAwareRoundRobin}</li>
     * </ul>
     * <p/>
     * Defaults to {@link CassandraLoadBalancingPolicy#RoundRobin}
     */
    loadBalancingPolicy(CassandraLoadBalancingPolicy.RoundRobin.name()),
    /**
     * A policy that defines a default behaviour to adopt when a request fails. There are three
     * retry policies to choose from:
     * <ul>
     * <li>{@link CassandraRetryPolicy#defaultRetryPolicy}</li>
     * <li>{@link CassandraRetryPolicy#downgradingConsistencyRetryPolicy}</li>
     * <li>{@link CassandraRetryPolicy#fallthroughRetryPolicy}</li>
     * </ul>
     * <p/>
     *
     * Defaults to {@link CassandraRetryPolicy#defaultRetryPolicy}
     */
    retryPolicy(CassandraRetryPolicy.defaultRetryPolicy.name()),
    /**
     * Logs the retry decision of the policy.
     * <p/>
     * Defaults to <code>false</code>
     */
    logRetryPolicy(false),
    /**
     * Enables the query logger which logs all executed statements
     * <p/>
     * Defatuls to <code>false</code>
     */
    enableQueryLogger(false),
    /**
     * Defines the latency threshold in milliseconds beyond which queries are considered 'slow'
     * and logged as such by the Cassandra service. Used in conjunction with the 'enableQueryLogger'
     * property.
     * <p/>
     * Defaults to <code>5000</code> msec.
     */
    queryLatencyThreshold(5000),
    /**
     * Defines the amount of time (in seconds) for connection keepalive in the form of a heartbeat.
     * When a connection has been idle for the given amount of time, the Cassandra service will
     * simulate activity by writing a dummy request to it (by sending an <code>OPTIONS</code> message).
     * <p/>
     * To disable heartbeat, set the interval to 0.
     * <p/>
     * Defaults to 30 seconds
     */
    poolingHeartbeat(30),
    /**
     * The Cassandra service's connection pools have a variable size, which gets adjusted automatically
     * depending on the current load. There will always be at least a minimum number of connections, and
     * at most a maximum number. These values can be configured independently by host distance (the distance
     * is determined by your LoadBalancingPolicy, and will generally indicate whether a host is in the same
     * datacenter or not).
     * <p/>
     * Defaults to minimum 4 and maximum 10 for local nodes (i.e. in the same datacenter) and minimum 2 and
     * maximum 4 for remote nodes
     */
    minimumLocalConnectionsPerNode(4),
    maximumLocalConnectionsPerNode(10),
    minimumRemoteConnectionsPerNode(2),
    maximumRemoteConnectionsPerNode(4),
    /**
     * When activity goes down, the driver will "trash" connections if the maximum number of requests
     * in a 10 second time period can be satisfied by less than the number of connections opened. Trashed
     * connections are kept open but do not accept new requests. After the given timeout, trashed connections
     * are closed and removed. If during that idle period activity increases again, those connections will be
     * resurrected back into the active pool and reused.
     * <p/>
     * Defaults to 120 seconds
     */
    idleConnectionTrashTimeout(120),
    /**
     * Defines the throttling of concurrent requests per connection on local (on the same datacenter)
     * and remote nodes (on a different datacenter).
     * <p/>
     * For Cassandra clusters that use a protocol v2 and below, there is no reason to throttle.
     * It should be set to 128 (the max)
     * <p/>
     * For Cassandra clusters that use a protocol v3 and up, it is set by default to 1024 for LOCAL hosts,
     * and to 256 for REMOTE hosts. These low defaults were chosen so that the default configuration
     * for protocol v2 and v3 allow the same total number of simultaneous requests (to avoid bad surprises
     * when clients migrate from v2 to v3). This threshold can be raised, or even set it to the max which is
     * 32768 for LOCAL nodes and 2000 for REMOTE nodes
     * <p/>
     * Note that that high values will give clients more bandwidth and therefore put more pressure on
     * the cluster. This might require some tuning, especially with many clients.
     */
    maximumRequestsPerLocalConnection(1024),
    maximumRequestsPerRemoteConnection(256),
    /**
     * When the {@link CassandraService} tries to send a request to a host, it will first try to acquire
     * a connection from this host's pool. If the pool is busy (i.e. all connections are already handling
     * their maximum number of in flight requests), the acquisition attempt gets enqueued until a
     * connection becomes available again.
     * <p/>
     * The size of that queue is controlled by {@link PoolingOptions#setMaxQueueSize}. If the queue has
     * already reached its limit, further attempts to acquire a connection will be rejected immediately:
     * the {@link CassandraService} will move on and try to acquire a connection from the next host's
     * pool. The limit can be set to 0 to disable queueing entirely.
     * <p/>
     * If all hosts are busy with a full queue, the request will fail with a {@link NoHostAvailableException}.
     */
    acquisitionQueueMaxSize(256),
    /**
     * The connection timeout in milliseconds.
     */
    connectTimeout(SocketOptions.DEFAULT_CONNECT_TIMEOUT_MILLIS),
    /**
     * The read timeout in milliseconds.
     */
    readTimeout(SocketOptions.DEFAULT_READ_TIMEOUT_MILLIS),
    ;

    private final Object defaultValue;

    private static final String PREFIX = "com.openexchange.nosql.cassandra.";

    /**
     * Initializes a new {@link CassandraProperty}.
     */
    private CassandraProperty(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the fully qualified name for the property
     *
     * @return the fully qualified name for the property
     */
    @Override
    public String getFQPropertyName() {
        return PREFIX + name();
    }

    /**
     * Returns the default value of this property
     *
     * @return the default value of this property
     */
    @Override
    public <T extends Object> T getDefaultValue(Class<T> cls) {
        if (false == defaultValue.getClass().isAssignableFrom(cls)) {
            throw new IllegalArgumentException("The object cannot be converted to the specified type '" + cls.getCanonicalName() + "'");
        }

        return cls.cast(defaultValue);
    }
}
