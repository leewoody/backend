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

package com.openexchange.mailaccount.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.mail.utils.DefaultFolderNamesProvider.extractFullname;
import static com.openexchange.mail.utils.ProviderUtility.toSocketAddrString;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.idn.IDNA;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig.PasswordSource;
import com.openexchange.mail.cache.IMailAccessCache;
import com.openexchange.mail.config.ConfiguredServer;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mail.utils.ProviderUtility;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.Event;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.TransportAccount;
import com.openexchange.mailaccount.TransportAccountDescription;
import com.openexchange.mailaccount.TransportAuth;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.mailaccount.UpdateProperties;
import com.openexchange.mailaccount.json.fields.GetSwitch;
import com.openexchange.mailaccount.json.fields.MailAccountGetSwitch;
import com.openexchange.mailaccount.json.fields.SetSwitch;
import com.openexchange.mailaccount.json.fields.TransportAccountGetSwitch;
import com.openexchange.mailaccount.json.fields.TransportGetSwitch;
import com.openexchange.mailaccount.json.fields.TransportSetSwitch;
import com.openexchange.mailaccount.utils.MailAccountUtils;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.secret.SecretEncryptionStrategy;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbMailAccountStorage} - The relational database implementation of mail account storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbMailAccountStorage implements MailAccountStorageService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbMailAccountStorage.class);

    private static final SecretEncryptionStrategy<GenericProperty> STRATEGY = MailPasswordUtil.STRATEGY;

    /**
     * The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type VARCHAR.
     */
    private static final int TYPE_VARCHAR = Types.VARCHAR;

    private static final String PARAM_POP3_STORAGE_FOLDERS = "com.openexchange.mailaccount.pop3Folders";

    private static <V> V performSynchronized(final Callable<V> task, final Session session) throws Exception {
        Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        if (null == lock) {
            lock = Session.EMPTY_LOCK;
        }
        lock.lock();
        try {
            return task.call();
        } finally {
            lock.unlock();
        }
    }

    private static void dropPOP3StorageFolders(final int userId, final int contextId) {
        final SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null != service) {
            for (final Session session : service.getSessions(userId, contextId)) {
                session.setParameter(PARAM_POP3_STORAGE_FOLDERS, null);
            }
        }
    }

    @Override
    public void invalidateMailAccount(final int id, final int userId, final int contextId) throws OXException {
        // Nothing to do
    }

    @Override
    public void invalidateMailAccounts(final int userId, final int contextId) throws OXException {
        // Nothing to do
    }

    /**
     * Gets the POP3 storage folders for specified session.
     *
     * @param session The session
     * @return The POP3 storage folder full names
     * @throws OXException If an error occurs
     */
    @SuppressWarnings("unchecked")
    public static Set<String> getPOP3StorageFolders(final Session session) throws OXException {
        Set<String> set = (Set<String>) session.getParameter(PARAM_POP3_STORAGE_FOLDERS);
        if (null == set) {
            try {
                final Callable<Set<String>> task = new Callable<Set<String>>() {

                    @Override
                    public Set<String> call() throws OXException {
                        Set<String> set = (Set<String>) session.getParameter(PARAM_POP3_STORAGE_FOLDERS);
                        if (null == set) {
                            set = getPOP3StorageFolders0(session.getContextId(), session.getUserId());
                            session.setParameter(PARAM_POP3_STORAGE_FOLDERS, set);
                        }
                        return set;
                    }
                };
                set = performSynchronized(task, session);
            } catch (final OXException e) {
                throw e;
            } catch (final Exception e) {
                throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return set;
    }

    /**
     * Gets the POP3 storage folders for specified user.
     *
     * @param user The user
     * @param context The context
     * @return The POP3 storage folder full names
     * @throws OXException If an error occurs
     */
    public static Set<String> getPOP3StorageFolders(final User user, final Context context) throws OXException {
        return getPOP3StorageFolders0(context.getContextId(), user.getId());
    }

    static Set<String> getPOP3StorageFolders0(int contextId, int userId) throws OXException {
        final Connection con = Database.get(contextId, false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT value FROM user_mail_account_properties WHERE cid = ? AND user = ? AND name = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, "pop3.path");
            rs = stmt.executeQuery();
            final Set<String> set = new HashSet<>(4);
            while (rs.next()) {
                set.add(rs.getString(1));
            }
            return set;
        } catch (final SQLException e) {
            if (null != stmt) {
                final String sql = stmt.toString();
                LOG.debug("\n\tFailed mail account statement:\n\t{}", new Object() { @Override public String toString() { return sql.substring(sql.indexOf(": ") + 2);}});
            }
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            Database.back(contextId, false, con);
        }
    }

    private void fillMailAccount(AbstractMailAccount mailAccount, int id, int userId, int contextId, boolean raw, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam, confirmed_ham, spam_handler, unified_inbox, trash_fullname, sent_fullname, drafts_fullname, spam_fullname, confirmed_spam_fullname, confirmed_ham_fullname, personal, replyTo, archive, archive_fullname, starttls, oauth, disabled FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id);
            stmt.setLong(3, userId);
            result = stmt.executeQuery();
            if (!result.next()) {
                throw MailAccountExceptionCodes.NOT_FOUND.create(I(id), I(userId), I(contextId));
            }
            mailAccount.setId(id);
            mailAccount.setLogin(result.getString(3));
            mailAccount.parseMailServerURL(result.getString(2));
            mailAccount.setName(result.getString(1));
            final String pw = result.getString(4);
            if (result.wasNull()) {
                mailAccount.setPassword(null);
            } else {
                mailAccount.setPassword(pw);
            }
            mailAccount.setPrimaryAddress(result.getString(5));
            mailAccount.setMailStartTls(result.getBoolean(25));
            /*
             * Default folder names
             */
            mailAccount.setTrash(getOptionalString(result.getString(7)));
            mailAccount.setSent(getOptionalString(result.getString(8)));
            mailAccount.setDrafts(getOptionalString(result.getString(9)));
            mailAccount.setSpam(getOptionalString(result.getString(10)));
            mailAccount.setConfirmedSpam(getOptionalString(result.getString(11)));
            mailAccount.setConfirmedHam(getOptionalString(result.getString(12)));
            mailAccount.setArchive(getOptionalString(result.getString(23)));
            /*
             * Spam handler name
             */
            mailAccount.setSpamHandler(result.getString(13));
            /*
             * Unified mail enabled
             */
            mailAccount.setUnifiedINBOXEnabled(result.getInt(14) > 0);
            /*-
             * Default folder full names
             *
             * Full names for: Trash, Sent, Drafts, and Spam
             */
            mailAccount.setTrashFullname(getOptionalString(result.getString(15)));
            mailAccount.setSentFullname(getOptionalString(result.getString(16)));
            mailAccount.setDraftsFullname(getOptionalString(result.getString(17)));
            mailAccount.setSpamFullname(getOptionalString(result.getString(18)));
            mailAccount.setArchiveFullname(getOptionalString(result.getString(24)));
            /*
             * Full names for confirmed-spam and confirmed-ham
             */
            mailAccount.setConfirmedSpamFullname(getOptionalString(result.getString(19)));
            mailAccount.setConfirmedHamFullname(getOptionalString(result.getString(20)));
            /*
             * Personal
             */
            final String pers = result.getString(21);
            if (result.wasNull()) {
                mailAccount.setPersonal(null);
            } else {
                mailAccount.setPersonal(pers);
            }
            /*
             * Reply-To address
             */
            final String replyTo = result.getString(22);
            if (result.wasNull()) {
                mailAccount.setReplyTo(null);
            } else {
                mailAccount.setReplyTo(replyTo);
            }
            /*
             * (Optional) OAuth account identifier
             */
            int oauthAccountId = result.getInt(26);
            if (result.wasNull()) {
                mailAccount.setMailOAuthId(-1);
            } else {
                mailAccount.setMailOAuthId(oauthAccountId);
            }
            /*
             * Enabled flag
             */
            boolean disabled = result.getBoolean(27);
            mailAccount.setMailDisabled(disabled);

            mailAccount.setUserId(userId);
            /*
             * Fill properties
             */
            fillProperties(mailAccount, contextId, userId, id, false, con);
        } catch (final SQLException e) {
            if (null != stmt) {
                final String sql = stmt.toString();
                LOG.debug("\n\tFailed mail account statement:\n\t{}", new Object() { @Override public String toString() { return sql.substring(sql.indexOf(": ") + 2);}});
            }
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private void fillTransportAccount(final AbstractMailAccount mailAccount, final int id, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT name, url, login, password, send_addr, default_flag, personal, replyTo, starttls, oauth, disabled FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id);
            stmt.setLong(3, userId);
            result = stmt.executeQuery();
            if (result.next()) {
                mailAccount.parseTransportServerURL(result.getString(2));
                {
                    final String transportLogin = result.getString(3);
                    if (result.wasNull()) {
                        mailAccount.setTransportLogin(null);
                    } else {
                        mailAccount.setTransportLogin(transportLogin);
                    }
                }
                {
                    final String transportPassword = result.getString(4);
                    if (result.wasNull()) {
                        mailAccount.setTransportPassword(null);
                    } else {
                        mailAccount.setTransportPassword(transportPassword);
                    }
                }
                final String pers = result.getString(7);
                if (!result.wasNull()) {
                    mailAccount.setPersonal(pers);
                }
                final String replyTo = result.getString(8);
                if (!result.wasNull()) {
                    mailAccount.setReplyTo(replyTo);
                }
                mailAccount.setTransportStartTls(result.getBoolean(9));
                int oauthAccountId = result.getInt(10);
                if (result.wasNull()) {
                    mailAccount.setTransportOAuthId(-1);
                } else {
                    mailAccount.setTransportOAuthId(oauthAccountId);
                }
                boolean disabled = result.getBoolean(11);
                mailAccount.setTransportDisabled(disabled);
                /*
                 * Fill properties
                 */
                fillProperties(mailAccount, contextId, userId, id, true, con);
            } else {
                // throw MailAccountExceptionMessages.NOT_FOUND, I(id), I(user), I(contextId));
                mailAccount.setTransportServer((String) null);
            }
        } catch (final SQLException e) {
            if (null != stmt) {
                final String sql = stmt.toString();
                LOG.debug("\n\tFailed mail account statement:\n\t{}", new Object() { @Override public String toString() { return sql.substring(sql.indexOf(": ") + 2);}});
            }
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private static void fillProperties(final AbstractMailAccount mailAccount, final int contextId, final int userId, final int id, final boolean transportProps, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final String table = transportProps ? "user_transport_account_properties" : "user_mail_account_properties";
            stmt = con.prepareStatement("SELECT name, value FROM "+table+" WHERE cid = ? AND user = ? AND id = ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                final Map<String, String> properties = new HashMap<>(8, 1);
                do {
                    final String name = rs.getString(1);
                    if (!rs.wasNull()) {
                        final String value = rs.getString(2);
                        if (!rs.wasNull()) {
                            properties.put(name, value);
                        }
                    }
                } while (rs.next());
                // Add aliases, too
                if (false == transportProps && MailAccount.DEFAULT_ID == id) {
                    properties.put("addresses", getAliases(userId, contextId, mailAccount));
                }
                if (transportProps) {
                    String sTransAuth = properties.remove("transport.auth");
                    if (null != sTransAuth) {
                        mailAccount.setTransportAuth(TransportAuth.transportAuthFor(sTransAuth));
                    }
                    if (!properties.isEmpty()) {
                        mailAccount.setTransportProperties(properties);
                    }
                } else {
                    mailAccount.setProperties(properties);
                }
            } else {
                if (false == transportProps) {
                    // Add aliases, too
                    if (MailAccount.DEFAULT_ID == id) {
                        final Map<String, String> properties = new HashMap<>(8, 1);
                        properties.put("addresses", getAliases(userId, contextId, mailAccount));
                        mailAccount.setProperties(properties);
                    } else {
                        mailAccount.setProperties(Collections.<String, String> emptyMap());
                    }
                }
            }
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private void fillTransportProperties(final TransportAccountImpl transportAccount, final int contextId, final int userId, final int id, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT name, value FROM user_transport_account_properties WHERE cid = ? AND user = ? AND id = ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                final Map<String, String> properties = new HashMap<>(8, 1);
                do {
                    final String name = rs.getString(1);
                    if (!rs.wasNull()) {
                        final String value = rs.getString(2);
                        if (!rs.wasNull()) {
                            properties.put(name, value);
                        }
                    }
                } while (rs.next());

                String sTransAuth = properties.remove("transport.auth");
                if (null != sTransAuth) {
                    transportAccount.setTransportAuth(TransportAuth.transportAuthFor(sTransAuth));
                }
                transportAccount.setTransportProperties(properties);

            }
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static String getAliases(final int userId, final int contextId, final AbstractMailAccount mailAccount) {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(mailAccount.getPrimaryAddress());
        final Set<String> s = new HashSet<>(4);
        s.add(mailAccount.getPrimaryAddress());
        String[] aliases;
        try {
            aliases = UserStorage.getInstance().getUser(userId, contextId).getAliases();
        } catch (final OXException e) {
            LOG.warn("", e);
            return sb.toString();
        }

        for (final String alias : aliases) {
            if (s.add(alias)) {
                sb.append(", ").append(alias);
            }
        }
        return sb.toString();
    }

    /**
     * Initializes a new {@link RdbMailAccountStorage}.
     */
    RdbMailAccountStorage() {
        super();
    }

    @Override
    public void clearFullNamesForMailAccount(final int id, final int userId, final int contextId) throws OXException {
        clearFullNamesForMailAccount(id, null, userId, contextId);
    }

    @Override
    public void clearFullNamesForMailAccount(final int id, final int[] indexes, final int userId, final int contextId) throws OXException {
        Connection con = Database.get(contextId, true);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            clearFullNamesForMailAccount(id, indexes, userId, contextId, con);
            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    private static final TIntObjectMap<String> INDEX_2_COL;
    static {
        final TIntObjectMap<String> map = new TIntObjectHashMap<>(8);
        map.put(StorageUtility.INDEX_CONFIRMED_HAM, "confirmed_ham_fullname");
        map.put(StorageUtility.INDEX_CONFIRMED_SPAM, "confirmed_spam_fullname");
        map.put(StorageUtility.INDEX_DRAFTS, "drafts_fullname");
        map.put(StorageUtility.INDEX_SENT, "sent_fullname");
        map.put(StorageUtility.INDEX_SPAM, "spam_fullname");
        map.put(StorageUtility.INDEX_TRASH, "trash_fullname");
        INDEX_2_COL = map;
    }

    /**
     * Clears full names for specified mail account.
     *
     * @param id The account ID
     * @param userId The user ID
     * @param contextId The context ID
     * @param con The connection to use
     * @throws OXException If invalidation fails
     */
    public void clearFullNamesForMailAccount(final int id, final int[] indexes, final int userId, final int contextId, final Connection con) throws OXException {
        if (null == con) {
            clearFullNamesForMailAccount(id, indexes, userId, contextId);
            return;
        }
        PreparedStatement stmt = null;
        try {
            if (null == indexes || indexes.length == 0) {
                stmt = con.prepareStatement("UPDATE user_mail_account SET trash_fullname=?, sent_fullname=?, drafts_fullname=?, spam_fullname=?, confirmed_spam_fullname=?, confirmed_ham_fullname=? WHERE cid=? AND id=? AND user=?");
                int num = 1;
                stmt.setString(num++, "");
                stmt.setString(num++, "");
                stmt.setString(num++, "");
                stmt.setString(num++, "");
                stmt.setString(num++, "");
                stmt.setString(num++, "");
                stmt.setLong(num++, contextId);
                stmt.setLong(num++, id);
                stmt.setLong(num++, userId);
            } else {
                StringBuilder stmtBuilder = new StringBuilder(512).append("UPDATE user_mail_account SET ");
                int finds = 0;
                for (final int index : indexes) {
                    final String col = INDEX_2_COL.get(index);
                    if (null != col) {
                        finds++;
                        stmtBuilder.append(col).append("=?,");
                    }
                }
                if (finds <= 0) {
                    // Nothing to do
                    return;
                }
                stmtBuilder.deleteCharAt(stmtBuilder.length() - 1);
                stmtBuilder.append(" WHERE cid=? AND id=? AND user=?");
                stmt = con.prepareStatement(stmtBuilder.toString());
                stmtBuilder = null;
                int num = 1;
                for (int i = finds; i-- > 0;) {
                    stmt.setString(num++, "");
                }
                stmt.setLong(num++, contextId);
                stmt.setLong(num++, id);
                stmt.setLong(num++, userId);
            }
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void setFullNamesForMailAccount(int id, int[] indexes, String[] fullNames, int userId, int contextId) throws OXException {
        Connection con = Database.get(contextId, true);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            setFullNamesForMailAccount(id, indexes, fullNames, userId, contextId, con);
            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    /**
     * Sets specified full names for specified mail account using given connection.
     *
     * @param id The account ID
     * @param indexes The indexes of the full names to set
     * @param fullNames The full names to set
     * @param userId The user ID
     * @param contextId The context ID
     * @param con The connection
     * @throws OXException If invalidation fails
     */
    public void setFullNamesForMailAccount(int id, int[] indexes, String[] fullNames, int userId, int contextId, Connection con) throws OXException {
        if (null == con) {
            setFullNamesForMailAccount(id, indexes, fullNames, userId, contextId);
            return;
        }
        PreparedStatement stmt = null;
        try {
            StringBuilder sqlBuilder = new StringBuilder("UPDATE user_mail_account SET ");
            List<String> strings = new ArrayList<>(fullNames.length);

            boolean somethingAdded = false;
            for (int i = indexes.length; i-- > 0;) {
                int index = indexes[i];
                switch (index) {
                    case StorageUtility.INDEX_DRAFTS:
                        sqlBuilder.append("drafts_fullname=?, ");
                        strings.add(fullNames[i]);
                        somethingAdded = true;
                        break;
                    case StorageUtility.INDEX_SENT:
                        sqlBuilder.append("sent_fullname=?, ");
                        strings.add(fullNames[i]);
                        somethingAdded = true;
                        break;
                    case StorageUtility.INDEX_SPAM:
                        sqlBuilder.append("spam_fullname=?, ");
                        strings.add(fullNames[i]);
                        somethingAdded = true;
                        break;
                    case StorageUtility.INDEX_TRASH:
                        sqlBuilder.append("trash_fullname=?, ");
                        strings.add(fullNames[i]);
                        somethingAdded = true;
                        break;
                    default:
                        break;
                }
            }

            if (!somethingAdded) {
                return;
            }

            sqlBuilder.setLength(sqlBuilder.length() - 2);
            sqlBuilder.append(" WHERE cid=? AND id=? AND user=?");

            stmt = con.prepareStatement(sqlBuilder.toString());
            int num = 1;
            for (String string : strings) {
                stmt.setString(num++, string);
            }
            stmt.setLong(num++, contextId);
            stmt.setLong(num++, id);
            stmt.setLong(num++, userId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void setNamesForMailAccount(int id, int[] indexes, String[] names, int userId, int contextId) throws OXException {
        Connection con = Database.get(contextId, true);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            setNamesForMailAccount(id, indexes, names, userId, contextId, con);
            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    /**
     * Sets specified full names for specified mail account using given connection.
     *
     * @param id The account ID
     * @param indexes The indexes of the full names to set
     * @param fullNames The full names to set
     * @param userId The user ID
     * @param contextId The context ID
     * @param con The connection
     * @throws OXException If invalidation fails
     */
    public void setNamesForMailAccount(int id, int[] indexes, String[] names, int userId, int contextId, Connection con) throws OXException {
        if (null == con) {
            setNamesForMailAccount(id, indexes, names, userId, contextId);
            return;
        }
        PreparedStatement stmt = null;
        try {
            StringBuilder sqlBuilder = new StringBuilder("UPDATE user_mail_account SET ");
            List<String> strings = new ArrayList<>(names.length);

            boolean somethingAdded = false;
            for (int i = indexes.length; i-- > 0;) {
                int index = indexes[i];
                switch (index) {
                    case StorageUtility.INDEX_DRAFTS:
                        sqlBuilder.append("drafts=?, ");
                        strings.add(names[i]);
                        somethingAdded = true;
                        break;
                    case StorageUtility.INDEX_SENT:
                        sqlBuilder.append("sent=?, ");
                        strings.add(names[i]);
                        somethingAdded = true;
                        break;
                    case StorageUtility.INDEX_SPAM:
                        sqlBuilder.append("spam=?, ");
                        strings.add(names[i]);
                        somethingAdded = true;
                        break;
                    case StorageUtility.INDEX_TRASH:
                        sqlBuilder.append("trash=?, ");
                        strings.add(names[i]);
                        somethingAdded = true;
                        break;
                    default:
                        break;
                }
            }

            if (!somethingAdded) {
                return;
            }

            sqlBuilder.setLength(sqlBuilder.length() - 2);
            sqlBuilder.append(" WHERE cid=? AND id=? AND user=?");

            stmt = con.prepareStatement(sqlBuilder.toString());
            int num = 1;
            for (String string : strings) {
                stmt.setString(num++, string);
            }
            stmt.setLong(num++, contextId);
            stmt.setLong(num++, id);
            stmt.setLong(num++, userId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void propagateEvent(Event event, int id, Map<String, Object> eventProps, int userId, int contextId) throws OXException {
        Connection con = Database.get(contextId, true);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            propagateEvent(event, id, eventProps, userId, contextId, con);

            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    private void propagateEvent(Event event, int id, Map<String, Object> eventProps, int userId, int contextId, Connection con) throws OXException {
        switch (event) {
            case DELETED:
                DeleteListenerRegistry.getInstance().triggerOnAfterDeletion(id, eventProps, userId, contextId, con);
                break;
            case CREATED:
                DeleteListenerRegistry.getInstance().triggerOnCreation(id, eventProps, userId, contextId, con);
                break;
            case MODIFIED:
                DeleteListenerRegistry.getInstance().triggerOnModification(id, eventProps, userId, contextId, con);
                break;
            default:
                LOG.warn("Unknown event: {}", event);
                break;
        }
    }

    @Override
    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int userId, final int contextId) throws OXException {
        deleteMailAccount(id, properties, userId, contextId, false);
    }

    @Override
    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int userId, final int contextId, final boolean deletePrimary) throws OXException {
        if (!deletePrimary && MailAccount.DEFAULT_ID == id) {
            throw MailAccountExceptionCodes.NO_DEFAULT_DELETE.create(I(userId), I(contextId));
        }
        dropPOP3StorageFolders(userId, contextId);
        final Connection con = Database.get(contextId, true);
        try {
            con.setAutoCommit(false);
            deleteMailAccount(id, properties, userId, contextId, deletePrimary, con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    @Override
    public void deleteTransportAccount(final int id, final int userId, final int contextId) throws OXException {
        dropPOP3StorageFolders(userId, contextId);
        final Connection con = Database.get(contextId, true);
        try {
            con.setAutoCommit(false);
            deleteTransportAccount(id, userId, contextId, con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    @Override
    public void deleteTransportAccount(final int id, final int userId, final int contextId, final Connection con) throws OXException {
        dropPOP3StorageFolders(userId, contextId);
        PreparedStatement stmt = null;
        try {
            // First delete properties
            deleteProperties(contextId, userId, id, true, con);
            // Then delete account data
            stmt = con.prepareStatement("DELETE FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id);
            stmt.setLong(3, userId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            final String className = e.getClass().getName();
            if ((null != className) && className.endsWith("MySQLIntegrityConstraintViolationException")) {
                try {
                    if (handleConstraintViolationException(e, id, userId, contextId, con)) {
                        /*
                         * Retry & return
                         */
                        deleteTransportAccount(id, userId, contextId, con);
                        return;
                    }
                } catch (final RuntimeException re) {
                    LOG.debug("", re);
                }
            }
            /*
             * Indicate SQL error
             */
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int userId, final int contextId, final boolean deletePrimary, final Connection con) throws OXException {
        if (!deletePrimary && MailAccount.DEFAULT_ID == id) {
            throw MailAccountExceptionCodes.NO_DEFAULT_DELETE.create(I(userId), I(contextId));
        }
        dropPOP3StorageFolders(userId, contextId);
        final boolean restoreConstraints = disableForeignKeyChecks(con);
        PreparedStatement stmt = null;
        try {
            final DeleteListenerRegistry registry = DeleteListenerRegistry.getInstance();
            registry.triggerOnBeforeDeletion(id, properties, userId, contextId, con);
            // First delete properties
            deleteProperties(contextId, userId, id, false, con);
            deleteProperties(contextId, userId, id, true, con);
            // Then delete account data
            stmt = con.prepareStatement("DELETE FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id);
            stmt.setLong(3, userId);
            stmt.executeUpdate();
            closeSQLStuff(stmt);
            stmt = con.prepareStatement("DELETE FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id);
            stmt.setLong(3, userId);
            stmt.executeUpdate();
            registry.triggerOnAfterDeletion(id, properties, userId, contextId, con);
        } catch (final SQLException e) {
            final String className = e.getClass().getName();
            if ((null != className) && className.endsWith("MySQLIntegrityConstraintViolationException")) {
                try {
                    if (handleConstraintViolationException(e, id, userId, contextId, con)) {
                        /*
                         * Retry & return
                         */
                        deleteMailAccount(id, properties, userId, contextId, deletePrimary, con);
                        return;
                    }
                } catch (final RuntimeException re) {
                    LOG.debug("", re);
                }
            }
            /*
             * Indicate SQL error
             */
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            if (restoreConstraints) {
                try {
                    enableForeignKeyChecks(con);
                } catch (final SQLException e) {
                    LOG.error("", e);
                }
            }
        }
    }

    private static String getPOP3Path(final int id, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT url FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id);
            stmt.setLong(3, userId);
            rs = stmt.executeQuery();
            if (!rs.next() || !rs.getString(1).startsWith("pop3")) {
                return null;
            }
            closeSQLStuff(rs, stmt);
            stmt = con.prepareStatement("SELECT value FROM user_mail_account_properties WHERE cid = ? AND id = ? AND user = ? AND name = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id);
            stmt.setLong(3, userId);
            stmt.setString(4, "pop3.path");
            rs = stmt.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } catch (final SQLException e) {
            if (null != stmt) {
                final String sql = stmt.toString();
                LOG.debug("\n\tFailed mail account statement:\n\t{}", new Object() { @Override public String toString() { return sql.substring(sql.indexOf(": ") + 2);}});
            }
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static final Pattern PATTERN_CONSTRAINT_VIOLATION;

    static {
        /*
         * Specify regex quotes
         */
        final String quote1 = Pattern.quote("Cannot delete or update a parent row: a foreign key constraint fails (`");
        final String quote2 = Pattern.quote("`, CONSTRAINT `");
        final String quote3 = Pattern.quote("` FOREIGN KEY (");
        final String quote4 = Pattern.quote(") REFERENCES `user_mail_account` (`cid`, `user`, `id`))");
        /*
         * Compose pattern
         */
        PATTERN_CONSTRAINT_VIOLATION =
            Pattern.compile(quote1 + "([^`]+)" + quote2 + "[^`]+" + quote3 + "([^)]+)" + quote4, Pattern.CASE_INSENSITIVE);
    }

    private static boolean handleConstraintViolationException(final SQLException e, final int id, final int userId, final int contextId, final Connection con) throws OXException {
        final Matcher m = PATTERN_CONSTRAINT_VIOLATION.matcher(e.getMessage());
        if (!m.matches()) {
            return false;
        }
        /*
         * Check rows
         */
        final String[] rows = m.group(2).replaceAll(Pattern.quote("`"), "").split(" *, *");
        if (rows.length != 3) {
            return false;
        }
        final Set<String> set = new HashSet<>(Arrays.asList(rows));
        set.removeAll(Arrays.asList("cid", "user", "id"));
        if (!set.isEmpty()) {
            return false;
        }
        /*
         * Get table name
         */
        String tableName = m.group(1);
        final int pos = tableName.indexOf('/') + 1;
        if (pos > 0) {
            tableName = tableName.substring(pos);
        }
        return dropReferenced(id, userId, contextId, tableName, con);
    }

    private static boolean dropReferenced(final int id, final int userId, final int contextId, final String tableName, final Connection con) throws OXException {
        final boolean transactional;
        try {
            transactional = !con.getAutoCommit();
            if (transactional) {
                rollback(con);
                con.setAutoCommit(true);
            }
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * Delete referenced
         */
        final String sql =
            new StringBuilder(64).append("DELETE FROM ").append(tableName).append(" WHERE cid = ? AND id = ? and user = ?").toString();
        PreparedStatement stmt = null;
        boolean retval = false;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setLong(1, contextId);
            stmt.setLong(2, id);
            stmt.setLong(3, userId);
            stmt.executeUpdate();
            retval = true;
        } catch (final SQLException e) {
            LOG.warn("Couldn't delete referenced entries with: {}", sql, e);
        } catch (final Exception e) {
            LOG.warn("Couldn't delete referenced entries with: {}", sql, e);
        } finally {
            closeSQLStuff(stmt);
        }
        /*
         * Restore transaction state
         */
        try {
            if (transactional) {
                con.setAutoCommit(false);
            }
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
        return retval;
    }

    @Override
    public String getDefaultFolderPrefix(Session session) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session);
            mailAccess.connect(false);
            return mailAccess.getFolderStorage().getDefaultFolderPrefix();
        } finally {
            MailAccess.closeInstance(mailAccess, false);
        }
    }

    @Override
    public char getDefaultSeparator(Session session) throws OXException {
        return getSeparator(MailAccount.DEFAULT_ID, session).charValue();
    }

    private Character getSeparator(int accountId, Session session) throws OXException {
        final MailSessionCache sessionCache = MailSessionCache.getInstance(session);
        Character sep = (Character) sessionCache.getParameter(accountId, MailSessionParameterNames.getParamSeparator());
        if (null == sep) {
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> ma = null;
            try {
                ma = MailAccess.getInstance(session, accountId);
                ma.connect(false);
                sep = Character.valueOf(ma.getFolderStorage().getFolder("INBOX").getSeparator());
                sessionCache.putParameter(accountId, MailSessionParameterNames.getParamSeparator(), sep);
            } finally {
                MailAccess.closeInstance(ma, false);
            }
        }
        return sep;
    }

    public MailAccount getDefaultMailAccount(final int userId, final int contextId, final Connection con) throws OXException {
        return getMailAccount(MailAccount.DEFAULT_ID, userId, contextId, con);
    }

    @Override
    public MailAccount getDefaultMailAccount(final int userId, final int contextId) throws OXException {
        return getMailAccount(MailAccount.DEFAULT_ID, userId, contextId);
    }

    @Override
    public MailAccount getMailAccount(final int id, final int userId, final int contextId, final Connection con) throws OXException {
        if (null == con) {
            return getMailAccount(id, userId, contextId);
        }
        AbstractMailAccount retval = MailAccount.DEFAULT_ID == id ? new DefaultMailAccount() : new CustomMailAccount(id);
        fillMailAccount(retval, id, userId, contextId, false, con);
        fillTransportAccount(retval, id, userId, contextId, con);
        if (MailAccount.DEFAULT_ID == id){
            checkDefaultAccountConfiguration(retval, userId, contextId);
        } else {
            int oauthId = retval.getTransportOAuthId();
            if (oauthId < 0 && TransportAuth.MAIL.equals(retval.getTransportAuth()) && retval.getMailOAuthId() >= 0) {
                retval.setTransportOAuthId(retval.getMailOAuthId());
            }
        }
        return retval;
    }

    /**
     * Checks if some special configuration is active for the primary mail account and updates the mail account fields if necessary
     *
     * @param retval The primary mail account
     * @param userId The user id
     * @param contextId The context id
     * @throws OXException
     */
    private void checkDefaultAccountConfiguration(AbstractMailAccount retval, int userId, int contextId) throws OXException {

        if (PasswordSource.GLOBAL.equals(MailProperties.getInstance().getPasswordSource(userId, contextId))) {
            retval.setPassword(null);
        }

        switch(MailProperties.getInstance().getLoginSource(userId, contextId)){
            case PRIMARY_EMAIL:
                retval.setLogin(UserStorage.getInstance().getUser(userId, contextId).getMail());
                break;
            case USER_IMAPLOGIN:
            default:
                //nothing to do
                break;
            case USER_NAME:
                retval.setLogin(UserStorage.getInstance().getUser(userId, contextId).getLoginInfo());
                break;
        }

        switch(MailProperties.getInstance().getMailServerSource(userId, contextId)){
            case GLOBAL:
                {
                    ConfiguredServer server = MailProperties.getInstance().getMailServer(userId, contextId);
                    retval.setMailServer(server.getHostName());
                    String protocol = server.getProtocol();
                    if (null != protocol) {
                        retval.setMailProtocol(protocol);
                    }
                    int port = server.getPort();
                    if (port > 0) {
                        retval.setMailPort(port);
                    }
                    if (server.isSecure()) {
                        retval.setMailSecure(true);
                    }
                }
                break;
            case USER:
            default:
                break;

        }

        switch(MailProperties.getInstance().getTransportServerSource(userId, contextId)){
            case GLOBAL:
                {
                    ConfiguredServer server = MailProperties.getInstance().getTransportServer(userId, contextId);
                    retval.setTransportServer(server.getHostName());
                    String protocol = server.getProtocol();
                    if (null != protocol) {
                        retval.setTransportProtocol(protocol);
                    }
                    int port = server.getPort();
                    if (port > 0) {
                        retval.setTransportPort(port);
                    }
                    if (server.isSecure()) {
                        retval.setTransportSecure(true);
                    }
                }
                break;
            case USER:
            default:
                break;

        }
    }

    @Override
    public MailAccount getMailAccount(final int id, final int userId, final int contextId) throws OXException {
        final Connection rcon = Database.get(contextId, false);
        try {
            return getMailAccount(id, userId, contextId, rcon);
        } finally {
            Database.back(contextId, false, rcon);
        }
    }

    /**
     * Checks if the mail account referenced by specified identifier does exist.
     *
     * @param id The mail account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @return <code>true</code> if exists; otherwise <code>false</code>
     * @throws OXException If check for existence fails
     */
    public boolean existsMailAccount(int id, int userId, int contextId, Connection con) throws OXException {
        if (null == con) {
            return existsMailAccount(id, userId, contextId);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, id);
            stmt.setLong(3, userId);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (final SQLException e) {
            if (null != stmt) {
                final String sql = stmt.toString();
                LOG.debug("\n\tFailed mail account statement:\n\t{}", new Object() { @Override public String toString() { return sql.substring(sql.indexOf(": ") + 2);}});
            }
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public boolean existsMailAccount(int id, int userId, int contextId) throws OXException {
        Connection rcon = Database.get(contextId, false);
        try {
            return existsMailAccount(id, userId, contextId, rcon);
        } finally {
            Database.back(contextId, false, rcon);
        }
    }

    @Override
    public MailAccount getRawMailAccount(int id, int userId, int contextId) throws OXException {
        final Connection con = Database.get(contextId, false);
        try {
            AbstractMailAccount retval = MailAccount.DEFAULT_ID == id ? new DefaultMailAccount() : new CustomMailAccount(id);
            fillMailAccount(retval, id, userId, contextId, true, con);
            fillTransportAccount(retval, id, userId, contextId, con);
            return retval;
        } finally {
            Database.back(contextId, false, con);
        }
    }

    @Override
    public MailAccount[] getUserMailAccounts(final int userId, final int contextId) throws OXException {
        final Connection con = Database.get(contextId, false);
        try {
            return getUserMailAccounts(userId, contextId, con);
        } finally {
            Database.back(contextId, false, con);
        }
    }

    @Override
    public MailAccount[] getUserMailAccounts(final int userId, final int contextId, final Connection con) throws OXException {
        final int[] ids = getUserMailAccountIDs(userId, contextId, con);
        final MailAccount[] retval = new MailAccount[ids.length];
        for (int i = 0; i < ids.length; i++) {
            retval[i] = getMailAccount(ids[i], userId, contextId, con);
        }
        return retval;
    }

    int[] getUserMailAccountIDs(final int userId, final int contextId) throws OXException {
        final Connection con = Database.get(contextId, false);
        try {
            return getUserMailAccountIDs(userId, contextId, con);
        } finally {
            Database.back(contextId, false, con);
        }
    }

    int[] getUserMailAccountIDs(final int userId, final int contextId, final Connection con) throws OXException {
        if (null == con) {
            return getUserMailAccountIDs(userId, contextId);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id, url FROM user_mail_account WHERE cid = ? AND user = ? ORDER BY id");
            stmt.setLong(1, contextId);
            stmt.setLong(2, userId);
            result = stmt.executeQuery();
            if (!result.next()) {
                return new int[0];
            }

            TIntList ids = new TIntArrayList(8);
            do {
                final String url = result.getString(2);
                if (null != MailProviderRegistry.getRealMailProvider(ProviderUtility.extractProtocol(url, URIDefaults.IMAP.getProtocol()))) {
                    ids.add(result.getInt(1));
                }
            } while (result.next());
            return ids.toArray();
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    int[] getUserTransportAccountIDs(final int userId, final int contextId) throws OXException {
        final Connection con = Database.get(contextId, false);
        try {
            return getUserTransportAccountIDs(userId, contextId, con);
        } finally {
            Database.back(contextId, false, con);
        }
    }

    int[] getUserTransportAccountIDs(final int userId, final int contextId, final Connection con) throws OXException {
        if (null == con) {
            return getUserMailAccountIDs(userId, contextId);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id, url FROM user_transport_account WHERE cid = ? AND user = ? ORDER BY id");
            stmt.setLong(1, contextId);
            stmt.setLong(2, userId);
            result = stmt.executeQuery();
            if (!result.next()) {
                return new int[0];
            }

            TIntList ids = new TIntArrayList(8);
            do {
                final String url = result.getString(2);
                if (null != MailProviderRegistry.getRealMailProvider(ProviderUtility.extractProtocol(url, URIDefaults.SMTP.getProtocol()))) {
                    ids.add(result.getInt(1));
                }
            } while (result.next());
            return ids.toArray();
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    @Override
    public MailAccount[] resolveLogin(final String login, final int contextId) throws OXException {
        final int[][] idsAndUsers = resolveLogin2IDs(login, contextId);
        final MailAccount[] retval = new MailAccount[idsAndUsers.length];
        for (int i = 0; i < idsAndUsers.length; i++) {
            final int[] idAndUser = idsAndUsers[i];
            retval[i] = getMailAccount(idAndUser[0], idAndUser[1], contextId);
        }
        return retval;
    }

    int[][] resolveLogin2IDs(final String login, final int contextId) throws OXException {
        final int[] ids;
        final int[] users;
        {
            final Connection con = Database.get(contextId, false);
            PreparedStatement stmt = null;
            ResultSet result = null;
            final TIntList idsArr = new TIntArrayList(8);
            final TIntList usersArr = new TIntArrayList(8);
            try {
                stmt = con.prepareStatement("SELECT id, user FROM user_mail_account WHERE cid = ? AND login = ?");
                stmt.setLong(1, contextId);
                stmt.setString(2, login);
                result = stmt.executeQuery();
                if (!result.next()) {
                    return new int[0][];
                }
                do {
                    idsArr.add(result.getInt(1));
                    usersArr.add(result.getInt(2));
                } while (result.next());
            } catch (final SQLException e) {
                throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(result, stmt);
                Database.back(contextId, false, con);
            }
            ids = idsArr.toArray();
            users = usersArr.toArray();
        }
        final int[][] retval = new int[ids.length][];
        for (int i = 0; i < ids.length; i++) {
            retval[i] = new int[] { ids[i], users[i] };
        }
        return retval;
    }

    @Override
    public MailAccount[] resolveLogin(final String login, final String serverUrl, final int contextId) throws OXException {
        final int[][] idsAndUsers = resolveLogin2IDs(login, contextId);
        final List<MailAccount> l = new ArrayList<>(idsAndUsers.length);
        for (final int[] idAndUser : idsAndUsers) {
            final MailAccount candidate = getMailAccount(idAndUser[0], idAndUser[1], contextId);
            if (serverUrl.equals(toSocketAddrString(candidate.generateMailServerURL(), 143))) {
                l.add(candidate);
            }
        }
        return l.toArray(new MailAccount[l.size()]);
    }

    @Override
    public MailAccount[] resolvePrimaryAddr(final String primaryAddress, final int contextId) throws OXException {
        final int[][] idsAndUsers = resolvePrimaryAddr2IDs(primaryAddress, contextId);
        final List<MailAccount> l = new ArrayList<>(idsAndUsers.length);
        for (final int[] idAndUser : idsAndUsers) {
            final MailAccount candidate = getMailAccount(idAndUser[0], idAndUser[1], contextId);
            l.add(candidate);
        }
        return l.toArray(new MailAccount[l.size()]);
    }

    int[][] resolvePrimaryAddr2IDs(String primaryAddress, int contextId) throws OXException {
        final int[] ids;
        final int[] users;
        {
            final Connection con = Database.get(contextId, false);
            PreparedStatement stmt = null;
            ResultSet result = null;
            final TIntList idsArr = new TIntArrayList(8);
            final TIntList usersArr = new TIntArrayList(8);
            try {
                stmt = con.prepareStatement("SELECT id, user FROM user_mail_account WHERE cid = ? AND primary_addr = ?");
                stmt.setLong(1, contextId);
                stmt.setString(2, primaryAddress);
                result = stmt.executeQuery();
                if (!result.next()) {
                    return new int[0][];
                }
                do {
                    idsArr.add(result.getInt(1));
                    usersArr.add(result.getInt(2));
                } while (result.next());
            } catch (final SQLException e) {
                throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(result, stmt);
                Database.back(contextId, false, con);
            }
            ids = idsArr.toArray();
            users = usersArr.toArray();
        }
        final int[][] idsAndUsers = new int[ids.length][];
        for (int i = 0; i < ids.length; i++) {
            idsAndUsers[i] = new int[] { ids[i], users[i] };
        }
        return idsAndUsers;
    }

    @Override
    public void enableMailAccount(int accountId, int userId, int contextId) throws OXException {
        Connection con = Database.get(contextId, true);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            enableMailAccount(accountId, userId, contextId, con);

            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    /**
     * Enables the specified mail/transport account.
     *
     * @param accountId The account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @throws OXException If enabling the account fails
     */
    @Override
    public void enableMailAccount(int accountId, int userId, int contextId, Connection con) throws OXException {
        if (null == con) {
            enableMailAccount(accountId, userId, contextId);
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE user_mail_account SET failed_auth_count=0, failed_auth_date=0, disabled=0 WHERE cid=? AND id=? AND user=?");
            int pos = 1;
            stmt.setLong(pos++, contextId);
            stmt.setLong(pos++, accountId);
            stmt.setLong(pos++, userId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            stmt = con.prepareStatement("UPDATE user_transport_account SET failed_auth_count=0, failed_auth_date=0, disabled=0 WHERE cid=? AND id=? AND user=?");
            pos = 1;
            stmt.setLong(pos++, contextId);
            stmt.setLong(pos++, accountId);
            stmt.setLong(pos++, userId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public void updateMailAccount(MailAccountDescription mailAccount, Set<Attribute> attributes, int userId, int contextId, Session session) throws OXException {
        updateMailAccount(mailAccount, attributes, userId, contextId, session, false);
    }

    private void updateMailAccount(final MailAccountDescription mailAccount, final Set<Attribute> attributes, final int userId, final int contextId, final Session session, final boolean changePrimary) throws OXException {
        final Connection con = Database.get(contextId, true);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            updateMailAccount(mailAccount, attributes, userId, contextId, session, con, changePrimary);
            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    /**
     * Contains attributes which denote an account's default folders.
     */
    private static final EnumSet<Attribute> DEFAULT = Attribute.DEFAULT;

    /**
     * Contains attributes which denote the full names of an account's default folders.
     */
    private static final EnumSet<Attribute> DEFAULT_FULL_NAMES = Attribute.DEFAULT_FULL_NAMES;

    /**
     * Contains attributes which are allowed to be edited for primary mail account.
     */
    private static final EnumSet<Attribute> PRIMARY_EDITABLE = EnumSet.of(
        Attribute.NAME_LITERAL,
        Attribute.UNIFIED_INBOX_ENABLED_LITERAL,
        Attribute.PERSONAL_LITERAL,
        Attribute.REPLY_TO_LITERAL,
        Attribute.ARCHIVE_LITERAL,
        Attribute.ARCHIVE_FULLNAME_LITERAL,
        Attribute.SENT_LITERAL,
        Attribute.SENT_FULLNAME_LITERAL,
        Attribute.TRASH_LITERAL,
        Attribute.TRASH_FULLNAME_LITERAL,
        Attribute.SPAM_LITERAL,
        Attribute.SPAM_FULLNAME_LITERAL,
        Attribute.DRAFTS_LITERAL,
        Attribute.DRAFTS_FULLNAME_LITERAL);

    @Override
    public void updateMailAccount(MailAccountDescription mailAccount, Set<Attribute> attributes, int userId, int contextId, Session session, Connection con, boolean changePrimary) throws OXException {
        UpdateProperties updateProperties = new UpdateProperties.Builder().setChangePrimary(changePrimary).setChangeProtocol(false).setCon(con).setSession(session).build();
        updateMailAccount(mailAccount, attributes, userId, contextId, updateProperties);
    }

    @Override
    public void updateMailAccount(MailAccountDescription mailAccount, Set<Attribute> attributes, int userId, int contextId, UpdateProperties updateProperties) throws OXException {
        Connection con = updateProperties == null ? null : updateProperties.getCon();
        Session session = updateProperties == null ? null : updateProperties.getSession();
        boolean changePrimary = updateProperties == null ? false : updateProperties.isChangePrimary();
        boolean changeProtocol = updateProperties == null ? false : updateProperties.isChangeProtocol();

        if (null == con) {
            updateMailAccount(mailAccount, attributes, userId, contextId, session, changePrimary);
            return;
        }

        dropPOP3StorageFolders(userId, contextId);
        if (attributes.contains(Attribute.NAME_LITERAL)) {
            // Check name
            final String name = mailAccount.getName();
            if (!isValid(name)) {
                throw MailAccountExceptionCodes.INVALID_NAME.create(name);
            }
        }
        if (!changePrimary && (mailAccount.isDefaultFlag() || MailAccount.DEFAULT_ID == mailAccount.getId())) {
            /*
             * Iterate attributes and compare their values except the one for Attribute.UNIFIED_INBOX_ENABLED_LITERAL,
             * Attribute.PERSONAL_LITERAL and Attribute.REPLY_TO_LITERAL
             */
            for (final Attribute attribute : attributes) {
                /*
                 * Check for not editable attributes
                 */
                if (!Attribute.ID_LITERAL.equals(attribute) && !PRIMARY_EDITABLE.contains(attribute)) {
                    /*
                     * Another attribute must not be changed
                     */
                    throw MailAccountExceptionCodes.NO_DEFAULT_UPDATE.create(I(userId), I(contextId));
                }
            }
        }
        /*
         * Perform common update
         */
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            MailAccount storageVersion = null;
            if (prepareURL(attributes, Attribute.MAIL_URL_ATTRIBUTES, Attribute.MAIL_URL_LITERAL)) {
                storageVersion = getMailAccount(mailAccount.getId(), userId, contextId, con);
                final MailAccountGetSwitch getSwitch = new MailAccountGetSwitch(storageVersion);
                final SetSwitch setSwitch = new SetSwitch(mailAccount);

                for (final Attribute attribute : Attribute.MAIL_URL_ATTRIBUTES) {
                    if (!attributes.contains(attribute)) {
                        final Object value = attribute.doSwitch(getSwitch);
                        setSwitch.setValue(value);
                        attribute.doSwitch(setSwitch);
                    }
                }
                checkDuplicateMailAccount(mailAccount, new TIntHashSet(new int[] { mailAccount.getId() }), userId, contextId, con);

                // Check protocol mismatch
                if (false == changeProtocol) {
                    String newProtocol = mailAccount.getMailProtocol();
                    if (null != newProtocol) {
                        String oldProtocol = storageVersion.getMailProtocol();
                        if (!newProtocol.equalsIgnoreCase(oldProtocol)) {
                            throw MailAccountExceptionCodes.PROTOCOL_CHANGE.create(oldProtocol, newProtocol, I(userId), I(contextId));
                        }
                    }
                }
            } else if (attributes.contains(Attribute.MAIL_URL_LITERAL)) {
                checkDuplicateMailAccount(mailAccount, new TIntHashSet(new int[] { mailAccount.getId() }), userId, contextId, con);

                // Check protocol mismatch
                if (false == changeProtocol) {
                    String newProtocol = mailAccount.getMailProtocol();
                    if (null != newProtocol) {
                        storageVersion = getMailAccount(mailAccount.getId(), userId, contextId, con);
                        String oldProtocol = storageVersion.getMailProtocol();
                        if (!newProtocol.equalsIgnoreCase(oldProtocol)) {
                            throw MailAccountExceptionCodes.PROTOCOL_CHANGE.create(oldProtocol, newProtocol, I(userId), I(contextId));
                        }
                    }
                }
            }

            if (prepareURL(attributes, Attribute.TRANSPORT_URL_ATTRIBUTES, Attribute.TRANSPORT_URL_LITERAL)) {
                if (null == storageVersion) {
                    storageVersion = getMailAccount(mailAccount.getId(), userId, contextId, con);
                }
                final MailAccountGetSwitch getSwitch = new MailAccountGetSwitch(storageVersion);
                final SetSwitch setSwitch = new SetSwitch(mailAccount);

                for (final Attribute attribute : Attribute.TRANSPORT_URL_ATTRIBUTES) {
                    if (!attributes.contains(attribute)) {
                        final Object value = attribute.doSwitch(getSwitch);
                        setSwitch.setValue(value);
                        attribute.doSwitch(setSwitch);
                    }
                }

                // Check protocol mismatch
                if (false == changeProtocol) {
                    String newProtocol = mailAccount.getTransportProtocol();
                    if (null != newProtocol) {
                        String oldProtocol = storageVersion.getTransportProtocol();
                        if (!newProtocol.equalsIgnoreCase(oldProtocol)) {
                            throw MailAccountExceptionCodes.PROTOCOL_CHANGE.create(oldProtocol, newProtocol, I(userId), I(contextId));
                        }
                    }
                }
            } else if (attributes.contains(Attribute.TRANSPORT_URL_LITERAL)) {
                // Check protocol mismatch
                if (false == changeProtocol) {
                    String newProtocol = mailAccount.getTransportProtocol();
                    if (null != newProtocol) {
                        if (null == storageVersion) {
                            storageVersion = getMailAccount(mailAccount.getId(), userId, contextId, con);
                        }
                        String oldProtocol = storageVersion.getTransportProtocol();
                        if (!newProtocol.equalsIgnoreCase(oldProtocol)) {
                            throw MailAccountExceptionCodes.PROTOCOL_CHANGE.create(oldProtocol, newProtocol, I(userId), I(contextId));
                        }
                    }
                }
            }

            attributes.removeAll(Attribute.MAIL_URL_ATTRIBUTES);
            attributes.removeAll(Attribute.TRANSPORT_URL_ATTRIBUTES);

            String encryptedPassword = null; //

            List<Attribute> orderedAttributes = null;
            boolean clearFailAuthCount = false;
            if (UpdateMailAccountBuilder.needsUpdate(attributes)) {
                orderedAttributes = new ArrayList<>(attributes);

                UpdateMailAccountBuilder sqlBuilder = new UpdateMailAccountBuilder();
                GetSwitch getter = new GetSwitch(mailAccount);

                // Build SQL statement
                for (Iterator<Attribute> iter = orderedAttributes.iterator(); iter.hasNext();) {
                    Attribute attribute = iter.next();
                    if (Attribute.MAIL_URL_LITERAL == attribute) {
                        Object value = attribute.doSwitch(getter);
                        if (null == value) {
                            iter.remove();
                        } else {
                            attribute.doSwitch(sqlBuilder);
                        }
                    } else {
                        attribute.doSwitch(sqlBuilder);
                    }
                }

                if (sqlBuilder.isValid()) {
                    stmt = con.prepareStatement(sqlBuilder.getUpdateQuery());
                    clearFailAuthCount = sqlBuilder.isInjectClearingFailAuthCount();
                    // Fill prepared statement
                    int pos = 1;
                    for (Attribute attribute : orderedAttributes) {
                        if (!sqlBuilder.handles(attribute)) {
                            continue;
                        }
                        Object value = attribute.doSwitch(getter);
                        switch (attribute) {
                            case PASSWORD_LITERAL:
                                encryptedPassword = encrypt(mailAccount.getPassword(), session);
                                setOptionalString(stmt, pos++, encryptedPassword);
                                break;
                            case PERSONAL_LITERAL:
                                String personal = mailAccount.getPersonal();
                                if (isEmpty(personal)) {
                                    stmt.setNull(pos++, TYPE_VARCHAR);
                                } else {
                                    stmt.setString(pos++, personal);
                                }
                                break;
                            case REPLY_TO_LITERAL:
                                String replyTo = mailAccount.getReplyTo();
                                if (isEmpty(replyTo)) {
                                    stmt.setNull(pos++, TYPE_VARCHAR);
                                } else {
                                    stmt.setString(pos++, replyTo);
                                }
                                break;
                            case ARCHIVE_LITERAL:
                                String a = mailAccount.getArchive();
                                if (isEmpty(a)) {
                                    stmt.setString(pos++, "");
                                } else {
                                    stmt.setString(pos++, a);
                                }
                                break;
                            case ARCHIVE_FULLNAME_LITERAL:
                                String af = mailAccount.getArchiveFullname();
                                if (isEmpty(af)) {
                                    stmt.setString(pos++, "");
                                } else {
                                    stmt.setString(pos++, MailFolderUtility.prepareMailFolderParam(af).getFullname());
                                }
                                break;
                            case MAIL_STARTTLS_LITERAL:
                                boolean mtls = mailAccount.isMailStartTls();
                                stmt.setBoolean(pos++, mtls);
                                break;
                            case TRANSPORT_STARTTLS_LITERAL:
                                boolean ttls = mailAccount.isTransportStartTls();
                                stmt.setBoolean(pos++, ttls);
                                break;
                            case MAIL_OAUTH_LITERAL:
                                int mOAuthId = mailAccount.getMailOAuthId();
                                stmt.setInt(pos++, mOAuthId);
                                break;
                            case TRANSPORT_OAUTH_LITERAL:
                                int tOAuthId = mailAccount.getTransportOAuthId();
                                stmt.setInt(pos++, tOAuthId);
                                break;
                            default:
                                if (DEFAULT.contains(attribute)) {
                                    if (DEFAULT_FULL_NAMES.contains(attribute)) {
                                        String fullName = null == value ? "" : MailFolderUtility.prepareMailFolderParam((String) value).getFullname();
                                        stmt.setString(pos++, fullName);
                                    } else {
                                        if (null == value) {
                                            stmt.setObject(pos++, "");
                                        } else {
                                            stmt.setObject(pos++, value);
                                        }
                                    }
                                } else {
                                    stmt.setObject(pos++, value);
                                }
                                break;
                        }
                    }
                    stmt.setLong(pos++, contextId);
                    stmt.setLong(pos++, mailAccount.getId());
                    stmt.setLong(pos++, userId);

                    if (LOG.isDebugEnabled()) {
                        final String query = stmt.toString();
                        LOG.debug("Trying to perform SQL update query for attributes {} :\n{}", orderedAttributes, query.substring(query.indexOf(':') + 1));
                    }

                    stmt.executeUpdate();
                    closeSQLStuff(stmt);
                }
            }

            if (UpdateTransportAccountBuilder.needsUpdate(attributes) || clearFailAuthCount) {
                if (orderedAttributes == null) {
                    orderedAttributes = new ArrayList<>(attributes);
                }
                /*
                 * Check existence of transport entry
                 */
                stmt = con.prepareStatement("SELECT 1 FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?");
                int pos = 1;
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, mailAccount.getId());
                stmt.setLong(pos++, userId);
                rs = stmt.executeQuery();
                final boolean exists = rs.next();
                closeSQLStuff(rs, stmt);

                if (exists) {
                    UpdateTransportAccountBuilder sqlBuilder = new UpdateTransportAccountBuilder(clearFailAuthCount);
                    GetSwitch getter = new GetSwitch(mailAccount);

                    // Compose SQL statement
                    for (Iterator<Attribute> iter = orderedAttributes.iterator(); iter.hasNext();) {
                        Attribute attribute = iter.next();
                        if (Attribute.TRANSPORT_URL_LITERAL == attribute) {
                            Object value = attribute.doSwitch(getter);
                            if (null == value) {
                                iter.remove();
                            } else {
                                attribute.doSwitch(sqlBuilder);
                            }
                        } else {
                            attribute.doSwitch(sqlBuilder);
                        }
                    }

                    if (sqlBuilder.isValid()) {
                        stmt = con.prepareStatement(sqlBuilder.getUpdateQuery());

                        // Fill prepared statement
                        pos = 1;
                        for (Attribute attribute : orderedAttributes) {
                            if (!sqlBuilder.handles(attribute)) {
                                continue;
                            }
                            Object value = attribute.doSwitch(getter);
                            switch (attribute) {
                                case TRANSPORT_PASSWORD_LITERAL:
                                    if (encryptedPassword == null) {
                                        encryptedPassword = encrypt(mailAccount.getTransportPassword(), session);
                                    }
                                    setOptionalString(stmt, pos++, encryptedPassword);
                                    break;
                                case TRANSPORT_LOGIN_LITERAL:
                                    setOptionalString(stmt, pos++, (String) value);
                                    break;
                                case TRANSPORT_URL_LITERAL:
                                    setOptionalString(stmt, pos++, (String) value);
                                    break;
                                case PERSONAL_LITERAL:
                                    final String personal = mailAccount.getPersonal();
                                    if (isEmpty(personal)) {
                                        stmt.setNull(pos++, TYPE_VARCHAR);
                                    } else {
                                        stmt.setString(pos++, personal);
                                    }
                                    break;
                                case REPLY_TO_LITERAL:
                                    final String replyTo = mailAccount.getReplyTo();
                                    if (isEmpty(replyTo)) {
                                        stmt.setNull(pos++, TYPE_VARCHAR);
                                    } else {
                                        stmt.setString(pos++, replyTo);
                                    }
                                    break;
                                case TRANSPORT_STARTTLS_LITERAL:
                                    boolean ttls = mailAccount.isTransportStartTls();
                                    stmt.setBoolean(pos++, ttls);
                                    break;
                                case TRANSPORT_OAUTH_LITERAL:
                                    int tOAuthId = mailAccount.getTransportOAuthId();
                                    stmt.setInt(pos++, tOAuthId);
                                    break;
                                default:
                                    stmt.setObject(pos++, value);
                                    break;
                            }
                        }

                        stmt.setLong(pos++, contextId);
                        stmt.setLong(pos++, mailAccount.getId());
                        stmt.setLong(pos++, userId);

                        if (LOG.isDebugEnabled()) {
                            final String query = stmt.toString();
                            LOG.debug("Trying to perform SQL update query for attributes {} :\n{}", orderedAttributes, query.substring(query.indexOf(':') + 1));
                        }

                        stmt.executeUpdate();
                        closeSQLStuff(stmt);
                    }
                } else {
                    /*
                     * Such an entry does not exist, yet
                     */
                    String transportURL = mailAccount.generateTransportServerURL();
                    if (null != transportURL) {
                        stmt.close();
                        String encryptedTransportPassword;
                        if (session == null) {
                            encryptedTransportPassword = null;
                        } else {
                            encryptedTransportPassword = encrypt(mailAccount.getTransportPassword(), session);
                        }
                        // cid, id, user, name, url, login, password, send_addr, default_flag
                        stmt = con.prepareStatement("INSERT INTO user_transport_account (cid, id, user, name, url, login, password, send_addr, default_flag, personal, replyTo, starttls, oauth) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        pos = 1;
                        stmt.setLong(pos++, contextId);
                        stmt.setLong(pos++, mailAccount.getId());
                        stmt.setLong(pos++, userId);
                        setOptionalString(stmt, pos++, mailAccount.getName());
                        stmt.setString(pos++, transportURL);
                        if (null == mailAccount.getTransportLogin()) {
                            stmt.setString(pos++, "");
                        } else {
                            stmt.setString(pos++, mailAccount.getTransportLogin());
                        }
                        setOptionalString(stmt, pos++, encryptedTransportPassword);
                        setOptionalString(stmt, pos++, mailAccount.getPrimaryAddress());
                        stmt.setInt(pos++, 0); // default flag
                        String personal = mailAccount.getPersonal();
                        if (isEmpty(personal)) {
                            stmt.setNull(pos++, TYPE_VARCHAR);
                        } else {
                            stmt.setString(pos++, personal);
                        }
                        String replyTo = mailAccount.getReplyTo();
                        if (isEmpty(replyTo)) {
                            stmt.setNull(pos++, TYPE_VARCHAR);
                        } else {
                            stmt.setString(pos++, replyTo);
                        }
                        stmt.setInt(pos++, mailAccount.isTransportStartTls() ? 1 : 0);

                        int oAuth = mailAccount.getTransportOAuthId();
                        if (oAuth < 0) {
                            stmt.setNull(pos++, Types.INTEGER);
                        } else {
                            stmt.setInt(pos++, oAuth);
                        }

                        if (LOG.isDebugEnabled()) {
                            String query = stmt.toString();
                            LOG.debug("Trying to perform SQL insert query:\n{}", query.substring(query.indexOf(':') + 1));
                        }

                        stmt.executeUpdate();
                        closeSQLStuff(stmt);
                    }
                }
            }

            Map<String, String> properties = mailAccount.getProperties();
            if (attributes.contains(Attribute.POP3_DELETE_WRITE_THROUGH_LITERAL)) {
                updateProperty(contextId, userId, mailAccount.getId(), "pop3.deletewt", properties.get("pop3.deletewt"), false, con);
            }
            if (attributes.contains(Attribute.POP3_EXPUNGE_ON_QUIT_LITERAL)) {
                updateProperty(contextId, userId, mailAccount.getId(), "pop3.expunge", properties.get("pop3.expunge"), false, con);
            }
            if (attributes.contains(Attribute.POP3_REFRESH_RATE_LITERAL)) {
                updateProperty(contextId, userId, mailAccount.getId(), "pop3.refreshrate", properties.get("pop3.refreshrate"), false, con);
            }
            if (attributes.contains(Attribute.POP3_STORAGE_LITERAL)) {
                updateProperty(contextId, userId, mailAccount.getId(), "pop3.storage", properties.get("pop3.storage"), false, con);
            }
            if (attributes.contains(Attribute.POP3_PATH_LITERAL)) {
                updateProperty(contextId, userId, mailAccount.getId(), "pop3.path", properties.get("pop3.path"), false, con);
            }
            if (attributes.contains(Attribute.TRANSPORT_AUTH_LITERAL)) {
                TransportAuth transportAuth = mailAccount.getTransportAuth();
                updateProperty(contextId, userId, mailAccount.getId(), "transport.auth", null == transportAuth ? null : transportAuth.getId(), true, con);
            }
        } catch (SQLSyntaxErrorException e) {
            if (null != stmt) {
                final String sql = stmt.toString();
                LOG.debug("\n\tFailed mail account statement:\n\t{}", new Object() {

                    @Override
                    public String toString() {
                        return sql.substring(sql.indexOf(": ") + 2);
                    }
                });
            }
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        /*
         * Automatically check Unified Mail existence
         */
        if (attributes.contains(Attribute.UNIFIED_INBOX_ENABLED_LITERAL) && mailAccount.isUnifiedINBOXEnabled()) {
            final UnifiedInboxManagement management = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
            if (null != management && !management.exists(userId, contextId, con)) {
                management.createUnifiedINBOX(userId, contextId, con);
            }
        }
    }

    private void updateProperty(final int contextId, final int userId, final int accountId, final String name, final String newValue, final boolean transportProps, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            if (transportProps) {
                stmt = con.prepareStatement("DELETE FROM user_transport_account_properties WHERE cid = ? AND user = ? AND id = ? AND name = ?");
            } else {
                stmt = con.prepareStatement("DELETE FROM user_mail_account_properties WHERE cid = ? AND user = ? AND id = ? AND name = ?");
            }
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, name);
            stmt.executeUpdate();
            closeSQLStuff(stmt);
            stmt = null;

            if (null != newValue && newValue.length() > 0) {
                if (transportProps) {
                    stmt = con.prepareStatement("INSERT INTO user_transport_account_properties (cid, user, id, name, value) VALUES (?, ?, ?, ?, ?)");
                } else {
                    stmt = con.prepareStatement("INSERT INTO user_mail_account_properties (cid, user, id, name, value) VALUES (?, ?, ?, ?, ?)");
                }
                pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, userId);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, name);
                stmt.setString(pos++, newValue);
                stmt.executeUpdate();
                closeSQLStuff(stmt);
                stmt = null;
            }
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void deleteProperties(final int contextId, final int userId, final int accountId, final boolean transportProps, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            if (transportProps) {
                stmt = con.prepareStatement("DELETE FROM user_transport_account_properties WHERE cid = ? AND user = ? AND id = ?");
            } else {
                stmt = con.prepareStatement("DELETE FROM user_mail_account_properties WHERE cid = ? AND user = ? AND id = ?");
            }
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, accountId);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private boolean prepareURL(final Set<Attribute> attributes, final Set<Attribute> compareWith, final Attribute urlAttribute) {
        final EnumSet<Attribute> copy = attributes.isEmpty() ? EnumSet.noneOf(Attribute.class) : EnumSet.copyOf(attributes);
        if (copy.removeAll(compareWith)) {
            attributes.add(urlAttribute);
            // At least one of the mail url attributes is present in attributes
            if (attributes.containsAll(compareWith)) {
                // All mail url attributes are present
                return false;
            }
            // Not all are present
            return true;
        }
        // None are present
        return false;
    }

    @Override
    public void updateMailAccount(final MailAccountDescription mailAccount, final int userId, final int contextId, final Session session) throws OXException {
        updateAndReturnMailAccount(mailAccount, userId, contextId, session);
    }

    public MailAccount updateAndReturnMailAccount(final MailAccountDescription mailAccount, final int userId, final int contextId, final Session session) throws OXException {
        final int accountId = mailAccount.getId();
        if (mailAccount.isDefaultFlag() || MailAccount.DEFAULT_ID == accountId) {
            throw MailAccountExceptionCodes.NO_DEFAULT_UPDATE.create(I(userId), I(contextId));
        }
        // Check name
        final String name = mailAccount.getName();
        if (!isValid(name)) {
            throw MailAccountExceptionCodes.INVALID_NAME.create(name);
        }
        dropPOP3StorageFolders(userId, contextId);
        final Connection con = Database.get(contextId, true);
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            // Check prerequisites
            checkDuplicateMailAccount(mailAccount, new TIntHashSet(new int[] {accountId}), userId, contextId, con);

            // Check protocol mismatch
            {
                final MailAccount storageVersion = getMailAccount(accountId, userId, contextId, con);
                // Mail protocol
                String newProtocol = mailAccount.getMailProtocol();
                if (null != newProtocol) {
                    final String oldProtocol = storageVersion.getMailProtocol();
                    if (!newProtocol.equalsIgnoreCase(oldProtocol)) {
                        throw MailAccountExceptionCodes.PROTOCOL_CHANGE.create(oldProtocol, newProtocol, I(userId), I(contextId));
                    }
                }

                // Transport protocol
                newProtocol = mailAccount.getTransportProtocol();
                if (null != newProtocol) {
                    final String oldProtocol = storageVersion.getTransportProtocol();
                    if (!newProtocol.equalsIgnoreCase(oldProtocol)) {
                        throw MailAccountExceptionCodes.PROTOCOL_CHANGE.create(oldProtocol, newProtocol, I(userId), I(contextId));
                    }
                }
            }

            // Update...
            con.setAutoCommit(false);
            rollback = true;
            {
                final String encryptedPassword = encrypt(mailAccount.getPassword(), session);
                stmt = con.prepareStatement("UPDATE user_mail_account SET name = ?, url = ?, login = ?, password = ?, primary_addr = ?, spam_handler = ?, trash = ?, sent = ?, drafts = ?, spam = ?, confirmed_spam = ?, confirmed_ham = ?, unified_inbox = ?, trash_fullname = ?, sent_fullname = ?, drafts_fullname = ?, spam_fullname = ?, confirmed_spam_fullname = ?, confirmed_ham_fullname = ?, personal = ?, replyTo = ?, archive = ?, archive_fullname = ?, starttls = ?, oauth = ?, failed_auth_count=0, failed_auth_date=0, disabled=0 WHERE cid = ? AND id = ? AND user = ?");
                int pos = 1;
                stmt.setString(pos++, name);
                stmt.setString(pos++, mailAccount.generateMailServerURL());
                stmt.setString(pos++, mailAccount.getLogin());
                setOptionalString(stmt, pos++, encryptedPassword);
                stmt.setString(pos++, mailAccount.getPrimaryAddress());
                final String sh = mailAccount.getSpamHandler();
                if (null == sh) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, sh);
                }
                setOptionalString(stmt, pos++, mailAccount.getTrash());
                setOptionalString(stmt, pos++, mailAccount.getSent());
                setOptionalString(stmt, pos++, mailAccount.getDrafts());
                setOptionalString(stmt, pos++, mailAccount.getSpam());
                setOptionalString(stmt, pos++, mailAccount.getConfirmedSpam());
                setOptionalString(stmt, pos++, mailAccount.getConfirmedHam());
                stmt.setInt(pos++, mailAccount.isUnifiedINBOXEnabled() ? 1 : 0);
                setOptionalString(stmt, pos++, mailAccount.getTrashFullname());
                setOptionalString(stmt, pos++, mailAccount.getSentFullname());
                setOptionalString(stmt, pos++, mailAccount.getDraftsFullname());
                setOptionalString(stmt, pos++, mailAccount.getSpamFullname());
                setOptionalString(stmt, pos++, mailAccount.getConfirmedSpamFullname());
                setOptionalString(stmt, pos++, mailAccount.getConfirmedHamFullname());
                final String personal = mailAccount.getPersonal();
                if (isEmpty(personal)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, personal);
                }
                final String replyTo = mailAccount.getReplyTo();
                if (isEmpty(replyTo)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, replyTo);
                }

                // Archive
                setOptionalString(stmt, pos++, mailAccount.getArchive());
                setOptionalString(stmt, pos++, mailAccount.getArchiveFullname());

                // starttls flag
                stmt.setInt(pos++, mailAccount.isMailStartTls() ? 1 : 0);

                // OAuth account identifier
                int oAuthId = mailAccount.getMailOAuthId();
                if (oAuthId < 0) {
                    stmt.setNull(pos++, Types.INTEGER);
                } else {
                    stmt.setInt(pos++, oAuthId);
                }

                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, accountId);
                stmt.setLong(pos++, userId);
                stmt.executeUpdate();
            }
            final String transportURL = mailAccount.generateTransportServerURL();
            if (null != transportURL) {
                final String encryptedTransportPassword = encrypt(mailAccount.getTransportPassword(), session);
                stmt.close();
                stmt = con.prepareStatement("UPDATE user_transport_account SET name = ?, url = ?, login = ?, password = ?, send_addr = ?, personal = ?, replyTo = ?, starttls = ?, oauth = ?, failed_auth_count=0, failed_auth_date=0, disabled=0 WHERE cid = ? AND id = ? AND user = ?");
                int pos = 1;
                stmt.setString(pos++, name);
                stmt.setString(pos++, transportURL);
                setOptionalString(stmt, pos++, mailAccount.getTransportLogin());
                setOptionalString(stmt, pos++, encryptedTransportPassword);
                stmt.setString(pos++, mailAccount.getPrimaryAddress());
                final String personal = mailAccount.getPersonal();
                if (isEmpty(personal)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, personal);
                }
                final String replyTo = mailAccount.getReplyTo();
                if (isEmpty(replyTo)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, replyTo);
                }
                // starttls flag
                stmt.setInt(pos++, mailAccount.isTransportStartTls() ? 1 : 0);

                // OAuth account identifier
                int oAuthId = mailAccount.getTransportOAuthId();
                if (oAuthId < 0) {
                    stmt.setNull(pos++, Types.INTEGER);
                } else {
                    stmt.setInt(pos++, oAuthId);
                }
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, accountId);
                stmt.setLong(pos++, userId);
                stmt.executeUpdate();
            }
            // Properties
            Map<String, String> properties = mailAccount.getProperties();
            if (properties.containsKey("pop3.deletewt")) {
                updateProperty(contextId, userId, accountId, "pop3.deletewt", properties.get("pop3.deletewt"), false, con);
            }
            if (properties.containsKey("pop3.expunge")) {
                updateProperty(contextId, userId, accountId, "pop3.expunge", properties.get("pop3.expunge"), false, con);
            }
            if (properties.containsKey("pop3.refreshrate")) {
                updateProperty(contextId, userId, accountId, "pop3.refreshrate", properties.get("pop3.refreshrate"), false, con);
            }
            if (properties.containsKey("pop3.storage")) {
                updateProperty(contextId, userId, accountId, "pop3.storage", properties.get("pop3.storage"), false, con);
            }
            if (properties.containsKey("pop3.path")) {
                updateProperty(contextId, userId, accountId, "pop3.path", properties.get("pop3.path"), false, con);
            }
            TransportAuth transportAuth = mailAccount.getTransportAuth();
            if (null != transportAuth) {
                updateProperty(contextId, userId, mailAccount.getId(), "transport.auth", transportAuth.getId(), true, con);
            }

            final MailAccount retval = getMailAccount(accountId, userId, contextId, con);

            con.commit();
            rollback = false;

            /*
             * Automatically check Unified Mail existence
             */
            if (mailAccount.isUnifiedINBOXEnabled()) {
                final UnifiedInboxManagement management = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
                if (null != management && !management.exists(userId, contextId, con)) {
                    management.createUnifiedINBOX(userId, contextId, con);
                }
            }

            return retval;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            closeSQLStuff(null, stmt);
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    public TransportAccount updateAndReturnTransportAccount(final TransportAccountDescription transportAccount, final int userId, final int contextId, final Session session) throws OXException {
        updateTransportAccount(transportAccount, userId, contextId, session);
        return getTransportAccount(transportAccount.getId(), userId, contextId);
    }

    @Override
    public void updateTransportAccount(final TransportAccountDescription transportAccount, final int userId, final int contextId, final Session session) throws OXException {
        final int accountId = transportAccount.getId();

        // Check name
        String name = transportAccount.getName();
        if (!isValid(name)) {
            name = null;
        }

        dropPOP3StorageFolders(userId, contextId);
        final Connection con = Database.get(contextId, true);
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            // Check prerequisites
            checkDuplicateTransportAccount(transportAccount, new TIntHashSet(new int[] { accountId }), userId, contextId, con);

            // Check protocol mismatch
            {
                final TransportAccount storageVersion = getTransportAccount(accountId, userId, contextId, con);

                // Transport protocol
                String newProtocol = transportAccount.getTransportProtocol();
                if (null != newProtocol) {
                    final String oldProtocol = storageVersion.getTransportProtocol();
                    if (!newProtocol.equalsIgnoreCase(oldProtocol)) {
                        throw MailAccountExceptionCodes.PROTOCOL_CHANGE.create(oldProtocol, newProtocol, I(userId), I(contextId));
                    }
                }
            }

            // Update...
            con.setAutoCommit(false);
            rollback = true;

            final String transportURL = transportAccount.generateTransportServerURL();
            if (null != transportURL) {
                String encryptedTransportPassword = encrypt(transportAccount.getTransportPassword(), session);

                StringBuilder sb = new StringBuilder(128).append("UPDATE user_transport_account SET ");
                if (null != name) {
                    sb.append("name = ?, ");
                }
                sb.append("url = ?, login = ?, password = ?, send_addr = ?, personal = ?, replyTo = ?, starttls = ?, oauth = ?, failed_auth_count=0, failed_auth_date=0, disabled=0 WHERE cid = ? AND id = ? AND user = ?");
                stmt = con.prepareStatement(sb.toString());
                sb = null;

                int pos = 1;
                if (null != name) {
                    stmt.setString(pos++, name);
                }
                stmt.setString(pos++, transportURL);
                setOptionalString(stmt, pos++, transportAccount.getTransportLogin());
                setOptionalString(stmt, pos++, encryptedTransportPassword);
                stmt.setString(pos++, transportAccount.getPrimaryAddress());
                final String personal = transportAccount.getPersonal();
                if (isEmpty(personal)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, personal);
                }
                final String replyTo = transportAccount.getReplyTo();
                if (isEmpty(replyTo)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, replyTo);
                }
                stmt.setInt(pos++, transportAccount.isTransportStartTls() ? 1 : 0);
                int oAuthAccountId = transportAccount.getTransportOAuthId();
                if (oAuthAccountId < 0) {
                    stmt.setNull(pos++, Types.INTEGER);
                } else {
                    stmt.setInt(pos++, oAuthAccountId);
                }
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, accountId);
                stmt.setLong(pos++, userId);
                stmt.executeUpdate();
            }

            TransportAuth transportAuth = transportAccount.getTransportAuth();
            if (null != transportAuth) {
                updateProperty(contextId, userId, transportAccount.getId(), "transport.auth", transportAuth.getId(), true, con);
            }
            String reference = transportAccount.getTransportProperties().get("reference");
            if (null != reference) {
                updateProperty(contextId, userId, transportAccount.getId(), "reference", reference, true, con);
            }

            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            closeSQLStuff(null, stmt);
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    @Override
    public void updateTransportAccount(TransportAccountDescription transportAccount, Set<Attribute> attributes, int userId, int contextId, Session session) throws OXException {
        Connection con = Database.get(contextId, true);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            updateTransportAccount(transportAccount, attributes, userId, contextId, UpdateProperties.builder().setChangePrimary(false).setChangeProtocol(false).setCon(con).setSession(session).build());

            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    private void updateTransportAccount0(TransportAccountDescription transportAccount, Set<Attribute> attributes, int userId, int contextId, UpdateProperties updateProperties) throws OXException {
        Connection con = Database.get(contextId, true);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            updateTransportAccount(transportAccount, attributes, userId, contextId, UpdateProperties.builder(updateProperties).setCon(con).build());

            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    @Override
    public void updateTransportAccount(TransportAccountDescription transportAccount, Set<Attribute> attributes, int userId, int contextId, UpdateProperties updateProperties) throws OXException {
        Connection con = updateProperties == null ? null : updateProperties.getCon();
        if (null == con) {
            updateTransportAccount0(transportAccount, attributes, userId, contextId, updateProperties);
            return;
        }

        Session session = updateProperties.getSession();
        boolean changePrimary = updateProperties.isChangePrimary();
        boolean changeProtocol = updateProperties.isChangeProtocol();

        if (attributes.contains(Attribute.NAME_LITERAL)) {
            // Check name
            final String name = transportAccount.getName();
            if (!isValid(name)) {
                throw MailAccountExceptionCodes.INVALID_NAME.create(name);
            }
        }

        if (!changePrimary && (MailAccount.DEFAULT_ID == transportAccount.getId())) {
            /*
             * Iterate attributes and compare their values except the one for Attribute.UNIFIED_INBOX_ENABLED_LITERAL,
             * Attribute.PERSONAL_LITERAL and Attribute.REPLY_TO_LITERAL
             */
            for (final Attribute attribute : attributes) {
                // Check for not editable attributes
                if (!Attribute.ID_LITERAL.equals(attribute) && !PRIMARY_EDITABLE.contains(attribute)) {
                    throw MailAccountExceptionCodes.NO_DEFAULT_UPDATE.create(I(userId), I(contextId));
                }
            }
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            TransportAccount storageVersion = null;
            if (prepareURL(attributes, Attribute.TRANSPORT_URL_ATTRIBUTES, Attribute.TRANSPORT_URL_LITERAL)) {
                storageVersion = getTransportAccount(transportAccount.getId(), userId, contextId, con);

                TransportAccountGetSwitch getSwitch = new TransportAccountGetSwitch(storageVersion);
                TransportSetSwitch setSwitch = new TransportSetSwitch(transportAccount);

                for (Attribute attribute : Attribute.TRANSPORT_URL_ATTRIBUTES) {
                    if (!attributes.contains(attribute)) {
                        Object value = attribute.doSwitch(getSwitch);
                        setSwitch.setValue(value);
                        attribute.doSwitch(setSwitch);
                    }
                }
                checkDuplicateTransportAccount(transportAccount, new TIntHashSet(new int[] { transportAccount.getId() }), userId, contextId, con);

                // Check protocol mismatch
                if (false == changeProtocol) {
                    String newProtocol = transportAccount.getTransportProtocol();
                    if (null != newProtocol) {
                        String oldProtocol = storageVersion.getTransportProtocol();
                        if (!newProtocol.equalsIgnoreCase(oldProtocol)) {
                            throw MailAccountExceptionCodes.PROTOCOL_CHANGE.create(oldProtocol, newProtocol, I(userId), I(contextId));
                        }
                    }
                }
            } else if (attributes.contains(Attribute.TRANSPORT_URL_LITERAL)) {
                checkDuplicateTransportAccount(transportAccount, new TIntHashSet(new int[] { transportAccount.getId() }), userId, contextId, con);

                // Check protocol mismatch
                if (false == changeProtocol) {
                    String newProtocol = transportAccount.getTransportProtocol();
                    if (null != newProtocol) {
                        storageVersion = getTransportAccount(transportAccount.getId(), userId, contextId, con);

                        String oldProtocol = storageVersion.getTransportProtocol();
                        if (!newProtocol.equalsIgnoreCase(oldProtocol)) {
                            throw MailAccountExceptionCodes.PROTOCOL_CHANGE.create(oldProtocol, newProtocol, I(userId), I(contextId));
                        }
                    }
                }
            }

            attributes.removeAll(Attribute.MAIL_URL_ATTRIBUTES);
            attributes.removeAll(Attribute.TRANSPORT_URL_ATTRIBUTES);

            String encryptedPassword = null; //

            List<Attribute> orderedAttributes = null;
            if (UpdateTransportAccountBuilder.needsUpdate(attributes)) {
                orderedAttributes = new ArrayList<>(attributes);

                /*
                 * Check existence of transport entry
                 */
                stmt = con.prepareStatement("SELECT 1 FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?");
                int pos = 1;
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, transportAccount.getId());
                stmt.setLong(pos++, userId);
                rs = stmt.executeQuery();
                final boolean exists = rs.next();
                closeSQLStuff(rs, stmt);

                if (exists) {
                    UpdateTransportAccountBuilder sqlBuilder = new UpdateTransportAccountBuilder(false);
                    TransportGetSwitch getter = new TransportGetSwitch(transportAccount);

                    // Compose SQL statement
                    for (Iterator<Attribute> iter = orderedAttributes.iterator(); iter.hasNext();) {
                        Attribute attribute = iter.next();
                        if (Attribute.TRANSPORT_URL_LITERAL == attribute) {
                            Object value = attribute.doSwitch(getter);
                            if (null == value) {
                                iter.remove();
                            } else {
                                attribute.doSwitch(sqlBuilder);
                            }
                        } else {
                            attribute.doSwitch(sqlBuilder);
                        }
                    }

                    if (sqlBuilder.isValid()) {
                        stmt = con.prepareStatement(sqlBuilder.getUpdateQuery());

                        // Fill prepared statement
                        pos = 1;
                        for (Attribute attribute : orderedAttributes) {
                            if (!sqlBuilder.handles(attribute)) {
                                continue;
                            }
                            Object value = attribute.doSwitch(getter);
                            switch (attribute) {
                                case TRANSPORT_PASSWORD_LITERAL:
                                    if (encryptedPassword == null) {
                                        encryptedPassword = encrypt(transportAccount.getTransportPassword(), session);
                                    }
                                    setOptionalString(stmt, pos++, encryptedPassword);
                                    break;
                                case TRANSPORT_LOGIN_LITERAL:
                                    setOptionalString(stmt, pos++, (String) value);
                                    break;
                                case TRANSPORT_URL_LITERAL:
                                    setOptionalString(stmt, pos++, (String) value);
                                    break;
                                case PERSONAL_LITERAL:
                                    final String personal = transportAccount.getPersonal();
                                    if (isEmpty(personal)) {
                                        stmt.setNull(pos++, TYPE_VARCHAR);
                                    } else {
                                        stmt.setString(pos++, personal);
                                    }
                                    break;
                                case REPLY_TO_LITERAL:
                                    final String replyTo = transportAccount.getReplyTo();
                                    if (isEmpty(replyTo)) {
                                        stmt.setNull(pos++, TYPE_VARCHAR);
                                    } else {
                                        stmt.setString(pos++, replyTo);
                                    }
                                    break;
                                case TRANSPORT_STARTTLS_LITERAL:
                                    boolean ttls = transportAccount.isTransportStartTls();
                                    stmt.setBoolean(pos++, ttls);
                                    break;
                                case TRANSPORT_OAUTH_LITERAL:
                                    int tOAuthId = transportAccount.getTransportOAuthId();
                                    stmt.setInt(pos++, tOAuthId);
                                    break;
                                default:
                                    stmt.setObject(pos++, value);
                                    break;
                            }
                        }

                        stmt.setLong(pos++, contextId);
                        stmt.setLong(pos++, transportAccount.getId());
                        stmt.setLong(pos++, userId);

                        if (LOG.isDebugEnabled()) {
                            final String query = stmt.toString();
                            LOG.debug("Trying to perform SQL update query for attributes {} :\n{}", orderedAttributes, query.substring(query.indexOf(':') + 1));
                        }

                        stmt.executeUpdate();
                        closeSQLStuff(stmt);
                    }
                } else {
                    /*
                     * Such an entry does not exist, yet
                     */
                    String transportURL = transportAccount.generateTransportServerURL();
                    if (null != transportURL) {
                        stmt.close();
                        String encryptedTransportPassword;
                        if (session == null) {
                            encryptedTransportPassword = null;
                        } else {
                            encryptedTransportPassword = encrypt(transportAccount.getTransportPassword(), session);
                        }
                        // cid, id, user, name, url, login, password, send_addr, default_flag
                        stmt = con.prepareStatement("INSERT INTO user_transport_account (cid, id, user, name, url, login, password, send_addr, default_flag, personal, replyTo, starttls, oauth) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        pos = 1;
                        stmt.setLong(pos++, contextId);
                        stmt.setLong(pos++, transportAccount.getId());
                        stmt.setLong(pos++, userId);
                        setOptionalString(stmt, pos++, transportAccount.getName());
                        stmt.setString(pos++, transportURL);
                        if (null == transportAccount.getTransportLogin()) {
                            stmt.setString(pos++, "");
                        } else {
                            stmt.setString(pos++, transportAccount.getTransportLogin());
                        }
                        setOptionalString(stmt, pos++, encryptedTransportPassword);
                        setOptionalString(stmt, pos++, transportAccount.getPrimaryAddress());
                        stmt.setInt(pos++, 0); // default flag
                        String personal = transportAccount.getPersonal();
                        if (isEmpty(personal)) {
                            stmt.setNull(pos++, TYPE_VARCHAR);
                        } else {
                            stmt.setString(pos++, personal);
                        }
                        String replyTo = transportAccount.getReplyTo();
                        if (isEmpty(replyTo)) {
                            stmt.setNull(pos++, TYPE_VARCHAR);
                        } else {
                            stmt.setString(pos++, replyTo);
                        }
                        stmt.setInt(pos++, transportAccount.isTransportStartTls() ? 1 : 0);

                        int oAuth = transportAccount.getTransportOAuthId();
                        if (oAuth < 0) {
                            stmt.setNull(pos++, Types.INTEGER);
                        } else {
                            stmt.setInt(pos++, oAuth);
                        }

                        if (LOG.isDebugEnabled()) {
                            String query = stmt.toString();
                            LOG.debug("Trying to perform SQL insert query:\n{}", query.substring(query.indexOf(':') + 1));
                        }

                        stmt.executeUpdate();
                        closeSQLStuff(stmt);
                    }
                }
            }

            if (attributes.contains(Attribute.TRANSPORT_AUTH_LITERAL)) {
                TransportAuth transportAuth = transportAccount.getTransportAuth();
                updateProperty(contextId, userId, transportAccount.getId(), "transport.auth", null == transportAuth ? null : transportAuth.getId(), true, con);
            }
        } catch (SQLSyntaxErrorException e) {
            if (null != stmt) {
                final String sql = stmt.toString();
                LOG.debug("\n\tFailed transport account statement:\n\t{}", new Object() {

                    @Override
                    public String toString() {
                        return sql.substring(sql.indexOf(": ") + 2);
                    }
                });
            }
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }

    }

    @Override
    public TransportAccount getTransportAccount(int accountId, int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT t.name, t.url, t.login, t.password, t.personal, t.replyTo, t.starttls, t.send_addr, t.oauth, t.disabled, m.login, m.password, m.oauth FROM user_transport_account AS t LEFT JOIN user_mail_account AS m ON t.cid=m.cid AND t.id=m.id AND t.user=m.user WHERE t.cid=? and t.id=? and t.user=?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, accountId);
            stmt.setLong(3, userId);
            result = stmt.executeQuery();
            if (!result.next()) {
                throw MailAccountExceptionCodes.NOT_FOUND.create(I(accountId), I(userId), I(contextId));
            }

            TransportAccountImpl transportAccount = new TransportAccountImpl();
            transportAccount.setName(result.getString(1));
            transportAccount.setId(accountId);
            transportAccount.parseTransportServerURL(result.getString(2));
            {
                final String transportLogin = result.getString(3);
                if (result.wasNull()) {
                    transportAccount.setTransportLogin(null);
                } else {
                    transportAccount.setTransportLogin(transportLogin);
                }
            }
            {
                final String transportPassword = result.getString(4);
                if (result.wasNull()) {
                    transportAccount.setTransportPassword(null);
                } else {
                    transportAccount.setTransportPassword(transportPassword);
                }
            }
            final String pers = result.getString(5);
            if (!result.wasNull()) {
                transportAccount.setPersonal(pers);
            }
            final String replyTo = result.getString(6);
            if (!result.wasNull()) {
                transportAccount.setReplyTo(replyTo);
            }
            transportAccount.setTransportStartTls(result.getBoolean(7));
            transportAccount.setSendAddress(result.getString(8));

            int oauthAccountId = result.getInt(9);
            if (result.wasNull()) {
                transportAccount.setTransportOAuthId(-1);
            } else {
                transportAccount.setTransportOAuthId(oauthAccountId);
            }
            boolean disabled = result.getBoolean(10);
            transportAccount.setTransportDisabled(disabled);
            /*
             * Fill properties
             */
            fillTransportProperties(transportAccount, contextId, userId, accountId, con);
            /*
             * Check credentials
             */
            if (transportAccount.getTransportAuth() == null) {
                if (Strings.isEmpty(transportAccount.getTransportLogin()) || Strings.isEmpty(transportAccount.getTransportPassword())) {
                    String login = result.getString(11);
                    if (!result.wasNull()) {
                        transportAccount.setTransportLogin(login);
                    }

                    String password = result.getString(12);
                    if (!result.wasNull()) {
                        transportAccount.setTransportPassword(password);
                    }

                    int oauthId = result.getInt(13);
                    if (result.wasNull()) {
                        transportAccount.setTransportOAuthId(-1);
                    } else {
                        transportAccount.setTransportOAuthId(oauthId < 0 ? -1 : oauthId);
                    }
                }
            } else {
                if (transportAccount.getTransportAuth().equals(TransportAuth.MAIL)) {
                    String login = result.getString(11);
                    if (!result.wasNull()) {
                        transportAccount.setTransportLogin(login);
                    }

                    String password = result.getString(12);
                    if (!result.wasNull()) {
                        transportAccount.setTransportPassword(password);
                    }

                    int oauthId = result.getInt(13);
                    if (result.wasNull()) {
                        transportAccount.setTransportOAuthId(-1);
                    } else {
                        transportAccount.setTransportOAuthId(oauthId < 0 ? -1 : oauthId);
                    }
                }
            }

            return transportAccount;
        } catch (final SQLException e) {
            if (null != stmt) {
                final String sql = stmt.toString();
                LOG.debug("\n\tFailed mail account statement:\n\t{}", new Object() {

                    @Override
                    public String toString() {
                        return sql.substring(sql.indexOf(": ") + 2);
                    }
                });
            }
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    @Override
    public TransportAccount getTransportAccount(final int id, final int userId, final int contextId) throws OXException {
        final Connection rcon = Database.get(contextId, false);
        try {
            return getTransportAccount(id, userId, contextId, rcon);
        } finally {
            Database.back(contextId, false, rcon);
        }
    }

    @Override
    public int insertMailAccount(final MailAccountDescription mailAccount, final int userId, final Context context, final Session session, final Connection con) throws OXException {
        final int contextId = context.getContextId();
        final boolean isUnifiedMail = mailAccount.getMailProtocol().startsWith(UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX, 0);
        final String primaryAddress = mailAccount.getPrimaryAddress();
        final String name = mailAccount.getName();
        if (!isUnifiedMail) {
            // Check if black-listed
            if ((false == mailAccount.isDefaultFlag())) {
                checkHostIfBlacklisted(mailAccount.getMailServer(), mailAccount.getMailPort());
                checkHostIfBlacklisted(mailAccount.getTransportServer(), mailAccount.getTransportPort());
            }

            // Check for duplicate
            if (-1 != getByPrimaryAddress(primaryAddress, userId, contextId, con)) {
                throw MailAccountExceptionCodes.CONFLICT_ADDR.create(primaryAddress, I(userId), I(contextId));
            }
            checkDuplicateMailAccount(mailAccount, null, userId, contextId, con);

            // Check name
            if (!isValid(name)) {
                throw MailAccountExceptionCodes.INVALID_NAME.create(name);
            }
        }
        dropPOP3StorageFolders(userId, contextId);
        // Get ID
        final int id;
        if (mailAccount.isDefaultFlag()) {
            try {
                getDefaultMailAccount(userId, contextId, con);
                throw MailAccountExceptionCodes.NO_DUPLICATE_DEFAULT.create();
            } catch (final OXException e) {
                LOG.trace("", e);
            }
            id = MailAccount.DEFAULT_ID;
        } else {
            try {
                id = IDGenerator.getId(context, com.openexchange.groupware.Types.MAIL_SERVICE, con);
            } catch (final SQLException e) {
                throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            }
        }
        PreparedStatement stmt = null;
        try {
            // Mail data
            {
                stmt = con.prepareStatement("INSERT INTO user_mail_account (cid, id, user, name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam, confirmed_ham, spam_handler, unified_inbox, trash_fullname, sent_fullname, drafts_fullname, spam_fullname, confirmed_spam_fullname, confirmed_ham_fullname, personal, replyTo, archive, archive_fullname, starttls, oauth) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                // Encrypt password
                String encryptedPassword;
                if (session == null) {
                    encryptedPassword = null;
                } else {
                    encryptedPassword = encrypt(mailAccount.getPassword(), session);
                }

                // cid, id, user, name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam,
                // confirmed_ham, spam_handler
                int pos = 1;
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, id);
                stmt.setLong(pos++, userId);
                stmt.setString(pos++, name);
                stmt.setString(pos++, mailAccount.generateMailServerURL());
                stmt.setString(pos++, mailAccount.getLogin());
                if (mailAccount.isDefaultFlag()) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    setOptionalString(stmt, pos++, encryptedPassword);
                }
                stmt.setString(pos++, primaryAddress);
                stmt.setInt(pos++, mailAccount.isDefaultFlag() ? 1 : 0);

                // Default folder names: trash, sent, drafts, spam, confirmed_spam, confirmed_ham
                {
                    stmt.setString(pos++, mailAccount.getTrash());
                    stmt.setString(pos++, mailAccount.getSent());
                    stmt.setString(pos++, mailAccount.getDrafts());
                    stmt.setString(pos++, mailAccount.getSpam());
                    stmt.setString(pos++, mailAccount.getConfirmedSpam());
                    stmt.setString(pos++, mailAccount.getConfirmedHam());
                }

                // Spam handler
                final String sh = mailAccount.getSpamHandler();
                if (null == sh) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, sh);
                }
                stmt.setInt(pos++, mailAccount.isUnifiedINBOXEnabled() ? 1 : 0);

                // Default folder full names
                {
                    stmt.setString(pos++, extractFullname(mailAccount.getTrashFullname()));
                    stmt.setString(pos++, extractFullname(mailAccount.getSentFullname()));
                    stmt.setString(pos++, extractFullname(mailAccount.getDraftsFullname()));
                    stmt.setString(pos++, extractFullname(mailAccount.getSpamFullname()));
                    stmt.setString(pos++, extractFullname(mailAccount.getConfirmedSpamFullname()));
                    stmt.setString(pos++, extractFullname(mailAccount.getConfirmedHamFullname()));
                }

                // Personal
                final String personal = mailAccount.getPersonal();
                if (isEmpty(personal)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, personal);
                }
                final String replyTo = mailAccount.getReplyTo();
                if (isEmpty(replyTo)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, replyTo);
                }

                // Archive
                setOptionalString(stmt, pos++, mailAccount.getArchive());
                setOptionalString(stmt, pos++, mailAccount.getArchiveFullname());

                // starttls flag
                stmt.setInt(pos++, mailAccount.isMailStartTls() ? 1 : 0);

                // OAuth account identifier
                int oAuthId = mailAccount.getMailOAuthId();
                if (oAuthId < 0) {
                    stmt.setNull(pos++, Types.INTEGER);
                } else {
                    stmt.setInt(pos++, oAuthId);
                }

                // Execute update
                stmt.executeUpdate();
                closeSQLStuff(null, stmt);
                stmt = null;
            }

            // Transport data
            String transportURL = mailAccount.generateTransportServerURL();
            if (null != transportURL) {
                stmt = con.prepareStatement("INSERT INTO user_transport_account (cid, id, user, name, url, login, password, send_addr, default_flag, personal, replyTo, starttls, oauth) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");

                // Encrypt password
                String encryptedTransportPassword;
                if (session == null) {
                    encryptedTransportPassword = null;
                } else {
                    encryptedTransportPassword = encrypt(mailAccount.getTransportPassword(), session);
                }

                // cid, id, user, name, url, login, password, send_addr, default_flag
                int pos = 1;
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, id);
                stmt.setLong(pos++, userId);
                stmt.setString(pos++, name);
                stmt.setString(pos++, transportURL);
                if (null == mailAccount.getTransportLogin()) {
                    stmt.setString(pos++, "");
                } else {
                    stmt.setString(pos++, mailAccount.getTransportLogin());
                }
                if (mailAccount.isDefaultFlag()) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    setOptionalString(stmt, pos++, encryptedTransportPassword);
                }
                stmt.setString(pos++, primaryAddress);
                stmt.setInt(pos++, mailAccount.isDefaultFlag() ? 1 : 0);
                final String personal = mailAccount.getPersonal();
                if (isEmpty(personal)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, personal);
                }
                final String replyTo = mailAccount.getReplyTo();
                if (isEmpty(replyTo)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, replyTo);
                }

                // STARTTLS flag
                stmt.setInt(pos++, mailAccount.isTransportStartTls() ? 1 : 0);

                // OAuth account identifier
                int oAuthId = mailAccount.getTransportOAuthId();
                if (oAuthId < 0) {
                    stmt.setNull(pos++, Types.INTEGER);
                } else {
                    stmt.setInt(pos++, oAuthId);
                }

                // Execute update
                stmt.executeUpdate();
                closeSQLStuff(null, stmt);
                stmt = null;
            }

            // Mail properties
            Map<String, String> properties = mailAccount.getProperties();
            if (!properties.isEmpty()) {
                if (properties.containsKey("pop3.deletewt")) {
                    updateProperty(contextId, userId, id, "pop3.deletewt", properties.get("pop3.deletewt"), false, con);
                }
                if (properties.containsKey("pop3.expunge")) {
                    updateProperty(contextId, userId, id, "pop3.expunge", properties.get("pop3.expunge"), false, con);
                }
                if (properties.containsKey("pop3.refreshrate")) {
                    updateProperty(contextId, userId, id, "pop3.refreshrate", properties.get("pop3.refreshrate"), false, con);
                }
                if (properties.containsKey("pop3.storage")) {
                    updateProperty(contextId, userId, id, "pop3.storage", properties.get("pop3.storage"), false, con);
                }
                if (properties.containsKey("pop3.path")) {
                    updateProperty(contextId, userId, id, "pop3.path", properties.get("pop3.path"), false, con);
                }
            }

            // Transport properties (only if transport data available)
            if (null != transportURL) {
                TransportAuth transportAuth = mailAccount.getTransportAuth();
                if (null != transportAuth) {
                    updateProperty(contextId, userId, id, "transport.auth", transportAuth.getId(), true, con);
                }
                String reference = mailAccount.getTransportProperties().get("reference");
                if (null != reference) {
                    updateProperty(contextId, userId, id, "reference", reference, true, con);
                }
            }
        } catch (SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }

        // Automatically check Unified Mail existence
        if (mailAccount.isUnifiedINBOXEnabled()) {
            final UnifiedInboxManagement management = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
            if (null != management && !management.exists(userId, contextId, con)) {
                management.createUnifiedINBOX(userId, contextId, con);
            }
        }
        return id;
    }

    private void checkHostIfBlacklisted(String host, int port) throws OXException {
        if (!isEmpty(host)) {
            boolean blacklisted = false;
            try {
                blacklisted = MailAccountUtils.isBlacklisted(host);
                if (false == blacklisted) {
                    String hostAddress = InetAddress.getByName(host).getHostAddress();
                    blacklisted = MailAccountUtils.isBlacklisted(hostAddress);
                }
                if (false == blacklisted && port > 0) {
                    blacklisted = false == MailAccountUtils.isAllowed(port);
                }
            } catch (final Exception e) {
                LOG.warn("Could not check host name \"" + host + "\" against IP range black-list", e);
            }
            if (blacklisted) {
                throw MailAccountExceptionCodes.BLACKLISTED_SERVER.create(host);
            }
        }
    }

    @Override
    public int acquireId(int userId, Context context) throws OXException {
        final int contextId = context.getContextId();
        final Connection con = Database.get(contextId, true);
        final int retval;
        try {
            con.setAutoCommit(false);
            retval = acquireId(userId, context, con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(contextId, true, con);
        }
        return retval;
    }

    /**
     * Acquires the next available identifier.
     *
     * @param userId The user identifier
     * @param context The context
     * @param con The connection to use
     * @return The reserved identifier
     * @throws OXException If next available identifier cannot be returned
     */
    public int acquireId(int userId, Context context, Connection con) throws OXException {
        try {
            return IDGenerator.getId(context, com.openexchange.groupware.Types.MAIL_SERVICE, con);
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public int insertMailAccount(final MailAccountDescription mailAccount, final int userId, final Context context, final Session session) throws OXException {
        final int contextId = context.getContextId();
        final Connection con = Database.get(contextId, true);
        final int retval;
        try {
            con.setAutoCommit(false);
            retval = insertMailAccount(mailAccount, userId, context, session, con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(contextId, true, con);
        }
        return retval;
    }

    @Override
    public int insertTransportAccount(TransportAccountDescription transportAccount, int userId, Context ctx, Session session) throws OXException {
        final int contextId = ctx.getContextId();
        final Connection con = Database.get(contextId, true);
        final int retval;
        try {
            con.setAutoCommit(false);
            retval = insertTransportAccount(transportAccount, userId, ctx, session, con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(contextId, true, con);
        }
        return retval;
    }

    private int insertTransportAccount(final TransportAccountDescription transportAccount, final int userId, final Context context, final Session session, final Connection con) throws OXException {
        final int contextId = context.getContextId();
        final String primaryAddress = transportAccount.getPrimaryAddress();
        final String name = transportAccount.getName();
        // Check if black-listed
        checkHostIfBlacklisted(transportAccount.getTransportServer(), transportAccount.getTransportPort());

        // Check for duplicate
        if (-1 != getTransportByPrimaryAddress(primaryAddress, userId, contextId, con)) {
                throw MailAccountExceptionCodes.CONFLICT_ADDR.create(primaryAddress, I(userId), I(contextId));
        }
        checkDuplicateTransportAccount(transportAccount, null, userId, contextId, con);

        // Check name
        if (!isValid(name)) {
            throw MailAccountExceptionCodes.INVALID_NAME.create(name);
        }
        dropPOP3StorageFolders(userId, contextId);
        // Get ID
        final int id;

        try {
            id = IDGenerator.getId(context, com.openexchange.groupware.Types.MAIL_SERVICE, con);
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
        PreparedStatement stmt = null;
        try {
            // Transport data
            String transportURL = transportAccount.generateTransportServerURL();
            if (null != transportURL) {
                stmt = con.prepareStatement("INSERT INTO user_transport_account (cid, id, user, name, url, login, password, send_addr, default_flag, personal, replyTo, starttls, oauth) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");

                // Encrypt password
                String encryptedTransportPassword;
                if (session == null) {
                    encryptedTransportPassword = null;
                } else {
                    encryptedTransportPassword = encrypt(transportAccount.getTransportPassword(), session);
                }

                // cid, id, user, name, url, login, password, send_addr, default_flag
                int pos = 1;
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, id);
                stmt.setLong(pos++, userId);
                stmt.setString(pos++, name);
                stmt.setString(pos++, transportURL);
                if (null == transportAccount.getTransportLogin()) {
                    stmt.setString(pos++, "");
                } else {
                    stmt.setString(pos++, transportAccount.getTransportLogin());
                }

                stmt.setString(pos++, encryptedTransportPassword);
                stmt.setString(pos++, primaryAddress);
                stmt.setInt(pos++, 0);
                final String personal = transportAccount.getPersonal();
                if (isEmpty(personal)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, personal);
                }
                final String replyTo = transportAccount.getReplyTo();
                if (isEmpty(replyTo)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, replyTo);
                }
                stmt.setInt(pos++, transportAccount.isTransportStartTls() ? 1 : 0);

                int oAuth = transportAccount.getTransportOAuthId();
                if (oAuth < 0) {
                    stmt.setNull(pos++, Types.INTEGER);
                } else {
                    stmt.setInt(pos++, oAuth);
                }

                // Execute update
                stmt.executeUpdate();
                closeSQLStuff(null, stmt);
                stmt = null;
            }

            // Transport properties (only if transport data available)
            if (null != transportURL) {
                TransportAuth transportAuth = transportAccount.getTransportAuth();
                if (null != transportAuth) {
                    updateProperty(contextId, userId, id, "transport.auth", transportAuth.getId(), true, con);
                }
                String reference = transportAccount.getTransportProperties().get("reference");
                if (null != reference) {
                    updateProperty(contextId, userId, id, "reference", reference, true, con);
                }
            }
        } catch (SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }

        return id;
    }

    @Override
    public int[] getByHostNames(final Collection<String> hostNames, final int userId, final int contextId) throws OXException {
        final Connection con = Database.get(contextId, false);
        try {
            return getByHostNames(hostNames, userId, contextId, con);
        } finally {
            Database.back(contextId, false, con);
        }
    }

    private int[] getByHostNames(final Collection<String> hostNames, final int userId, final int contextId, final Connection con) throws OXException {
        if (null == hostNames || hostNames.isEmpty()) {
            return new int[0];
        }
        final Set<String> set = new HashSet<>(hostNames.size());
        for (final String hostName : hostNames) {
            set.add(hostName.toLowerCase(Locale.ENGLISH));
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id, url FROM user_mail_account WHERE cid = ? AND user = ? ORDER BY id");
            stmt.setLong(1, contextId);
            stmt.setLong(2, userId);
            result = stmt.executeQuery();
            if (!result.next()) {
                return new int[0];
            }
            final CustomMailAccount tmp = new CustomMailAccount(-1);
            final TIntList ids = new TIntArrayList(6);
            do {
                tmp.parseMailServerURL(result.getString(2));
                if (set.contains(tmp.getMailServer().toLowerCase(Locale.ENGLISH))) {
                    ids.add(result.getInt(1));
                }
            } while (result.next());
            if (ids.isEmpty()) {
                return new int[0];
            }
            final int[] array = ids.toArray();
            Arrays.sort(array);
            return array;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    @Override
    public int getByPrimaryAddress(final String primaryAddress, final int userId, final int contextId) throws OXException {
        final Connection con = Database.get(contextId, false);
        try {
            return getByPrimaryAddress(primaryAddress, userId, contextId, con);
        } finally {
            Database.back(contextId, false, con);
        }
    }

    private int getByPrimaryAddress(final String primaryAddress, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM user_mail_account WHERE cid = ? AND primary_addr = ? AND user = ?");
            stmt.setLong(1, contextId);
            stmt.setString(2, primaryAddress);
            stmt.setLong(3, userId);
            result = stmt.executeQuery();
            if (!result.next()) {
                return -1;
            }
            final int id = result.getInt(1);
            if (result.next()) {
                throw MailAccountExceptionCodes.CONFLICT_ADDR.create(primaryAddress, I(userId), I(contextId));
            }
            return id;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    @Override
    public int getTransportByPrimaryAddress(final String primaryAddress, final int userId, final int contextId) throws OXException {
        final Connection con = Database.get(contextId, false);
        try {
            return getTransportByPrimaryAddress(primaryAddress, userId, contextId, con);
        } finally {
            Database.back(contextId, false, con);
        }
    }

    private int getTransportByPrimaryAddress(final String primaryAddress, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM user_transport_account WHERE cid = ? AND send_addr = ? AND user = ?");
            stmt.setLong(1, contextId);
            stmt.setString(2, primaryAddress);
            stmt.setLong(3, userId);
            result = stmt.executeQuery();
            if (!result.next()) {
                return -1;
            }
            final int id = result.getInt(1);
            if (result.next()) {
                throw MailAccountExceptionCodes.CONFLICT_ADDR.create(primaryAddress, I(userId), I(contextId));
            }
            return id;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    @Override
    public TransportAccount getTransportByReference(final String reference, final int userId, final int contextId) throws OXException {
        final Connection con = Database.get(contextId, false);
        try {
            return getTransportByReference(reference, userId, contextId, con);
        } finally {
            Database.back(contextId, false, con);
        }
    }

    private TransportAccount getTransportByReference(final String reference, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM user_transport_account_properties WHERE cid = ? AND user = ? AND name = ? AND value = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, userId);
            stmt.setString(3, "reference");
            stmt.setString(4, reference);
            result = stmt.executeQuery();
            if (!result.next()) {
                return null;
            }

            // Get identifier
            int id = result.getInt(1);
            closeSQLStuff(result, stmt);
            result = null;
            stmt = null;

            return getTransportAccount(id, userId, contextId, con);
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private static final class FailedAuthInfo {
        final int count;
        final long start;
        final String url;

        FailedAuthInfo(int count, long start, String url) {
            super();
            this.count = count;
            this.url = url;
            this.start = 0 == start ? System.currentTimeMillis() : start;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(32);
            builder.append("[count=").append(count).append(", start=").append(start).append(", ");
            if (url != null) {
                builder.append("url=").append(url);
            }
            builder.append("]");
            return builder.toString();
        }
    }

    private static int getFailedAuthThreshold(int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        int def = 5;
        ConfigView view = viewFactory.getView(userId, contextId);
        ComposedConfigProperty<Integer> property = view.property("com.openexchange.mailaccount.failedAuth.limit", int.class);

        if (false == property.isDefined()) {
            return def;
        }

        Integer limit = property.get();
        if (null == limit) {
            return def;
        }

        return limit.intValue();
    }

    private static long getFailedAuthTimeSpan(int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        String def = "30m";
        ConfigView view = viewFactory.getView(userId, contextId);
        ComposedConfigProperty<String> property = view.property("com.openexchange.mailaccount.failedAuth.span", String.class);

        if (false == property.isDefined()) {
            return ConfigTools.parseTimespan(def);
        }

        String span = property.get();
        if (Strings.isEmpty(span)) {
            return ConfigTools.parseTimespan(def);
        }

        return ConfigTools.parseTimespan(span.trim());
    }

    private boolean disableAccount(boolean mailAccess, int accountId, int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            if (mailAccess) {
                stmt = con.prepareStatement("UPDATE user_mail_account SET disabled=1 WHERE cid = ? AND id = ? AND user = ? AND disabled=0");
            } else {
                stmt = con.prepareStatement("UPDATE user_transport_account SET disabled=1 WHERE cid = ? AND id = ? AND user = ? AND disabled=0");
            }
            stmt.setLong(1, contextId);
            stmt.setLong(2, accountId);
            stmt.setLong(3, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private boolean incrementOrResetAccount(boolean mailAccess, boolean reset, int currentCount, int accountId, int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            if (reset) {
                if (mailAccess) {
                    stmt = con.prepareStatement("UPDATE user_mail_account SET failed_auth_count=1, failed_auth_date=? WHERE cid=? AND id=? AND user=? AND failed_auth_count=?");
                } else {
                    stmt = con.prepareStatement("UPDATE user_transport_account SET failed_auth_count=1, failed_auth_date=? WHERE cid=? AND id=? AND user=? AND failed_auth_count=?");
                }
                stmt.setLong(1, System.currentTimeMillis());
                stmt.setLong(2, contextId);
                stmt.setLong(3, accountId);
                stmt.setLong(4, userId);
                stmt.setInt(5, currentCount);
            } else {
                int newCount = currentCount + 1;
                if (newCount == 1) {
                    if (mailAccess) {
                        stmt = con.prepareStatement("UPDATE user_mail_account SET failed_auth_count=1, failed_auth_date=? WHERE cid=? AND id=? AND user=? AND failed_auth_count=0");
                    } else {
                        stmt = con.prepareStatement("UPDATE user_transport_account SET failed_auth_count=1, failed_auth_date=? WHERE cid=? AND id=? AND user=? AND failed_auth_count=0");
                    }
                    stmt.setLong(1, System.currentTimeMillis());
                    stmt.setLong(2, contextId);
                    stmt.setLong(3, accountId);
                    stmt.setLong(4, userId);
                } else {
                    if (mailAccess) {
                        stmt = con.prepareStatement("UPDATE user_mail_account SET failed_auth_count=? WHERE cid=? AND id=? AND user=? AND failed_auth_count=?");
                    } else {
                        stmt = con.prepareStatement("UPDATE user_transport_account SET failed_auth_count=? WHERE cid=? AND id=? AND user=? AND failed_auth_count=?");
                    }
                    stmt.setInt(1, newCount);
                    stmt.setLong(2, contextId);
                    stmt.setLong(3, accountId);
                    stmt.setLong(4, userId);
                    stmt.setInt(5, currentCount);
                }
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private boolean incrementFailedAuthCount(boolean mailAccess, int accountId, int userId, int contextId, Exception optReason, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (mailAccess) {
                stmt = con.prepareStatement("SELECT failed_auth_count, failed_auth_date, disabled, url FROM user_mail_account WHERE cid=? AND id=? AND user=?");
            } else {
                stmt = con.prepareStatement("SELECT failed_auth_count, failed_auth_date, disabled, url FROM user_transport_account WHERE cid=? AND id=? AND user=?");
            }
            stmt.setLong(1, contextId);
            stmt.setLong(2, accountId);
            stmt.setLong(3, userId);
            rs = stmt.executeQuery();

            if (false == rs.next() || rs.getBoolean(3)) {
                // No such account or already disabled
                return false;
            }

            FailedAuthInfo failedAuthInfo = new FailedAuthInfo(rs.getInt(1), rs.getLong(2), rs.getString(4));
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            if (failedAuthInfo.count + 1 > getFailedAuthThreshold(userId, contextId)) {
                // Exceeded...
                boolean disabled = disableAccount(mailAccess, accountId, userId, contextId, con);
                if (disabled) {
                    if (null == optReason) {                        
                        LOG.info("Disabled {} account {} ({}) of user {} in context {} due to exceeded failed auth count", mailAccess ? "mail" : "transport", accountId, failedAuthInfo.url, userId, contextId);
                    } else {
                        LOG.info("Disabled {} account {} ({}) of user {} in context {} due to exceeded failed auth count", mailAccess ? "mail" : "transport", accountId, failedAuthInfo.url, userId, contextId, optReason);
                    }
                }
                return disabled;
            }

            if ((System.currentTimeMillis() - failedAuthInfo.start) <= getFailedAuthTimeSpan(userId, contextId)) {
                // Increment
                boolean incremented = incrementOrResetAccount(mailAccess, false, failedAuthInfo.count, accountId, userId, contextId, con);
                if (incremented) {
                    LOG.debug("Incremented failed auth count to {} for {} account {} ({}) of user {} in context {}", failedAuthInfo.count + 1, mailAccess ? "mail" : "transport", accountId, failedAuthInfo.url, userId, contextId);
                    return false;
                }
            } else {
                // Reset
                boolean resetted = incrementOrResetAccount(mailAccess, true, failedAuthInfo.count, accountId, userId, contextId, con);
                if (resetted) {
                    LOG.debug("Set failed auth count to {} for {} account {} ({}) of user {} in context {}", 1, mailAccess ? "mail" : "transport", accountId, failedAuthInfo.url, userId, contextId);
                    return false;
                }
            }

            // Concurrent update...
            return incrementFailedAuthCount(mailAccess, accountId, userId, contextId, optReason, con);
        } catch (SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public boolean incrementFailedMailAuthCount(int accountId, int userId, int contextId, Exception optReason) throws OXException {
        Connection con = Database.get(contextId, true);
        try {
            return incrementFailedMailAuthCount(accountId, userId, contextId, optReason, con);
        } finally {
            Database.back(contextId, true, con);
        }
    }

    /**
     * Increments the count of failed authentications for specified account's mail access.
     *
     * @param accountId The account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param optReason The optional reason for failed authentication or <code>null</code>
     * @param con The connection to use
     * @return <code>true</code> if mail access has been disabled due to this call; otherwise <code>false</code>
     * @throws OXException If incrementing the count fails
     */
    public boolean incrementFailedMailAuthCount(int accountId, int userId, int contextId, Exception optReason, Connection con) throws OXException {
        return incrementFailedAuthCount(true, accountId, userId, contextId, optReason, con);
    }

    @Override
    public boolean incrementFailedTransportAuthCount(int accountId, int userId, int contextId, Exception optReason) throws OXException {
        Connection con = Database.get(contextId, true);
        try {
            return incrementFailedTransportAuthCount(accountId, userId, contextId, optReason, con);
        } finally {
            Database.back(contextId, true, con);
        }
    }

    /**
     * Increments the count of failed authentications for specified account's mail transport.
     *
     * @param accountId The account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param optReason The optional reason for failed authentication or <code>null</code>
     * @param con The connection to use
     * @return <code>true</code> if mail transport has been disabled due to this call; otherwise <code>false</code>
     * @throws OXException If incrementing the count fails
     */
    public boolean incrementFailedTransportAuthCount(int accountId, int userId, int contextId, Exception optReason, Connection con) throws OXException {
        return incrementFailedAuthCount(false, accountId, userId, contextId, optReason, con);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void checkDuplicateMailAccount(final MailAccountDescription mailAccount, final TIntSet excepts, final int userId, final int contextId, final Connection con) throws OXException {
        final String server = mailAccount.getMailServer();
        if (isEmpty(server)) {
            /*
             * No mail server specified
             */
            return;
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id, url, login FROM user_mail_account WHERE cid = ? AND user = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, userId);
            result = stmt.executeQuery();
            if (!result.next()) {
                return;
            }
            InetAddress addr;
            try {
                addr = InetAddress.getByName(IDNA.toASCII(server));
            } catch (final UnknownHostException e) {
                LOG.warn("Unable to resolve host name '{}' to an IP address", server, e);
                addr = null;
            }
            final int port = mailAccount.getMailPort();
            final String login = mailAccount.getLogin();
            do {
                final int id = (int) result.getLong(1);
                if (null == excepts || !excepts.contains(id)) {
                    final AbstractMailAccount current = MailAccount.DEFAULT_ID == id ? new DefaultMailAccount() : new CustomMailAccount(id);
                    final String url = result.getString(2);
                    if (null != url) {
                        current.parseMailServerURL(url);
                        if (checkMailServer(server, addr, current.getMailServer()) && checkProtocol(mailAccount.getMailProtocol(), current.getMailProtocol()) && current.getMailPort() == port && (null != login && login.equals(result.getString(3)))) {
                            throw MailAccountExceptionCodes.DUPLICATE_MAIL_ACCOUNT.create(I(userId), I(contextId));
                        }
                    }
                }
            } while (result.next());
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private void checkDuplicateTransportAccount(final TransportAccountDescription transportAccount, final TIntSet excepts, final int userId, final int contextId, final Connection con) throws OXException {
        final String server = transportAccount.getTransportServer();
        if (isEmpty(server)) {
            /*
             * No mail server specified
             */
            return;
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id, url, login FROM user_transport_account WHERE cid = ? AND user = ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, userId);
            result = stmt.executeQuery();
            if (!result.next()) {
                return;
            }
            InetAddress addr;
            try {
                addr = InetAddress.getByName(IDNA.toASCII(server));
            } catch (final UnknownHostException e) {
                LOG.warn("Unable to resolve host name '{}' to an IP address", server, e);
                addr = null;
            }
            final int port = transportAccount.getTransportPort();
            final String login = transportAccount.getTransportLogin();
            do {
                final int id = (int) result.getLong(1);
                if (null == excepts || !excepts.contains(id)) {
                    final TransportAccountImpl current = new TransportAccountImpl();
                    final String url = result.getString(2);
                    if (null != url) {
                        current.parseTransportServerURL(url);
                        if (checkMailServer(server, addr, current.getTransportServer()) && checkProtocol(transportAccount.getTransportProtocol(), current.getTransportProtocol()) && current.getTransportPort() == port && (null != login && login.equals(result.getString(3)))) {
                            throw MailAccountExceptionCodes.DUPLICATE_MAIL_ACCOUNT.create(I(userId), I(contextId));
                        }
                    }
                }
            } while (result.next());
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private static boolean checkMailServer(final String newMailServer, final InetAddress addr, final String existingMailServer) {
        if (isEmpty(existingMailServer)) {
            return false;
        }
        if (null == addr) {
            /*
             * Check by server string
             */
            return newMailServer.equalsIgnoreCase(existingMailServer);
        }
        try {
            return addr.equals(InetAddress.getByName(IDNA.toASCII(existingMailServer)));
        } catch (final UnknownHostException e) {
            LOG.warn("", e);
            /*
             * Check by server string
             */
            return newMailServer.equalsIgnoreCase(existingMailServer);
        }
    }

    private static boolean checkProtocol(final String protocol1, final String protocol2) {
        if (isEmpty(protocol1) || isEmpty(protocol2)) {
            return false;
        }
        return protocol1.equalsIgnoreCase(protocol2);
    }

    @Override
    public boolean hasAccounts(final Session session) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final int contextId = session.getContextId();
        final int userId = session.getUserId();
        try {
            con = Database.get(contextId, false);
            stmt = con.prepareStatement("SELECT 1 FROM user_mail_account WHERE cid = ? AND user = ? AND id > 0 LIMIT 1");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
            DBUtils.closeSQLStuff(rs, stmt);

            stmt = con.prepareStatement("SELECT 1 FROM user_transport_account WHERE cid = ? AND user = ? AND id > 0 LIMIT 1");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            if (con != null) {
                Database.back(contextId, false, con);
            }
        }
    }

    @Override
    public void migratePasswords(final String oldSecret, final String newSecret, final Session session) throws OXException {
        // Clear possible cached MailAccess instances
        final int contextId = session.getContextId();
        final int userId = session.getUserId();
        cleanUp(userId, contextId);
        // Migrate password
        Connection con = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            con = Database.get(contextId, true);
            con.setAutoCommit(false); // BEGIN
            rollback = true;

            // Perform SELECT query
            selectStmt = con.prepareStatement("SELECT id, password FROM user_mail_account WHERE cid = ? AND user = ?");
            selectStmt.setInt(1, contextId);
            selectStmt.setInt(2, userId);
            rs = selectStmt.executeQuery();

            // Gather needed services
            CryptoService cryptoService = ServerServiceRegistry.getInstance().getService(CryptoService.class);
            if (null == cryptoService) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(CryptoService.class.getName());
            }

            // Iterate mail accounts
            Map<Integer, String> transcribedPasswords = new LinkedHashMap<>(8);
            while (rs.next()) {
                String password = rs.getString(2);
                if (!isEmpty(password)) {
                    int id = rs.getInt(1);
                    if (id != MailAccount.DEFAULT_ID) {
                        try {
                            // If we can decrypt the password with the newSecret, we don't need to do anything about this account
                            cryptoService.decrypt(password, newSecret);
                        } catch (final OXException x) {
                            // Decrypt with old -- encrypt with new
                            String decrypted = cryptoService.decrypt(password, oldSecret);
                            String transcribed = cryptoService.encrypt(decrypted, newSecret);

                            // Add to batch update
                            transcribedPasswords.put(id, transcribed);
                        }
                    }
                }
            }
            DBUtils.closeSQLStuff(rs, selectStmt);
            if (false == transcribedPasswords.isEmpty()) {
                updateStmt = con.prepareStatement("UPDATE user_mail_account SET password = ? WHERE cid = ? AND id = ? AND user = ?");
                updateStmt.setInt(2, contextId);
                updateStmt.setInt(4, userId);
                for (Map.Entry<Integer, String> e : transcribedPasswords.entrySet()) {
                    updateStmt.setString(1, e.getValue());
                    updateStmt.setInt(3, e.getKey().intValue());
                    updateStmt.addBatch();
                }
                transcribedPasswords.clear();

                updateStmt.executeBatch();
                DBUtils.closeSQLStuff(updateStmt);
                updateStmt = null;
            }

            // Perform other SELECT query
            selectStmt = con.prepareStatement("SELECT id, password FROM user_transport_account WHERE cid = ? AND user = ?");
            selectStmt.setInt(1, contextId);
            selectStmt.setInt(2, userId);
            rs = selectStmt.executeQuery();

            // Iterate transport accounts
            while (rs.next()) {
                String password = rs.getString(2);
                if (!isEmpty(password)) {
                    int id = rs.getInt(1);
                    if (id != MailAccount.DEFAULT_ID) {
                        try {
                            // If we can decrypt the password with the newSecret, we don't need to do anything about this account
                            cryptoService.decrypt(password, newSecret);
                        } catch (final OXException x) {
                            // Decrypt with old -- encrypt with new
                            String decrypted = cryptoService.decrypt(password, oldSecret);
                            String transcribed = cryptoService.encrypt(decrypted, newSecret);

                            // Add to batch update
                            transcribedPasswords.put(id, transcribed);
                        }
                    }
                }
            }
            DBUtils.closeSQLStuff(rs, selectStmt);
            if (false == transcribedPasswords.isEmpty()) {
                updateStmt = con.prepareStatement("UPDATE user_transport_account SET password = ? WHERE cid = ? AND id = ? AND user = ?");
                updateStmt.setInt(2, contextId);
                updateStmt.setInt(4, userId);
                for (Map.Entry<Integer, String> e : transcribedPasswords.entrySet()) {
                    updateStmt.setString(1, e.getValue());
                    updateStmt.setInt(3, e.getKey().intValue());
                    updateStmt.addBatch();
                }
                transcribedPasswords.clear();

                updateStmt.executeBatch();
                DBUtils.closeSQLStuff(updateStmt);
                updateStmt = null;
            }
            con.commit(); // COMMIT
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.rollback(con);
            }
            DBUtils.closeSQLStuff(rs, selectStmt);
            DBUtils.closeSQLStuff(updateStmt);
            if (con != null) {
                DBUtils.autocommit(con);
                Database.back(contextId, true, con);
            }
        }
    }

    @Override
    public void cleanUp(final String secret, final Session session) throws OXException {
        final int userId = session.getUserId();
        final int contextId = session.getContextId();
        // Clear possible cached MailAccess instances
        cleanUp(userId, contextId);
        // Migrate password
        Connection con = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        boolean modified = false;
        try {
            con = Database.get(contextId, true);
            con.setAutoCommit(false); // BEGIN
            rollback = true;
            /*
             * Perform SELECT query
             */
            selectStmt = con.prepareStatement("SELECT id, password, login, url FROM user_mail_account WHERE cid = ? AND user = ?");
            selectStmt.setInt(1, contextId);
            selectStmt.setInt(2, userId);
            rs = selectStmt.executeQuery();
            while (rs.next()) {
                final String password = rs.getString(2);
                if (!isEmpty(password)) {
                    final int id = rs.getInt(1);
                    if (id != MailAccount.DEFAULT_ID) {
                        try {
                            // If we can decrypt the password with the newSecret, we don't need to do anything about this account
                            MailPasswordUtil.decrypt(password, secret);
                        } catch (final GeneralSecurityException x) {
                            // We couldn't decrypt
                            if (null == updateStmt) {
                                updateStmt = con.prepareStatement("UPDATE user_mail_account SET password = ?  WHERE cid = ? AND id = ? AND user = ?");
                                updateStmt.setInt(2, contextId);
                                updateStmt.setInt(4, userId);
                            }
                            updateStmt.setString(1, "");
                            updateStmt.setInt(3, id);
                            updateStmt.addBatch();
                        }
                    }
                }
            }
            if (null != updateStmt) {
                updateStmt.executeBatch();
                modified = true;
                DBUtils.closeSQLStuff(updateStmt);
                updateStmt = null;
            }
            /*
             * Close stuff
             */
            DBUtils.closeSQLStuff(rs, selectStmt);
            /*
             * Perform other SELECT query
             */
            selectStmt = con.prepareStatement("SELECT id, password, login, url FROM user_transport_account WHERE cid = ? AND user = ?");
            selectStmt.setInt(1, contextId);
            selectStmt.setInt(2, userId);
            rs = selectStmt.executeQuery();
            while (rs.next()) {
                final String password = rs.getString(2);
                if (!isEmpty(password)) {
                    final int id = rs.getInt(1);
                    if (id == MailAccount.DEFAULT_ID) {
                        continue;
                    }
                    try {
                        // If we can decrypt the password with the newSecret, we don't need to do anything about this account
                        MailPasswordUtil.decrypt(password, secret);
                    } catch (final GeneralSecurityException x) {
                        // We couldn't decrypt
                        if (null == updateStmt) {
                            updateStmt = con.prepareStatement("UPDATE user_transport_account SET password = ?  WHERE cid = ? AND id = ? AND user = ?");
                            updateStmt.setInt(2, contextId);
                            updateStmt.setInt(4, userId);
                        }
                        updateStmt.setString(1, "");
                        updateStmt.setInt(3, id);
                        updateStmt.addBatch();
                    }
                }
            }
            if (null != updateStmt) {
                updateStmt.executeBatch();
                modified = false;
                DBUtils.closeSQLStuff(updateStmt);
                updateStmt = null;
            }
            con.commit(); // COMMIT
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.rollback(con);
            }
            DBUtils.closeSQLStuff(rs, selectStmt);
            DBUtils.closeSQLStuff(updateStmt);
            if (con != null) {
                DBUtils.autocommit(con);
                if (modified) {
                    Database.getDatabaseService().backWritable(contextId, con);
                } else {
                    Database.getDatabaseService().backWritableAfterReading(contextId, con);
                }
            }
        }
    }

    @Override
    public void removeUnrecoverableItems(final String secret, final Session session) throws OXException {
        final int userId = session.getUserId();
        final int contextId = session.getContextId();
        // Clear possible cached MailAccess instances
        cleanUp(userId, contextId);
        // Migrate password
        Connection con = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            con = Database.get(contextId, true);
            con.setAutoCommit(false); // BEGIN
            rollback = true;
            /*
             * Perform SELECT query
             */
            selectStmt = con.prepareStatement("SELECT id, password, login, url FROM user_mail_account WHERE cid = ? AND user = ?");
            selectStmt.setInt(1, contextId);
            selectStmt.setInt(2, userId);
            rs = selectStmt.executeQuery();
            while (rs.next()) {
                final String password = rs.getString(2);
                if (!isEmpty(password)) {
                    final int id = rs.getInt(1);
                    if (id != MailAccount.DEFAULT_ID) {
                        try {
                            // If we can decrypt the password with the newSecret, we don't need to do anything about this account
                            MailPasswordUtil.decrypt(password, secret);
                        } catch (final GeneralSecurityException x) {
                            // We couldn't decrypt
                            if (null == updateStmt) {
                                updateStmt = con.prepareStatement("DELETE FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?");
                                updateStmt.setInt(1, contextId);
                                updateStmt.setInt(3, userId);
                            }
                            updateStmt.setInt(2, id);
                            updateStmt.addBatch();
                        }
                    }
                }
            }
            if (null != updateStmt) {
                updateStmt.executeBatch();
                DBUtils.closeSQLStuff(updateStmt);
                updateStmt = null;
            }
            /*
             * Close stuff
             */
            DBUtils.closeSQLStuff(rs, selectStmt);
            /*
             * Perform other SELECT query
             */
            selectStmt = con.prepareStatement("SELECT id, password, login, url FROM user_transport_account WHERE cid = ? AND user = ?");
            selectStmt.setInt(1, contextId);
            selectStmt.setInt(2, userId);
            rs = selectStmt.executeQuery();
            while (rs.next()) {
                final String password = rs.getString(2);
                if (!isEmpty(password)) {
                    final int id = rs.getInt(1);
                    if (id == MailAccount.DEFAULT_ID) {
                        continue;
                    }
                    try {
                        // If we can decrypt the password with the newSecret, we don't need to do anything about this account
                        MailPasswordUtil.decrypt(password, secret);
                    } catch (final GeneralSecurityException x) {
                        // We couldn't decrypt
                        if (null == updateStmt) {
                            updateStmt = con.prepareStatement("DELETE FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?");
                            updateStmt.setInt(1, contextId);
                            updateStmt.setInt(3, userId);
                        }
                        updateStmt.setInt(2, id);
                        updateStmt.addBatch();
                    }
                }
            }
            if (null != updateStmt) {
                updateStmt.executeBatch();
                DBUtils.closeSQLStuff(updateStmt);
                updateStmt = null;
            }
            con.commit(); // COMMIT
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.rollback(con);
            }
            DBUtils.closeSQLStuff(rs, selectStmt);
            DBUtils.closeSQLStuff(updateStmt);
            if (con != null) {
                DBUtils.autocommit(con);
                Database.back(contextId, true, con);
            }
        }
        cleanUp(userId, contextId);
    }

    private void cleanUp(final int userId, final int contextId) {
        final SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null != service) {
            final Session session = service.getAnyActiveSessionForUser(userId, contextId);
            if (null != session) {
                try {
                    final IMailAccessCache mac = MailAccess.getMailAccessCache();
                    final int[] ids = getUserMailAccountIDs(userId, contextId);
                    for (final int id : ids) {
                        while (mac.removeMailAccess(session, id) != null) {
                            // Nope...
                        }
                    }
                } catch (final Exception exc) {
                    LOG.error("Unable to clear cached mail accesses.", exc);
                }
            }
        }
    }

    private String encrypt(final String toCrypt, final Session session) throws OXException {
        if (null == toCrypt) {
            return null;
        }
        final SecretEncryptionService<GenericProperty> encryptionService = getService(SecretEncryptionFactoryService.class).createService(STRATEGY);
        return encryptionService.encrypt(session, toCrypt);
    }

    private static <S> S getService(final Class<? extends S> clazz) {
        return ServerServiceRegistry.getInstance().getService(clazz);
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++ UTILITY METHOD(S) ++++++++++++++++++++++++++++++++++++
     */

    private static boolean disableForeignKeyChecks(final Connection con) {
        if (null == con) {
            return false;
        }
        try {
            DBUtils.disableMysqlForeignKeyChecks(con);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    private static void enableForeignKeyChecks(final Connection con) throws SQLException {
        if (null == con) {
            return;
        }
        DBUtils.enableMysqlForeignKeyChecks(con);
    }

    private static void setOptionalString(final PreparedStatement stmt, final int pos, final String string) throws SQLException {
        stmt.setString(pos, null == string ? "" : string);
    }

    private static String getOptionalString(final String string) {
        return (null == string || 0 == string.length()) ? null : string;
    }

    /**
     * Binary-sorted invalid characters: No control <code>\t\n\f\r</code> or punctuation <code>!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~</code>
     * except <code>'-'</code> and <code>'_'</code>.
     */
    private static final char[] CHARS_INVALID = {
        '\t', '\n', '\f', '\r', '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '.', '/', ':', ';', '<', '=', '>', '?', '@',
        '[', '\\', ']', '^', '`', '{', '|', '}', '~' };

    /**
     * Checks if specified name contains an invalid character.
     *
     * @param name The name to check
     * @return <code>true</code> if name contains an invalid character; otherwise <code>false</code>
     */
    private static boolean isValid(final String name) {
        /*
         * TODO: Re-think about invalid characters
         */
        return (null != name && 0 != name.length());
    }

}
