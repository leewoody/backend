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

package com.openexchange.drive;

/**
 * {@link DriveClientVersion}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveClientVersion implements Comparable<DriveClientVersion> {

    /**
     * The version "0".
     */
    public static final DriveClientVersion VERSION_0 = new DriveClientVersion("0");

    private final String version;
    private final int[] versionParts;

    /**
     * Initializes a new {@link DriveClientVersion}.
     *
     * @param version The version string, matching the pattern <code>^[0-9]+(\.[0-9]+)*$</code>.
     * @throws IllegalArgumentException If the version has an unexpected format
     */
    public DriveClientVersion(String version) throws IllegalArgumentException {
        super();
        if (null == version || false == version.matches("^[0-9]+(\\.[0-9]+)*$")) {
            throw new IllegalArgumentException(version);
        }
        this.version = version;
        String[] parts = version.split("\\.");
        this.versionParts = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                versionParts[i] = Integer.parseInt(parts[i]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(version, e);
        }
    }

    /**
     * Gets the version string
     *
     * @return The version string
     */
    public String getVersion() {
        return version;
    }

    @Override
    public int compareTo(DriveClientVersion other) {
        if (null == other) {
            return 1;
        }
        int maxLength = Math.max(versionParts.length, other.versionParts.length);
        for (int i = 0; i < maxLength; i++) {
            int thisPart = i < versionParts.length ? versionParts[i] : 0;
            int otherPart = i < other.versionParts.length ? other.versionParts[i] : 0;
            if (thisPart < otherPart) {
                return -1;
            } else if (thisPart > otherPart) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return version;
    }

}
