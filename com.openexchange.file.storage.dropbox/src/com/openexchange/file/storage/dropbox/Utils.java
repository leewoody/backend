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

package com.openexchange.file.storage.dropbox;

import com.openexchange.file.storage.FileStorageFolder;

/**
 * {@link Utils} - Utility class for Dropbox resources.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Utils {

    /**
     * Initializes a new {@link Utils}.
     */
    private Utils() {
        super();
    }

    /**
     * Normalizes given folder identifier
     *
     * @param folderId The folder identifier
     * @return The normalizes folder identifier
     */
    public static String normalizeFolderId(String folderId) {
        if (null == folderId) {
            return folderId;
        }
        return folderId.endsWith("/") ? folderId.substring(0, folderId.length() - 1) : folderId;
    }

    /**
     * Checks if specified folder identifier ends with a <code>'/'</code> character.
     *
     * @param folderId The folder identifier to check
     * @param rootUri The root URI of the connected WebDAV server
     * @return The checked folder identifier
     */
    public static String checkFolderId(final String folderId, final String rootUri) {
        if (FileStorageFolder.ROOT_FULLNAME.equals(folderId)) {
            return rootUri;
        }
        return checkFolderId(folderId);
    }

    /**
     * Checks if specified folder identifier ends with a <code>'/'</code> character.
     *
     * @param folderId The folder identifier to check
     * @return The checked folder identifier
     */
    public static String checkFolderId(final String folderId) {
        if (null == folderId) {
            return null;
        }
        if (folderId.endsWith("/")) {
            return folderId;
        }
        return new StringBuilder(folderId).append('/').toString();
    }

    /**
     * Checks the href provided by a multi-status response.
     *
     * @param href The multi-status response's href
     * @param isDirectory <code>true</code> if href denotes a directory; otherwise <code>false</code>
     * @return The checked href
     */
    public static String checkHref(final String href, final boolean isDirectory) {
        return isDirectory ? checkFolderId(href) : checkFileId(href);
    }

    /**
     * Checks specified file identifier.
     *
     * @param fileId The file identifier
     * @return The checked file identifier
     */
    public static String checkFileId(final String fileId) {
        if (null == fileId) {
            return null;
        }
        if (fileId.endsWith("/")) {
            final int length = fileId.length();
            return length == 1 ? "" : fileId.substring(0, length - 1);
        }
        return fileId;
    }
}
