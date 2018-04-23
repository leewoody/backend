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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns;


/**
 * {@link PushNotificationField} - The well-known fields for a push notification.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public enum PushNotificationField {

    // -------------------------------- Generic fields --------------------------------
    /** The key providing the actual message to display; type is <code>java.lang.String</code> */
    MESSAGE("message"),
    /** The item identifier; type is <code>java.lang.String</code> */
    ID("id"),
    /** The folder identifier; type is <code>java.lang.String</code> */
    FOLDER("folder"),
    /** A listing of arguments; type is <code>java.util.List</code> */
    ARGS("args"),

    // -------------------------------- Mail-related fields --------------------------------
    /** The subject of a mail; type is <code>java.lang.String</code> */
    MAIL_SUBJECT("subject"),
    /** The sender's address of a mail; type is <code>java.lang.String</code> */
    MAIL_SENDER_EMAIL("email"),
    /** The sender's address of a mail; type is <code>java.lang.String</code> */
    MAIL_SENDER_PERSONAL("displayname"),
    /** The unread count; type is <code>java.lang.Integer</code> */
    MAIL_UNREAD("unread"),
    /** The key providing the mail path; type is <code>java.lang.String</code> */
    MAIL_PATH("cid"),
    /** The teaser for the mail text; type is <code>java.lang.String</code> */
    MAIL_TEASER("teaser"),

    // -------------------------------- Calendar-related fields ----------------------------
    /** The title of an appointment; type is <code>java.lang.String</code> */
    APPOINTMENT_TITLE("title"),
    /** The location of an appointment; type is <code>java.lang.String</code> */
    APPOINTMENT_LOCATION("location"),
    /** The start date of an appointment; type is <code>java.util.Date</code> */
    APPOINTMENT_START_DATE("start_date"),
    /** The end date of an appointment; type is <code>java.util.Date</code> */
    APPOINTMENT_END_DATE("end_date"),

    ;

    private final String id;

    private PushNotificationField(String id) {
        this.id = id;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

}
