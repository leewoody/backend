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

package com.openexchange.ajax.find.tasks;

import static org.junit.Assert.fail;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.json.JSONException;
import org.junit.Before;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.VisibleFoldersRequest;
import com.openexchange.ajax.folder.actions.VisibleFoldersResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.find.basic.tasks.TaskType;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.tasks.TasksFacetType;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link FindTasksTestEnvironment}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FindTasksTestEnvironment extends AbstractFindTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FindTasksTestEnvironment.class);

    /** UserA's private test folder */
    private FolderObject userAprivateTestFolder;

    /** UserA's public test folder */
    private FolderObject userApublicTestFolder;

    /** UserB's private shared folder, Read-Only for UserA */
    private FolderObject userBsharedTestFolderRO;

    /** UserB's private shared folder, Read-Write for UserA */
    private FolderObject userBsharedTestFolderRW;

    /** UserB's private folder, No Access for UserA */
    private FolderObject userBprivateTestFolder;

    /** UserB's public folder */
    private FolderObject userBpublicTestFolder;

    private UserValues userA;

    private UserValues userB;

    /** List of Lists with filters */
    private List<List<ActiveFacet>> facets = new ArrayList<List<ActiveFacet>>();

    private enum Status {
        NOT_STARTED, IN_PROGRESS, DONE, WAITING, DEFERRED
    };

    private Set<Integer> tasksToFind = new HashSet<Integer>();

    private final static UUID trackingID = UUID.randomUUID();

    private Map<String, List<Integer>> rootTasks = new HashMap<String, List<Integer>>();

    private Map<Integer, Task> tasks = new HashMap<Integer, Task>();
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        try {
            initUsers();
            createFolderStructure();
            createAndInsertTasks();
            createFilters();
        } catch (Exception e) {
            LOG.error("Exception while setting up FindTasksTestEnvironment.", e);
            fail(e.getMessage());
        }
    }    

    /**
     * Initialize the users
     */
    private final void initUsers() throws Exception {
        userA = getClient().getValues();
        userB = getClient2().getValues();
    }

    /**
     * Create the test folder structure
     *
     * @throws IOException
     * @throws OXException
     * @throws Exception
     */
    private final void createFolderStructure() throws OXException, IOException, Exception {
        InsertRequest insertRequestReq;
        InsertResponse insertResponseResp;

        //Get current structure
        Map<String, FolderObject> foldersA = getFolderStructure(getClient(), userA.getPrivateTaskFolder());
        Map<String, FolderObject> foldersB = getFolderStructure(getClient2(), userB.getPrivateTaskFolder());

        String userAPrivateTaskFolder = "UserA - findAPIPrivateTaskFolder-" + UUID.randomUUID().toString();
        try {
            //create private test folder
            userAprivateTestFolder = Create.createPrivateFolder(userAPrivateTaskFolder, FolderObject.TASK, userA.getUserId());
            userAprivateTestFolder.setParentFolderID(userA.getPrivateTaskFolder());
            insertRequestReq = new InsertRequest(EnumAPI.OX_NEW, userAprivateTestFolder, false);
            insertResponseResp = getClient().execute(insertRequestReq);
            insertResponseResp.fillObject(userAprivateTestFolder);
            fttm.rememberFolderFromClientA(userAprivateTestFolder);
//            ftm.rememberCreatedItems(userAprivateTestFolder);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (userAprivateTestFolder.getObjectID() == 0) {
            userAprivateTestFolder.setObjectID(foldersA.get(userAPrivateTaskFolder).getObjectID()); //maybe contains to avoid null?
        }

        //create public test folder
        String userAPublicTaskFolder = "UserA - findAPIPublicTaskFolder-" + UUID.randomUUID().toString();
        try {
            userApublicTestFolder = Create.createPublicFolder(getClient(), userAPublicTaskFolder, FolderObject.TASK, false);
            fttm.rememberFolderFromClientA(userApublicTestFolder);
//            ftm.rememberCreatedItems(userApublicTestFolder);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (userApublicTestFolder.getObjectID() == 0) {
            userApublicTestFolder.setObjectID(foldersA.get(userAPublicTaskFolder).getObjectID());
        }

        String userBPrivateSharedTaskFolder = "UserB - findAPIPrivateSharedTaskFolder - RO-" + UUID.randomUUID().toString();
        try {
            //create shared folder, read-only
            userBsharedTestFolderRO = Create.createPrivateFolder(userBPrivateSharedTaskFolder, FolderObject.TASK, userB.getUserId());
            userBsharedTestFolderRO.setParentFolderID(userB.getPrivateTaskFolder());
            insertRequestReq = new InsertRequest(EnumAPI.OX_NEW, userBsharedTestFolderRO, false);
            insertResponseResp = getClient2().execute(insertRequestReq);
            insertResponseResp.fillObject(userBsharedTestFolderRO);
            fttm.rememberFolderFromClientB(userBsharedTestFolderRO);
//            ftm.rememberCreatedItems(userBsharedTestFolderRO);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (userBsharedTestFolderRO.getObjectID() == 0) {
            userBsharedTestFolderRO.setObjectID(foldersB.get(userBPrivateSharedTaskFolder).getObjectID());
        }

        try {
            //share read only folder to userA
            FolderTools.shareFolder(getClient2(), EnumAPI.OX_NEW, userBsharedTestFolderRO.getObjectID(), userA.getUserId(), OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);           
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String userBPrivateSharedTaskFolderRW = "UserB - findAPIPrivateSharedTaskFolder - RW-" + UUID.randomUUID().toString();
        try {
            //create shared folder, read/write
            userBsharedTestFolderRW = Create.createPrivateFolder(userBPrivateSharedTaskFolderRW, FolderObject.TASK, userB.getUserId());
            userBsharedTestFolderRW.setParentFolderID(userB.getPrivateTaskFolder());
            insertRequestReq = new InsertRequest(EnumAPI.OX_NEW, userBsharedTestFolderRW, false);
            insertResponseResp = getClient2().execute(insertRequestReq);
            insertResponseResp.fillObject(userBsharedTestFolderRW);
            fttm.rememberFolderFromClientB(userBsharedTestFolderRW);
//            ftm.rememberCreatedItems(userBsharedTestFolderRW);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (userBsharedTestFolderRW.getObjectID() == 0) {
            userBsharedTestFolderRW.setObjectID(foldersB.get(userBPrivateSharedTaskFolderRW).getObjectID());
        }

        try {
            //share read/write folder to userA
            FolderTools.shareFolder(getClient2(), EnumAPI.OX_NEW, userBsharedTestFolderRW.getObjectID(), userA.getUserId(), OCLPermission.READ_FOLDER, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String userBPrivateTaskFolderNA = "UserB - findAPIPrivateTaskFolder - NA-" + UUID.randomUUID().toString();
        try {
            //create userB's private folder
            userBprivateTestFolder = Create.createPrivateFolder(userBPrivateTaskFolderNA, FolderObject.TASK, userB.getUserId());
            userBprivateTestFolder.setParentFolderID(userB.getPrivateTaskFolder());
            insertRequestReq = new InsertRequest(EnumAPI.OX_NEW, userBprivateTestFolder, false);
            insertResponseResp = getClient2().execute(insertRequestReq);
            insertResponseResp.fillObject(userBprivateTestFolder);
            fttm.rememberFolderFromClientB(userBprivateTestFolder);
//            ftm.rememberCreatedItems(userBprivateTestFolder);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (userBprivateTestFolder.getObjectID() == 0) {
            userBprivateTestFolder.setObjectID(foldersB.get(userBPrivateTaskFolderNA).getObjectID());
        }

        String userBPublicTaskFolder = "UserB - findAPIPublicTaskFolder" + UUID.randomUUID().toString();
        try {
            //create public test folder for user B
            userBpublicTestFolder = Create.createPublicFolder(getClient2(), userBPublicTaskFolder, FolderObject.TASK, false);
            fttm.rememberFolderFromClientB(userBpublicTestFolder);
//            ftm.rememberCreatedItems(userBpublicTestFolder);
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (userBpublicTestFolder.getObjectID() == 0) {
            userBpublicTestFolder.setObjectID(foldersB.get(userBPublicTaskFolder).getObjectID());
        }
        
//        fttm.setClient2(getClient2());
    }

    /**
     * Get the folder structure of the specified folder
     * 
     * @param client
     * @param folderID
     * @return
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    private final Map<String, FolderObject> getFolderStructure(AJAXClient client, int folderID) throws Exception {
        Map<String, FolderObject> folders = new HashMap<String, FolderObject>();
        VisibleFoldersRequest lr = new VisibleFoldersRequest(EnumAPI.OX_NEW, "tasks");
        VisibleFoldersResponse response = client.execute(lr);
        Iterator<FolderObject> it = response.getPrivateFolders();
        while (it.hasNext()) {
            FolderObject fo = it.next();
            folders.put(fo.getFolderName(), fo);
        }

        it = response.getPublicFolders();
        while (it.hasNext()) {
            FolderObject fo = it.next();
            folders.put(fo.getFolderName(), fo);
        }

        it = response.getSharedFolders();
        while (it.hasNext()) {
            FolderObject fo = it.next();
            folders.put(fo.getFolderName(), fo);
        }

        return folders;
    }

    /**
     * Create and insert tasks to the previously created folder structure
     * Inserting :
     * - 5 tasks in both users' private folder
     * - 5 tasks in both users' public folder
     * - 5 tasks in user's B both shared folder (RO, RW for user A)
     * - 1 task with attachment in user's A private folder
     * - 1 task with 1 participants (1int) in user's B private folder
     * - 1 task with 2 participants (2int) in user's A private folder
     * - 1 task with 3 participants (2int,1ext) in user's B private folder
     * - 1 task with 2 participants (1int,1ext) in user's A private folder
     * - 1 task with 2 participants (2int) in user's B shared folder (series)
     * Total: 36 tasks
     * 
     * @throws Exception
     */
    private final void createAndInsertTasks() throws Exception {
        // Prepare assets
        UserParticipant usrPartA = new UserParticipant(userA.getUserId());
        UserParticipant usrPartB = new UserParticipant(userB.getUserId());
        ExternalUserParticipant extPart = new ExternalUserParticipant("foo@bar.org");

        //insert some tasks
        for (Status s : Status.values()) {
            for (FolderType ft : FolderType.values()) {
                switch (ft) {
                    case PUBLIC:
                        insertTask(getClient(), ft, s, userApublicTestFolder.getObjectID(), Collections.<Participant> emptyList(), false, false);
                        insertTask(getClient2(), ft, s, userBpublicTestFolder.getObjectID(), Collections.<Participant> emptyList(), false, false);
                        break;

                    case PRIVATE:
                        insertTask(getClient(), ft, s, userAprivateTestFolder.getObjectID(), Collections.<Participant> emptyList(), false, false);
                        insertTask(getClient2(), ft, s, userBprivateTestFolder.getObjectID(), Collections.<Participant> emptyList(), false, false);
                        break;

                    case SHARED:
                        insertTask(getClient2(), ft, s, userBsharedTestFolderRO.getObjectID(), Collections.<Participant> emptyList(), false, false);
                        insertTask(getClient2(), ft, s, userBsharedTestFolderRW.getObjectID(), Collections.<Participant> emptyList(), false, false);
                        break;
                }
            }
        }

        //insert a task with attachment in private with status not started for user a
        insertTask(getClient(), FolderType.PRIVATE, Status.NOT_STARTED, userAprivateTestFolder.getObjectID(), Collections.<Participant> emptyList(), true, false);

        //insert a task with no attachment in private with status deferred and 1 internal participants (b) for user B
        List<Participant> list = new ArrayList<Participant>();
        list.add(usrPartB);
        insertTask(getClient2(), FolderType.PRIVATE, Status.DEFERRED, userBprivateTestFolder.getObjectID(), list, false, false);

        //insert a task with no attachment in private with status done and 2 internal participants (a+b) for user A
        list.add(usrPartA);
        rememberTask(userB, insertTask(getClient(), FolderType.PRIVATE, Status.DONE, userAprivateTestFolder.getObjectID(), list, false, false));

        //insert a recurring task with attachment in shared folder with status not started and 2 internal participants for user b
        rememberTask(userB, insertTask(getClient2(), FolderType.SHARED, Status.NOT_STARTED, userBsharedTestFolderRO.getObjectID(), list, true, true));

        //insert a task with attachment in private with status in progress and 2 internal (a+b) and 1 external participant for user b
        list.add(extPart);
        rememberTask(userA, insertTask(getClient2(), FolderType.PRIVATE, Status.IN_PROGRESS, userBprivateTestFolder.getObjectID(), list, true, false));

        //insert a task with attachment in private with status not_started and 1 internal (a) and 1 external participant for user a
        list.clear();
        list.add(usrPartA);
        list.add(extPart);
        insertTask(getClient(), FolderType.PRIVATE, Status.NOT_STARTED, userAprivateTestFolder.getObjectID(), list, true, false);
        
        ttm.setClient2(getClient2());
    }

    /**
     * Read a file into a string
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    private final String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR) + fileName));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }
        if (br != null)
            br.close();
        return sb.toString();
    }

    /**
     * Remember tasks
     * 
     * @param u
     * @param t
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    private final void rememberTask(UserValues u, Task t) throws OXException, IOException, JSONException {
        List<Integer> list = rootTasks.get(u.getDefaultAddress());
        if (list == null)
            list = new ArrayList<Integer>();
        list.add(t.getObjectID());
        rootTasks.put(u.getDefaultAddress(), list);
    }

    /**
     * Helper method to insert tasks
     *
     * @param client the AJAXClient
     * @param ft FolderType
     * @param status Task's status
     * @param folder parent folder
     * @param recurrence of the task
     * @return inserted task
     * @throws Exception
     */
    private final Task insertTask(AJAXClient client, FolderType ft, Status status, int folder, List<Participant> participants, boolean attachment, boolean recurrence) throws Exception {
        StringBuilder builder = new StringBuilder();
        String title = builder.append("Find me, I am in a ").append(ft).append(" Folder - Hint User ").append(client.getValues().getDefaultAddress()).append(", trackingID: ").append(trackingID.toString()).toString();
        builder.setLength(0);
        String body = builder.append("User ").append(client.getValues().getDefaultAddress()).append("'s task in his ").append(ft).append(" folder and have status: ").append(status).toString();

        Task t = com.openexchange.groupware.tasks.Create.createWithDefaults(title, body, status.ordinal() + 1, folder);
        if (participants.size() > 0) {
            t.setParticipants(participants);
            builder.setLength(0);
            builder.append(t.getNote()).append(" and have ").append(participants.size()).append(" participants");
            t.setNote(builder.toString());
        }

        if (recurrence) {
            t.setStartDate(new Date(System.currentTimeMillis()));
            t.setInterval(1);
            t.setRecurrenceCount(10);
            t.setEndDate(new Date(System.currentTimeMillis() + 3600));
            t.setRecurrenceType(1);
        }

        if (attachment) {
            builder.setLength(0);
            builder.append(t.getNote()).append(" and have ATTACHMENT");
            t.setNote(builder.toString());
        }

        client.execute(new com.openexchange.ajax.task.actions.InsertRequest(t, client.getValues().getTimeZone())).fillTask(t);

        if (attachment)
            client.execute(new AttachRequest(t, "my cool attachment", new ByteArrayInputStream(readFile("attachment.base64").getBytes()), "image/jpeg"));

        t = client.execute(new com.openexchange.ajax.task.actions.GetRequest(folder, t.getObjectID())).getTask(client.getValues().getTimeZone());

        tasks.put(t.getObjectID(), t);
        ttm.addEntities(t);

        return t;
    }

    private final void createFilters() throws Exception {
        //create single filters
        //participants
        List<ActiveFacet> l = new ArrayList<ActiveFacet>(3);
        FacetType type = TasksFacetType.TASK_PARTICIPANTS;
        l.add(createActiveFacet(type, userA.getUserId(), createFilter("participant", Integer.toString(userA.getUserId()))));
        l.add(createActiveFacet(type, userB.getUserId(), createFilter("participant", Integer.toString(userB.getUserId()))));
        l.add(createActiveFacet(type, "foo@bar.org", createFilter("participant", "foo@bar.org"))); //external
        facets.add(l);

        //status
        l = new ArrayList<ActiveFacet>(5);
        type = TasksFacetType.TASK_STATUS;
        l.add(createActiveFacet(type, Task.NOT_STARTED, createFilter("status", Integer.toString(Task.NOT_STARTED))));
        l.add(createActiveFacet(type, Task.IN_PROGRESS, createFilter("status", Integer.toString(Task.IN_PROGRESS))));
        l.add(createActiveFacet(type, Task.DONE, createFilter("status", Integer.toString(Task.DONE))));
        l.add(createActiveFacet(type, Task.WAITING, createFilter("status", Integer.toString(Task.WAITING))));
        l.add(createActiveFacet(type, Task.DEFERRED, createFilter("status", Integer.toString(Task.DEFERRED))));
        facets.add(l);

        //folder type
        l = new ArrayList<ActiveFacet>(3);
        l.add(createFolderTypeFacet(FolderType.PRIVATE));
        l.add(createFolderTypeFacet(FolderType.PUBLIC));
        l.add(createFolderTypeFacet(FolderType.SHARED));
        facets.add(l);

        //type
        l = new ArrayList<ActiveFacet>(2);
        type = TasksFacetType.TASK_TYPE;
        l.add(createActiveFacet(type, 0, createFilter("type", TaskType.SINGLE_TASK.toString().toLowerCase()))); //single
        l.add(createActiveFacet(type, 1, createFilter("type", TaskType.SERIES.toString().toLowerCase()))); //series
        facets.add(l);
    }

    /**
     * Create a single filter
     * 
     * @param name
     * @param value
     * @return
     */
    private final Filter createFilter(String name, String value) {
        return new Filter(Collections.singletonList(name), value);
    }

    /**
     * Clean up the root tasks
     * 
     * @param client
     * @param map
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    private final void cleanRootTasks(AJAXClient client, List<Integer> list) throws OXException, IOException, JSONException {
        for (Integer i : list) {
            client.execute(new com.openexchange.ajax.task.actions.DeleteRequest(client.getValues().getPrivateTaskFolder(), i, new Date((System.currentTimeMillis() + 7300)))); //cheat and set a future last modified
        }
    }

    /**
     * Get the List of Lists of active facets
     * 
     * @return the List of Lists of active facets
     */
    public List<List<ActiveFacet>> getLoActiveFacets() {
        return facets;
    }

    /**
     * Set of task ids to find
     * 
     * @return
     */
    public final Set<Integer> getTasksToFind() {
        return tasksToFind;
    }

    /**
     * Get the tracking id for this test run
     * 
     * @return
     */
    public static final String getTrackingID() {
        return trackingID.toString();
    }

    /**
     * Get the global active facet with tracking id as query.
     */
    public final ActiveFacet createGlobalFacet() {
        return createGlobalFacet(trackingID.toString());
    }

    /**
     * Get the global active facet for an arbitrary query.
     */
    public final ActiveFacet createGlobalFacet(String query) {
        return new ActiveFacet(CommonFacetType.GLOBAL, "global", new Filter(Collections.singletonList("global"), query));
    }

    /**
     * Get a task
     * 
     * @param id
     * @return
     */
    public Task getTask(int id) {
        return tasks.get(id);
    }
}
