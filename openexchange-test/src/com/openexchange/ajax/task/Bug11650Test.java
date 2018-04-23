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

package com.openexchange.ajax.task;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.SearchRequest;
import com.openexchange.ajax.task.actions.SearchResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.pool.TestUser;

/**
 * Checks if bug 11650 appears again.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug11650Test extends AbstractTaskTest {

    private TestUser owner;
    private TestUser sharee;

    /**
     * Default constructor.
     * 
     * @param name Name of the test.
     */
    public Bug11650Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Get users
        owner = testContext.acquireUser();
        sharee = testContext.acquireUser();
    }

    @After
    public void tearDown() throws Exception {
        // Context cleared, so need to back user
        super.tearDown();
    }

    /**
     * Checks if the search in shared task folder is broken.
     * 
     * @throws Throwable if an exception occurs.
     */
    @Test
    public void testSearchInSharedFolder() throws Throwable {
        final AJAXClient client;
        final AJAXClient client2;
        if (null == owner || null == sharee) {
            // Use fallback in case of missing user(s)
            client = getClient();
            client2 = getClient2();
        } else {
            client = new AJAXClient(owner);
            client2 = new AJAXClient(sharee);
        }
        final int privateTaskFID = client.getValues().getPrivateTaskFolder();
        final FolderObject folder = createFolder(client.getValues().getUserId(), client2.getValues().getUserId());
        folder.setParentFolderID(privateTaskFID);
        // Share a folder.
        final CommonInsertResponse fResponse = client.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        fResponse.fillObject(folder);
        final Task task = Create.createWithDefaults();
        task.setTitle("Bug11650Test");
        task.setParentFolderID(folder.getObjectID());
        try {
            {
                // Insert a task for searching for it.
                final InsertResponse insert = Executor.execute(client, new InsertRequest(task, client.getValues().getTimeZone()));
                insert.fillTask(task);
            }
            {
                // Search in that shared task folder.
                final TaskSearchObject search = new TaskSearchObject();
                search.setPattern("*");
                search.addFolder(folder.getObjectID());
                final SearchResponse response = Executor.execute(client2, new SearchRequest(search, SearchRequest.GUI_COLUMNS));
                assertThat("Searching for tasks in a shared folder failed.", !response.hasError());
            }
        } finally {
            client.execute(new DeleteRequest(EnumAPI.OX_OLD, folder.getObjectID(), folder.getLastModified()));
        }
    }

    private static FolderObject createFolder(final int owner, final int sharee) {
        assertThat("Owner and sharee are the same. Sharing is not possible", owner, is(not(sharee)));
        final FolderObject folder = new FolderObject();
        folder.setFolderName("Bug 11650 folder");
        folder.setModule(FolderObject.TASK);
        folder.setType(FolderObject.PRIVATE);
        final OCLPermission perm1 = new OCLPermission();
        perm1.setEntity(owner);
        perm1.setGroupPermission(false);
        perm1.setFolderAdmin(true);
        perm1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        final OCLPermission perm2 = new OCLPermission();
        perm2.setEntity(sharee);
        perm2.setGroupPermission(false);
        perm2.setFolderAdmin(false);
        perm2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        folder.setPermissionsAsArray(new OCLPermission[] { perm1, perm2 });
        return folder;
    }
}
