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

package com.openexchange.folderstorage.database;

import java.sql.Connection;
import java.util.List;
import java.util.Locale;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.osgi.FolderStorageServices;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link DatabaseFolderStorageUtility} - Utility methods for database folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DatabaseFolderStorageUtility {

    /**
     * Initializes a new {@link DatabaseFolderStorageUtility}.
     */
    private DatabaseFolderStorageUtility() {
        super();
    }

    /**
     * Checks if passed <code>String</code> starts with shared prefix.
     *
     * @param str The string to check
     * @return <code>true</code> if passed <code>String</code> starts with shared prefix; otherwise <code>false</code>
     */
    public static boolean hasSharedPrefix(final String str) {
        return null != str && str.startsWith(FolderObject.SHARED_PREFIX, 0);
    }

    /**
     * Extracts the user's permission bits from the supplied storage paramters.
     *
     * @param connection A readable database connection, or <code>null</code> if not available
     * @param storageParameters The storage parameters
     * @return The permission bits
     */
    public static UserPermissionBits getUserPermissionBits(Connection connection, StorageParameters storageParameters) throws OXException {
        Session session = storageParameters.getSession();
        if (session != null && ServerSession.class.isInstance(session)) {
            return ((ServerSession) session).getUserPermissionBits();
        }

        UserPermissionService userPermissionService = FolderStorageServices.requireService(UserPermissionService.class);
        if (null == connection) {
            return userPermissionService.getUserPermissionBits(storageParameters.getUserId(), storageParameters.getContextId());
        } else {
            Context context = storageParameters.getContext();
            if (context == null) {
                context = FolderStorageServices.requireService(ContextService.class).getContext(storageParameters.getContextId());
            }
            return userPermissionService.getUserPermissionBits(connection, storageParameters.getUserId(), context);
        }
    }

    /**
     * Creates an array containing sortable identifiers from a list of folders.
     *
     * @param folders The folder to extract the identifiers for
     * @return The extracted identifiers
     */
    public static SortableId[] extractIDs(List<FolderObject> folders) {
        final SortableId[] ret = new SortableId[folders.size()];
        for (int i = 0; i < ret.length; i++) {
            final FolderObject folderObject = folders.get(i);
            final String id = String.valueOf(folderObject.getObjectID());
            ret[i] = new DatabaseId(id, i, folderObject.getFolderName());
        }
        return ret;
    }

    /**
     * Localizes the display names of the supplied folders based on a specific locale.
     *
     * @param folders The folders
     * @param locale The target locale
     */
    public static void localizeFolderNames(List<FolderObject> folders, Locale locale) throws OXException {
        StringHelper stringHelper = null;
        for (final FolderObject folderObject : folders) {
            /*
             * Check if folder is user's default folder and set locale-sensitive name
             */
            if (folderObject.isDefaultFolder()) {
                final int module = folderObject.getModule();
                if (FolderObject.CALENDAR == module) {
                    {
                        if (null == stringHelper) {
                            stringHelper = StringHelper.valueOf(locale);
                        }
                        folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_CALENDAR_FOLDER_NAME));
                    }
                } else if (FolderObject.CONTACT == module) {
                    {
                        if (null == stringHelper) {
                            stringHelper = StringHelper.valueOf(locale);
                        }
                        folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_CONTACT_FOLDER_NAME));
                    }
                } else if (FolderObject.TASK == module) {
                    {
                        if (null == stringHelper) {
                            stringHelper = StringHelper.valueOf(locale);
                        }
                        folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_TASK_FOLDER_NAME));
                    }
                }
            }
        }
    }

}
