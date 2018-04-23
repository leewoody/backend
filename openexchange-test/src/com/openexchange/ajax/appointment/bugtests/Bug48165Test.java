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
import java.util.Calendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug42018Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.1
 */
public class Bug48165Test extends AbstractAJAXSession {

    private CalendarTestManager ctm2;
    private Appointment         conflict;
    private Appointment         series;
    private int                 nextYear;

    public Bug48165Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        ctm2 = new CalendarTestManager(getClient2());

        conflict = new Appointment();
        conflict.setTitle("Bug 48165 Test - conflict");
        conflict.setStartDate(TimeTools.D("03.08." + nextYear + " 11:00"));
        conflict.setEndDate(TimeTools.D("03.08." + nextYear + " 12:00"));
        conflict.setIgnoreConflicts(true);
        conflict.setParentFolderID(getClient2().getValues().getPrivateAppointmentFolder());

        series = new Appointment();
        series.setTitle("Bug 48165 Test - series");
        series.setStartDate(TimeTools.D("01.08." + nextYear + " 09:00"));
        series.setEndDate(TimeTools.D("01.08." + nextYear + " 10:00"));
        series.setRecurrenceType(Appointment.DAILY);
        series.setInterval(1);
        series.setIgnoreConflicts(true);
        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        series.setParticipants(new Participant[] { new UserParticipant(getClient().getValues().getUserId()), new UserParticipant(getClient2().getValues().getUserId()) });
    }

    @Test
    public void testBug48165() throws Exception {
        ctm2.insert(conflict);
        catm.insert(series);

        Appointment exception = new Appointment();
        exception.setObjectID(series.getObjectID());
        exception.setRecurrenceID(series.getObjectID()); // This is crucial
        exception.setRecurrenceType(Appointment.NO_RECURRENCE); // This is crucial
        exception.setParentFolderID(series.getParentFolderID());
        exception.setLastModified(series.getLastModified());
        exception.setRecurrencePosition(3);
        exception.setStartDate(TimeTools.D("03.08." + nextYear + " 11:00"));
        exception.setEndDate(TimeTools.D("03.08." + nextYear + " 12:00"));
        exception.setIgnoreConflicts(false);

        catm.update(exception);
        assertTrue("Expect conflicts.", catm.getLastResponse().hasConflicts());
        boolean found = false;
        for (ConflictObject conflictObject : catm.getLastResponse().getConflicts()) {
            if (conflictObject.getId() == conflict.getObjectID()) {
                found = true;
                break;
            }
        }
        assertTrue("Expect conflict.", found);
    }

    @After
    public void tearDown() throws Exception {
        try {
            ctm2.cleanUp();
        } finally {
            super.tearDown();
        }
    }

}
