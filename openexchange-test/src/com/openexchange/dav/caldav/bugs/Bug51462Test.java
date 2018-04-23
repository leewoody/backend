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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.*;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.UserAgents;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.util.TimeZones;

/**
 * {@link Bug51462Test}
 *
 * Thunderbird Lightning only: Full day Appointment can not be set to regular Apppointment
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug51462Test extends CalDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.LIGHTNING_4_7_7;
    }

    @Test
    public void testRemoveAllDay() throws Exception {
        /*
         * create all-day event at client
         */
        String uid = randomUID();

        Calendar calendar = Calendar.getInstance(TimeZones.UTC);
        calendar.setTime(TimeTools.D("tomorrow at midnight", TimeZones.UTC));
        Date start = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date end = calendar.getTime();
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:Bug51462Test\r\n" +
            "DTSTART;VALUE=DATE:" + formatAsDate(start) + "\r\n" +
            "DTEND;VALUE=DATE:" + formatAsDate(end) + "\r\n" +
            "DTEND;VALUE=DATE:20170208\r\n" +
            "TRANSP:TRANSPARENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertTrue("not fulltime", appointment.getFullTime());
        /*
         * verify event on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("DTSTART wrong", "DATE", iCalResource.getVEvent().getProperty("DTSTART").getAttribute("VALUE"));
        assertEquals("DTEND wrong", "DATE", iCalResource.getVEvent().getProperty("DTEND").getAttribute("VALUE"));
        /*
         * update event on client, set start- and endtime
         */
        Date newStart = TimeTools.D("tomorrow at 16:00", TimeZone.getTimeZone("Europe/Berlin"));
        Date newEnd = TimeTools.D("tomorrow at 17:00", TimeZone.getTimeZone("Europe/Berlin"));
        iCalResource.getVEvent().setProperty("DTSTART", format(newStart, "Europe/Berlin"), Collections.singletonMap("TZID", "Europe/Berlin"));
        iCalResource.getVEvent().setProperty("DTEND", format(newEnd, "Europe/Berlin"), Collections.singletonMap("TZID", "Europe/Berlin"));
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify event on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNull("DTSTART wrong", iCalResource.getVEvent().getProperty("DTSTART").getAttribute("VALUE"));
        assertNull("DTEND wrong", iCalResource.getVEvent().getProperty("DTEND").getAttribute("VALUE"));
        /*
         * verify appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertFalse("still fulltime", appointment.getFullTime());
    }

}
