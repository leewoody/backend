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

package com.openexchange.ajax.appointment;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * Attachment tests for appointments.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class AppointmentAttachmentTests extends AbstractAJAXSession {

    private int folderId;

    private TimeZone tz;

    private Appointment appointment;

    private int attachmentId;

    private Date creationDate;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderId = getClient().getValues().getPrivateAppointmentFolder();
        tz = getClient().getValues().getTimeZone();
        appointment = new Appointment();
        appointment.setTitle("Test appointment for testing attachments");
        Calendar calendar = TimeTools.createCalendar(tz);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setParentFolderID(folderId);
        appointment.setIgnoreConflicts(true);
        getClient().execute(new InsertRequest(appointment, tz)).fillAppointment(appointment);
        attachmentId = getClient().execute(new AttachRequest(appointment, "test.txt", new ByteArrayInputStream("Test".getBytes()), "text/plain")).getId();
        com.openexchange.ajax.attach.actions.GetResponse response = getClient().execute(new com.openexchange.ajax.attach.actions.GetRequest(appointment, attachmentId));
        long timestamp = response.getAttachment().getCreationDate().getTime();
        creationDate = new Date(timestamp - tz.getOffset(timestamp));
    }

    @After
    public void tearDown() throws Exception {
        try {
            getClient().execute(new DeleteRequest(appointment));
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testLastModifiedOfNewestAttachmentWithGet() throws Throwable {
        GetResponse response = getClient().execute(new GetRequest(appointment.getParentFolderID(), appointment.getObjectID()));
        appointment.setLastModified(response.getTimestamp());
        Appointment test = response.getAppointment(tz);
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }

    @Test
    public void testLastModifiedOfNewestAttachmentWithAll() throws Throwable {
        CommonAllResponse response = getClient().execute(new AllRequest(appointment.getParentFolderID(), new int[] { Appointment.OBJECT_ID, Appointment.LAST_MODIFIED_OF_NEWEST_ATTACHMENT }, appointment.getStartDate(), appointment.getEndDate(), tz, true));
        appointment.setLastModified(response.getTimestamp());
        Appointment test = null;
        int objectIdPos = response.getColumnPos(Appointment.OBJECT_ID);
        int lastModifiedOfNewestAttachmentPos = response.getColumnPos(Appointment.LAST_MODIFIED_OF_NEWEST_ATTACHMENT);
        for (Object[] objA : response) {
            if (appointment.getObjectID() == ((Integer) objA[objectIdPos]).intValue()) {
                test = new Appointment();
                test.setLastModifiedOfNewestAttachment(new Date(((Long) objA[lastModifiedOfNewestAttachmentPos]).longValue()));
                break;
            }
        }
        Assert.assertNotNull("Can not find the created appointment with an attachment.", test);
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }

    @Test
    public void testLastModifiedOfNewestAttachmentWithList() throws Throwable {
        CommonListResponse response = getClient().execute(new ListRequest(ListIDs.l(new int[] { appointment.getParentFolderID(), appointment.getObjectID() }), new int[] { Appointment.OBJECT_ID, Appointment.LAST_MODIFIED_OF_NEWEST_ATTACHMENT }));
        appointment.setLastModified(response.getTimestamp());
        Appointment test = null;
        int objectIdPos = response.getColumnPos(Appointment.OBJECT_ID);
        int lastModifiedOfNewestAttachmentPos = response.getColumnPos(Appointment.LAST_MODIFIED_OF_NEWEST_ATTACHMENT);
        for (Object[] objA : response) {
            if (appointment.getObjectID() == ((Integer) objA[objectIdPos]).intValue()) {
                test = new Appointment();
                test.setLastModifiedOfNewestAttachment(new Date(((Long) objA[lastModifiedOfNewestAttachmentPos]).longValue()));
                break;
            }
        }
        Assert.assertNotNull("Can not find the created appointment with an attachment.", test);
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }
}
