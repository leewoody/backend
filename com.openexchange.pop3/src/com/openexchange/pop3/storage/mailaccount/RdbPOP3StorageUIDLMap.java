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

package com.openexchange.pop3.storage.mailaccount;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.POP3ExceptionCode;
import com.openexchange.pop3.storage.FullnameUIDPair;
import com.openexchange.pop3.storage.POP3StorageUIDLMap;
import com.openexchange.session.Session;

/**
 * {@link RdbPOP3StorageUIDLMap} - Database-backed implementation of {@link POP3StorageUIDLMap}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbPOP3StorageUIDLMap implements POP3StorageUIDLMap {

    private final int cid;
    private final int user;
    private final int accountId;

    /**
     * Initializes a new {@link RdbPOP3StorageUIDLMap}.
     */
    public RdbPOP3StorageUIDLMap(final POP3Access pop3Access) {
        super();
        final Session s = pop3Access.getSession();
        cid = s.getContextId();
        user = s.getUserId();
        accountId = pop3Access.getAccountId();
    }

    /**
     * Drops all ID entries related to specified POP3 account.
     *
     * @param accountId The account ID
     * @param user The user ID
     * @param cid The context ID
     * @param con The connection to use
     * @throws OXException If dropping properties fails
     */
    public static void dropIDs(final int accountId, final int user, final int cid, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM pop3_storage_ids WHERE cid = ? AND user = ? AND id = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void addMappings(final String[] uidls, final FullnameUIDPair[] fullnameUIDPairs) throws OXException {
        final Connection con = Database.get(cid, true);
        PreparedStatement stmt = null;
        try {
            // Insert or update specified mappings
            stmt = con.prepareStatement("INSERT INTO pop3_storage_ids (cid, user, id, uidl, fullname, uid) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE fullname=?, uid=?");
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            stmt.setInt(3, accountId);
            for (int i = 0; i < uidls.length; i++) {
                String uidl = uidls[i];
                if (uidl != null) {
                    FullnameUIDPair pair = fullnameUIDPairs[i];
                    String fullname = pair.getFullname();
                    String mailId = pair.getMailId();
                    stmt.setString(4, uidl);
                    stmt.setString(5, fullname);
                    stmt.setString(6, mailId);
                    stmt.setString(7, fullname);
                    stmt.setString(8, mailId);
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        } catch (final java.sql.BatchUpdateException e) {
            closeSQLStuff(null, stmt);
            stmt = null;
            // One-by-one
            for (int i = 0; i < uidls.length; i++) {
                String uidl = uidls[i];
                if (uidl != null) {
                    insertOnDuplicateUpdate(uidl, fullnameUIDPairs[i], con);
                }
            }
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    private void insertOnDuplicateUpdate(final String uidl, final FullnameUIDPair pair, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO pop3_storage_ids (cid, user, id, uidl, fullname, uid) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE fullname=?, uid=?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, uidl);
            stmt.setString(pos++, pair.getFullname());
            stmt.setString(pos++, pair.getMailId());
            stmt.setString(pos++, pair.getFullname());
            stmt.setString(pos, pair.getMailId());
            stmt.executeUpdate();
        } catch (final DataTruncation e) {
            throw POP3ExceptionCode.UIDL_TOO_BIG.create(e, uidl);
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    @Override
    public FullnameUIDPair getFullnameUIDPair(final String uidl) throws OXException {
        final Connection con = Database.get(cid, false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT fullname, uid FROM pop3_storage_ids WHERE cid = ? AND user = ? AND id = ? AND uidl = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos, uidl);
            rs = stmt.executeQuery();
            return rs.next() ? new FullnameUIDPair(rs.getString(1), rs.getString(2)) : null;
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    @Override
    public String getUIDL(final FullnameUIDPair fullnameUIDPair) throws OXException {
        final Connection con = Database.get(cid, false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT uidl FROM pop3_storage_ids WHERE cid = ? AND user = ? AND id = ? AND fullname = ? AND uid = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, fullnameUIDPair.getFullname());
            stmt.setString(pos, fullnameUIDPair.getMailId());
            rs = stmt.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    @Override
    public String[] getUIDLs(final FullnameUIDPair[] fullnameUIDPairs) throws OXException {
        final String[] uidls = new String[fullnameUIDPairs.length];
        for (int i = 0; i < uidls.length; i++) {
            uidls[i] = getUIDL(fullnameUIDPairs[i]);
        }
        return uidls;
    }

    @Override
    public FullnameUIDPair[] getFullnameUIDPairs(final String[] uidls) throws OXException {
        final FullnameUIDPair[] pairs = new FullnameUIDPair[uidls.length];
        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = getFullnameUIDPair(uidls[i]);
        }
        return pairs;
    }

    @Override
    public Map<String, FullnameUIDPair> getAllUIDLs() throws OXException {
        final Connection con = Database.get(cid, false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT uidl, fullname, uid FROM pop3_storage_ids WHERE cid = ? AND user = ? AND id = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyMap();
            }

            Map<String, FullnameUIDPair> m = new HashMap<String, FullnameUIDPair>(32);
            do {
                m.put(rs.getString(1), new FullnameUIDPair(rs.getString(2), rs.getString(3)));
            } while (rs.next());
            return m;
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    @Override
    public void deleteFullnameUIDPairMappings(final FullnameUIDPair[] fullnameUIDPairs) throws OXException {
        final Connection con = Database.get(cid, true);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM pop3_storage_ids WHERE cid = ? AND user = ? AND id = ? AND fullname = ? AND uid = ?");
            for (int i = 0; i < fullnameUIDPairs.length; i++) {
                final FullnameUIDPair pair = fullnameUIDPairs[i];
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, pair.getFullname());
                stmt.setString(pos++, pair.getMailId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    @Override
    public void deleteUIDLMappings(final String[] uidls) throws OXException {
        final Connection con = Database.get(cid, true);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM pop3_storage_ids WHERE cid = ? AND user = ? AND id = ? AND uidl = ?");
            for (int i = 0; i < uidls.length; i++) {
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, uidls[i]);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }
}
