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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.idn.IDNA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.Account;
import com.openexchange.mailaccount.TransportAccount;
import com.openexchange.mailaccount.TransportAuth;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;
import com.openexchange.tools.net.URITools;


/**
 * {@link TransportAccountImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class TransportAccountImpl implements TransportAccount {

    private static final Logger LOG = LoggerFactory.getLogger(TransportAccount.class);

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -402344252962912219L;

    private int id = -1;
    private String name;
    private String personal;
    private String replyTo;
    private String sendAddress;
    private TransportAuth transportAuth;
    private String transportLogin;
    private String transportPassword;
    private int transportPort;
    private Map<String, String> transportProperties;
    private String transportProtocol;
    private boolean transportSecure;
    private String transportServer;
    private String transportServerUrl;
    private boolean transportStartTls;
    private int transportOAuthId;
    private boolean transportDisabled;

    /**
     * Initializes a new {@link TransportAccountImpl}.
     */
    public TransportAccountImpl() {
        super();
        transportProperties = new HashMap<>(4);
        transportAuth = TransportAuth.MAIL;
        transportPort = 25;
        transportProtocol = "smtp";
        id = -1;
        transportOAuthId = -1;
        transportDisabled = false;
    }

    @Override
    public String generateTransportServerURL() {
        if (null != transportServerUrl) {
            return transportServerUrl;
        }
        if (com.openexchange.java.Strings.isEmpty(transportServer)) {
            return null;
        }
        final String protocol = transportSecure ? transportProtocol + 's' : transportProtocol;
        try {
            return transportServerUrl = URITools.generateURI(protocol, IDNA.toASCII(transportServer), transportPort).toString();
        } catch (final URISyntaxException e) {
            LOG.error("", e);
            // Old implementation is not capable of handling IPv6 addresses.
            final StringBuilder sb = new StringBuilder(32);
            sb.append(transportProtocol);
            if (transportSecure) {
                sb.append('s');
            }
            return transportServerUrl = sb.append("://").append(transportServer).append(':').append(transportPort).toString();
        }
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getLogin() {
        return this.getTransportLogin();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPassword() {
        return this.getTransportPassword();
    }

    @Override
    public String getPrimaryAddress() {
        return this.sendAddress;
    }

    @Override
    public TransportAuth getTransportAuth() {
        return this.transportAuth;
    }

    @Override
    public String getTransportLogin() {
        return this.transportLogin;
    }

    @Override
    public String getTransportPassword() {
        return this.transportPassword;
    }

    @Override
    public int getTransportPort() {
        return transportPort;
    }

    @Override
    public String getTransportProtocol() {
        return transportProtocol;
    }

    @Override
    public String getTransportServer() {
        return transportServer;
    }

    @Override
    public boolean isDefaultAccount() {
        return this.id == Account.DEFAULT_ID;
    }

    @Override
    public boolean isTransportStartTls() {
        return transportStartTls;
    }

    @Override
    public boolean isTransportDisabled() {
        return transportDisabled;
    }

    /**
     * Parses specified transport server URL. If the given URL is <code>null</code>, then the transport server URL will be set to <code>null</code> too.
     *
     * @param transportServerURL The transport server URL to parse
     * @throws OXException if parsing the URL fails.
     */
    public void parseTransportServerURL(String transportServerURL) {
        if (null == transportServerURL) {
            setTransportServer((String) null);
            return;
        }
        try {
            setTransportServer(URIParser.parse(IDNA.toASCII(transportServerURL), URIDefaults.SMTP));
        } catch (final URISyntaxException e) {
            setTransportServer((String) null);
            return;
            //throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.URI_PARSE_FAILED, e, transportServerURL);
        }
    }

    /**
     * Sets the account ID.
     *
     * @param id The account ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the account name.
     *
     * @param name The account name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the personal.
     *
     * @param personal The personal
     */
    public void setPersonal(final String personal) {
        this.personal = personal;
    }

    @Override
    public String getPersonal() {
        return personal;
    }

    /**
     * Sets the reply-to address
     *
     * @param replyTo The reply-to address
     */
    public void setReplyTo(final String replyTo) {
        this.replyTo = replyTo;
    }

    @Override
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * Sets whether mail transport is disabled
     *
     * @param transportDisabled <code>true</code> if disabled; otherwise <code>false</code>
     */
    public void setTransportDisabled(boolean transportDisabled) {
        this.transportDisabled = transportDisabled;
    }

    /**
     * Sets the transport authentication information
     *
     * @param transportAuth The transport authentication information to set
     */
    public void setTransportAuth(TransportAuth transportAuth) {
        this.transportAuth = transportAuth;
    }

    /**
     * Sets the transport login.
     *
     * @param transportLogin The transport login
     */
    public void setTransportLogin(String login) {
        this.transportLogin = login;
    }

    /**
     * Sets the transport password.
     *
     * @param transportLogin The transport password
     */
    public void setTransportPassword(String transportPassword) {
        this.transportPassword = transportPassword;
    }

    /**
     * Sets the transport server port.
     *
     * @param transportPort The transport server port to set
     */
    public void setTransportPort(final int transportPort) {
        transportServerUrl = null;
        this.transportPort = transportPort;
    }

    /**
     * Sets the transport properties
     *
     * @param properties The transport properties to set
     */
    public void setTransportProperties(final Map<String, String> transportProperties) {
        if (null == transportProperties) {
            this.transportProperties = new HashMap<>(4);
        } else {
            this.transportProperties = transportProperties;
        }
    }

    /**
     * Sets the transport server protocol
     *
     * @param transportProtocol The transport server protocol to set
     */
    public void setTransportProtocol(final String transportProtocol) {
        transportServerUrl = null;
        this.transportProtocol = transportProtocol;
    }

    /**
     * Sets whether to establish a secure connection to transport server.
     *
     * @param transportSecure Whether to establish a secure connection to transport server
     */
    public void setTransportSecure(final boolean transportSecure) {
        transportServerUrl = null;
        this.transportSecure = transportSecure;
    }

    @Override
    public boolean isTransportSecure() {
        return transportSecure;
    }

    /**
     * Sets the transport server name.
     *
     * @param transportServer The transport server name to set
     */
    public void setTransportServer(final String transportServer) {
        transportServerUrl = null;
        this.transportServer = transportServer == null ? null : IDNA.toUnicode(transportServer);
    }

    @Override
    public boolean isTransportOAuthAble() {
        return transportOAuthId >= 0;
    }

    @Override
    public int getTransportOAuthId() {
        return transportOAuthId < 0 ? -1 : transportOAuthId;
    }

    /**
     * Sets the identifier of the associated OAuth account for transport server
     *
     * @param transportOAuthId The OAuth account identifier or <code>-1</code> to signal none
     */
    public void setTransportOAuthId(int transportOAuthId) {
        this.transportOAuthId = transportOAuthId < 0 ? -1 : transportOAuthId;
    }

    /**
     * Sets the transport server.
     *
     * @param transportServer The transport server URI.
     */
    public void setTransportServer(final URI transportServer) {
        if (null == transportServer) {
            // Parse like old parser to prevent problems.
            setTransportServer("");
        } else {
            final String protocol = transportServer.getScheme();
            if (protocol.endsWith("s")) {
                setTransportSecure(true);
                setTransportProtocol(protocol.substring(0, protocol.length() - 1));
            } else {
                setTransportSecure(false);
                setTransportProtocol(protocol);
            }
            setTransportServer(URITools.getHost(transportServer));
            setTransportPort(transportServer.getPort());
        }
    }

    /**
     * Sets the STARTTLS flag
     *
     * @param startTLS The STARTTLS flag
     */
    public void setTransportStartTls(boolean startTLS) {
        this.transportStartTls = startTLS;
    }

    /**
     * Sets the send address
     *
     * @param sendAddress The send address
     */
    public void setSendAddress(String sendAddress) {
        this.sendAddress = sendAddress;
    }

}
