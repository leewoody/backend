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

package com.openexchange.mail.api;

import static com.openexchange.mail.utils.ProviderUtility.toSocketAddrString;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.mail.internet.AddressException;
import javax.mail.internet.idn.IDNA;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.ConfiguredServer;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.oauth.MailOAuthService;
import com.openexchange.mail.oauth.TokenInfo;
import com.openexchange.mail.utils.ImmutableReference;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.Account;
import com.openexchange.mailaccount.Credentials;
import com.openexchange.mailaccount.CredentialsProviderRegistry;
import com.openexchange.mailaccount.CredentialsProviderService;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.Password;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailConfig} - The user-specific mail properties; e.g. containing user's login data.
 * <p>
 * Provides access to global mail properties.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailConfig {

    private static final class AuthTypeKey {

        final boolean forMailAccess;
        final int userId;
        final int contextId;
        private final int hash;

        AuthTypeKey(boolean forMailAccess, int userId, int contextId) {
            super();
            this.forMailAccess = forMailAccess;
            this.userId = userId;
            this.contextId = contextId;

            int prime = 31;
            int result = 1;
            result = prime * result + (forMailAccess ? 1231 : 1237);
            result = prime * result + contextId;
            result = prime * result + userId;
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
            AuthTypeKey other = (AuthTypeKey) obj;
            if (forMailAccess != other.forMailAccess) {
                return false;
            }
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }
    }

    public static enum BoolCapVal {

        /**
         * AUTO
         */
        AUTO("auto"),
        /**
         * FALSE
         */
        FALSE("false"),
        /**
         * TRUE
         */
        TRUE("true");

        /**
         * Parses given capability value. If given value equals ignore-case to string <code>true</code>, constant {@link #TRUE} will be
         * returned. Else if given value equals ignore-case to string <code>auto</code>, constant {@link #AUTO} will be returned. Otherwise
         * {@link #FALSE} will be returned.
         *
         * @param capVal - the string value to parse
         * @return an instance of <code>BoolCapVal</code>: either {@link #TRUE}, {@link #FALSE}, or {@link #AUTO}
         */
        public final static BoolCapVal parseBoolCapVal(final String capVal) {
            if (TRUE.str.equalsIgnoreCase(capVal)) {
                return TRUE;
            } else if (AUTO.str.equalsIgnoreCase(capVal)) {
                return AUTO;
            }
            return FALSE;
        }

        private final String str;

        private BoolCapVal(final String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    public static enum LoginSource {

        /**
         * Login is taken from user.mail kept in storage; e.g. <code>test@foo.bar</code>
         */
        PRIMARY_EMAIL("mail"),
        /**
         * Login is taken from user.imapLogin kept in storage; e.g. <code>test</code>
         */
        USER_IMAPLOGIN("login"),
        /**
         * Login is user's name; e.g. <code>test</code>
         */
        USER_NAME("name");

        /**
         * Parses specified string into a login source.
         *
         * @param loginSourceStr The string to parse to a login source
         * @return An appropriate login source or <code>null</code> if string could not be parsed to a login source
         */
        public static final LoginSource parse(final String loginSourceStr) {
            final LoginSource[] values = LoginSource.values();
            for (final LoginSource loginSource : values) {
                if (loginSource.str.equalsIgnoreCase(loginSourceStr)) {
                    return loginSource;
                }
            }
            return null;
        }

        private final String str;

        private LoginSource(final String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    public static enum PasswordSource {

        /**
         * Password is taken from appropriate property
         */
        GLOBAL("global"),
        /**
         * Password is equal to session password
         */
        SESSION("session");

        /**
         * Parses specified string into a password source.
         *
         * @param passwordSourceStr The string to parse to a password source
         * @return An appropriate password source or <code>null</code> if string could not be parsed to a password source
         */
        public static final PasswordSource parse(final String passwordSourceStr) {
            final PasswordSource[] values = PasswordSource.values();
            for (final PasswordSource passwordSource : values) {
                if (passwordSource.str.equalsIgnoreCase(passwordSourceStr)) {
                    return passwordSource;
                }
            }
            return null;
        }

        private final String str;

        private PasswordSource(final String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    public static enum ServerSource {

        /**
         * Server is taken from appropriate property
         */
        GLOBAL("global"),
        /**
         * Server is taken from user
         */
        USER("user");

        /**
         * Parses specified string into a server source.
         *
         * @param serverSourceStr The string to parse to a server source
         * @return An appropriate server source or <code>null</code> if string could not be parsed to a server source
         */
        public static final ServerSource parse(final String serverSourceStr) {
            final ServerSource[] values = ServerSource.values();
            for (final ServerSource serverSource : values) {
                if (serverSource.str.equalsIgnoreCase(serverSourceStr)) {
                    return serverSource;
                }
            }
            return null;
        }

        private final String str;

        private ServerSource(final String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    protected static final Class<?>[] CONSTRUCTOR_ARGS = new Class[0];

    protected static final Object[] INIT_ARGS = new Object[0];

    private static final String PROPERTY_AUTH_TYPE_MAIL = "com.openexchange.mail.authType";
    private static final String PROPERTY_AUTH_TYPE_TRANSPORT = "com.openexchange.mail.transport.authType";

    /**
     * Gets the user-specific mail configuration.
     *
     * @param <C> The return value type
     * @param mailConfig A newly created {@link MailConfig mail configuration}
     * @param session The session providing needed user data
     * @param accountId The mail account ID
     * @return The user-specific mail configuration
     * @throws OXException If user-specific mail configuration cannot be determined
     */
    public static final <C extends MailConfig> C getConfig(final C mailConfig, final Session session, final int accountId) throws OXException {
        /*
         * Fetch mail account
         */
        int userId = session.getUserId();
        int contextId = session.getContextId();
        MailAccount mailAccount = ServerServiceRegistry.getServize(MailAccountStorageService.class, true).getMailAccount(accountId, userId, contextId);
        mailConfig.account = mailAccount;
        mailConfig.accountId = accountId;
        mailConfig.session = session;
        mailConfig.applyStandardNames(mailAccount);
        fillLoginAndPassword(mailConfig, session, getUser(session).getLoginInfo(), mailAccount, true);
        UrlInfo urlInfo = MailConfig.getMailServerURL(mailAccount, userId, contextId);
        String serverURL = urlInfo.getServerURL();
        if (serverURL == null) {
            if (ServerSource.GLOBAL.equals(MailProperties.getInstance().getMailServerSource(userId, contextId))) {
                throw MailConfigException.create("Property \"com.openexchange.mail.mailServer\" not set in mail properties for user " + userId + " in context " + contextId);
            }
            throw MailConfigException.create(new StringBuilder(64).append("Cannot determine mail server URL for user ").append(userId).append(" in context ").append(contextId).toString());
        }

        mailConfig.parseServerURL(urlInfo);
        mailConfig.doCustomParsing(mailAccount, session);

        return mailConfig;
    }

    /**
     * Gets the user associated with specified session.
     *
     * @param session The session
     * @return The user
     * @throws OXException If user cannot be returned
     */
    protected static User getUser(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser();
        }
        return UserStorage.getInstance().getUser(session.getUserId(), session.getContextId());
    }

    /**
     * Gets the mail login with respect to configured login source.
     *
     * @param mailAccount The mail account used to determine the login
     * @param userLoginInfo The login information of the user
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The mail login of specified user
     * @throws OXException If login cannot be determined
     */
    public static final String getMailLogin(Account mailAccount, String userLoginInfo, int userId, int contextId) throws OXException {
        return saneLogin(getMailLogin0(mailAccount, userLoginInfo, userId, contextId));
    }

    /**
     * Gets the mail login with respect to configured login source.
     *
     * @param mailAccount The mail account used to determine the login
     * @param userLoginInfo The login information of the user
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The mail login of specified user
     * @throws OXException If login cannot be determined
     */
    private static final String getMailLogin0(Account mailAccount, String userLoginInfo, int userId, int contextId) throws OXException {
        if (!mailAccount.isDefaultAccount()) {
            return mailAccount.getLogin();
        }

        // For primary mail account
        String login;
        switch (MailProperties.getInstance().getLoginSource(userId, contextId)) {
            case USER_IMAPLOGIN:
                login = mailAccount.getLogin();
                break;
            case PRIMARY_EMAIL: {
                    String primaryAddress = mailAccount.getPrimaryAddress();
                    try {
                        login = QuotedInternetAddress.toACE(primaryAddress);
                    } catch (AddressException e) {
                        org.slf4j.LoggerFactory.getLogger(MailConfig.class).warn("Login source primary email address \"{}\" could not be converted to ASCII. Using unicode representation.", primaryAddress, e);
                        login = primaryAddress;
                    }
                    break;
                }
            default:
                login = userLoginInfo;
                break;
        }
        if (null == login) {
            throw MailExceptionCode.MISSING_CONNECT_PARAM.create("Login not set. Either an invalid session or property \"com.openexchange.mail.loginSource\" is set incorrectly.");
        }
        return login;
    }

    /**
     * Gets the mail server URL appropriate to configured mail server source.
     *
     * @param mailAccount The mail account
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The appropriate mail server URL or <code>null</code>
     */
    public static final UrlInfo getMailServerURL(MailAccount mailAccount, int userId, int contextId) {
        if (!mailAccount.isDefaultAccount()) {
            return new UrlInfo(mailAccount.generateMailServerURL(), mailAccount.isMailStartTls());
        }
        if (ServerSource.GLOBAL.equals(MailProperties.getInstance().getMailServerSource(userId, contextId))) {
            return new UrlInfo(MailProperties.getInstance().getMailServer(userId, contextId).getUrlString(true), MailProperties.getInstance().isMailStartTls(userId, contextId));
        }
        return new UrlInfo(mailAccount.generateMailServerURL(), mailAccount.isMailStartTls());
    }

    /**
     * Gets the mail server URL appropriate to configured mail server source.
     *
     * @param session The user session
     * @param accountId The account ID
     * @return The appropriate mail server URL or <code>null</code>
     * @throws OXException If mail server URL cannot be returned
     */
    public static final UrlInfo getMailServerURL(final Session session, final int accountId) throws OXException {
        int userId = session.getUserId();
        int contextId = session.getContextId();
        if (MailAccount.DEFAULT_ID == accountId && ServerSource.GLOBAL.equals(MailProperties.getInstance().getMailServerSource(userId, contextId))) {
            return new UrlInfo(MailProperties.getInstance().getMailServer(userId, contextId).getUrlString(true), MailProperties.getInstance().isMailStartTls(userId, contextId));
        }

        MailAccountStorageService storage = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
        return new UrlInfo(storage.getMailAccount(accountId, userId, contextId).generateMailServerURL(), storage.getMailAccount(accountId, userId, contextId).isMailStartTls());
    }

    private static final class UserID {

        final Context context;
        final String pattern;
        final String serverUrl;
        final int userId;
        private final int hash;

        protected UserID(final String pattern, final String serverUrl, int userId, final Context context) {
            super();
            this.pattern = pattern;
            this.serverUrl = serverUrl;
            this.userId = userId;
            this.context = context;

            int prime = 31;
            int result = prime * 1 + ((context == null) ? 0 : context.getContextId());
            result = prime * result + userId;
            result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
            result = prime * result + ((serverUrl == null) ? 0 : serverUrl.hashCode());
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof UserID)) {
                return false;
            }
            final UserID other = (UserID) obj;
            if (userId != other.userId) {
                return false;
            }
            if (context == null) {
                if (other.context != null) {
                    return false;
                }
            } else if (other.context == null || context.getContextId() != other.context.getContextId()) {
                return false;
            }
            if (pattern == null) {
                if (other.pattern != null) {
                    return false;
                }
            } else if (!pattern.equals(other.pattern)) {
                return false;
            }
            if (serverUrl == null) {
                if (other.serverUrl != null) {
                    return false;
                }
            } else if (!serverUrl.equals(other.serverUrl)) {
                return false;
            }
            return true;
        }
    }

    private static final LoadingCache<UserID, int[]> USER_ID_CACHE = CacheBuilder.newBuilder().concurrencyLevel(4).maximumSize(65536 << 1).initialCapacity(8192).expireAfterAccess(30, TimeUnit.MINUTES).build(new CacheLoader<UserID, int[]>() {

        @Override
        public int[] load(UserID userID) throws Exception {
            MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            return forDefaultAccount(userID.pattern, userID.serverUrl, userID.userId, userID.context, storageService);
        }
    });

    /**
     * Resolves the user IDs by specified pattern dependent on configuration's setting for mail login source.
     *
     * @param pattern The pattern
     * @param serverUrl The server URL; e.g. <code>"mail.company.org:143"</code>
     * @param userId The user identifier
     * @param ctx The context
     * @return The user IDs from specified pattern dependent on configuration's setting for mail login source
     * @throws OXException If resolving user by specified pattern fails
     */
    public static int[] getUserIDsByMailLogin(String pattern, boolean isDefaultAccount, String serverUrl, int userId, Context ctx) throws OXException {
        if (isDefaultAccount) {
            UserID userID = new UserID(pattern, serverUrl, userId, ctx);
            boolean remove = true;
            try {
                int[] retval = USER_ID_CACHE.get(userID);
                remove = false;
                return retval;
            } catch (final ExecutionException e) {
                ThreadPools.launderThrowable(e, OXException.class);
            } finally {
                if (remove) {
                    USER_ID_CACHE.invalidate(userID);
                }
            }
        }

        // Find user name by user's IMAP login
        MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
        final MailAccount[] accounts = storageService.resolveLogin(pattern, serverUrl, ctx.getContextId());
        final int[] retval = new int[accounts.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = accounts[i].getUserId();
        }
        return retval;
    }

    /**
     * Resolves the user IDs by specified pattern dependent on configuration's setting for mail login source for default account
     */
    protected static int[] forDefaultAccount(final String pattern, final String serverUrl, final int iUserId, final Context ctx, final MailAccountStorageService storageService) throws OXException {
        LoginSource loginSource = MailProperties.getInstance().getLoginSource(iUserId, ctx.getContextId());
        switch (loginSource) {
        case USER_IMAPLOGIN:
        case PRIMARY_EMAIL:
            final MailAccount[] accounts;
            switch (loginSource) {
            case USER_IMAPLOGIN:
                accounts = storageService.resolveLogin(pattern, ctx.getContextId());
                break;
            case PRIMARY_EMAIL:
                accounts = storageService.resolvePrimaryAddr(pattern, ctx.getContextId());
                break;
            default:
                throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create("Unimplemented mail login source.");
            }
            final TIntSet userIds;
            if (accounts.length == 1) {
                // On ASE some accounts are configured to connect to localhost, some to the full qualified local host name. The socket
                // would then not match. If we only find one then, use it.
                userIds = new TIntHashSet(1);
                userIds.add(accounts[0].getUserId());
            } else {
                userIds = new TIntHashSet(accounts.length);
                for (final MailAccount candidate : accounts) {
                    final String shouldMatch;
                    switch (MailProperties.getInstance().getMailServerSource(iUserId, ctx.getContextId())) {
                    case USER:
                        shouldMatch = toSocketAddrString(candidate.generateMailServerURL(), 143);
                        break;
                    case GLOBAL:
                        {
                            ConfiguredServer server = MailProperties.getInstance().getMailServer(iUserId, ctx.getContextId());
                            shouldMatch = toSocketAddrString(server.getHostName(), server.getPort());
                        }
                        break;
                    default:
                        throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create("Unimplemented mail server source.");
                    }
                    if (serverUrl.equals(shouldMatch)) {
                        userIds.add(candidate.getUserId());
                    }
                }
            }
            // Prefer the default mail account.
            final int size = userIds.size();
            final TIntSet notDefaultAccount = new TIntHashSet(size);
            if (size > 0) {
                final TIntIterator iter = userIds.iterator();
                for (int i = size; i-- > 0;) {
                    final int userId = iter.next();
                    for (final MailAccount candidate : accounts) {
                        if (candidate.getUserId() == userId && !candidate.isDefaultAccount()) {
                            notDefaultAccount.add(userId);
                        }
                    }
                }
            }
            if (notDefaultAccount.size() < size) {
                userIds.removeAll(notDefaultAccount);
            }
            return userIds.toArray();
        case USER_NAME:
            return new int[] { UserStorage.getInstance().getUserId(pattern, ctx) };
        default:
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create("Unimplemented mail login source.");
        }
    }

    /**
     * Parses protocol out of specified server string according to URL specification; e.g. <i>mailprotocol://dev.myhost.com:1234</i>
     *
     * @param server The server string
     * @return An array of {@link String} with length <code>2</code>. The first element is the protocol and the second the server. If no
     *         protocol pattern could be found <code>null</code> is returned; meaning no protocol is present in specified server string.
     */
    public final static String[] parseProtocol(final String server) {
        final int len = server.length();
        char c = '\0';
        for (int i = 0; (i < len) && ((c = server.charAt(i)) != '/'); i++) {
            if (c == ':' && ((c = server.charAt(i + 1)) == '/') && ((c = server.charAt(i + 2)) == '/')) {
                final String s = server.substring(0, i).toLowerCase(Locale.ENGLISH);
                if (isValidProtocol(s)) {
                    int start = i + 1;
                    while (server.charAt(start) == '/') {
                        start++;
                    }
                    return new String[] { s, server.substring(start) };
                }
                break;
            }
        }
        return null;
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                doSaneLogin = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.mail.saneLogin");
            }
        });
    }

    private final static boolean isValidProtocol(final String protocol) {
        final int len = protocol.length();
        if (len < 1) {
            return false;
        }
        char c = protocol.charAt(0);
        if (!Character.isLetter(c)) {
            return false;
        }
        for (int i = 1; i < len; i++) {
            c = protocol.charAt(i);
            if (!Character.isLetterOrDigit(c) && (c != '.') && (c != '+') && (c != '-')) {
                return false;
            }
        }
        return true;
    }

    static volatile Boolean doSaneLogin;
    private static boolean doSaneLogin() {
        Boolean tmp = doSaneLogin;
        if (null == tmp) {
            synchronized (MailConfig.class) {
                tmp = doSaneLogin;
                if (null == tmp) {
                    boolean defaultValue = true;
                    ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Boolean.valueOf(service.getBoolProperty("com.openexchange.mail.saneLogin", defaultValue));
                    doSaneLogin = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    /**
     * Gets the sane (puny-code) representation of passed login in case it appears to be an Internet address.
     *
     * @param login The login
     * @return The sane login
     */
    public static final String saneLogin(final String login) {
        if (false == doSaneLogin()) {
            return login;
        }
        try {
            return IDNA.toACE(login);
        } catch (final Exception e) {
            return login;
        }
    }

    private static final Cache<AuthTypeKey, ImmutableReference<AuthType>> CACHE_AUTH_TYPE = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();

    /**
     * Invalidates the <i>auth type cache</i>.
     */
    public static void invalidateAuthTypeCache() {
        CACHE_AUTH_TYPE.invalidateAll();
    }

    public static AuthType getConfiguredAuthType(boolean forMailAccess, Session session) throws OXException {
        AuthTypeKey key = new AuthTypeKey(forMailAccess, session.getUserId(), session.getContextId());
        ImmutableReference<AuthType> authType = CACHE_AUTH_TYPE.getIfPresent(key);
        if (null == authType) {
            ConfigViewFactory factory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
            if (null == factory) {
                return AuthType.LOGIN;
            }

            authType = doGetConfiguredAuthType(forMailAccess, session, factory);
            CACHE_AUTH_TYPE.put(key, authType);
        }
        return authType.getValue();
    }

    private static ImmutableReference<AuthType> doGetConfiguredAuthType(boolean forMailAccess, Session session, ConfigViewFactory factory) throws OXException {
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());
        String property = forMailAccess ? PROPERTY_AUTH_TYPE_MAIL : PROPERTY_AUTH_TYPE_TRANSPORT;
        String authTypeStr = view.opt(property, String.class, AuthType.LOGIN.getName());
        AuthType authType = AuthType.parse(authTypeStr);
        if (null == authType) {
            throw MailConfigException.create("Invalid or unsupported value configured for property \"" + property + "\": " + authTypeStr);
        }
        return new ImmutableReference<AuthType>(authType);
    }

    /**
     * Fills login and password in specified instance of {@link MailConfig}.
     *
     * @param mailConfig The mail config whose login and password shall be set
     * @param sessionPassword The session password
     * @param account The mail account
     * @param forMailAccess <code>true</code> if credentials are supposed to be set for mail access; otherwise <code>false</code> for mail transport
     * @throws OXException If a configuration error occurs
     */
    protected static final void fillLoginAndPassword(MailConfig mailConfig, Session session, String userLoginInfo, Account account, boolean forMailAccess) throws OXException {
        // Assign login
        {
            String proxyDelimiter = account.isDefaultAccount() ? MailProperties.getInstance().getAuthProxyDelimiter() : null;
            String slogin = session.getLoginName();
            if (proxyDelimiter != null && slogin.contains(proxyDelimiter)) {
                mailConfig.login = saneLogin(slogin);
            } else {
                mailConfig.login = getMailLogin(account, userLoginInfo, session.getUserId(), session.getContextId());
            }
        }

        // Assign password
        if (account.isDefaultAccount()) {
            // First, check the configured authentication type for current user
            AuthType configuredAuthType = getConfiguredAuthType(forMailAccess, session);
            if (AuthType.isOAuthType(configuredAuthType)) {
                // Apparently, OAuth is supposed to be used
                Object obj = session.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN);
                if (obj == null) {
                    throw MailExceptionCode.MISSING_CONNECT_PARAM.create("The session contains no OAuth token.");
                }
                mailConfig.password = obj.toString();
                mailConfig.authType = configuredAuthType;
            } else {
                // Common handling based on configuration
                PasswordSource cur = MailProperties.getInstance().getPasswordSource(session.getUserId(), session.getContextId());
                if (PasswordSource.GLOBAL.equals(cur)) {
                    final String masterPw = MailProperties.getInstance().getMasterPassword(session.getUserId(), session.getContextId());
                    if (masterPw == null) {
                        throw MailConfigException.create("Property \"com.openexchange.mail.masterPassword\" not set");
                    }
                    mailConfig.password = masterPw;
                } else {
                    String sessionPassword = session.getPassword();
                    if (null == sessionPassword) {
                        throw MailExceptionCode.MISSING_CONNECT_PARAM.create("Session password not set. Either an invalid session or master authentication is not enabled (property \"com.openexchange.mail.passwordSource\" is not set to \"global\")");
                    }
                    mailConfig.password = sessionPassword;
                }
            }
        } else {
            CredentialsProviderService credentialsProvider = CredentialsProviderRegistry.getInstance().optCredentialsProviderFor(forMailAccess, account.getId(), session);
            if (null == credentialsProvider) {
                applyPasswordAndAuthType(mailConfig, session, account, forMailAccess);
            } else {
                if (forMailAccess) {
                    if (false == applyCredentials(mailConfig, credentialsProvider.getMailCredentials(account.getId(), session))) {
                        applyPasswordAndAuthType(mailConfig, session, account, forMailAccess);
                    }
                } else {
                    if (false == applyCredentials(mailConfig, credentialsProvider.getTransportCredentials(account.getId(), session))) {
                        applyPasswordAndAuthType(mailConfig, session, account, forMailAccess);
                    }
                }
            }
        }
    }

    private static boolean applyCredentials(MailConfig mailConfig, Credentials credentials) {
        if (null == credentials) {
            return false;
        }

        try {
            String login = credentials.getLogin();
            if (Strings.isEmpty(login)) {
                return false;
            }
            Password pw = credentials.getPassword();
            if (null == pw) {
                return false;
            }
            try {
                mailConfig.login = saneLogin(login);
                mailConfig.password = new String(pw.getPassword());
                AuthType authType = credentials.getAuthType();
                mailConfig.authType = null == authType ? AuthType.LOGIN : authType;
                return true;
            } finally {
                Streams.close(pw);
            }
        } finally {
            Streams.close(credentials);
        }
    }

    private static void applyPasswordAndAuthType(MailConfig mailConfig, Session session, Account account, boolean forMailAccess) throws OXException {
        AuthInfo authInfo = determinePasswordAndAuthType(mailConfig.login, session, account, forMailAccess);
        mailConfig.password = authInfo.getPassword();
        mailConfig.authType = authInfo.getAuthType();
        mailConfig.oauthAccountId = authInfo.getOauthAccountId();
    }

    /**
     * Determines given account's password and authentication type.
     *
     * @param login The login to assume
     * @param session The session to check by
     * @param account The account
     * @param forMailAccess <code>true</code> to resolve for mail access; otherwise <code>false</code> for mail transport
     * @return The authentication information
     * @throws OXException If authentication information cannot be resolved
     */
    public static AuthInfo determinePasswordAndAuthType(String login, Session session, Account account, boolean forMailAccess) throws OXException {
        // This method is only called for external accounts
        int oAuthAccontId = assumeOauthFor(account, forMailAccess);
        if (oAuthAccontId >= 0) {
            // Do the OAuth dance...
            MailOAuthService mailOAuthService = ServerServiceRegistry.getInstance().getService(MailOAuthService.class);
            TokenInfo tokenInfo = mailOAuthService.getTokenFor(oAuthAccontId, session);
            return new AuthInfo(login, tokenInfo.getToken(), AuthType.parse(tokenInfo.getAuthMechanism()), oAuthAccontId);
        }

        String mailAccountPassword = account.getPassword();
        if (null == mailAccountPassword || mailAccountPassword.length() == 0) {
            // Advertise empty string
            return new AuthInfo(login, "", AuthType.LOGIN, -1);
        }

        // Mail account's password
        String server = forMailAccess ? ((MailAccount) account).getMailServer() : account.getTransportServer();
        String password = MailPasswordUtil.decrypt(mailAccountPassword, session, account.getId(), account.getLogin(), server);
        return new AuthInfo(login, password, AuthType.LOGIN, -1);
    }

    /**
     * Checks whether XOAUTH2 authentication is assumed for specified account.
     *
     * @param account The account to check
     * @param forMailAccess <code>true</code> to resolve for mail access; otherwise <code>false</code> for mail transport
     * @return The verified identifier of the associated OAuth account or <code>-1</code>
     */
    protected static int assumeOauthFor(Account account, boolean forMailAccess) {
        if (forMailAccess) {
            MailAccount mailAccount = (MailAccount) account;
            if (false == mailAccount.isMailOAuthAble()) {
                return -1;
            }
            return (mailAccount.getMailOAuthId() >= 0 ? mailAccount.getMailOAuthId() : -1);
        }

        if (false == account.isTransportOAuthAble()) {
            return -1;
        }
        return (account.getTransportOAuthId() >= 0 ? account.getTransportOAuthId() : -1);
    }

    private static final int LENGTH = 6;

    /*-
     * Member section
     */

    protected AuthType authType;
    protected Map<String, Object> authProps;
    protected int accountId;
    protected int oauthAccountId;
    protected Session session;
    protected String login;
    protected String password;
    protected boolean requireTls;
    protected Account account;
    protected final String[] standardNames;
    protected final String[] standardFullNames;

    /**
     * Initializes a new {@link MailConfig}
     */
    protected MailConfig() {
        super();
        oauthAccountId = -1;
        requireTls = false;
        authProps = null;
        authType = AuthType.LOGIN;
        standardFullNames = new String[LENGTH];
        standardNames = new String[LENGTH];
    }

    /**
     * Gets the account currently associated with this instance
     *
     * @return The account or <code>null</code>
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Gets the authentication type.
     *
     * @return The authentication type
     */
    public AuthType getAuthType() {
        return authType;
    }

    /**
     * Gets the optional identifier of the associated OAuth account
     *
     * @return The identifier of the associated OAuth account or <code>-1</code>
     */
    public int getOAuthAccountId() {
        return oauthAccountId;
    }

    /**
     * Gets the authentication properties.
     *
     * @return The authentication properties or <code>null</code>
     */
    public Map<String, Object> getAuthProps() {
        return authProps;
    }

    /**
     * Gets the standard names.
     *
     * @return The standard names
     */
    public String[] getStandardNames() {
        final String[] ret = new String[LENGTH];
        System.arraycopy(standardNames, 0, ret, 0, LENGTH);
        return ret;
    }

    /**
     * Gets the standard full names.
     *
     * @return The standard full names
     */
    public String[] getStandardFullNames() {
        final String[] ret = new String[LENGTH];
        System.arraycopy(standardFullNames, 0, ret, 0, LENGTH);
        return ret;
    }

    /**
     * Applies folder name information from given mail account
     *
     * @param mailAccount The mail account
     */
    public void applyStandardNames(MailAccount mailAccount) {
        applyStandardNames(mailAccount, false);
    }

    /**
     * Applies folder name information from given mail account
     *
     * @param mailAccount The mail account
     * @param force <code>true</code> to enforce setting folder name information from given mail account; otherwise <code>false</code>
     */
    public void applyStandardNames(MailAccount mailAccount, boolean force) {
        if (null == mailAccount) {
            return;
        }
        put(StorageUtility.INDEX_CONFIRMED_HAM, mailAccount.getConfirmedHam(), standardNames, force);
        put(StorageUtility.INDEX_CONFIRMED_SPAM, mailAccount.getConfirmedSpam(), standardNames, force);
        put(StorageUtility.INDEX_DRAFTS, mailAccount.getDrafts(), standardNames, force);
        put(StorageUtility.INDEX_SENT, mailAccount.getSent(), standardNames, force);
        put(StorageUtility.INDEX_SPAM, mailAccount.getSpam(), standardNames, force);
        put(StorageUtility.INDEX_TRASH, mailAccount.getTrash(), standardNames, force);

        put(StorageUtility.INDEX_CONFIRMED_HAM, mailAccount.getConfirmedHamFullname(), standardFullNames, force);
        put(StorageUtility.INDEX_CONFIRMED_SPAM, mailAccount.getConfirmedSpamFullname(), standardFullNames, force);
        put(StorageUtility.INDEX_DRAFTS, mailAccount.getDraftsFullname(), standardFullNames, force);
        put(StorageUtility.INDEX_SENT, mailAccount.getSentFullname(), standardFullNames, force);
        put(StorageUtility.INDEX_SPAM, mailAccount.getSpamFullname(), standardFullNames, force);
        put(StorageUtility.INDEX_TRASH, mailAccount.getTrashFullname(), standardFullNames, force);
    }

    private static void put(int index, String value, String[] arr, boolean force) {
        if (!force && Strings.isEmpty(value)) {
            return;
        }
        arr[index] = null == value ? null : MailFolderUtility.prepareMailFolderParam(value).getFullname();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        final MailConfig other = (MailConfig) obj;
        if (login == null) {
            if (other.login != null) {
                return false;
            }
        } else if (!login.equals(other.login)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (getPort() != other.getPort()) {
            return false;
        }
        if (getServer() == null) {
            if (other.getServer() != null) {
                return false;
            }
        } else if (!getServer().equals(other.getServer())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("{ MailConfig [accountId=").append(accountId).append(", ");
        if (login != null) {
            builder.append("login=").append(login).append(", ");
        }
        if (password != null) {
            // builder.append("password=").append(password).append(", ");
        }
        builder.append("getPort()=").append(getPort()).append(", ");
        if (getServer() != null) {
            builder.append("getServer()=").append(getServer()).append(", ");
        }
        builder.append("isSecure()=").append(isSecure()).append("] }");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + (getPort());
        final String server = getServer();
        result = prime * result + ((server == null) ? 0 : server.hashCode());
        return result;
    }

    /**
     * Checks if STARTTLS is required in case {@link #isSecure()} returns <code>false</code>
     *
     * @return <code>true</code> if STARTTLS is required; otherwise <code>false</code>
     */
    public boolean isRequireTls() {
        return requireTls;
    }

    /**
     * Sets whether STARTTLS is required in case {@link #isSecure()} returns <code>false</code>
     *
     * @param requireTls <code>true</code> if STARTTLS is required; otherwise <code>false</code>
     */
    public void setRequireTls(boolean requireTls) {
        this.requireTls = requireTls;
    }

    /**
     * Gets the account ID.
     *
     * @return The account ID
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the session.
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the login.
     *
     * @return the login
     */
    public final String getLogin() {
        return login;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public final String getPassword() {
        return password;
    }

    /**
     * Sets the account ID (externally).
     *
     * @param accountId The account ID
     */
    public void setAccountId(final int accountId) {
        this.accountId = accountId;
    }

    /**
     * Sets the session
     *
     * @param session The session
     */
    public void setSession(final Session session) {
        this.session = session;
    }

    /**
     * Sets the login (externally).
     *
     * @param login The login
     */
    public void setLogin(final String login) {
        this.login = saneLogin(login);
    }

    /**
     * Sets the password (externally).
     *
     * @param password The password
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Sets the authentication type.
     *
     * @param authType The authentication type to set
     */
    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    /**
     * Performs optional custom parsing.
     * <p>
     * Returns <code>false</code> by default.
     *
     * @param account The associated mail account
     * @param session The user's session
     * @return <code>true</code> if custom parsing has been performed; otherwise <code>false</code>
     * @throws OXException If custom parsing fails
     */
    protected boolean doCustomParsing(final Account account, final Session session) throws OXException {
        return false;
    }

    /**
     * Gets the mail system's capabilities
     *
     * @return The mail system's capabilities
     */
    public abstract MailCapabilities getCapabilities();

    /**
     * Gets the optional port of the server.
     *
     * @return The optional port of the server obtained via {@link #getServer()} or <code>-1</code> if no port needed.
     */
    public abstract int getPort();

    /**
     * Gets the host name or IP address of the server.
     *
     * @return The host name or IP address of the server.
     */
    public abstract String getServer();

    /**
     * Checks if a secure connection shall be established.
     *
     * @return <code>true</code> if a secure connection shall be established; otherwise <code>false</code>
     */
    public abstract boolean isSecure();

    /**
     * Sets the port (externally).
     *
     * @param port The port
     */
    public abstract void setPort(int port);

    /**
     * Sets (externally) whether a secure connection should be established or not.
     *
     * @param secure <code>true</code> if a secure connection should be established; otherwise <code>false</code>
     */
    public abstract void setSecure(boolean secure);

    /**
     * Sets the host name or IP address of the server (externally).
     *
     * @param server The host name or IP address of the server
     */
    public abstract void setServer(String server);

    /**
     * Gets the mail properties for this mail configuration.
     *
     * @return The mail properties for this mail configuration
     */
    public abstract IMailProperties getMailProperties();

    /**
     * Sets the mail properties for this mail configuration.
     *
     * @param mailProperties The mail properties for this mail configuration
     */
    public abstract void setMailProperties(IMailProperties mailProperties);

    /**
     * Parses given server URL which is then accessible through {@link #getServer()} and optional {@link #getPort()}.
     * <p>
     * The implementation is supposed to use {@link #parseProtocol(String)} to determine the protocol.
     * <p>
     * Moreover this method should check if a secure connection shall be established dependent on URL's protocol. The result is then
     * accessible via {@link #isSecure()}.
     *
     * @param serverURL The server URL of the form:<br>
     *            (&lt;protocol&gt;://)?&lt;host&gt;(:&lt;port&gt;)?
     * @throws OXException If server URL cannot be parsed
     */
    protected abstract void parseServerURL(UrlInfo urlInfo) throws OXException;
}
