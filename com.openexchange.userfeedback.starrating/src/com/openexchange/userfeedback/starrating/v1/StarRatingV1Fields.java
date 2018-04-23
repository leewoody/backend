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

package com.openexchange.userfeedback.starrating.v1;

import java.util.HashSet;
import java.util.Set;
import org.json.JSONObject;

/**
 * {@link StarRatingV1Fields}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public enum StarRatingV1Fields {
    date("Date"), // only for export
    score("Score"),
    comment("Comment"),
    app("App"),
    entry_point("Entry Point"),
    operating_system("Operating System"),
    browser("Browser"),
    browser_version("Browser Version"),
    user_agent("User Agent"),
    screen_resolution("Screen Resolution"),
    language("Language"),
    user("User"), // only for export
    server_version("Server Version"), // only for export
    client_version("Client Version"),
    ;

    private static final Set<String> INTERNAL_KEYS = new HashSet<String>();

    static {
        for (StarRatingV1Fields field : StarRatingV1Fields.values()) {
            INTERNAL_KEYS.add(field.name().toLowerCase());
        }
    }

    private String displayName;

    StarRatingV1Fields(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the displayName
     *
     * @return The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns keys that are required within the to persist JSONObject. Those removed from {@link com.openexchange.userfeedback.starrating.v1.StarRatingV1Fields#values()} are retrieved from other tables.
     * 
     * @return Set of {@link String} that are required within the {@link JSONObject}
     */
    public static Set<String> requiredJsonKeys() {
        Set<String> copy = new HashSet<>(INTERNAL_KEYS);
        copy.remove("date");
        copy.remove("user");
        copy.remove("server_version");
        return copy;
    }
}