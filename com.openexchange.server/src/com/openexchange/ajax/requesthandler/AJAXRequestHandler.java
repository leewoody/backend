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

package com.openexchange.ajax.requesthandler;

import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link AJAXRequestHandler} - Handles an AJAX request.
 * @deprecated use {@link AJAXActionService} instead.
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Deprecated
public interface AJAXRequestHandler {

    /**
     * Performs the action indicated through given parameter <code>action</code>.
     *
     * @param action The action to perform
     * @param jsonObject The JSON data object (containing "data", "timestamp", etc.)
     * @param session The session providing needed user data
     * @param ctx The context
     * @return An appropriate result corresponding to request
     * @throws OXException If action cannot be performed
     * @throws JSONException If a JSON error occurs
     */
    public AJAXRequestResult performAction(String action, JSONObject jsonObject, Session session, Context ctx) throws OXException, JSONException;

    /**
     * Gets this request handler's module.
     *
     * @return The module
     */
    public String getModule();

    /**
     * Gets this request handler's supported actions.
     *
     * @return The supported actions
     */
    public Set<String> getSupportedActions();

}
