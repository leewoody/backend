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

package com.openexchange.file.storage.dropbox.access;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.CreateFolderError;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderContinueErrorException;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.LookupError;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.RelocationError;
import com.dropbox.core.v2.files.RelocationErrorException;
import com.dropbox.core.v2.files.WriteError;
import com.dropbox.core.v2.users.IndividualSpaceAllocation;
import com.dropbox.core.v2.users.SpaceAllocation;
import com.dropbox.core.v2.users.SpaceUsage;
import com.dropbox.core.v2.users.TeamSpaceAllocation;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAutoRenameFoldersAccess;
import com.openexchange.file.storage.FileStorageCaseInsensitiveAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.NameBuilder;
import com.openexchange.file.storage.PathKnowingFileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.dropbox.DropboxServices;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link DropboxFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropboxFolderAccess extends AbstractDropboxAccess implements FileStorageFolderAccess, FileStorageCaseInsensitiveAccess, FileStorageAutoRenameFoldersAccess, PathKnowingFileStorageFolderAccess {

    private static final Logger LOG = LoggerFactory.getLogger(DropboxFolderAccess.class);

    private final int userId;
    private final String accountDisplayName;
    private final boolean useOptimisticSubfolderDetection;

    /**
     * Initialises a new {@link DropboxFolderAccess}.
     *
     * @throws OXException
     */
    public DropboxFolderAccess(AbstractOAuthAccess dropboxOAuthAccess, FileStorageAccount account, Session session) throws OXException {
        super(dropboxOAuthAccess, account, session);
        userId = session.getUserId();
        accountDisplayName = account.getDisplayName();
        ConfigViewFactory viewFactory = DropboxServices.getOptionalService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        useOptimisticSubfolderDetection = ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.file.storage.dropbox.useOptimisticSubfolderDetection", true, view);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#exists(java.lang.String)
     */
    @Override
    public boolean exists(String folderId) throws OXException {
        try {
            // The Dropbox V2 API does not allow to fetch metadata for the root folder,
            // thus we assume that it always exists
            if (isRoot(folderId)) {
                return true;
            }
            Metadata metadata = getMetadata(folderId);
            return metadata instanceof FolderMetadata;
        } catch (GetMetadataErrorException e) {
            if (LookupError.NOT_FOUND.equals(e.errorValue.getPathValue())) {
                return false;
            }
            throw DropboxExceptionHandler.handleGetMetadataErrorException(e, folderId, "");
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getFolder(java.lang.String)
     */
    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        try {
            if (isRoot(folderId)) {
                return getRootFolder();
            }

            FolderMetadata metadata = getFolderMetadata(folderId);
            // The '/get_metadata' Dropbox V2 API call does not return a hint to indicate if a folder has sub-folders,
            // thus we have to initiate an extra 'listFolder' call and check for sub folders.
            // More information: https://www.dropbox.com/developers/documentation/http/documentation#files-get_metadata
            boolean hasSubfolders = hasSubFolders(folderId);
            // Parse metadata
            return new DropboxFolder(metadata, userId, accountDisplayName, hasSubfolders);
        } catch (ListFolderErrorException e) {
            throw DropboxExceptionHandler.handleListFolderErrorException(e, folderId);
        } catch (GetMetadataErrorException e) {
            throw DropboxExceptionHandler.handleGetMetadataErrorException(e, folderId, "");
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getPersonalFolder()
     */
    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getTrashFolder()
     */
    @Override
    public FileStorageFolder getTrashFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getPublicFolders()
     */
    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getSubfolders(java.lang.String, boolean)
     */
    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        try {
            List<Metadata> entries = listFolder(parentIdentifier);

            List<FileStorageFolder> folders = new ArrayList<>(entries.size());
            for (Metadata entry : entries) {
                if (entry instanceof FolderMetadata) {
                    FolderMetadata folderMetadata = (FolderMetadata) entry;
                    folders.add(new DropboxFolder(folderMetadata, userId, accountDisplayName, hasSubFolders(folderMetadata.getPathDisplay())));
                }
            }

            return folders.toArray(new FileStorageFolder[folders.size()]);
        } catch (ListFolderContinueErrorException e) {
            throw DropboxExceptionHandler.handleListFolderContinueErrorException(e, parentIdentifier);
        } catch (ListFolderErrorException e) {
            throw DropboxExceptionHandler.handleListFolderErrorException(e, parentIdentifier);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getUserSharedFolders()
     */
    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getRootFolder()
     */
    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        DropboxFolder rootFolder = new DropboxFolder(userId);
        rootFolder.setRootFolder(true);
        rootFolder.setId(FileStorageFolder.ROOT_FULLNAME);
        rootFolder.setParentId(null);
        rootFolder.setName(accountDisplayName);
        rootFolder.setSubfolders(true);
        rootFolder.setSubscribedSubfolders(true);
        return rootFolder;
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        return createFolder(toCreate, true);
    }

    @Override
    public String createFolder(FileStorageFolder toCreate, boolean autoRename) throws OXException {
        String parentId = toCreate.getParentId();

        if (false == autoRename) {
            String fullpath = constructPath(parentId, toCreate.getName());
            try {
                FolderMetadata folderMetadata = client.files().createFolder(fullpath);
                return folderMetadata.getPathDisplay();
            } catch (CreateFolderErrorException e) {
                CreateFolderError error = e.errorValue;
                if (CreateFolderError.Tag.PATH == error.tag()) {
                    if (WriteError.Tag.CONFLICT == error.getPathValue().tag()) {
                        String parentName = "/".equals(parentId) ? accountDisplayName : parentId.substring(parentId.lastIndexOf('/') + 1);
                        throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(e, toCreate.getName(), parentName);
                    }
                }

                throw DropboxExceptionHandler.handleCreateFolderErrorException(e, fullpath, accountDisplayName);
            } catch (DbxException e) {
                throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
            }
        }

        String baseName = toCreate.getName();
        String fullpath = constructPath(parentId, baseName);

        NameBuilder name = null;
        while (true) {
            try {
                FolderMetadata folderMetadata = client.files().createFolder(fullpath);
                return folderMetadata.getPathDisplay();
            } catch (CreateFolderErrorException e) {
                CreateFolderError error = e.errorValue;
                if (CreateFolderError.Tag.PATH != error.tag()) {
                    throw DropboxExceptionHandler.handleCreateFolderErrorException(e, fullpath, accountDisplayName);
                }

                if (WriteError.Tag.CONFLICT != error.getPathValue().tag()) {
                    throw DropboxExceptionHandler.handleCreateFolderErrorException(e, fullpath, accountDisplayName);
                }

                // Compile a new name and retry...
                if (null == name) {
                    name = new NameBuilder(baseName);
                }
                fullpath = constructPath(parentId, name.advance().toString());
            } catch (DbxException e) {
                throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#updateFolder(java.lang.String, com.openexchange.file.storage.FileStorageFolder)
     */
    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        return identifier;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#moveFolder(java.lang.String, java.lang.String)
     */
    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        return moveFolder(folderId, newParentId, null);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        return moveFolder(folderId, newParentId, newName, true);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName, boolean autoRename) throws OXException {
        if (newName == null) {
            int lastIndex = folderId.lastIndexOf('/');
            newName = folderId.substring(lastIndex);
        }

        if (false == autoRename) {
            try {
                Metadata metadata = client.files().move(folderId, newParentId);
                return metadata.getPathDisplay();
            } catch (RelocationErrorException e) {
                RelocationError relocationError = e.errorValue;
                if (RelocationError.Tag.TO == relocationError.tag()) {
                    WriteError error = relocationError.getToValue();
                    if (WriteError.Tag.CONFLICT == error.tag()) {
                        String parentName = "/".equals(newParentId) ? accountDisplayName : newParentId.substring(newParentId.lastIndexOf('/') + 1);
                        throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(e, newName, parentName);
                    }
                }

                throw DropboxExceptionHandler.handleRelocationErrorException(e, folderId, "", accountDisplayName);
            } catch (DbxException e) {
                throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
            }
        }


        String baseName = newName;
        NameBuilder name = new NameBuilder(baseName);
        while (true) {
            try {
                String toPath = newParentId + name.toString();
                Metadata metadata = client.files().move(folderId, toPath);
                return metadata.getPathDisplay();
            } catch (RelocationErrorException e) {
                RelocationError relocationError = e.errorValue;
                if (RelocationError.Tag.TO != relocationError.tag()) {
                    throw DropboxExceptionHandler.handleRelocationErrorException(e, folderId, "", accountDisplayName);
                }

                WriteError error = relocationError.getToValue();
                if (WriteError.Tag.CONFLICT != error.tag()) {
                    throw DropboxExceptionHandler.handleRelocationErrorException(e, folderId, "", accountDisplayName);
                }

                // Compile a new name and retry...
                name.advance();
            } catch (DbxException e) {
                throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#renameFolder(java.lang.String, java.lang.String)
     */
    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        int lastIndex = folderId.lastIndexOf('/');
        String parentId = folderId.substring(0, lastIndex + 1);
        try {
            String newPath = parentId + newName;
            Metadata metadata = client.files().move(folderId, newPath);
            return metadata.getPathDisplay();
        } catch (RelocationErrorException e) {
            RelocationError relocationError = e.errorValue;
            if (RelocationError.Tag.TO == relocationError.tag()) {
                WriteError error = relocationError.getToValue();
                if (WriteError.Tag.CONFLICT == error.tag()) {
                    String parentName = "/".equals(parentId) ? accountDisplayName : parentId.substring(parentId.lastIndexOf('/') + 1);
                    throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(e, newName, parentName);
                }
            }

            throw DropboxExceptionHandler.handleRelocationErrorException(e, folderId, "", accountDisplayName);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#deleteFolder(java.lang.String)
     */
    @Override
    public String deleteFolder(String folderId) throws OXException {
        try {
            Metadata metadata = client.files().delete(folderId);
            return metadata.getName();
        } catch (DeleteErrorException e) {
            throw DropboxExceptionHandler.handleDeleteErrorException(e, folderId, "", accountDisplayName);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#deleteFolder(java.lang.String, boolean)
     */
    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        return deleteFolder(folderId);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#clearFolder(java.lang.String)
     */
    @Override
    public void clearFolder(String folderId) throws OXException {
        try {
            List<Metadata> entries = listFolder(folderId);
            for (Metadata entry : entries) {
                if (entry instanceof FolderMetadata) {
                    try {
                        client.files().delete(entry.getPathDisplay());
                    } catch (DeleteErrorException e) {
                        LOG.debug("The folder '{}' could not be deleted. Skipping.", entry.getPathDisplay(), e);
                    }
                }
            }
        } catch (ListFolderErrorException e) {
            throw DropboxExceptionHandler.handleListFolderErrorException(e, folderId);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#clearFolder(java.lang.String, boolean)
     */
    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        clearFolder(folderId);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getPath2DefaultFolder(java.lang.String)
     */
    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        String parentId = folderId;
        FileStorageFolder folder;
        List<FileStorageFolder> folders = new ArrayList<>();
        do {
            folder = getFolder(parentId);
            folders.add(folder);
        } while ((parentId = folder.getParentId()) != null);

        return folders.toArray(new FileStorageFolder[folders.size()]);
    }

    @Override
    public String[] getPathIds2DefaultFolder(String folderId) throws OXException {
        List<String> path = new LinkedList<>();
        String parentId = folderId;
        path.add(parentId);
        for (int index; (index = parentId.lastIndexOf('/')) >= 0;) {
            parentId = parentId.substring(0, index);
            path.add(parentId);
        }
        return path.toArray(new String[path.size()]);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getStorageQuota(java.lang.String)
     */
    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        try {
            SpaceUsage spaceUsage = client.users().getSpaceUsage();
            SpaceAllocation allocation = spaceUsage.getAllocation();

            IndividualSpaceAllocation individualValue = allocation.getIndividualValue();

            if (allocation.isTeam()) {
                TeamSpaceAllocation teamValue = allocation.getTeamValue();
                return new Quota(individualValue.getAllocated() + teamValue.getAllocated(), spaceUsage.getUsed(), Type.STORAGE);
            }

            return new Quota(individualValue.getAllocated(), spaceUsage.getUsed(), Type.STORAGE);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        } catch (IllegalStateException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getFileQuota(java.lang.String)
     */
    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        return Type.FILE.getUnlimited();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getQuotas(java.lang.String, com.openexchange.file.storage.Quota.Type[])
     */
    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
        if (null == types) {
            return null;
        }
        Quota[] quotas = new Quota[types.length];
        for (int i = 0; i < types.length; i++) {
            switch (types[i]) {
                case FILE:
                    quotas[i] = getFileQuota(folder);
                    break;
                case STORAGE:
                    quotas[i] = getStorageQuota(folder);
                    break;
                default:
                    throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create("Quota " + types[i]);
            }
        }
        return quotas;
    }

    ///////////////////////////////////////////// HELPERS //////////////////////////////////////////////

    /**
     * Check for sub folders
     *
     * @param folderId The folder to check for sub folders
     * @return <code>true</code> if at least one entry of the specified folder is of type {@link FolderMetadata}; <code>false</code> otherwise
     * @throws ListFolderErrorException If a list folder error is occurred
     * @throws DbxException if a generic Dropbox error is occurred
     */
    private boolean hasSubFolders(String folderId) throws ListFolderErrorException, DbxException {
        if (useOptimisticSubfolderDetection) {
            return true;
        }

        ListFolderResult listResult = client.files().listFolder(folderId);
        boolean hasMore;
        do {
            hasMore = listResult.getHasMore();

            List<Metadata> entries = listResult.getEntries();
            for (Metadata metadata : entries) {
                if (metadata instanceof FolderMetadata) {
                    return true;
                }
            }

            if (hasMore) {
                String cursor = listResult.getCursor();
                listResult = client.files().listFolderContinue(cursor);
            }
        } while (hasMore);
        return false;
    }

    /**
     * Construct a full path from the specified parent and folder name.
     * It simply concatenates both strings by using the '/' path separator.
     *
     * @param parent The parent folder
     * @param folder The folder name
     * @return The full path
     */
    private String constructPath(String parent, String folder) {
        if (isRoot(parent)) {
            parent = "/";
        }

        //Strip leading '/'
        if (folder.startsWith("/")) {
            folder = folder.substring(1);
        }

        StringBuilder builder = new StringBuilder();
        builder.append(parent);
        if (!parent.endsWith("/")) {
            builder.append("/");
        }
        builder.append(folder);
        return builder.toString();
    }
}
