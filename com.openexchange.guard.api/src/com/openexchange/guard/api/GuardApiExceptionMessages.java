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

package com.openexchange.guard.api;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link GuardApiExceptionMessages} - Exception messages for errors that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GuardApiExceptionMessages implements LocalizableStrings {

    // A Guard error occurred: %1$s
    public static final String GUARD_ERROR_MSG = "A Guard error occurred: %1$s";

    // A Guard error occurred: %1$s
    public static final String GUARD_SERVER_ERROR_MSG = "A Guard server error occurred with HTTP status code %1$s. Error message: %2$s";

    // Invalid Guard URL: %1$s
    public static final String INVALID_GUARD_URL_MSG = "The provided Guard URL: %1$s is invalid";

    // The Guard resource does not exist: %1$s
    public static final String NOT_FOUND_MSG = "The provided Guard resource does not exist: %1$s";

    // An I/O error occurred: %1$s
    public static final String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    // Authentication failed: %1$s
    public static final String AUTH_ERROR_MSG = "Authentication failed: %1$s";

    // The Guard resource does not exist
    public static final String NOT_FOUND_SIMPLE_MSG = "The Guard resource does not exist";

    /**
     * Initializes a new {@link GuardApiExceptionMessages}.
     */
    private GuardApiExceptionMessages() {
        super();
    }

}
