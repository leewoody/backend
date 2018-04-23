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

package liquibase.precondition.ext;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomPreconditionErrorException;
import liquibase.exception.CustomPreconditionFailedException;
import liquibase.precondition.CustomPrecondition;
import org.apache.commons.lang.Validate;

/**
 * Verifies the size of the database column by ignoring the type of the column!
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class ColumnSizePrecondition implements CustomPrecondition {

    private int expectedSize;

    private String tableName;

    private String columnName;

    /**
     * Sets the expected size of the column (by reflection from liquibase)
     *
     * @param expectedSize - String with the size
     */
    public void setExpectedSize(String expectedSize) {
        this.expectedSize = Integer.parseInt(expectedSize);
    }

    /**
     * Sets the tableName (by reflection from liquibase)
     *
     * @param tableName The tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Sets the columnName (by reflection from liquibase)
     *
     * @param columnName The columnName to set
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void check(final Database database) throws CustomPreconditionFailedException, CustomPreconditionErrorException {
        try {
            Validate.notNull(database, "Database provided by Liquibase might not be null!");
    
            DatabaseConnection databaseConnection = database.getConnection();
            Validate.notNull(databaseConnection, "DatabaseConnection might not be null!");
    
            JdbcConnection connection = null;
            if (databaseConnection instanceof JdbcConnection) {
                connection = (JdbcConnection)databaseConnection;
            } else {
                throw new CustomPreconditionErrorException("Cannot get underlying connection because database connection is not from type JdbcConnection. Type is: " + databaseConnection.getClass().getName());
            }
    
            boolean columnFound = false;
    
            try {
                DatabaseMetaData meta = connection.getUnderlyingConnection().getMetaData();
                ResultSet rsColumns = meta.getColumns(null, null, tableName, null);
                if (!rsColumns.next()) {
                    throw new CustomPreconditionErrorException("No columns for table " + tableName + " found! Aborting database migration execution for the given changeset.");
                }
                rsColumns.beforeFirst();
    
                while (rsColumns.next()) {
                    final String lColumnName = rsColumns.getString("COLUMN_NAME");
                    if (columnName.equals(lColumnName)) {
                        columnFound = true;
                        final int size = rsColumns.getInt("COLUMN_SIZE");
                        if (size == expectedSize) {
                            throw new CustomPreconditionFailedException("Column size is already up to date! Nothing to do.");
                        }
                    }
                }
            } catch (SQLException sqlException) {
                throw new CustomPreconditionErrorException("Error while evaluating type of column " + columnName + " in table " + tableName + ".", sqlException);
            }
            if (!columnFound) {
                throw new CustomPreconditionErrorException("Desired column to update not found! Tried update for column " + columnName + " on table " + tableName);
            }
        } catch (CustomPreconditionErrorException e) {
            throw e;
        } catch (CustomPreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomPreconditionErrorException("Unexpected error", e);
        }
    }
}
