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

package com.openexchange.imap;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Provider;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.idn.IDNA;
import javax.security.auth.Subject;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.imap.acl.ACLExtension;
import com.openexchange.imap.acl.ACLExtensionInit;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.ListLsubEntry;
import com.openexchange.imap.cache.MBoxEnabledCache;
import com.openexchange.imap.cache.RootSubfoldersEnabledCache;
import com.openexchange.imap.config.IIMAPProperties;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.imap.config.IMAPReloadable;
import com.openexchange.imap.config.IMAPSessionProperties;
import com.openexchange.imap.config.MailAccountIMAPProperties;
import com.openexchange.imap.converters.IMAPFolderConverter;
import com.openexchange.imap.entity2acl.Entity2ACLInit;
import com.openexchange.imap.ping.IMAPCapabilityAndGreetingCache;
import com.openexchange.imap.services.Services;
import com.openexchange.imap.storecache.IMAPStoreCache;
import com.openexchange.imap.storecache.IMAPStoreContainer;
import com.openexchange.imap.util.HostAndPort;
import com.openexchange.imap.util.HostAndPortAndCredentials;
import com.openexchange.imap.util.StampAndOXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.log.audit.AuditLogService;
import com.openexchange.log.audit.DefaultAttribute;
import com.openexchange.log.audit.DefaultAttribute.Name;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.api.AuthType;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.IMailStoreAware;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mail.cache.IMailAccessCache;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.MimeSessionPropertyNames;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.exception.SSLExceptionCode;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.session.Sessions;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.sun.mail.iap.ConnectQuotaExceededException;
import com.sun.mail.iap.StarttlsRequiredException;
import com.sun.mail.imap.GreetingListener;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.JavaIMAPStore;

