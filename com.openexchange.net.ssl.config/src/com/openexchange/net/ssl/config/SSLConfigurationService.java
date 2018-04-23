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

package com.openexchange.net.ssl.config;

import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link SSLConfigurationService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
@SingletonService
public interface SSLConfigurationService {

    /**
     * Returns if the provided hostname is whitelisted and therefore considered as 'trustable'
     *
     * @param hostName A hostname to check
     * @return <code>true</code> if the given hostname is whitelisted; otherwise <code>false</code>
     */
    boolean isWhitelisted(String hostName);

    /**
     * Returns the {@link TrustLevel} configured for the server.
     *
     * @return {@link TrustLevel} enum of the configured level
     */
    TrustLevel getTrustLevel();

    /**
     * Returns the protocols that will be considered by the server for SSL handshaking with external systems.
     *
     * @return An Array with supported protocols.
     */
    String[] getSupportedProtocols();

    /**
     * Returns the cipher suites that will be considered by the server for SSL handshaking with external systems.
     *
     * @return An Array with supported cipher suites.
     */
    String[] getSupportedCipherSuites();

    /**
     * Returns if the server is configured to check hostnames while SSL handshaking.
     *
     * @return <code>true</code> if the hostnames should be checked; otherwise <code>false</code>
     */
    boolean isVerifyHostname();

    /**
     * Returns if the default truststore provided with the JVM should be used.
     *
     * Hint: Loaded once per startup and cannot be reloaded as additional initialization is made based on the configuration.
     *
     * @return <code>true</code> if the default truststore is used; otherwise <code>false</code>
     */
    boolean isDefaultTruststoreEnabled();

    /**
     * Returns if the custom truststore defined by the administrator should be used.
     *
     * Hint: Loaded once per startup and cannot be reloaded as additional initialization is made based on the configuration.
     *
     * @return <code>true</code> if the custom truststore is used; otherwise <code>false</code>
     * @see #getCustomTruststoreLocation()
     * @see #getCustomTruststorePassword()
     */
    boolean isCustomTruststoreEnabled();

    /**
     * Returns the location of the custom truststore consisting of the path and file name (e. g. /opt/open-xchange/customTrustStore.jks).
     *
     * Hint: Loaded once per startup and cannot be reloaded as additional initialization is made based on the configuration.
     *
     * @return the location of the custom truststore
     */
    String getCustomTruststoreLocation();

    /**
     * Returns the password of the custom truststore to get access.
     *
     * Hint: Loaded once per startup and cannot be reloaded as additional initialization is made based on the configuration.
     *
     * @return the password to access the custom truststore
     */
    String getCustomTruststorePassword();

}
