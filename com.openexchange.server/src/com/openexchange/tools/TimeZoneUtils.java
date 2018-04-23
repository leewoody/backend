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

package com.openexchange.tools;

import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link TimeZoneUtils} - Utility class for time zone.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TimeZoneUtils {

    /**
     * Initializes a new {@link TimeZoneUtils}.
     */
    private TimeZoneUtils() {
        super();
    }

    private static final ConcurrentMap<String, TimeZone> ZONE_CACHE = new ConcurrentHashMap<String, TimeZone>();

    /**
     * Gets the <code>TimeZone</code> for the given ID.
     *
     * @param ID The ID for a <code>TimeZone</code>, either an abbreviation such as "PST", a full name such as "America/Los_Angeles", or a
     *            custom ID such as "GMT-8:00".
     * @return The specified <code>TimeZone</code>, or the GMT zone if the given ID cannot be understood.
     */
    public static TimeZone getTimeZone(final String ID) {
        if (null == ID) {
            return ZONE_CACHE.get("GMT");
        }
        TimeZone tz = ZONE_CACHE.get(ID);
        if (tz == null) {
            final TimeZone tmp =  TimeZone.getTimeZone(ID);
            tz = ZONE_CACHE.putIfAbsent(ID, tmp);
            if (null == tz) {
                tz = tmp;
            }
        }
        return tz;
    }

    /**
     * Adds the time zone offset to given date millis.
     *
     * @param date The date millis
     * @param timeZone The time zone identifier
     * @return The date millis with time zone offset added
     */
    public static long addTimeZoneOffset(final long date, final String timeZone) {
        return addTimeZoneOffset(date, getTimeZone(timeZone));
    }

    /**
     * Adds the time zone offset to given date millis.
     *
     * @param date The date millis
     * @param timeZone The time zone
     * @return The date millis with time zone offset added
     */
    public static long addTimeZoneOffset(final long date, final TimeZone timeZone) {
        return (date + timeZone.getOffset(date));
    }

}
