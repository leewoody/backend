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

package com.openexchange.ajax.drive.test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.drive.action.DeleteLinkRequest;
import com.openexchange.ajax.drive.action.GetLinkRequest;
import com.openexchange.ajax.drive.action.GetLinkResponse;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.TestInit;

/**
 * {@link DeleteLinkTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class DeleteLinkTest extends AbstractDriveShareTest {

    private InfostoreTestManager itm;
    private FolderObject rootFolder;
    private FolderObject folder;
    private DefaultFile file;

    public DeleteLinkTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        itm = new InfostoreTestManager(getClient());

        UserValues values = getClient().getValues();
        rootFolder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder());
        folder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), rootFolder.getObjectID());

        long now = System.currentTimeMillis();
        file = new DefaultFile();
        file.setFolderId(String.valueOf(folder.getObjectID()));
        file.setTitle("GetLinkTest_" + now);
        file.setFileName(file.getTitle());
        file.setDescription(file.getTitle());
        file.setFileMD5Sum(getChecksum(new File(TestInit.getTestProperty("ajaxPropertiesFile"))));
        itm.newAction(file, new File(TestInit.getTestProperty("ajaxPropertiesFile")));
    }

    @Test
    public void testDelete() throws Exception {
        DriveShareTarget target = new DriveShareTarget();
        target.setDrivePath("/" + folder.getFolderName());
        target.setName(file.getFileName());
        target.setChecksum(file.getFileMD5Sum());
        GetLinkRequest getLinkRequest = new GetLinkRequest(rootFolder.getObjectID(), target);
        GetLinkResponse getLinkResponse = getClient().execute(getLinkRequest);
        String url = getLinkResponse.getUrl();

        GuestClient guestClient = resolveShare(url, null, null);
        OCLGuestPermission expectedPermission = createAnonymousGuestPermission();
        expectedPermission.setEntity(guestClient.getValues().getUserId());
        guestClient.checkShareAccessible(expectedPermission);
        int guestID = guestClient.getValues().getUserId();

        getClient().execute(new DeleteLinkRequest(rootFolder.getObjectID(), target));
        ExtendedPermissionEntity guestEntity;
        if (target.isFolder()) {
            guestEntity = discoverGuestEntity(EnumAPI.OX_NEW, FolderObject.INFOSTORE, folder.getObjectID(), guestID);
        } else {
            guestEntity = discoverGuestEntity(file.getFolderId(), file.getId(), guestID);
        }
        assertNull("Share was not deleted", guestEntity);
        List<FileStorageObjectPermission> objectPermissions = getClient().execute(new GetInfostoreRequest(file.getId())).getDocumentMetadata().getObjectPermissions();
        assertTrue("Permission was not deleted", objectPermissions.isEmpty());
    }

    @After
    public void tearDown() throws Exception {
        try {
            itm.cleanUp();
        } finally {
            super.tearDown();
        }
    }

}
