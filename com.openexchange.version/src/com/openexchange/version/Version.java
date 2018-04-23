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

package com.openexchange.version;

import com.openexchange.version.internal.Numbers;

/**
 * Stores the version of the Middleware.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
public class Version {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Version.class);

    public static final String CODENAME = "Hyperion";
    public static final String NAME = "Open-Xchange";

    public static final Version SINGLETON = new Version();

    /**
     * Gets the version instance
     *
     * @return The version instance
     */
    public static final Version getInstance() {
        return SINGLETON;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private volatile Numbers numbers = null;
    private volatile String buildDate = null;
    private volatile String versionString = null;

    protected Version() {
        super();
    }

    /**
     * Sets the numbers for this version instance.
     *
     * @param numbers The number to set
     */
    public void setNumbers(Numbers numbers) {
        this.numbers = numbers;
        versionString = null;
    }

    /**
     * Sets the build date for this version instance.
     *
     * @param buildDate The build date to set
     */
    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    /**
     * Gets the build date.
     *
     * @return The build date
     * @throws IllegalStateException if version instance is not yet initialized
     */
    public String getBuildDate() {
        if (null == buildDate) {
            IllegalStateException e = new IllegalStateException("Central backend version not initialized yet.");
            LOG.error("", e);
            throw e;
        }
        return buildDate;
    }

    /**
     * Gets the major number.
     *
     * @return The major number
     * @throws IllegalStateException if version instance is not yet initialized
     */
    public int getMajor() {
        if (null == numbers) {
            IllegalStateException e = new IllegalStateException("Central backend version not initialized yet.");
            LOG.error("", e);
            throw e;
        }
        return numbers.getMajor();
    }

    /**
     * Gets the minor number.
     *
     * @return The minor number
     * @throws IllegalStateException if version instance is not yet initialized
     */
    public int getMinor() {
        if (null == numbers) {
            IllegalStateException e = new IllegalStateException("Central backend version not initialized yet.");
            LOG.error("", e);
            throw e;
        }
        return numbers.getMinor();
    }

    /**
     * Gets the patch number.
     *
     * @return The patch number
     * @throws IllegalStateException if version instance is not yet initialized
     */
    public int getPatch() {
        if (null == numbers) {
            IllegalStateException e = new IllegalStateException("Central backend version not initialized yet.");
            LOG.error("", e);
            throw e;
        }
        return numbers.getPatch();
    }

    /**
     * Gets the version string; e.g. <code>"7.8.3-Rev2"</code>.
     *
     * @return The version string
     * @throws IllegalStateException if version instance is not yet initialized
     */
    public String getVersionString() {
        String tmp = this.versionString;
        if (null == tmp) {
            // Acquire lock...
            synchronized (this) {
                // ... and try again
                tmp = this.versionString;
                if (null == tmp) {
                    // Initialize version string
                    if (null == numbers) {
                        IllegalStateException e = new IllegalStateException("Central backend version not initialized yet.");
                        LOG.error("", e);
                        throw e;
                    }
                    tmp = numbers.getVersion() + "-Rev" + numbers.getBuildNumber();
                    this.versionString = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Optionally gets the version string.
     *
     * @return The version string, or <code>null</code> if it is not yet initialized
     */
    public String optVersionString() {
        return versionString;
    }

}
