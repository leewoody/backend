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

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug19109Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug19109Test extends AbstractAJAXSession {

    private Appointment appointment;
    private Appointment updateAppointment;

    public Bug19109Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setStartDate(D("15.08.2011 08:00"));
        appointment.setEndDate(D("15.08.2011 09:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(4);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setTitle("Bug 19109 Test");
        appointment.setIgnoreConflicts(true);

        InsertRequest insertRequest = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = getClient().execute(insertRequest);
        insertResponse.fillAppointment(appointment);

        updateAppointment = new Appointment();
        updateAppointment.setObjectID(appointment.getObjectID());
        updateAppointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        updateAppointment.setStartDate(D("15.08.2011 06:00"));
        updateAppointment.setEndDate(D("15.08.2011 07:00"));
        updateAppointment.setRecurrenceType(Appointment.DAILY);
        updateAppointment.setInterval(1);
        updateAppointment.setOccurrence(4);
        updateAppointment.setIgnoreConflicts(true);
        updateAppointment.setLastModified(new Date(Long.MAX_VALUE));
    }

    @Test
    public void testBug19109() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(updateAppointment, getClient().getValues().getTimeZone());
        UpdateResponse updateResponse = getClient().execute(updateRequest);
        updateResponse.fillObject(appointment);

        AllRequest allRequest = new AllRequest(getClient().getValues().getPrivateAppointmentFolder(), new int[] { Appointment.OBJECT_ID }, new Date(1313452800000L), new Date(1313539200000L), getClient().getValues().getTimeZone());
        CommonAllResponse allResponse = getClient().execute(allRequest);
        assertTrue("Expected a result.", allResponse.getArray().length > 0);

        boolean found = false;
        Object[][] objects = allResponse.getArray();
        for (Object[] object : objects) {
            if ((Integer) object[0] == appointment.getObjectID()) {
                found = true;
            }
        }

        assertTrue("Expected Appointment Occurrence", found);
    }
}
