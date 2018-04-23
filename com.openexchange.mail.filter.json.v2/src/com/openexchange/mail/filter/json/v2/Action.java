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

package com.openexchange.mail.filter.json.v2;

import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * Enumeration of all possible mail filter module actions.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum Action {
    /**
     * Get config object
     */
    CONFIG("config"),
    /**
     * Uploads a new object to the server.
     */
    NEW("new"),
    /**
     * Reorders the rules on the server side
     */
    REORDER("reorder"),
    /**
     * Updates a single object.
     */
    UPDATE("update"),
    /**
     * Delete a single object.
     */
    DELETE("delete"),
    /**
     * Returns a list of a list of requested attributes. Request must contain
     * a list of identifier of objects that attributes should be returned.
     */
    LIST("list"),
    /**
     * Deletes the whole script
     */
    DELETESCRIPT("deletescript"),
    /**
     * Gets the whole script as text
     */
    GETSCRIPT("getscript");

    private final String ajaxName;

    private Action(final String name) {
        this.ajaxName = name;
    }

    /**
     * Gets the AJAX name for this action.
     *
     * @return The name
     */
    public String getAjaxName() {
        return ajaxName;
    }

    // -----------------------------------------------------------------------------------

    private static final Map<String, Action> name2Action;

    /**
     * Gets the action by specified name.
     *
     * @param ajaxName The name to look-up
     * @return The associated action or <code>null</code>
     */
    public static Action byName(final String ajaxName) {
        return name2Action.get(ajaxName);
    }

    static {
        final ImmutableMap.Builder<String, Action> tmp = ImmutableMap.builder();
        for (final Action action : values()) {
            tmp.put(action.getAjaxName(), action);
        }
        name2Action = tmp.build();
    }
}
