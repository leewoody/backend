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

package com.openexchange.folderstorage.filestorage;

import java.util.List;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.filestorage.osgi.Services;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link FileStorageFolderType} - The folder type for file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorageFolderType implements FolderType {

    /**
     * The private folder identifier.
     */
    private static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    private static final FileStorageFolderType instance = new FileStorageFolderType();

    /**
     * Gets the {@link FileStorageFolderType} instance.
     *
     * @return The {@link FileStorageFolderType} instance
     */
    public static FileStorageFolderType getInstance() {
        return instance;
    }

    /*-
     * Member stuff
     */

    /**
     * Initializes a new {@link FileStorageFolderType}.
     */
    private FileStorageFolderType() {
        super();
    }

    @Override
    public boolean servesTreeId(final String treeId) {
        return FolderStorage.REAL_TREE_ID.equals(treeId);
    }

    @Override
    public boolean servesFolderId(final String folderId) {
        if (null == folderId) {
            return false;
        }
        // Check if a real service is defined
        final List<String> components = IDMangler.unmangle(folderId);
        if (2 > components.size()) {
            return false;
        }
        /*
         * Check if service exists
         */
        String serviceID = new FolderID(folderId).getService();
        final FileStorageServiceRegistry registry = Services.getService(FileStorageServiceRegistry.class);
        return null != registry && registry.containsFileStorageService(serviceID);
    }

    @Override
    public boolean servesParentId(final String folderId) {
        if (null == folderId) {
            return false;
        }
        if (PRIVATE_FOLDER_ID.equals(folderId)) {
            return true;
        }
        return servesFolderId(folderId);
    }

}
