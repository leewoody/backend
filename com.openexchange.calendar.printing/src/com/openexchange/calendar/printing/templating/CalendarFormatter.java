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

package com.openexchange.calendar.printing.templating;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.calendar.itip.Messages;
import com.openexchange.i18n.tools.StringHelper;


/**
 * {@link CalendarFormatter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CalendarFormatter {
    private DateFormat timeFormat;
    private DateFormat dateFormat;
    private DateFormat weekdayFormat;

    private final Map<String, Object> appointment;
    private Locale locale;
    private TimeZone timezone;
    private final TimeZone utc = TimeZone.getTimeZone("UTC");


    public CalendarFormatter(Map<String, Object> appointment, Locale locale, TimeZone tz) {
        super();
        this.appointment = appointment;
        if (locale != null && tz != null) {
            timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
            dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
            this.locale = locale;
            this.timezone = tz;
            weekdayFormat = new SimpleDateFormat("E", locale);

            timeFormat.setTimeZone(timezone);
            dateFormat.setTimeZone(timezone);
            weekdayFormat.setTimeZone(timezone);
        }
    }
    
    public String getInterval() {
        return formatInterval(appointment);
    }


    public String getDateSpec() {
        StringBuilder b = new StringBuilder();
        b.append(formatDate(appointment));
        return b.toString();
    }


    public String formatDate(Map<String, Object> appointment) {
        Date startDate = new Date((Long) appointment.get("start_date"));
        Date endDate = new Date((Long) appointment.get("end_date"));
        if (isFullTime()) {
            endDate = new Date(endDate.getTime()-1000);
        }

        if (differentDays(startDate, endDate)) {
            if (isFullTime()) {
                return String.format("%s - %s", formatDate(startDate, utc), formatDate(endDate, utc));
            } else {
                return String.format("%s - %s", formatDate(startDate), formatDate(endDate));
            }
        } else {
            return formatDate(startDate);
        }
    }

    public String formatDate(Date date) {
        if (date == null || dateFormat == null) {
            return "";
        }
        return dateFormat.format(date);
    }

    public String formatDate(Date date, TimeZone timezone) {
        if (date == null || dateFormat == null) {
            return "";
        }
        DateFormat format = (DateFormat) dateFormat.clone();
        format.setTimeZone(timezone);
        return format.format(date);
    }


    public String formatTime(Date date) {
        return timeFormat.format(date);
    }

    public String formatInterval(Map<String, Object> appointment) {
        if (isFullTime()) {
            return StringHelper.valueOf(locale).getString(Messages.FULL_TIME);
        }
        // TODO: Longer than a day
        Date startDate = new Date((Long) appointment.get("start_date"));
        Date endDate = new Date((Long) appointment.get("end_date"));
        
        if (differentDays(startDate, endDate)) {
            if (differentWeeks(startDate, endDate)) {
                return formatTimeAndDay(startDate) + " - " + formatTimeAndDay(endDate);
            } else {
                return formatTimeAndWeekday(startDate) + " - " + formatTimeAndWeekday(endDate);
            }
        } else {
            return formatTime(startDate) + " - " + formatTime(endDate) + " (" + timezone.getDisplayName(locale) + ")";
        }
    }

    private boolean differentDays(Date startDate, Date endDate) {
        GregorianCalendar cal1 = new GregorianCalendar();
        cal1.setTime(startDate);
        cal1.setTimeZone(timezone);

        GregorianCalendar cal2 = new GregorianCalendar();
        cal2.setTime(endDate);
        cal2.setTimeZone(timezone);

        return cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR) || cal1.get(Calendar.DAY_OF_YEAR) != cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean differentWeeks(Date startDate, Date endDate) {
        GregorianCalendar cal1 = new GregorianCalendar();
        cal1.setTimeZone(timezone);
        cal1.setTime((new Date(startDate.getTime())));

        GregorianCalendar cal2 = new GregorianCalendar();
        cal2.setTimeZone(timezone);
        cal2.setTime(new Date(endDate.getTime()));

        return cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR) || cal1.get(Calendar.WEEK_OF_YEAR) != cal2.get(Calendar.WEEK_OF_YEAR);
    }

    private String formatTimeAndDay(Date date) {
        return String.format("%s, %s (%s)", formatDate(date), formatTime(date), timezone.getDisplayName(locale));
    }

    private String formatTimeAndWeekday(Date date) {
        return String.format("%s, %s (%s)", weekdayFormat.format(date), formatTime(date), timezone.getDisplayName(locale));
    }
    
    private boolean isFullTime() {
        Boolean fullTime = (Boolean) appointment.get("full_time");
        return fullTime == null || fullTime;
    }
    
}
