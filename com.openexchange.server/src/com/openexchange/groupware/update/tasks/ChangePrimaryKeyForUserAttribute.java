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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Tools;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * {@link ChangePrimaryKeyForUserAttribute} - Changes the PRIMARY KEY of the <code>"user_attribute"</code> table from ("cid", "uuid") to ("cid", "id", "name").
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ChangePrimaryKeyForUserAttribute extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link ChangePrimaryKeyForUserAttribute}.
     */
    public ChangePrimaryKeyForUserAttribute() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { com.openexchange.groupware.update.tasks.AddUUIDForUserAttributeTable.class.getName(), com.openexchange.groupware.update.tasks.RemoveAliasInUserAttributesTable.class.getName() };
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        // Get required service
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);

        // Initialize connection
        int contextId = params.getContextId();
        Connection con = dbService.getForUpdateTask(contextId);

        // Start task processing
        boolean restoreAutocommit = false;
        boolean rollback = false;
        boolean modified = false;
        try {
            if (Tools.existsPrimaryKey(con, "user_attribute", new String[] {"cid", "id", "name"})) {
                // PRIMARY KEY already changed
                return;
            }

            DBUtils.startTransaction(con);
            restoreAutocommit = true;
            rollback = true;

            doPerform(con);
            modified = true;

            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            if (restoreAutocommit) {
                autocommit(con);
            }
            if (modified) {
                dbService.backForUpdateTask(contextId, con);
            } else {
                dbService.backForUpdateTaskAfterReading(contextId, con);
            }
        }
    }

    private void doPerform(Connection con) throws SQLException {
        // Ensure all "alias" entries in 'user_attribute' table exist in 'user_alias' one
        ensureAllAliasEntriesExist(con);

        // Now drop them
        dropAllAliasEntries(con);

        // Check for duplicate entries
        boolean useGroupBy = false;
        if (useGroupBy) {
            checkAndResolveDuplicateEntries(con);
        } else {
            checkAndResolveDuplicateEntriesManually(con);
        }

        // Reset PRIMARY KEY
        if (Tools.hasPrimaryKey(con, "user_attribute")) {
            Tools.dropPrimaryKey(con, "user_attribute");
        }
        Tools.createPrimaryKey(con, "user_attribute", new String[] {"cid", "id", "name"});

        // Drop the 'uuid' column with next version
    }

    private void checkAndResolveDuplicateEntriesManually(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Extract all distinct context identifiers
            stmt = con.prepareStatement("SELECT DISTINCT cid FROM user_attribute");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No rows available in connection-associated schema
                return;
            }

            // Put context identifiers into a light-weight list
            TIntList contextIds = new TIntArrayList(512);
            do {
                contextIds.add(rs.getInt(1));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Chunk-wise query user attributes
            Map<Duplicate, Values> mapping;
            {
                int limit = Databases.IN_LIMIT;
                int length = contextIds.size();
                mapping = new HashMap<>(length);
                for (int off = 0; off < length; off += limit) {
                    int clen = off + limit > length ? length - off : limit;
                    stmt = con.prepareStatement(DBUtils.getIN("SELECT cid, id, name, value, uuid FROM user_attribute WHERE cid IN (", clen));
                    int pos = 1;
                    for (int j = 0; j < clen; j++) {
                        stmt.setInt(pos++, contextIds.get(off+j));
                    }
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        String name = rs.getString(3);
                        Duplicate d = new Duplicate(rs.getInt(1), rs.getInt(2), name.trim());
                        Values values = mapping.get(d);
                        if (null == values) {
                            values = new Values();
                            mapping.put(d, values);
                        }
                        String value = rs.getString(4);
                        values.addValue(new Value(value.trim(), UUIDs.toUUID(rs.getBytes(5))));
                    }
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;
                }
                contextIds = null; // Might help GC
            }

            // Handle multiple values associated with the same name for a user
            for (Map.Entry<Duplicate, Values> entry : mapping.entrySet()) {
                Values dupValues = entry.getValue();
                if (dupValues.size() > 1) {
                    handleDuplicateValues(entry.getKey(), dupValues, con);
                }
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void checkAndResolveDuplicateEntries(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, id, name, value, uuid FROM user_attribute WHERE (cid, id, name) IN (SELECT cid, id, name FROM user_attribute GROUP BY cid, id, name HAVING COUNT(*) > 1) ORDER BY cid,id,name");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No rows
                return;
            }

            Map<Duplicate, Values> mapping = new HashMap<>();
            do {
                Duplicate d = new Duplicate(rs.getInt(1), rs.getInt(2), rs.getString(3));
                Values values = mapping.get(d);
                if (null == values) {
                    values = new Values();
                    mapping.put(d, values);
                }
                values.addValue(new Value(rs.getString(4), UUIDs.toUUID(rs.getBytes(5))));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            for (Map.Entry<Duplicate, Values> entry : mapping.entrySet()) {
                Values dupValues = entry.getValue();
                if (dupValues.size() > 1) { // Just to be sure
                    handleDuplicateValues(entry.getKey(), dupValues, con);
                }
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void handleDuplicateValues(Duplicate duplicate, Values dupValues, Connection con) throws SQLException {
        List<Value> values = dupValues.values;
        if (1 == dupValues.numOfDifferentValues()) {
            // All duplicate entries have the same value
            deleteAllExceptLast(duplicate, values, con);
        } else {
            // Keep highest
            keepHighestValue(duplicate, values, con);
        }
    }

    private void keepHighestValue(Duplicate duplicate, List<Value> values, Connection con) throws SQLException {
        // Sort values
        Collections.sort(values);

        // Delete all, except last one
        deleteAllExceptLast(duplicate, values, con);
    }

    private void deleteAllExceptLast(Duplicate duplicate, List<Value> values, Connection con) throws SQLException {
        // Remove last element
        values.remove(values.size() - 1);

        // Delete rest
        delete(duplicate, values, con);
    }

    private void delete(Duplicate duplicate, List<Value> values, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(DBUtils.getIN("DELETE FROM user_attribute WHERE cid=? AND id=? AND name=? AND uuid IN (", values.size()));
            int pos = 1;
            stmt.setInt(pos++, duplicate.contextId);
            stmt.setInt(pos++, duplicate.userId);
            stmt.setString(pos++, duplicate.name);
            for (Iterator<Value> iter = values.iterator(); iter.hasNext();) {
                Value valueToRemove = iter.next();
                stmt.setBytes(pos++, UUIDs.toByteArray(valueToRemove.uuid));
            }
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private void dropAllAliasEntries(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM user_attribute WHERE name=?");
            stmt.setString(1, "alias");
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private void ensureAllAliasEntriesExist(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, id, value, uuid FROM user_attribute WHERE name=?");
            stmt.setString(1, "alias");
            rs = stmt.executeQuery();

            if (!rs.next()) {
                // No remaining "alias" entries left in 'user_attribute' table
                return;
            }

            Set<Alias> aliases = new LinkedHashSet<>();
            do {
                aliases.add(new Alias(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getBytes(4)));
            } while (rs.next());

            Databases.closeSQLStuff(rs, stmt);
            rs = null;

            stmt = con.prepareStatement("INSERT INTO user_alias (cid, user, alias, uuid) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE user=user");
            for (Alias alias : aliases) {
                stmt.setInt(1, alias.contextId);
                stmt.setInt(2, alias.userId);
                stmt.setString(3, alias.alias);
                stmt.setBytes(4, alias.uuid);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    // ------------------------------------------------------------- Helper classes ---------------------------------------------------------

    private static class Duplicate {

        final int contextId;
        final int userId;
        final String name;
        private final int hash;

        Duplicate(int contextId, int userId, String name) {
            super();
            this.contextId = contextId;
            this.userId = userId;
            String lowerCaseName = Strings.asciiLowerCase(name);
            this.name = lowerCaseName;

            int prime = 31;
            int result = prime * 1 + contextId;
            result = prime * result + userId;
            result = prime * result + ((lowerCaseName == null) ? 0 : lowerCaseName.hashCode());
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Duplicate other = (Duplicate) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{contextId=").append(contextId).append(", userId=").append(userId).append(", ");
            if (name != null) {
                builder.append("name=").append(name);
            }
            builder.append("}");
            return builder.toString();
        }
    }

    private static class Values {

        final List<Value> values;
        private final Set<String> diffs;

        Values() {
            super();
            values = new ArrayList<>(6);
            diffs = new HashSet<>(6);
        }

        void addValue(Value value) {
            values.add(value);
            diffs.add(value.value);
        }

        int numOfDifferentValues() {
            return diffs.size();
        }

        int size() {
            return values.size();
        }

        @Override
        public String toString() {
            return values.toString();
        }
    }

    private static class Value implements Comparable<Value> {

        final String value;
        final UUID uuid;

        Value(String value, UUID uuid) {
            super();
            this.value = value;
            this.uuid = uuid;
        }

        @Override
        public int compareTo(Value o) {
            return this.value.compareTo(o.value);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            if (value != null) {
                builder.append("value=").append(value).append(", ");
            }
            if (uuid != null) {
                builder.append("uuid=").append(uuid);
            }
            builder.append("}");
            return builder.toString();
        }
    }

    private static class Alias {

        final int contextId;
        final int userId;
        final String alias;
        final byte[] uuid;
        private final int hash;

        Alias(int contextId, int userId, String alias, byte[] uuid) {
            super();
            this.contextId = contextId;
            this.userId = userId;
            this.alias = alias;
            this.uuid = uuid;
            int prime = 31;
            int result = prime * 1 + contextId;
            result = prime * result + userId;
            result = prime * result + ((alias == null) ? 0 : alias.hashCode());
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Alias other = (Alias) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            if (alias == null) {
                if (other.alias != null) {
                    return false;
                }
            } else if (!alias.equals(other.alias)) {
                return false;
            }
            return true;
        }
    }

}
