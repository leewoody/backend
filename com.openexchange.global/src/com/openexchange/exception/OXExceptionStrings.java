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

package com.openexchange.exception;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link OXExceptionStrings}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXExceptionStrings implements LocalizableStrings {

    // Text displayed to user if there is no message.
    public static final String DEFAULT_MESSAGE = "[Not available]";

    // The default message displayed to user.
    public static final String MESSAGE = "An error occurred inside the server which prevented it from fulfilling the request.";

    // The default message displayed to user when a re-try is recommended
    public static final String MESSAGE_RETRY = "A temporary error occurred inside the server which prevented it from fulfilling the request. Please try again later.";

    // The default message displayed to user when processing was intentionally denied.
    public static final String MESSAGE_DENIED = "The server is refusing to process the request.";

    // The general message for a conflicting update operation.
    public static final String MESSAGE_CONFLICT = "The object has been changed in the meantime.";

    // The general message for a missing object.
    public static final String MESSAGE_NOT_FOUND = "Object not found. %1$s";

    // The general message if a user has no access to a certain module (e.g. calendar)
    public static final String MESSAGE_PERMISSION_MODULE = "No permission for module: %1$s.";

    // The general message if a user has no permission to access a certain folder.
    public static final String MESSAGE_PERMISSION_FOLDER = "No folder permission.";

    // The general message for a missing field.
    public static final String MESSAGE_MISSING_FIELD = "Missing field: %s";

    // The general message if an error occurred while reading or writing to the database
    public static final String SQL_ERROR_MSG = "Error while reading/writing data from/to the database.";

    // General message if a setting cannot be put into database because it exceeds a column's capacity constraints
    public static final String DATA_TRUNCATION_ERROR_MSG = "Data cannot be stored into the database because it is too big";

    // The request sent by the client was syntactically incorrect
    public static final String BAD_REQUEST = "The request sent by the client was syntactically incorrect";

    /**
     * Initializes a new {@link OXExceptionStrings}.
     */
    public OXExceptionStrings() {
        super();
    }

}