/**
 * {@link IMAPAccess} - Establishes an IMAP access and provides access to storages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPAccess extends MailAccess<IMAPFolderStorage, IMAPMessageStorage> implements IMailStoreAware {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -7510487764376433468L;

    /**
     * The logger instance for {@link IMAPAccess} class.
     */
    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPAccess.class);

    /**
     * The max. temporary-down value; 5 Minutes.
     */
    private static final long MAX_TEMP_DOWN = 300000L;

    private static final String KERBEROS_SESSION_SUBJECT = "kerberosSubject";

    /**
     * The flag indicating whether to use IMAPStoreCache.
     */
    private static final AtomicBoolean USE_IMAP_STORE_CACHE = new AtomicBoolean(true);

    /**
     * Remembers timed out servers for {@link IIMAPProperties#getImapTemporaryDown()} milliseconds. Any further attempts to connect to such
     * a server-port-pair will throw an appropriate exception.
     */
    private static volatile Map<HostAndPort, Long> timedOutServers;

    /**
     * Remembers auth-failed servers for {@link IIMAPProperties#getImapTemporaryDown()} milliseconds. Any further attempts to connect to such
     * a server-port-pair will throw an appropriate exception.
     */
    private static volatile Map<HostAndPortAndCredentials, StampAndOXException> authFailedServers;

    /**
     * Gets the timedOutServers
     *
     * @return The timedOutServers
     */
    public static Map<HostAndPort, Long> getTimedOutServers() {
        return timedOutServers;
    }

    /**
     * Remembers whether a certain IMAP server supports the ACL extension.
     */
    private static volatile ConcurrentMap<String, Boolean> aclCapableServers;

    /**
     * The scheduled timer task to clean-up maps.
     */
    private static volatile ScheduledTimerTask cleanUpTimerTask;

    private static volatile Boolean checkConnectivityIfPolled;

    private static boolean checkConnectivityIfPolled() {
        Boolean b = checkConnectivityIfPolled;
        if (null == b) {
            synchronized (IMAPAccess.class) {
                b = checkConnectivityIfPolled;
                if (null == b) {
                    boolean defaultValue = false;
                    ConfigurationService configService = Services.optService(ConfigurationService.class);
                    if (null == configService) {
                        return defaultValue;
                    }
                    b = Boolean.valueOf(configService.getBoolProperty("com.openexchange.imap.storecache.checkConnectivityIfPolled", defaultValue));
                    checkConnectivityIfPolled = b;
                }
            }
        }
        return b.booleanValue();
    }

    /*-
     * Member section
     */

    /**
     * The folder storage.
     */
    private transient IMAPFolderStorage folderStorage;

    /**
     * The message storage.
     */
    private transient IMAPMessageStorage messageStorage;

    /**
     * The mail logic tools.
     */
    private transient MailLogicTools logicTools;

    /**
     * The IMAP store.
     */
    private transient IMAPStore imapStore;

    /**
     * The IMAP session.
     */
    private transient javax.mail.Session imapSession;

    /**
     * The Kerberos subject.
     */
    private transient Subject kerberosSubject;

    /**
     * The IMAP protocol.
     */
    private final Protocol protocol;

    /**
     * The max. connection count.
     */
    private int maxCount;

    /**
     * The connected flag.
     */
    private boolean connected;

    /**
     * The server's host name.
     */
    private String server;

    /**
     * The server's port.
     */
    private int port;

    /**
     * The user's login name.
     */
    private String login;

    /**
     * The user's password.
     */
    private String password;

    /**
     * The client IP.
     */
    private String clientIp;

    /**
     * The IMAP configuration.
     */
    private volatile IMAPConfig imapConfig;

    /**
     * A simple cache for max. count values per server.
     */
    private static volatile ConcurrentMap<String, Integer> maxCountCache;

    static {
        IMAPReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(final ConfigurationService configService) {
                final ConcurrentMap<String, Integer> m = maxCountCache;
                if (null != m) {
                    m.clear();
                }
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.imap.maxNumConnections");
            }
        });
    }

    /**
     * Initializes a new {@link IMAPAccess IMAP access} for default IMAP account.
     *
     * @param session The session providing needed user data
     */
    protected IMAPAccess(final Session session) {
        super(session);
        setMailProperties((Properties) System.getProperties().clone());
        maxCount = -1;
        protocol = IMAPProvider.PROTOCOL_IMAP;
    }

    /**
     * Initializes a new {@link IMAPAccess IMAP access}.
     *
     * @param session The session providing needed user data
     * @param accountId The account ID
     */
    protected IMAPAccess(final Session session, final int accountId) {
        super(session, accountId);
        setMailProperties((Properties) System.getProperties().clone());
        maxCount = -1;
        protocol = IMAPProvider.PROTOCOL_IMAP;
    }

    /**
     * Gets the max. count for this IMAP access.
     *
     * @return The max. count
     * @throws OXException If an exception occurs
     */
    protected int getMaxCount() throws OXException {
        final ConcurrentMap<String, Integer> cache = maxCountCache;
        if (null == cache) {
            return protocol.getMaxCount(server, MailAccount.DEFAULT_ID == accountId);
        }
        Integer maxCount = cache.get(server);
        if (null == maxCount) {
            final Integer i = Integer.valueOf(protocol.getMaxCount(server, MailAccount.DEFAULT_ID == accountId));
            maxCount = cache.putIfAbsent(server, i);
            if (null == maxCount) {
                maxCount = i;
            }
        }
        return maxCount.intValue();
    }

    @Override
    public boolean isStoreSupported() throws OXException {
        return true;
    }

    @Override
    public Store getStore() throws OXException {
        if (!connected) {
            throw IMAPException.create(IMAPException.Code.NOT_CONNECTED, getMailConfig(), session, new Object[0]);
        }
        IMAPStore imapStore = this.imapStore;
        if (null == imapStore) {
            throw IMAPException.create(IMAPException.Code.NOT_CONNECTED, getMailConfig(), session, new Object[0]);
        }
        return imapStore;
    }

    /**
     * Gets the underlying IMAP store.
     *
     * @return The IMAP store or <code>null</code> if this IMAP access is not connected
     */
    public IMAPStore getIMAPStore() {
        return imapStore;
    }

    /**
     * Checks if Kerberos authentication is supposed to be performed.
     *
     * @return <code>true</code> for Kerberos authentication; otherwise <code>false</code>
     */
    private boolean isKerberosAuth() {
        return MailAccount.DEFAULT_ID == accountId && null != kerberosSubject;
    }

    private void reset() {
        super.resetFields();
        folderStorage = null;
        messageStorage = null;
        logicTools = null;
        imapStore = null;
        imapSession = null;
        connected = false;
        kerberosSubject = null;
    }

    @Override
    public void releaseResources() {
        /*-
         *
         * Don't need to close when cached!
         *
        if (folderStorage != null) {
            try {
                folderStorage.releaseResources();
            } catch (final OXException e) {
                LOG.error("Error while closing IMAP folder storage: {}", e.getMessage()).toString(), e));
            } finally {
                folderStorage = null;
            }
        }
        if (messageStorage != null) {
            try {
                messageStorage.releaseResources();
            } catch (final OXException e) {
                LOG.error("Error while closing IMAP message storage: {}", e.getMessage()).toString(), e));
            } finally {
                messageStorage = null;

            }
        }
        if (logicTools != null) {
            logicTools = null;
        }
         */
    }

    @Override
    protected void closeInternal() {
        try {
            final IMAPFolderStorage folderStorage = this.folderStorage;
            if (folderStorage != null) {
                try {
                    folderStorage.releaseResources();
                } catch (final OXException e) {
                    LOG.error("Error while closing IMAP folder storage,", e);
                }
            }
            final IMAPMessageStorage messageStorage = this.messageStorage;
            if (null != messageStorage) {
                try {
                    messageStorage.releaseResources();
                } catch (final OXException e) {
                    LOG.debug("Error while closing IMAP message storage.", e);
                }
            }
            final IMAPStore imapStore = this.imapStore;
            if (imapStore != null) {
                if (useIMAPStoreCache()) {
                    final IMAPStoreCache imapStoreCache = IMAPStoreCache.getInstance();
                    if (null == imapStoreCache) {
                        closeSafely(imapStore);
                    } else if (imapStore.isConnectedUnsafe()) {
                        imapStoreCache.returnIMAPStore(imapStore, accountId, server, port, login, session);
                    } else {
                        // Not null AND not connected...?
                        closeSafely(imapStore);
                    }
                } else {
                    closeSafely(imapStore);
                }
                // Drop associated IMAPConfig instance
                final IMAPConfig ic = getIMAPConfig();
                if (null != ic) {
                    ic.dropImapStore();
                }
                this.imapStore = null;
            }
        } finally {
            reset();
        }
    }

    @Override
    protected MailConfig createNewMailConfig() {
        return new IMAPConfig(accountId);
    }

    @Override
    public MailConfig getMailConfig() throws OXException {
        IMAPConfig tmp = imapConfig;
        if (null == tmp) {
            synchronized (this) {
                tmp = imapConfig;
                if (null == tmp) {
                    imapConfig = tmp = (IMAPConfig) super.getMailConfig();
                }
            }
        }
        return tmp;
    }

    /**
     * Gets the IMAP configuration.
     *
     * @return The IMAP configuration
     */
    public IMAPConfig getIMAPConfig() {
        final IMAPConfig tmp = imapConfig;
        if (null == tmp) {
            try {
                return (IMAPConfig) getMailConfig();
            } catch (final OXException e) {
                // Cannot occur
                return null;
            }
        }
        return tmp;
    }

    @Override
    protected boolean supports(AuthType authType) throws OXException {
        switch (authType) {
            case LOGIN:
                return true;
            case OAUTH:
                try {
                    IMAPConfig imapConfig = getIMAPConfig();
                    return IMAPCapabilityAndGreetingCache.getCapabilities(new HostAndPort(IDNA.toASCII(imapConfig.getServer()), imapConfig.getPort()), imapConfig.isSecure(), imapConfig.getIMAPProperties()).containsKey("AUTH=XOAUTH2");
                } catch (IOException e) {
                    throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
                }
            case OAUTHBEARER:
                try {
                    IMAPConfig imapConfig = getIMAPConfig();
                    return IMAPCapabilityAndGreetingCache.getCapabilities(new HostAndPort(IDNA.toASCII(imapConfig.getServer()), imapConfig.getPort()), imapConfig.isSecure(), imapConfig.getIMAPProperties()).containsKey("AUTH=OAUTHBEARER");
                } catch (IOException e) {
                    throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
                }
            default:
                return false;
        }
    }

    @Override
    public int getUnreadMessagesCount(final String fullname) throws OXException {
        if (!isConnected()) {
            connect(false);
        }
        /*
         * Check for root folder
         */
        if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
            return 0;
        }
        try {
            /*
             * Obtain IMAP folder
             */
            final IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(fullname);
            final IMAPConfig imapConfig = getIMAPConfig();
            final ListLsubEntry listEntry = ListLsubCache.getCachedLISTEntry(fullname, accountId, imapFolder, session, imapConfig.getIMAPProperties().isIgnoreSubscription());
            final boolean exists = "INBOX".equals(fullname) || (listEntry.exists());
            if (!exists) {
                throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullname);
            }
            final Set<String> attrs = listEntry.getAttributes();
            if (null != attrs) {
                for (final String attribute : attrs) {
                    if ("\\NonExistent".equalsIgnoreCase(attribute)) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullname);
                    }
                }
            }
            final int retval;
            /*
             * Selectable?
             */
            if (listEntry.canOpen()) {
                /*
                 * Check read access
                 */
                final ACLExtension aclExtension = imapConfig.getACLExtension();
                if (!aclExtension.aclSupport() || aclExtension.canRead(IMAPFolderConverter.getOwnRights(imapFolder, session, imapConfig))) {
                    retval = IMAPFolderConverter.getUnreadCount(imapFolder);
                } else {
                    // ACL support AND no read access
                    retval = -1;
                }
            } else {
                retval = -1;
            }
            return retval;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, getMailConfig(), session);
        }
    }

    @Override
    public boolean ping() throws OXException {
        final IMAPConfig config = getIMAPConfig();
        checkFieldsBeforeConnect(config);
        try {
            /*
             * Try to connect to IMAP server
             */
            final IIMAPProperties imapConfProps = (IIMAPProperties) config.getMailProperties();
            String tmpPass = authEncode(getMailConfig().getPassword(), imapConfProps.getImapAuthEnc());
            String login = authEncode(config.getLogin(), imapConfProps.getImapAuthEnc());
            /*
             * Get properties
             */
            final Properties imapProps = IMAPSessionProperties.getDefaultSessionProperties();
            if ((null != getMailProperties()) && !getMailProperties().isEmpty()) {
                imapProps.putAll(getMailProperties());
            }
            /*
             * Get parameterized IMAP session
             */
            javax.mail.Session imapSession;
            {
                boolean forceSecure = imapConfig.isRequireTls();
                imapSession = setConnectProperties(config, imapConfProps.getImapTimeout(), imapConfProps.getImapConnectionTimeout(), imapProps, JavaIMAPStore.class, forceSecure, session.getUserId(), session.getContextId());
            }
            /*
             * Check if debug should be enabled
             */
            if (Boolean.parseBoolean(imapSession.getProperty(MimeSessionPropertyNames.PROP_MAIL_DEBUG))) {
                imapSession.setDebug(true);
                imapSession.setDebugOut(System.err);
            }
            IMAPStore imapStore = null;
            try {
                final boolean[] preAuthStartTlsCap = new boolean[1];
                preAuthStartTlsCap[0] = false;
                /*
                 * Get connected store
                 */
                imapStore = newConnectedImapStore(imapSession, IDNA.toASCII(config.getServer()), config.getPort(), login, tmpPass, -1, preAuthStartTlsCap, true);
                /*
                 * Add warning if non-secure
                 */
                try {
                    if (!config.isSecure() && !imapStore.hasCapability("STARTTLS") && !preAuthStartTlsCap[0]) {
                        if ("create".equals(session.getParameter("mail-account.validate.type"))) {
                            warnings.add(MailExceptionCode.NON_SECURE_CREATION.create());
                        } else {
                            warnings.add(MailExceptionCode.NON_SECURE_WARNING.create());
                        }
                    }
                } catch (final MessagingException e) {
                    // Ignore
                }
            } catch (final AuthenticationFailedException e) {
                warnings.add(MailExceptionCode.PING_FAILED_AUTH.create(e, config.getServer(), config.getLogin()));
                throw MimeMailException.handleMessagingException(e, config, session);
            } catch (final MessagingException e) {
                if (MimeMailException.isSSLHandshakeException(e)) {
                    OXException oxe = SSLExceptionCode.UNTRUSTED_CERTIFICATE.create(e.getCause(), config.getServer());
                    warnings.add(oxe);
                    throw oxe;
                }

                Exception cause = e.getNextException();
                if (com.sun.mail.iap.ConnectionException.class.isInstance(cause)) {
                    OXException oxe = MimeMailException.handleMessagingException(e, config, session);
                    warnings.add(oxe);
                    throw oxe;
                } else if (java.net.SocketException.class.isInstance(cause)) {
                    OXException oxe = MimeMailException.handleMessagingException(e, config, session);
                    warnings.add(oxe);
                    throw oxe;
                } else if (StarttlsRequiredException.class.isInstance(cause)) {
                    OXException oxe = MailExceptionCode.NON_SECURE_DENIED.create(config.getServer());
                    warnings.add(oxe);
                    throw oxe;
                }
                warnings.add(MailExceptionCode.PING_FAILED.create(e, config.getServer(), config.getLogin(), e.getMessage()));
                throw MimeMailException.handleMessagingException(e, config, session);
            } finally {
                if (null != imapStore) {
                    try {
                        imapStore.close();
                    } catch (final MessagingException e) {
                        LOG.warn("", e);
                    }
                }
            }
            return true;
        } catch (final OXException e) {
            LOG.debug("Ping to IMAP server \"{}\" failed", config.getServer(), e);
            return false;
        }
    }

    @Override
    protected void connectInternal() throws OXException {
        if (connected) {
            return;
        }
        final IMAPConfig config = getIMAPConfig();
        final Session s = config.getSession();
        this.kerberosSubject = (Subject) s.getParameter(KERBEROS_SESSION_SUBJECT);
        try {
            final IIMAPProperties imapConfProps = (IIMAPProperties) config.getMailProperties();
            final boolean tmpDownEnabled = (imapConfProps.getImapTemporaryDown() > 0);
            if (tmpDownEnabled) {
                /*
                 * Check if IMAP server is marked as being (temporary) down since connecting to it failed before
                 */
                checkTemporaryDown(imapConfProps);
            }
            String encodedPassword = authEncode(config.getPassword(), imapConfProps.getImapAuthEnc());
            boolean certainPassword = false;
            if (certainPassword) {
                encodedPassword = "secret";
            }
            String user = config.getLogin();
            String proxyUser = null;
            boolean isProxyAuth = false;
            {
                String proxyDelimiter = MailAccount.DEFAULT_ID == accountId ? MailProperties.getInstance().getAuthProxyDelimiter() : null;
                if (proxyDelimiter != null) {
                    int pos = user.indexOf(proxyDelimiter);
                    if (pos >= 0) {
                        isProxyAuth = true;
                        proxyUser = user.substring(0, pos);
                        user = user.substring(pos + proxyDelimiter.length());
                    }
                }
            }
            user = authEncode(user, imapConfProps.getImapAuthEnc());
            proxyUser = authEncode(proxyUser, imapConfProps.getImapAuthEnc());
            /*
             * Get properties
             */
            final Properties imapProps = IMAPSessionProperties.getDefaultSessionProperties();
            if ((null != getMailProperties()) && !getMailProperties().isEmpty()) {
                imapProps.putAll(getMailProperties());
            }
            /*
             * Kerberos and/or proxy authentication
             */
            final boolean kerberosAuth = isKerberosAuth();
            if (kerberosAuth || isProxyAuth) {
                imapProps.put("mail.imap.sasl.enable", "true");
                imapProps.put("mail.imap.sasl.authorizationid", user);
                if (kerberosAuth) {
                    imapProps.put("mail.imap.sasl.mechanisms", "GSSAPI");
                    imapProps.put("mail.imap.sasl.kerberosSubject", kerberosSubject);
                } else {
                    imapProps.put("mail.imap.sasl.mechanisms", "PLAIN");
                }
            }
            /*
             * Get parameterized IMAP session
             */
            {
                final Class<? extends IMAPStore> clazz = useIMAPStoreCache() ? IMAPStoreCache.getInstance().getStoreClass() : JavaIMAPStore.class;
                boolean forceSecure = accountId > 0 && imapConfig.isRequireTls();
                imapSession = setConnectProperties(config, imapConfProps.getImapTimeout(), imapConfProps.getImapConnectionTimeout(), imapProps, clazz, forceSecure, session.getUserId(), session.getContextId());
            }
            /*
             * Check if debug should be enabled
             */
            final boolean certainUser = false;
            if (certainUser || Boolean.parseBoolean(imapSession.getProperty(MimeSessionPropertyNames.PROP_MAIL_DEBUG))) {
                imapSession.setDebug(true);
                imapSession.setDebugOut(System.out);
            }
            /*
             * Check if client IP address should be propagated
             */
            String clientIp = null;
            if (imapConfProps.isPropagateClientIPAddress() && isPropagateAccount(imapConfProps)) {
                final String ip = session.getLocalIp();
                if (!com.openexchange.java.Strings.isEmpty(ip)) {
                    clientIp = ip;
                }
            }
            /*
             * Get connected store
             */
            this.server = IDNA.toASCII(config.getServer());
            this.port = config.getPort();
            this.login = isProxyAuth ? proxyUser : user;
            this.password = encodedPassword;
            /*
             * Check for already failed authentication
             */
            checkAuthFailed(this.login, this.password, imapConfProps);
            this.clientIp = clientIp;
            maxCount = getMaxCount();
            try {
                imapStore = connectIMAPStore(maxCount);
            } catch (final AuthenticationFailedException e) {
                if (accountId != MailAccount.DEFAULT_ID) {
                    AbstractTask<Void> task = new AbstractTask<Void>() {

                        @Override
                        public Void call() throws Exception {
                            MailAccountStorageService mass = Services.optService(MailAccountStorageService.class);
                            if (null != mass) {
                                mass.incrementFailedMailAuthCount(accountId, session.getUserId(), session.getContextId(), null);
                            }
                            return null;
                        }
                    };
                    ThreadPools.getThreadPool().submit(task);
                }
                OXException oxe = MimeMailException.handleMessagingException(e, config, session);
                if (imapConfProps.getImapFailedAuthTimeout() > 0) {
                    Map<HostAndPortAndCredentials, StampAndOXException> map = authFailedServers;
                    if (null != map) {
                        map.put(new HostAndPortAndCredentials(this.login, this.password, this.server, this.port), new StampAndOXException(oxe, System.currentTimeMillis()));
                    }
                }
                throw oxe;
            } catch (final MessagingException e) {
                /*
                 * Check for a SocketTimeoutException
                 */
                if (tmpDownEnabled && MimeMailException.isTimeoutException(e)) {
                    /*
                     * Remember a timed-out IMAP server on connect attempt
                     */
                    final Map<HostAndPort, Long> map = timedOutServers;
                    if (null != map) {
                        map.put(newHostAndPort(config), Long.valueOf(System.currentTimeMillis()));
                    }
                }
                if (MimeMailException.isSSLHandshakeException(e)) {
                    throw SSLExceptionCode.UNTRUSTED_CERTIFICATE.create(e.getCause(), server);
                }
                {
                    Exception next = e.getNextException();
                    if (StarttlsRequiredException.class.isInstance(next)) {
                        throw MailExceptionCode.NON_SECURE_DENIED.create(server);
                    }
                }
                throw e;
            }
            this.connected = true;
            /*
             * Add folder listener
             */
            // imapStore.addFolderListener(new ListLsubCacheFolderListener(accountId, session));
            /*
             * Add server's capabilities
             */
            config.initializeCapabilities(imapStore, session);
            /*
             * Special check for ACLs
             */
            if (config.isSupportsACLs()) {
                String key = new StringBuilder(server).append('@').append(port).toString();
                ConcurrentMap<String, Boolean> aclCapableServers = IMAPAccess.aclCapableServers;
                Boolean b = aclCapableServers.get(key);
                if (null == b) {
                    Lock lock = Sessions.optLock(session);
                    lock.lock();
                    try {
                        b = aclCapableServers.get(key);
                        if (null == b) {
                            Boolean nb;
                            IMAPFolder dummy = (IMAPFolder) imapStore.getFolder("INBOX");
                            try {
                                dummy.myRights();
                                nb = Boolean.TRUE;
                            } catch (MessagingException e) {
                                // MessagingException - If the server doesn't support the ACL extension
                                nb = Boolean.FALSE;
                            }
                            b = aclCapableServers.putIfAbsent(key, nb);
                            if (null == b) {
                                b = nb;
                            }
                        }
                    } finally {
                        lock.unlock();
                    }
                }
                if (!b.booleanValue()) {
                    // MessagingException - If the server doesn't support the ACL extension
                    config.setAcl(false);
                }
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, config, session);
        }
    }

    private boolean isPropagateAccount(final IIMAPProperties imapConfProps) throws OXException {
        if (MailAccount.DEFAULT_ID == accountId) {
            return true;
        }

        final MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
        if (null == storageService) {
            return false;
        }
        final int[] ids = storageService.getByHostNames(imapConfProps.getPropagateHostNames(), session.getUserId(), session.getContextId());
        return Arrays.binarySearch(ids, accountId) >= 0;
    }

    /**
     * Gets a connected IMAP store
     *
     * @param maxCount <code>true</code> from cache; otherwise <code>false</code>
     * @return The connected IMAP store
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If another error occurs
     */
    public IMAPStore connectIMAPStore(final int maxCount) throws MessagingException, OXException {
        return connectIMAPStore(maxCount, imapSession, server, port, login, password, clientIp);
    }

    /**
     * Clears cached IMAP connections.
     */
    protected void clearCachedConnections() {
        final IMAPStoreContainer container = IMAPStoreCache.getInstance().optContainer(accountId, server, port, login, session);
        if (null != container) {
            container.clear();
        }
        try {
            final IMailAccessCache mailAccessCache = MailAccess.getMailAccessCache();
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> tmp;
            while ((tmp = mailAccessCache.removeMailAccess(session, accountId)) != null) {
                tmp.close(false);
            }
        } catch (final Exception e) {
            // Ignore
        }
    }

    private static final String PROTOCOL = IMAPProvider.PROTOCOL_IMAP.getName();

    private IMAPStore connectIMAPStore(final int maxCount, final javax.mail.Session imapSession, final String server, final int port, final String login, final String pw, final String clientIp) throws MessagingException, OXException {
        /*
         * Propagate client IP address
         */
        if (clientIp != null) {
            imapSession.getProperties().put("mail.imap.propagate.clientipaddress", clientIp);
        }
        imapSession.getProperties().put("mail.imap.failOnNOFetch", "true");

        /*
         * Set log properties
         */
        LogProperties.put(LogProperties.Name.MAIL_ACCOUNT_ID, Integer.valueOf(accountId));
        LogProperties.put(LogProperties.Name.MAIL_HOST, server + ":" + port);
        LogProperties.put(LogProperties.Name.MAIL_LOGIN, imapConfig.getLogin());
        /*-
         * Get connected IMAP store
         *
         * Get store either from store cache or newly created
         */
        if (useIMAPStoreCache()) {
            /*
             * Possible connect limitation
             */
            if (maxCount > 0) {
                final Properties properties = imapSession.getProperties();
                properties.put("mail.imap.maxNumAuthenticated", Integer.toString(maxCount));
                properties.put("mail.imap.authAwait", "true");
                properties.put("mail.imap.accountId", Integer.toString(accountId));
            }
            boolean checkConnectivityIfPolled = checkConnectivityIfPolled();
            final IMAPStore borrowedIMAPStore = borrowIMAPStore(imapSession, server, port, login, pw, (clientIp != null), checkConnectivityIfPolled);
            if (null == borrowedIMAPStore) {
                throw IMAPException.create(IMAPException.Code.CONNECTION_UNAVAILABLE, imapConfig, session, imapConfig.getServer(), imapConfig.getLogin());
            }
            return borrowedIMAPStore;
        }
        /*
         * Possible connect limitation
         */
        if (maxCount > 0) {
            imapSession.getProperties().put("mail.imap.maxNumAuthenticated", Integer.toString(maxCount));
            imapSession.getProperties().put("mail.imap.authAwait", "true");
            imapSession.getProperties().put("mail.imap.accountId", Integer.toString(accountId));
        }
        /*
         * Retry loop...
         */
        final int maxRetryCount = 3;
        int retryCount = 0;
        while (retryCount++ < maxRetryCount) {
            try {
                return newConnectedImapStore(imapSession, server, port, login, pw, accountId);
            } catch (final MessagingException e) {
                if (!(e.getNextException() instanceof ConnectQuotaExceededException)) {
                    throw e;
                }
                if (retryCount >= maxRetryCount) {
                    throw e;
                }
            }
        }
        throw new MessagingException("Unable to connect to IMAP store: " + new URLName("imap", server, port, null, imapConfig.getLogin(), "xxxx"));
    }

    private IMAPStore newConnectedImapStore(javax.mail.Session imapSession, String server, int port, String login, String pw, int accountId) throws MessagingException {
        return newConnectedImapStore(imapSession, server, port, login, pw, accountId, null, false);
    }

    private IMAPStore newConnectedImapStore(javax.mail.Session imapSession, String server, int port, String login, String pw, int accountId, boolean[] preAuthStartTlsCap, boolean knownExternal) throws MessagingException {
        /*
         * Establish a new one...
         */
        IMAPStore imapStore = (IMAPStore) imapSession.getStore(PROTOCOL);
        if (MailAccount.DEFAULT_ID == accountId) {
            IMAPClientParameters.setDefaultClientParameters(imapStore, session);
        }
        /*
         * ... and connect it
         */
        if (null != preAuthStartTlsCap) {
            try {
                Map<String, String> capabilities = IMAPCapabilityAndGreetingCache.getCapabilities(new HostAndPort(IDNA.toASCII(server), port), false, IMAPProperties.getInstance());
                if (null != capabilities) {
                    preAuthStartTlsCap[0] = capabilities.containsKey("STARTTLS");
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        doIMAPConnect(imapSession, imapStore, server, port, login, pw, accountId, session, knownExternal);
        return imapStore;
    }

    /**
     * This method triggers the connect to the IMAP server on the given {@link IMAPStore} object.
     * Furthermore this method contains a thread synchronization if Kerberos is used. If the Kerberos subject is found in the properties of
     * the IMAP session, this Kerberos subject object is used to only allow a single IMAP login per Kerberos subject. The service ticket for
     * the IMAP server is stored in the Kerberos subject once the IMAP login was successful. If multiple threads can execute this in
     * parallel, multiple IMAP service tickets are requested, which is discouraged for performance reasons.
     *
     * @param imapSession The IMAP session
     * @param imapStore The IMAP store
     * @param server The host name
     * @param port The port
     * @param login The login
     * @param pw The password
     * @param accountId The account identifier
     * @param session The associated Groupware session
     * @param knownExternal <code>true</code> if it is known that a connection is supposed to be established to an external IMAP service, otherwise <code>false</code> if not known
     * @throws MessagingException If operation fails
     */
    public static void doIMAPConnect(javax.mail.Session imapSession, IMAPStore imapStore, String server, int port, String login, String pw, int accountId, Session session, boolean knownExternal) throws MessagingException {
        Object kerberosSubject = imapSession.getProperties().get("mail.imap.sasl.kerberosSubject");
        if (null == kerberosSubject) {
            imapStore.connect(server, port, login, pw);
        } else {
            synchronized (kerberosSubject) {
                imapStore.connect(server, port, login, pw);
            }
        }

        AuditLogService auditLogService = Services.optService(AuditLogService.class);
        if (null != auditLogService) {
            String eventId = knownExternal ? "imap.external.login" : (MailAccount.DEFAULT_ID == accountId ? "imap.primary.login" : "imap.external.login");
            auditLogService.log(eventId, DefaultAttribute.valueFor(Name.LOGIN, session.getLoginName()), DefaultAttribute.valueFor(Name.IP_ADDRESS, session.getLocalIp()), DefaultAttribute.timestampFor(new Date()), DefaultAttribute.arbitraryFor("imap.login", login), DefaultAttribute.arbitraryFor("imap.server", server), DefaultAttribute.arbitraryFor("imap.port", Integer.toString(port)));
        }
        
        String sessionInformation = imapStore.getClientParameter(IMAPClientParameters.SESSION_ID.getParamName());
        if (null != sessionInformation) {
            LogProperties.put(LogProperties.Name.MAIL_SESSION, sessionInformation);
        }
        java.net.InetAddress remoteAddress = imapStore.getRemoteAddress();
        if (null != remoteAddress) {            
            LogProperties.put(LogProperties.Name.MAIL_HOST_REMOTE_ADDRESS, remoteAddress.getHostAddress());
        }
    }

    private IMAPStore borrowIMAPStore(javax.mail.Session imapSession, String server, int port, String login, String pw, boolean propagateClientIp, boolean checkConnectivityIfPolled) throws MessagingException, OXException {
        return IMAPStoreCache.getInstance().borrowIMAPStore(accountId, imapSession, server, port, login, pw, session, propagateClientIp, checkConnectivityIfPolled);
    }

    private void checkTemporaryDown(IIMAPProperties imapConfProps) throws OXException, IMAPException {
        Map<HostAndPort, Long> map = timedOutServers;
        if (null == map) {
            return;
        }

        MailConfig mailConfig = getMailConfig();
        HostAndPort key = newHostAndPort(mailConfig);
        Long range = map.get(key);
        if (range != null) {
            if (System.currentTimeMillis() - range.longValue() <= imapConfProps.getImapTemporaryDown()) {
                /*
                 * Still considered as being temporary broken
                 */
                throw MimeMailExceptionCode.CONNECT_ERROR.create(mailConfig.getServer(), mailConfig.getLogin()).markLightWeight();
            }
            map.remove(key);
        }
    }

    private void checkAuthFailed(String user, String password, IIMAPProperties imapConfProps) throws OXException, IMAPException {
        Map<HostAndPortAndCredentials, StampAndOXException> map = authFailedServers;
        if (null == map) {
            return;
        }

        MailConfig mailConfig = getMailConfig();
        HostAndPortAndCredentials key = new HostAndPortAndCredentials(user, password, mailConfig.getServer(), mailConfig.getPort());
        StampAndOXException range = map.get(key);
        if (range != null) {
            if (System.currentTimeMillis() - range.getStamp() <= imapConfProps.getImapFailedAuthTimeout()) {
                /*
                 * Still considered as being temporary broken
                 */
                throw range.getOXException().markLightWeight();
            }
            map.remove(key);
        }
    }

    @Override
    public IMAPFolderStorage getFolderStorage() throws OXException {
        // connected = ((imapStore != null) && imapStore.isConnected());
        if (!connected) {
            throw IMAPException.create(IMAPException.Code.NOT_CONNECTED, getMailConfig(), session, new Object[0]);
        }
        if (null == folderStorage) {
            folderStorage = new IMAPFolderStorage(imapStore, this, session);
        }
        return folderStorage;
    }

    @Override
    public IMAPMessageStorage getMessageStorage() throws OXException {
        // connected = ((imapStore != null) && imapStore.isConnected());
        if (!connected) {
            throw IMAPException.create(IMAPException.Code.NOT_CONNECTED, getMailConfig(), session, new Object[0]);
        }
        if (null == messageStorage) {
            messageStorage = new IMAPMessageStorage(imapStore, this, session);
        }
        return messageStorage;
    }

    @Override
    public MailLogicTools getLogicTools() throws OXException {
        // connected = ((imapStore != null) && imapStore.isConnected());
        if (!connected) {
            throw IMAPException.create(IMAPException.Code.NOT_CONNECTED, getMailConfig(), session, new Object[0]);
        }
        if (null == logicTools) {
            logicTools = new MailLogicTools(session, accountId);
        }
        return logicTools;
    }

    @Override
    public boolean isConnected() {
        /*-
         *
        if (!connected) {
            return false;
        }
        return (connected = ((imapStore != null) && imapStore.isConnected()));
         */
        return connected;
    }

    @Override
    public boolean isConnectedUnsafe() {
        return connected;
    }

    @Override
    public boolean isCacheable() {
        if (useIMAPStoreCache()) {
            return false;
        }
        return true;
    }

    private boolean useIMAPStoreCache() {
        return USE_IMAP_STORE_CACHE.get();
    }

    /**
     * Gets used IMAP session
     *
     * @return The IMAP session
     */
    public javax.mail.Session getMailSession() {
        return imapSession;
    }

    @Override
    protected void startup() throws OXException {
        initMaps();
        IMAPCapabilityAndGreetingCache.init();
        MBoxEnabledCache.init();
        RootSubfoldersEnabledCache.init();
        ACLExtensionInit.getInstance().start();
        Entity2ACLInit.getInstance().start();
        maxCountCache = new NonBlockingHashMap<>(16);

        final ConfigurationService confService = Services.getService(ConfigurationService.class);
        final boolean useIMAPStoreCache = null == confService ? true : confService.getBoolProperty("com.openexchange.imap.useIMAPStoreCache", true);
        USE_IMAP_STORE_CACHE.set(useIMAPStoreCache);
    }

    private static synchronized void initMaps() {
        if (null == timedOutServers) {
            timedOutServers = new NonBlockingHashMap<>();
        }
        if (null == authFailedServers) {
            authFailedServers = new NonBlockingHashMap<>();
        }
        if (null == aclCapableServers) {
            aclCapableServers = new NonBlockingHashMap<>();
        }
        if (null == cleanUpTimerTask) {
            final TimerService timerService = Services.getService(TimerService.class);
            if (null != timerService) {
                final Map<HostAndPort, Long> map1 = timedOutServers;
                final Map<HostAndPortAndCredentials, StampAndOXException> map2 = authFailedServers;
                final Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        /*
                         * Clean-up temporary-down map
                         */
                        for (final Iterator<Entry<HostAndPort, Long>> iter = map1.entrySet().iterator(); iter.hasNext();) {
                            final Entry<HostAndPort, Long> entry = iter.next();
                            if (System.currentTimeMillis() - entry.getValue().longValue() > MAX_TEMP_DOWN) {
                                iter.remove();
                            }
                        }
                        for (final Iterator<Entry<HostAndPortAndCredentials, StampAndOXException>> iter = map2.entrySet().iterator(); iter.hasNext();) {
                            final Entry<HostAndPortAndCredentials, StampAndOXException> entry = iter.next();
                            if (System.currentTimeMillis() - entry.getValue().getStamp() > MAX_TEMP_DOWN) {
                                iter.remove();
                            }
                        }
                    }
                };
                /*
                 * Schedule every minute
                 */
                cleanUpTimerTask = timerService.scheduleWithFixedDelay(r, 60000, 60000);
            }
        }
    }

    @Override
    protected void shutdown() throws OXException {
        USE_IMAP_STORE_CACHE.set(true);
        maxCountCache = null;
        Entity2ACLInit.getInstance().stop();
        ACLExtensionInit.getInstance().stop();
        IMAPCapabilityAndGreetingCache.tearDown();
        MBoxEnabledCache.tearDown();
        RootSubfoldersEnabledCache.tearDown();
        IMAPSessionProperties.resetDefaultSessionProperties();
        dropMaps();
    }

    private static synchronized void dropMaps() {
        final ScheduledTimerTask cleanUpTimerTask = IMAPAccess.cleanUpTimerTask;
        if (null != cleanUpTimerTask) {
            cleanUpTimerTask.cancel(false);
            IMAPAccess.cleanUpTimerTask = null;
        }
        aclCapableServers = null;
        timedOutServers = null;
        authFailedServers = null;
    }

    @Override
    protected boolean checkMailServerPort() {
        return true;
    }

    /**
     * Creates a new <code>HostAndPort</code> instance from given arguments.
     *
     * @param config The configuration providing host name or IP address of the IMAP server as well as port
     * @return The new instance
     */
    public static HostAndPort newHostAndPort(MailConfig config) {
        return newHostAndPort(config.getServer(), config.getPort());
    }

    /**
     * Creates a new <code>HostAndPort</code> instance from given arguments.
     *
     * @param host The host name or IP address of the IMAP server
     * @param port The port
     * @return The new instance
     */
    public static HostAndPort newHostAndPort(String host, int port) {
        return new HostAndPort(host, port);
    }

    @Override
    protected IMailProperties createNewMailProperties() throws OXException {
        final MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
        int userId = session.getUserId();
        int contextId = session.getContextId();
        return new MailAccountIMAPProperties(storageService.getMailAccount(accountId, userId, contextId), userId, contextId);
    }

    private static javax.mail.Session setConnectProperties(IMAPConfig config, int timeout, int connectionTimeout, Properties imapProps, Class<? extends IMAPStore> storeClass, boolean forceSecure, int userId, int contextId) throws OXException {
        /*
         * Custom IMAP store
         */
        imapProps.put("mail.imap.class", storeClass.getName());
        /*
         * Set timeouts
         */
        if (timeout > 0) {
            imapProps.put("mail.imap.timeout", String.valueOf(timeout));
        }
        if (connectionTimeout > 0) {
            imapProps.put("mail.imap.connectiontimeout", String.valueOf(connectionTimeout));
        }
        /*
         * Specify CLOSE behavior
         */
        imapProps.put("mail.imap.explicitCloseForReusedProtocol", "true");
        /*
         * Specify NOOP behavior
         */
        imapProps.put("mail.imap.issueNoopToKeepConnectionAlive", "false");
        /*
         * Whether to extend (default) or to overwrite pre-login capabilities
         */
        if (config.getIMAPProperties().isOverwritePreLoginCapabilities()) {
            imapProps.put("mail.imap.overwriteprelogincapabilities", "true");
        }
        /*
         * Enable/disable audit log
         */
        if (config.getIMAPProperties().isAuditLogEnabled()) {
            imapProps.put("mail.imap.auditLog.enabled", "true");
        }
        /*
         * Greeting listener (for primary IMAP)
         */
        if (config.getAccountId() == MailAccount.DEFAULT_ID) {
            GreetingListener greetingListener = IMAPProperties.getInstance().getHostNameRegex(userId, contextId);
            if (null != greetingListener) {
                imapProps.put("mail.imap.greeting.listeners", Collections.singletonList(greetingListener));
            }
        }
        /*
         * Allow round-robin address election for primary IMAP account
         */
        if (config.getAccountId() == MailAccount.DEFAULT_ID) {
            boolean useMultipleAddresses = IMAPProperties.getInstance().isUseMultipleAddresses(userId, contextId);
            if (useMultipleAddresses) {
                imapProps.put("mail.imap.multiAddress.enabled", "true");
                /*
                 * Pass hash if needed
                 */
                boolean useMultipleAddressesUserHash = IMAPProperties.getInstance().isUseMultipleAddressesUserHash(userId, contextId);
                if (useMultipleAddressesUserHash) {
                    int hash = getHashFor(userId, contextId);
                    imapProps.put("mail.imap.multiAddress.key", Integer.toString(hash));
                }
                /*
                 * Pass max. wait timeout
                 */
                int maxRetries = IMAPProperties.getInstance().getMultipleAddressesMaxRetryAttempts(userId, contextId);
                if (maxRetries >= 0) {
                    imapProps.put("mail.imap.multiAddress.maxRetries", Integer.toString(maxRetries));
                }
            }
        }
        /*
         * Enable XOAUTH2/OAUTHBEARER (if appropriate)
         */
        if (AuthType.OAUTH == config.getAuthType()) {
            imapProps.put("mail.imap.auth.mechanisms", "XOAUTH2");
        } else if (AuthType.OAUTHBEARER == config.getAuthType()) {
            imapProps.put("mail.imap.auth.mechanisms", "OAUTHBEARER");
        } else {
            imapProps.put("mail.imap.auth.mechanisms", "PLAIN LOGIN NTLM");
        }
        /*
         * Check if a secure IMAP connection should be established
         */
        String sPort = String.valueOf(config.getPort());
        SSLSocketFactoryProvider factoryProvider = Services.getService(SSLSocketFactoryProvider.class);
        String socketFactoryClass = factoryProvider.getDefault().getClass().getName();
        String protocols = config.getIMAPProperties().getSSLProtocols();
        String cipherSuites = config.getIMAPProperties().getSSLCipherSuites();
        SSLConfigurationService sslConfigService = Services.getService(SSLConfigurationService.class);
        if (config.isSecure()) {
            /*
             * Enables the use of the STARTTLS command.
             */
            // imapProps.put("mail.imap.starttls.enable", "true");
            /*
             * Set main socket factory to a SSL socket factory
             */
            imapProps.put("mail.imap.socketFactory.class", socketFactoryClass);
            imapProps.put("mail.imap.socketFactory.port", sPort);
            imapProps.put("mail.imap.socketFactory.fallback", "false");
            /*
             * Needed for JavaMail >= 1.4
             */
            // Security.setProperty("ssl.SocketFactory.provider", socketFactoryClass);
            /*
             * Specify SSL protocols
             */
            if (Strings.isNotEmpty(protocols)) {
                imapProps.put("mail.imap.ssl.protocols", protocols);
            } else {
                if (sslConfigService == null) {
                    throw ServiceExceptionCode.absentService(SSLConfigurationService.class);
                }
                imapProps.put("mail.imap.ssl.protocols", Strings.toWhitespaceSeparatedList(sslConfigService.getSupportedProtocols()));
            }
            /*
             * Specify SSL cipher suites
             */

            if (Strings.isNotEmpty(cipherSuites)) {
                imapProps.put("mail.imap.ssl.ciphersuites", cipherSuites);
            } else {
                if (null == sslConfigService) {
                    throw ServiceExceptionCode.absentService(SSLConfigurationService.class);
                }
                imapProps.put("mail.imap.ssl.ciphersuites", Strings.toWhitespaceSeparatedList(sslConfigService.getSupportedCipherSuites()));
            }
        } else {
            /*
             * Enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection.
             */
            if (forceSecure) {
                imapProps.put("mail.imap.starttls.required", "true");
            } else if (config.getIMAPProperties().isEnableTls()) {
                imapProps.put("mail.imap.starttls.enable", "true");
            }
            /*
             * Specify the javax.net.ssl.SSLSocketFactory class, this class will be used to create IMAP SSL sockets if TLS handshake says
             * so.
             */
            imapProps.put("mail.imap.socketFactory.port", sPort);
            imapProps.put("mail.imap.ssl.socketFactory.class", socketFactoryClass);
            imapProps.put("mail.imap.ssl.socketFactory.port", sPort);
            imapProps.put("mail.imap.socketFactory.fallback", "false");
            /*
             * Specify SSL protocols
             */
            if (Strings.isNotEmpty(protocols)) {
                imapProps.put("mail.imap.ssl.protocols", protocols);
            } else {
                if (null == sslConfigService) {
                    throw ServiceExceptionCode.absentService(SSLConfigurationService.class);
                }
                imapProps.put("mail.imap.ssl.protocols", Strings.toWhitespaceSeparatedList(sslConfigService.getSupportedProtocols()));
            }
            /*
             * Specify SSL cipher suites
             */
            if (Strings.isNotEmpty(cipherSuites)) {
                imapProps.put("mail.imap.ssl.ciphersuites", cipherSuites);
            } else {
                if (null == sslConfigService) {
                    throw ServiceExceptionCode.absentService(SSLConfigurationService.class);
                }
                imapProps.put("mail.imap.ssl.ciphersuites", Strings.toWhitespaceSeparatedList(sslConfigService.getSupportedCipherSuites()));
            }
            // imapProps.put("mail.imap.ssl.enable", "true");
            /*
             * Needed for JavaMail >= 1.4
             */
            // Security.setProperty("ssl.SocketFactory.provider", socketFactoryClass);
        }
        /*
         * Create new IMAP session from initialized properties
         */
        final javax.mail.Session imapSession = javax.mail.Session.getInstance(imapProps, null);
        imapSession.addProvider(new Provider(Provider.Type.STORE, "imap", storeClass.getName(), "OX Software GmbH", getVersion()));
        return imapSession;
    }

    private static int getHashFor(int user, int context) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update((byte) user);
            md.update((byte) context);

            // Calculate hash & create its string representation
            byte[] bytes = md.digest();

            int i = 0;
            for (byte b : bytes) {
                i += b;
            }
            return (i < 0) ? -i : i;
        } catch (NoSuchAlgorithmException e) {
            // Ignore
        }
        return 0;
    }

    @Override
    public String toString() {
        IMAPStore imapStore = this.imapStore;
        if (null != imapStore) {
            return imapStore.toString();
        }
        return "[not connected]";
    }

    private static final int MAX_STACK_TRACE_ELEMENTS = 1000;

    private static void appendStackTrace(final StackTraceElement[] trace, final String lineSeparator, final StringBuilder sb) {
        if (null == trace) {
            return;
        }
        final int length = (MAX_STACK_TRACE_ELEMENTS <= trace.length) ? MAX_STACK_TRACE_ELEMENTS : trace.length;
        for (int i = 0; i < length; i++) {
            final StackTraceElement ste = trace[i];
            final String className = ste.getClassName();
            if (null != className) {
                sb.append("    at ").append(className).append('.').append(ste.getMethodName());
                if (ste.isNativeMethod()) {
                    sb.append("(Native Method)");
                } else {
                    final String fileName = ste.getFileName();
                    if (null == fileName) {
                        sb.append("(Unknown Source)");
                    } else {
                        final int lineNumber = ste.getLineNumber();
                        sb.append('(').append(fileName);
                        if (lineNumber >= 0) {
                            sb.append(':').append(lineNumber);
                        }
                        sb.append(')');
                    }
                }
                sb.append(lineSeparator);
            }
        }
    }

    private static void closeSafely(final IMAPStore imapStore) {
        if (null != imapStore) {
            try {
                imapStore.close();
            } catch (final MessagingException e) {
                LOG.error("Error while closing IMAP store.", e);
            } catch (final RuntimeException e) {
                LOG.error("Error while closing IMAP store.", e);
            }
        }
    }

    private static String authEncode(String s, String charset) {
        String tmp = s;
        if (tmp != null) {
            try {
                tmp = new String(s.getBytes(Charsets.forName(charset)), Charsets.ISO_8859_1);
            } catch (final UnsupportedCharsetException e) {
                LOG.error("Unsupported encoding in a message detected and monitored", e);
                mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
            }
        }
        return tmp;
    }

}
