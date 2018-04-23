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

package com.openexchange.nosql.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.nosql.cassandra.exceptions.CassandraServiceExceptionCodes;

/**
 * {@link CassandraServices} - A utility class for Cassandra service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class CassandraServices {

    /**
     * Initializes a new {@link CassandraServices}.
     */
    private CassandraServices() {
        super();
    }

    /**
     * Executes given query using specified session.
     *
     * @param query The query to execute
     * @param session The session to use
     * @return The result of the query. That result will never be <code>null</code>, but can be empty (and will be for any non <code>SELECT</code> query).
     * @throws OXException If executing the query fails
     */
    public static ResultSet executeQuery(String query, Session session) throws OXException {
        if (Strings.isEmpty(query) || null == session) {
            return null;
        }

        try {
            return session.execute(query);
        } catch (com.datastax.driver.core.exceptions.NoHostAvailableException e) {
            throw CassandraServiceExceptionCodes.CONTACT_POINTS_NOT_REACHABLE_SIMPLE.create(e, new Object[0]);
        } catch (com.datastax.driver.core.exceptions.QueryExecutionException e) {
            throw CassandraServiceExceptionCodes.QUERY_EXECUTION_ERROR.create(e, query);
        } catch (com.datastax.driver.core.exceptions.QueryValidationException e) {
            throw CassandraServiceExceptionCodes.QUERY_VALIDATION_ERROR.create(e, query);
        } catch (com.datastax.driver.core.exceptions.DriverException e) {
            throw CassandraServiceExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw CassandraServiceExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Executes given query using specified session.
     *
     * @param statemnent The statement to execute
     * @param session The session to use
     * @return The result of the query. That result will never be <code>null</code>, but can be empty (and will be for any non <code>SELECT</code> query).
     * @throws OXException If executing the query fails
     */
    public static ResultSet executeQuery(Statement statemnent, Session session) throws OXException {
        if (null == statemnent || null == session) {
            return null;
        }

        try {
            return session.execute(statemnent);
        } catch (com.datastax.driver.core.exceptions.NoHostAvailableException e) {
            throw CassandraServiceExceptionCodes.CONTACT_POINTS_NOT_REACHABLE_SIMPLE.create(e, new Object[0]);
        } catch (com.datastax.driver.core.exceptions.QueryExecutionException e) {
            throw CassandraServiceExceptionCodes.QUERY_EXECUTION_ERROR.create(e, statemnent);
        } catch (com.datastax.driver.core.exceptions.QueryValidationException e) {
            throw CassandraServiceExceptionCodes.QUERY_VALIDATION_ERROR.create(e, statemnent);
        } catch (com.datastax.driver.core.exceptions.DriverException e) {
            throw CassandraServiceExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
