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

package com.openexchange.database.internal;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.Databases;
import com.openexchange.database.internal.wrapping.ConnectionReturnerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.pooling.PoolingException;

/**
 * Fetches a connection with timeouts and selects the wanted schema.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class TimeoutFetchAndSchema implements FetchAndSchema {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TimeoutFetchAndSchema.class);
    private final ReplicationMonitor monitor;
    private final boolean setSchema;

    /**
     * Initializes a new {@link TimeoutFetchAndSchema}.
     *
     * @param monitor The replication monitor
     * @param setSchema <code>true</code> to set the schema name when getting the connection, <code>false</code>, otherwise
     */
    TimeoutFetchAndSchema(ReplicationMonitor monitor, boolean setSchema) {
        super();
        this.monitor = monitor;
        this.setSchema = setSchema;
    }

    @Override
    public Connection get(Pools pools, AssignmentImpl assign, boolean write, boolean usedAsRead) throws PoolingException, OXException {
        int poolId = write ? assign.getWritePoolId() : assign.getReadPoolId();
        ConnectionPool pool = pools.getPool(poolId);
        Connection retval = null;
        do {
            try {
                // Pools cleaner may stop a pool just after it is fetched in the above line. See bug 27126. This is a race condition. Fixing
                // this with a lock in the Pools class around fetching the correct ConnectionPool and a connection from it will result in
                // too much threads waiting on that lock if creating the connection once becomes slow.
                retval = pool.get();
            } catch (PoolingException e) {
                // So we will try to catch up here with the pool that has been stopped unexpectedly.
                if (!pool.isStopped()) {
                    throw e;
                }
                pool = pools.getPool(poolId);
            }
        } while (null == retval);
        if (setSchema) {
            try {
                final String schema = assign.getSchema();
                if (null != schema && !retval.getCatalog().equals(schema)) {
                    retval.setCatalog(schema);
                }
            } catch (SQLException e) {
                try {
                    pool.back(retval);
                } catch (PoolingException e1) {
                    Databases.close(retval);
                    LOG.error(e1.getMessage(), e1);
                }
                throw DBPoolingExceptionCodes.SCHEMA_FAILED.create(e);
            }
        }
        return ConnectionReturnerFactory.createConnection(pools, monitor, assign, retval, false, write, usedAsRead);
    }
}
