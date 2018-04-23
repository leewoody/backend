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

package com.openexchange.ajax.appointment.bugtests;

import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.FolderTestManager;

/**
 * {@link Bug38079Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.3
 */
public class Bug48149Test extends AbstractAJAXSession {

    private AJAXClient client3;
    private CalendarTestManager ctm2;
    private CalendarTestManager ctm3;
    private FolderTestManager ftm2;
    private FolderObject sharedFolder1;    
    private Appointment app1;
    private Appointment app2;

    public Bug48149Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();        
        client3 = new AJAXClient(testContext.acquireUser());
        ctm2 = new CalendarTestManager(getClient2());
        ctm3 = new CalendarTestManager(client3);
        ftm2 = new FolderTestManager(getClient2());

        // Remove all permissions
        FolderObject folderUpdate = new FolderObject(getClient().getValues().getPrivateAppointmentFolder());
        folderUpdate.setPermissionsAsArray(new OCLPermission[] { com.openexchange.ajax.folder.Create.ocl(
            getClient().getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION) });
        folderUpdate.setLastModified(new Date(Long.MAX_VALUE));
        ftm.updateFolderOnServer(folderUpdate);

        folderUpdate = new FolderObject(getClient2().getValues().getPrivateAppointmentFolder());
        folderUpdate.setPermissionsAsArray(new OCLPermission[] { com.openexchange.ajax.folder.Create.ocl(
            getClient2().getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION) });
        folderUpdate.setLastModified(new Date(Long.MAX_VALUE));
        ftm2.updateFolderOnServer(folderUpdate);

        // Add new shared folder.
        sharedFolder1 = ftm.generateSharedFolder("Shared Folder" + UUID.randomUUID().toString(), FolderObject.CALENDAR, getClient().getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId(), client3.getValues().getUserId());
        ftm.insertFolderOnServer(sharedFolder1);

        // Appointments not visible for user 3.
        app1 = new Appointment();
        app1.setTitle("app1");
        app1.setStartDate(TimeTools.D("07.08.2016 08:00"));
        app1.setEndDate(TimeTools.D("07.08.2016 09:00"));
        app1.setIgnoreConflicts(true);
        app1.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.insert(app1);

        app2 = new Appointment();
        app2.setTitle("app1");
        app2.setStartDate(TimeTools.D("07.08.2016 08:00"));
        app2.setEndDate(TimeTools.D("07.08.2016 09:00"));
        app2.setIgnoreConflicts(true);
        app2.setParentFolderID(getClient2().getValues().getPrivateAppointmentFolder());
        ctm2.insert(app2);
    }

    @Test
    public void testLoadAppointmentFromUserWithShared() throws Exception {
        try {
            ctm3.get(sharedFolder1.getObjectID(), app1.getObjectID());
        } catch (Exception e) {
            // ignore
        }
        assertTrue("Expected error.", ctm3.getLastResponse().hasError());
        assertTrue("Excpected something with permissions. (" + ctm3.getLastResponse().getErrorMessage() + ")", ctm3.getLastResponse().getErrorMessage().contains("ermission"));
    }

    @Test
    public void testLoadAppointmentFromUserWithoutAnyShares() throws Exception {
        try {
            ctm3.get(sharedFolder1.getObjectID(), app2.getObjectID());
        } catch (Exception e) {
            // ignore
        }
        assertTrue("Expected error.", ctm3.getLastResponse().hasError());
        assertTrue("Excpected something with permissions. (" + ctm3.getLastResponse().getErrorMessage() + ")", ctm3.getLastResponse().getErrorMessage().contains("ermission"));
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            ctm2.cleanUp();
            ctm3.cleanUp();
            ftm2.cleanUp();
            if (null != client3) {
                client3.logout();
                client3 = null;
            }
        } finally {
            super.tearDown();
        }
    }

}
