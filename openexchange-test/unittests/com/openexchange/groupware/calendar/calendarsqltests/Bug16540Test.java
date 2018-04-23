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

package com.openexchange.groupware.calendar.calendarsqltests;

import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.osgi.service.event.Event;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.TimeZones;
import com.openexchange.tools.events.TestEventAdmin;

/**
 * {@link Bug16540Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug16540Test extends CalendarSqlTest {

    public Bug16540Test() {
        super();
    }

    @Test
    public void testShouldTriggerEventOnUpdateToAlarmFlag() throws OXException {
        Calendar c = TimeTools.createCalendar(TimeZones.UTC);
        c.add(Calendar.DATE, 1); // Must be in the future for the reminder to not be rejected.
        c.set(Calendar.HOUR, 10);
        final Date start = c.getTime();
        c.add(Calendar.HOUR, 2);
        final Date end = c.getTime();

        final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
        appointment.setAlarm(15);
        appointment.setAlarmFlag(true);

        appointments.save(appointment);
        clean.add(appointment);

        final CalendarDataObject update = appointments.createIdentifyingCopy(appointment);
        update.setAlarmFlag(false);
        update.setAlarm(-1);

        final TestEventAdmin eventAdmin = TestEventAdmin.getInstance();
        eventAdmin.clearEvents();
        appointments.save(update);

        List<Event> events = eventAdmin.getEvents();
        Event event = events.get(0);
        assertEquals("Wrong topic", "com/openexchange/groupware/appointment/update", event.getTopic());
        CommonEvent commonEvent = (CommonEvent) event.getProperty(CommonEvent.EVENT_KEY);
        assertEquals("Wrong action", CommonEvent.UPDATE, commonEvent.getAction());
        assertEquals("Wrong object identifier", appointment.getObjectID(), ((DataObject) commonEvent.getActionObj()).getObjectID());
        assertEquals("Wrong context", appointments.getSession().getContextId(), commonEvent.getContextId());
        assertEquals("Wrong module", Types.APPOINTMENT, commonEvent.getModule());
        assertEquals("Wrong folder", appointments.getPrivateFolder(), ((FolderObject) commonEvent.getSourceFolder()).getObjectID());
        assertEquals("Wrong user", appointments.getSession().getUserId(), commonEvent.getUserId());
    }
}
