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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.ListRevisionsResult;
import com.dropbox.core.v2.files.LookupError;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.RelocationErrorException;
import com.dropbox.core.v2.files.SearchErrorException;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.SearchResult;
import com.dropbox.core.v2.files.ThumbnailErrorException;
import com.dropbox.core.v2.files.ThumbnailFormat;
import com.dropbox.core.v2.files.ThumbnailSize;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.UploadSessionFinishUploader;
import com.dropbox.core.v2.files.UploadSessionStartResult;
import com.dropbox.core.v2.files.UploadSessionStartUploader;
import com.dropbox.core.v2.files.UploadUploader;
import com.dropbox.core.v2.files.WriteMode;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAutoRenameFoldersAccess;
import com.openexchange.file.storage.FileStorageCaseInsensitiveAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageSequenceNumberProvider;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.dropbox.DropboxConstants;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.SizeKnowingInputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link DropboxFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropboxFileAccess extends AbstractDropboxAccess implements ThumbnailAware, FileStorageSequenceNumberProvider, FileStorageCaseInsensitiveAccess, FileStorageAutoRenameFoldersAccess {

    private static final Logger LOG = LoggerFactory.getLogger(DropboxFileAccess.class);

    private final DropboxAccountAccess accountAccess;
    private final DropboxFolderAccess folderAccess;
    private final int userId;
    private final String accountDisplayName;

    // 8 MB chunks
    private static final int CHUNK_SIZE = 8 * (int) Math.pow(1024, 2);

    private static final Field[] copyFields = { Field.ID, Field.FOLDER_ID, Field.VERSION, Field.FILE_SIZE, Field.FILENAME, Field.LAST_MODIFIED, Field.CREATED };

    /**
     * Initialises a new {@link DropboxFileAccess}.
     *
     * @throws OXException
     */
    public DropboxFileAccess(final AbstractOAuthAccess dropboxOAuthAccess, final FileStorageAccount account, final Session session, final DropboxAccountAccess accountAccess, DropboxFolderAccess folderAccess) throws OXException {
        super(dropboxOAuthAccess, account, session);
        this.accountAccess = accountAccess;
        this.folderAccess = folderAccess;
        accountDisplayName = account.getDisplayName();
        userId = session.getUserId();
    }

    @Override
    public String createFolder(FileStorageFolder toCreate, boolean autoRename) throws OXException {
        return folderAccess.createFolder(toCreate, autoRename);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName, boolean autoRename) throws OXException {
        return folderAccess.moveFolder(folderId, newParentId, newName, autoRename);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#exists(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean exists(String folderId, String id, String version) throws OXException {
        // Dropbox V2 API does not support metadata fetching for a specific version, thus it is being ignored.
        // More information https://www.dropbox.com/developers/documentation/http/documentation#files-get_metadata
        try {
            Metadata metadata = getMetadata(toPath(folderId, id));
            return metadata instanceof FileMetadata;
        } catch (GetMetadataErrorException e) {
            if (LookupError.NOT_FOUND.equals(e.errorValue.getPathValue())) {
                return false;
            }
            throw DropboxExceptionHandler.handleGetMetadataErrorException(e, folderId, id);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#getFileMetadata(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public File getFileMetadata(String folderId, String id, String version) throws OXException {
        // Dropbox V2 API does not support metadata fetching for a specific version, thus it is being ignored.
        // More information https://www.dropbox.com/developers/documentation/http/documentation#files-get_metadata
        try {
            FileMetadata metadata = getFileMetadata(folderId, id);
            DropboxFile dropboxFile = new DropboxFile(metadata, userId);
            //TODO: fetching all revisions just to get the number of versions is quite expensive;
            //      maybe we can introduce something like "-1" for "unknown number of versions"
            String path = toPath(folderId, id);
            ListRevisionsResult revisions = client.files().listRevisions(path, 100);
            if (revisions != null) {
                dropboxFile.setNumberOfVersions(revisions.getEntries().size());
            }
            return dropboxFile;
        } catch (GetMetadataErrorException e) {
            throw DropboxExceptionHandler.handleGetMetadataErrorException(e, folderId, id);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveFileMetadata(com.openexchange.file.storage.File, long)
     */
    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber) throws OXException {
        return saveFileMetadata(file, sequenceNumber, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveFileMetadata(com.openexchange.file.storage.File, long, java.util.List)
     */
    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        if (file.getId() == FileStorageFileAccess.NEW) {
            // Create new, empty file ("touch")
            UploadUploader upload = null;
            try {
                String path = toPath(file.getFolderId(), file.getFileName());
                upload = client.files().upload(path);
                FileMetadata metadata = upload.finish();
                DropboxFile dbxFile = new DropboxFile(metadata, userId);
                file.copyFrom(dbxFile, copyFields);
                return dbxFile.getIDTuple();
            } catch (DbxException e) {
                throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
            } finally {
                Streams.close(upload);
            }
        }

        // Update an existing file
        String path = toPath(file.getFolderId(), file.getId());

        // Rename
        if (modifiedFields != null && modifiedFields.contains(Field.FILENAME)) {
            String toPath = toPath(file.getFolderId(), file.getFileName());
            if (!path.equals(toPath)) {
                try {
                    if (Strings.equalsNormalizedIgnoreCase(path, toPath)) {
                        String filePath = toPath(file.getFolderId(), UUID.randomUUID().toString() + ' ' + file.getFileName());
                        Metadata metadata = client.files().move(path, filePath);
                        path = metadata.getPathDisplay();
                    }

                    Metadata metadata = client.files().move(path, toPath);
                    DropboxFile dbxFile = new DropboxFile((FileMetadata) metadata, userId);
                    file.copyFrom(dbxFile, copyFields);
                    return dbxFile.getIDTuple();
                } catch (RelocationErrorException e) {
                    throw DropboxExceptionHandler.handleRelocationErrorException(e, file.getFolderId(), file.getId(), accountDisplayName);
                } catch (DbxException e) {
                    throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
                }
            }
        }

        return new IDTuple(file.getFolderId(), file.getId());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#copy(com.openexchange.file.storage.FileStorageFileAccess.IDTuple, java.lang.String, java.lang.String, com.openexchange.file.storage.File, java.io.InputStream, java.util.List)
     */
    @Override
    public IDTuple copy(IDTuple source, String version, String destFolder, File update, InputStream newFile, List<Field> modifiedFields) throws OXException {
        if (version != CURRENT_VERSION) {
            // can only copy the current revision
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(DropboxConstants.ID);
        }
        checkFolderExistence(destFolder);
        String path = toPath(source.getFolder(), source.getId());
        String destName = null != update && null != modifiedFields && modifiedFields.contains(Field.FILENAME) ? update.getFileName() : source.getId();

        // Ensure filename uniqueness in target folder
        for (int i = 1; exists(destFolder, destName, CURRENT_VERSION); i++) {
            destName = FileStorageUtility.enhance(destName, i);
        }

        try {
            String destPath = toPath(destFolder, destName);

            // Copy
            Metadata metadata = client.files().copy(path, destPath);
            if (!(metadata instanceof FileMetadata)) {
                throw FileStorageExceptionCodes.NOT_A_FILE.create(DropboxConstants.ID, destPath);
            }
            DropboxFile dbxFile = new DropboxFile((FileMetadata) metadata, userId);
            if (update != null) {
                update.copyFrom(dbxFile, copyFields);
            }
            return dbxFile.getIDTuple();
        } catch (RelocationErrorException e) {
            throw DropboxExceptionHandler.handleRelocationErrorException(e, source.getFolder(), source.getId(), accountDisplayName);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#move(com.openexchange.file.storage.FileStorageFileAccess.IDTuple, java.lang.String, long, com.openexchange.file.storage.File, java.util.List)
     */
    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<Field> modifiedFields) throws OXException {
        checkFolderExistence(destFolder);
        String path = toPath(source.getFolder(), source.getId());
        String destName = null != update && null != modifiedFields && modifiedFields.contains(Field.FILENAME) ? update.getFileName() : source.getId();
        String destPath = toPath(destFolder, destName);

        try {
            Metadata metadata = client.files().move(path, destPath);
            if (!(metadata instanceof FileMetadata)) {
                throw FileStorageExceptionCodes.NOT_A_FILE.create(DropboxConstants.ID, destPath);
            }
            DropboxFile dbxFile = new DropboxFile((FileMetadata) metadata, userId);
            if (update != null) {
                update.copyFrom(dbxFile, copyFields);
            }
            return dbxFile.getIDTuple();
        } catch (RelocationErrorException e) {
            throw DropboxExceptionHandler.handleRelocationErrorException(e, source.getFolder(), source.getId(), accountDisplayName);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocument(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public InputStream getDocument(String folderId, String id, String version) throws OXException {
        try {
            DbxDownloader<FileMetadata> download = client.files().download(toPath(folderId, id));
            return new SizeKnowingInputStream(download.getInputStream(), download.getResult().getSize());
        } catch (DownloadErrorException e) {
            throw DropboxExceptionHandler.handleDownloadErrorException(e, folderId, id);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveDocument(com.openexchange.file.storage.File, java.io.InputStream, long)
     */
    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        return saveDocument(file, data, sequenceNumber, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveDocument(com.openexchange.file.storage.File, java.io.InputStream, long, java.util.List)
     */
    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        return saveDocument(file, data, modifiedFields);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeDocument(java.lang.String, long)
     */
    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        try {
            // Empty the folder (remove only the files; leave sub-folders intact)
            List<Metadata> entries = listFolder(folderId);

            for (Metadata entry : entries) {
                if (entry instanceof FileMetadata) {
                    try {
                        client.files().delete(entry.getPathDisplay());
                    } catch (DeleteErrorException e) {
                        LOG.debug("The file '{}' could not be deleted. Skipping.", entry.getPathDisplay(), e);
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
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeDocument(java.util.List, long)
     */
    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeDocument(java.util.List, long, boolean)
     */
    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber, boolean hardDelete) throws OXException {
        try {
            final List<IDTuple> ret = new ArrayList<IDTuple>(ids.size());
            for (IDTuple id : ids) {
                String path = toPath(id.getFolder(), id.getId());
                try {
                    client.files().delete(path);
                } catch (DeleteErrorException e) {
                    LOG.debug("The file '{}' could not be deleted. Skipping.", path, e);
                    ret.add(id);
                }
            }
            return ret;
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#touch(java.lang.String, java.lang.String)
     */
    @Override
    public void touch(String folderId, String id) throws OXException {
        exists(folderId, id, CURRENT_VERSION);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.lang.String)
     */
    @Override
    public TimedResult<File> getDocuments(String folderId) throws OXException {
        try {
            ListFolderResult listFolder = client.files().listFolder(folderId);

            int numberOfFiles = listFolder.getEntries().size();
            List<File> files = new ArrayList<>(numberOfFiles);

            for (Metadata metadata : listFolder.getEntries()) {
                if (metadata instanceof FileMetadata) {
                    files.add(new DropboxFile((FileMetadata) metadata, userId));
                }
            }

            return new FileTimedResult(files);
        } catch (ListFolderErrorException e) {
            throw DropboxExceptionHandler.handleListFolderErrorException(e, folderId);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.lang.String, java.util.List)
     */
    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields) throws OXException {
        return getDocuments(folderId);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.lang.String, java.util.List, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection)
     */
    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        try {
            ListFolderResult listFolder = client.files().listFolder(folderId);
            Iterator<Metadata> iterator = listFolder.getEntries().iterator();
            final List<File> files = new ArrayList<File>(listFolder.getEntries().size());
            while (iterator.hasNext()) {
                Metadata next = iterator.next();
                if (next instanceof FileMetadata) {
                    files.add(new DropboxFile((FileMetadata) next, userId));
                }
            }
            sort(files, sort, order);
            return new FileTimedResult(files);
        } catch (ListFolderErrorException e) {
            throw DropboxExceptionHandler.handleListFolderErrorException(e, folderId);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.util.List, java.util.List)
     */
    @Override
    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> fields) throws OXException {
        List<File> files = new ArrayList<>(ids.size());
        Map<String, List<String>> filesPerFolder = getFilesPerFolder(ids);
        if (filesPerFolder.size() == 1 && filesPerFolder.values().iterator().next().size() > 2) {
            // Seems like a 'list' request for multiple items from one folder
            String folderId = filesPerFolder.keySet().iterator().next();
            String path = toPath(folderId);
            try {
                ListFolderResult listFolder = client.files().listFolder(path);
                for (IDTuple id : ids) {
                    for (Metadata metadata : listFolder.getEntries()) {
                        if (id.getId().equals(metadata.getName()) && metadata instanceof FileMetadata) {
                            files.add(new DropboxFile((FileMetadata) metadata, userId));
                            break;
                        }
                    }
                }
            } catch (ListFolderErrorException e) {
                throw DropboxExceptionHandler.handleListFolderErrorException(e, path);
            } catch (DbxException e) {
                throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
            }
        } else {
            // Load metadata one-by-one
            for (IDTuple id : ids) {
                try {
                    FileMetadata metadata = getFileMetadata(id.getFolder(), id.getId());
                    files.add(new DropboxFile(metadata, userId));
                } catch (OXException e) {
                    // Skip non-existing files
                    if (!FileStorageExceptionCodes.NOT_A_FILE.equals(e)) {
                        throw e;
                    }
                } catch (GetMetadataErrorException e) {
                    // Skip non-existing files
                    if (!LookupError.NOT_FOUND.equals(e.errorValue.getPathValue())) {
                        throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                } catch (DbxException e) {
                    throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
                }
            }
        }
        return new FileTimedResult(files);
    }

    private static final SearchIterator<File> EMPTY_ITER = SearchIteratorAdapter.emptyIterator();

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDelta(java.lang.String, long, java.util.List, boolean)
     */
    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDelta(java.lang.String, long, java.util.List, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection, boolean)
     */
    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#search(java.lang.String, java.util.List, java.lang.String, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection, int, int)
     */
    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        return search(pattern, fields, folderId, false, sort, order, start, end);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#search(java.lang.String, java.util.List, java.lang.String, boolean, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection, int, int)
     */
    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        if (folderId == null) {
            // Fall-back to root folder
            folderId = "";
        }

        // Search
        List<File> results = search(pattern, folderId, includeSubfolders);

        // Sort results
        sort(results, sort, order);

        // Range (if needed)
        results = range(results, start, end);
        return new SearchIteratorAdapter<File>(results.iterator(), results.size());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageFileAccess#getAccountAccess()
     */
    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.tx.TransactionAware#startTransaction()
     */
    @Override
    public void startTransaction() throws OXException {
        // no op
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.tx.TransactionAware#commit()
     */
    @Override
    public void commit() throws OXException {
        // no op
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.tx.TransactionAware#rollback()
     */
    @Override
    public void rollback() throws OXException {
        // no op
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.tx.TransactionAware#finish()
     */
    @Override
    public void finish() throws OXException {
        // no op
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.tx.TransactionAware#setTransactional(boolean)
     */
    @Override
    public void setTransactional(boolean transactional) {
        // no op
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.tx.TransactionAware#setRequestTransactional(boolean)
     */
    @Override
    public void setRequestTransactional(boolean transactional) {
        // no op
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.tx.TransactionAware#setCommitsTransaction(boolean)
     */
    @Override
    public void setCommitsTransaction(boolean commits) {
        // no op
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.FileStorageSequenceNumberProvider#getSequenceNumbers(java.util.List)
     */
    @Override
    public Map<String, Long> getSequenceNumbers(List<String> folderIds) throws OXException {
        Map<String, Long> sequenceNumbers = new HashMap<>(folderIds.size());
        for (String folderId : folderIds) {
            try {
                FolderMetadata metadata = getFolderMetadata(folderId);
                sequenceNumbers.put(folderId, getSequenceNumber(metadata));
            } catch (GetMetadataErrorException e) {
                throw DropboxExceptionHandler.handleGetMetadataErrorException(e, folderId, "");
            } catch (DbxException e) {
                throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
            }
        }
        return sequenceNumbers;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.file.storage.ThumbnailAware#getThumbnailStream(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public InputStream getThumbnailStream(String folderId, String id, String version) throws OXException {
        try {
            DbxDownloader<FileMetadata> dbxDownloader = client.files().getThumbnailBuilder(toPath(folderId, id)).withFormat(ThumbnailFormat.JPEG).withSize(ThumbnailSize.W128H128).start();
            return dbxDownloader.getInputStream();
        } catch (ThumbnailErrorException e) {
            throw DropboxExceptionHandler.handleThumbnailErrorException(e, folderId, id);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /**
     * Sorts the supplied list of files if needed.
     *
     * @param files The files to sort
     * @param sort The sort order, or <code>null</code> if not specified
     * @param order The sort direction
     */
    private void sort(List<File> files, Field sort, SortDirection order) {
        if (null != sort && 1 < files.size()) {
            Collections.sort(files, order.comparatorBy(sort));
        }
    }

    /**
     * Generates a mostly unique sequence number for the supplied folder entry, based on the contained {@link FolderMetadata#hashCode()}.
     *
     * @param FolderMetadata The {@link FolderMetadata}
     * @return The sequence number
     */
    private long getSequenceNumber(FolderMetadata metadata) {
        long hashCode = metadata.hashCode();
        return Math.abs(hashCode);
    }

    /**
     * Saves the specified data
     *
     * @param file The {@link File} containing all metadata information
     * @param data The actual data to save
     * @return The {@link IDTuple} of the uploaded file
     * @throws OXException If the file cannot be uploaded or any other error is occurred
     */
    private IDTuple saveDocument(File file, InputStream data, List<Field> modifiedFields) throws OXException {

        checkFolderExistence(file.getFolderId());

        if ((null == modifiedFields || modifiedFields.contains(Field.FILENAME)) && false == Strings.isEmpty(file.getFileName()) && file.getId() != FileStorageFileAccess.NEW)
        /*
         * first check if there is already such a file
         */
        {
            String path = toPath(file.getFolderId(), file.getId());
            String toPath = toPath(file.getFolderId(), file.getFileName());
            if (!path.equals(toPath)) {
                Metadata metaData;
                try {
                    metaData = client.files().getMetadata(toPath);
                    if (metaData != null) {
                        throw FileStorageExceptionCodes.FILE_ALREADY_EXISTS.create();
                    }
                } catch (DbxException e) {
                    // ignore
                }
            }
        }

        long fileSize = file.getFileSize();
        DropboxFile dbxFile = fileSize > CHUNK_SIZE ? sessionUpload(file, data) : singleUpload(file, data);
        file.copyFrom(dbxFile, copyFields);
        return dbxFile.getIDTuple();
    }

    private void checkFolderExistence(String folderId) throws OXException{
        try {
            if(Strings.isEmpty(folderId) || folderId.equals("/")){
                return; // The root folder is always present
            }
            getFolderMetadata(folderId);
        } catch (GetMetadataErrorException e) {
            OXException interpretedException = DropboxExceptionHandler.handleGetMetadataErrorException(e, folderId, "");
            if (interpretedException.getExceptionCode() == FileStorageExceptionCodes.NOT_FOUND) {
                throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
            }
            throw interpretedException;
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /**
     * Uploads the specified file in chunks
     *
     * @param file The {@link File} to upload
     * @param data The {@link InputStream} containing the actual data
     * @return The {@link IDTuple} of the uploaded file
     * @throws OXException If an error is occurred
     */
    private DropboxFile sessionUpload(File file, InputStream data) throws OXException {
        ThresholdFileHolder sink = null;
        try {
            sink = new ThresholdFileHolder();
            sink.write(data);

            // Work with local stream
            InputStream stream = sink.getStream();

            // Start an upload session and get the session id
            UploadSessionStartUploader uploadSession = client.files().uploadSessionStart();
            UploadSessionStartResult result = uploadSession.uploadAndFinish(stream, CHUNK_SIZE);
            String sessionId = result.getSessionId();
            long offset = CHUNK_SIZE;

            // Start uploading chunks of data
            UploadSessionCursor cursor = new UploadSessionCursor(sessionId, offset);
            while (sink.getCount() - offset > CHUNK_SIZE) {
                client.files().uploadSessionAppendV2(cursor).uploadAndFinish(stream, CHUNK_SIZE);
                offset += CHUNK_SIZE;
                cursor = new UploadSessionCursor(sessionId, offset);
            }

            // Upload the remaining chunk
            long remaining = sink.getCount() - offset;
            CommitInfo commitInfo = new CommitInfo(toPath(file.getFolderId(), file.getFileName()), WriteMode.OVERWRITE, false, file.getLastModified(), false);
            UploadSessionFinishUploader sessionFinish = client.files().uploadSessionFinish(cursor, commitInfo);
            FileMetadata metadata = sessionFinish.uploadAndFinish(stream, remaining);

            // Return
            return new DropboxFile(metadata, userId);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(sink);
        }
    }

    /**
     * Uploads the specified file in a single request
     *
     * @param file The {@link File} to upload
     * @param data The {@link InputStream} containing the actual data
     * @return The {@link IDTuple} of the uploaded file
     * @throws OXException if an error is occurred
     */
    private DropboxFile singleUpload(File file, InputStream data) throws OXException {
        String name = file.getFileName();
        if(name==null){
            name = file.getId();
        }
        String path = new StringBuilder(file.getFolderId()).append('/').append(name).toString();
        try {
            UploadBuilder builder = client.files().uploadBuilder(path).withMode(WriteMode.OVERWRITE).withAutorename(false);
            FileMetadata metadata = builder.uploadAndFinish(data);
            return new DropboxFile(metadata, userId);
        } catch (UploadErrorException e) {
            throw DropboxExceptionHandler.handleUploadErrorException(e, path, accountDisplayName);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Maps the file identifiers of the supplied ID tuples to their parent folder identifiers.
     *
     * @param ids The ID tuples to map
     * @return The mapped identifiers
     */
    private Map<String, List<String>> getFilesPerFolder(List<IDTuple> ids) {
        Map<String, List<String>> filesPerFolder = new HashMap<String, List<String>>();
        for (IDTuple id : ids) {
            List<String> files = filesPerFolder.get(id.getFolder());
            if (null == files) {
                files = new ArrayList<String>();
                filesPerFolder.put(id.getFolder(), files);
            }
            files.add(id.getId());
        }
        return filesPerFolder;
    }

    /**
     * Search under the specified folder for the specified pattern
     *
     * @param pattern The pattern to search for
     * @param folderId The folder identifier (full path)
     * @param includeSubfolders If the sub-folders will be included in the search
     * @return A list with {@link File}s matching the specified pattern
     * @throws OXException if an error is occurred
     */
    private List<File> search(String pattern, String folderId, boolean includeSubfolders) throws OXException {
        try {
            if (Strings.isEmpty(pattern) || pattern.equals("*")) {
                // Return everything
                return getAllFiles(folderId, includeSubfolders);
            } else {
                // Search
                return fireSearch(folderId, pattern, includeSubfolders);
            }
        } catch (SearchErrorException e) {
            throw DropboxExceptionHandler.handleSearchErrorException(e, folderId, pattern);
        } catch (ListFolderErrorException e) {
            throw DropboxExceptionHandler.handleListFolderErrorException(e, folderId);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, session, dropboxOAuthAccess.getOAuthAccount());
        }
    }

    /**
     * Retrieves all files from the specified folder.
     *
     * @param folderId The folder path
     * @param recursive If set to true then it retrieves all files recursively from all folders under the specified folder
     * @return A list with {@link File}s
     * @throws ListFolderErrorException if a list error is occurred
     * @throws DbxException if a generic Dropbox error is occurred
     */
    private List<File> getAllFiles(String folderId, boolean recursive) throws ListFolderErrorException, DbxException {
        List<File> results = new ArrayList<File>();
        ListFolderResult listFolderResult = client.files().listFolderBuilder(folderId).withRecursive(recursive).start();
        boolean hasMore = false;
        do {
            hasMore = listFolderResult.getHasMore();

            List<Metadata> entries = listFolderResult.getEntries();
            for (Metadata metadata : entries) {
                if (metadata instanceof FileMetadata) {
                    results.add(new DropboxFile((FileMetadata) metadata, userId));
                }
            }
            if (hasMore) {
                String cursor = listFolderResult.getCursor();
                listFolderResult = client.files().listFolderContinue(cursor);
            }
        } while (hasMore);

        return results;
    }

    /**
     * Searches in the specified folder for the specified pattern (fires the actual search request to Dropbox.
     *
     * @param folderId The folder identifier (full path)
     * @param pattern The pattern to search for
     * @param includeSubfolders If the sub-folders will be included in the search
     * @return A list with {@link File}s matching the specified pattern
     * @throws SearchErrorException if a search error is occurred
     * @throws DbxException if a generic Dropbox error is occurred
     */
    private List<File> fireSearch(String folderId, String pattern, boolean includeSubfolders) throws SearchErrorException, DbxException {
        SearchResult searchResult = client.files().searchBuilder(folderId, pattern).start();

        List<File> results = new ArrayList<File>();
        boolean hasMore = false;
        do {
            hasMore = searchResult.getMore();
            List<SearchMatch> matches = searchResult.getMatches();
            for (SearchMatch match : matches) {
                Metadata metadata = match.getMetadata();
                String parent = getParent(metadata.getPathDisplay());
                if (metadata instanceof FileMetadata && (includeSubfolders || folderId.equals(parent))) {
                    results.add(new DropboxFile((FileMetadata) metadata, userId));
                }
            }
            if (hasMore) {
                long start = searchResult.getStart();
                searchResult = client.files().searchBuilder(folderId, pattern).withStart(start).start();
            }
        } while (hasMore);

        return results;
    }

    /**
     * Returns a sub-list starting from the specified index and ending to the specified index
     *
     * @param files The {@link List} of {@link File}s
     * @param startIndex The start index
     * @param endIndex The end index
     * @return The sub-list
     */
    private List<File> range(List<File> files, int startIndex, int endIndex) {
        if (startIndex == NOT_SET && endIndex == NOT_SET) {
            return files;
        }
        if (startIndex > files.size()) {
            return Collections.emptyList();
        }
        if (endIndex > files.size()) {
            endIndex = files.size();
        }
        return files.subList(startIndex, endIndex);
    }
}
