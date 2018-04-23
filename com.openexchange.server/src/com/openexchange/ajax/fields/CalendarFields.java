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

package com.openexchange.ajax.fields;

public interface CalendarFields extends CommonFields {

    public static final String TITLE = "title";

    public static final String START_DATE = "start_date";

    public static final String END_DATE = "end_date";

    public static final String NOTE = "note";

    public static final String ALARM = "alarm";

    public static final String RECURRENCE_ID = "recurrence_id";

    public static final String OLD_RECURRENCE_POSITION = "pos";

    public static final String RECURRENCE_POSITION = "recurrence_position";

    public static final String RECURRENCE_DATE_POSITION = "recurrence_date_position";

    public static final String RECURRENCE_TYPE = "recurrence_type";

    public static final String RECURRENCE_START = "recurrence_start";

    public static final String CHANGE_EXCEPTIONS = "change_exceptions";

    public static final String DELETE_EXCEPTIONS = "delete_exceptions";

    public static final String DAYS = "days";

    public static final String DAY_IN_MONTH = "day_in_month";

    public static final String MONTH = "month";

    public static final String INTERVAL = "interval";

    public static final String UNTIL = "until";

    public static final String OCCURRENCES = "occurrences";

    public static final String NOTIFICATION = "notification";

    public static final String RECURRENCE_CALCULATOR = "recurrence_calculator";

    public static final String PARTICIPANTS = "participants";

    public static final String USERS = "users";

    public static final String CONFIRMATIONS = "confirmations";

    public static final String ORGANIZER = "organizer";

    public static final String ORGANIZER_ID = "organizerId";

    public static final String PRINCIPAL = "principal";

    public static final String PRINCIPAL_ID = "principalId";

    public static final String SEQUENCE = "sequence";

    static final String FULL_TIME = "full_time";

}
