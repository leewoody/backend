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

package com.openexchange.drive.json.action;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.NotificationParameters;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.json.JsonDirectoryVersion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link UpdateFolderAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class UpdateFolderAction extends AbstractDriveAction {

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        /*
         * parse parameters & directory metadata
         */
        String path = requestData.getParameter("path");
        if (Strings.isEmpty(path)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
        }
        String checksum = requestData.getParameter("checksum");
        if (Strings.isEmpty(checksum)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("checksum");
        }
        boolean cascadePermissions = Boolean.valueOf(requestData.getParameter("cascadePermissions"));
        JSONObject json = (JSONObject) requestData.requireData();
        NotificationParameters parameters = new NotificationParameters();
        JSONObject jsonFolder;
        try {
            jsonFolder = json.getJSONObject("folder");
            if (null == jsonFolder) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("folder");
            }
            JSONObject jsonNotification = json.optJSONObject("notification");
            if (null != jsonNotification) {
                parameters.setNotificationTransport(getShareParser().parseNotificationTransport(jsonNotification));
                parameters.setNotificationMessage(jsonNotification.optString("message", null));
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        /*
         * update the directory, return empty result in case of success
         */
        getDriveService().getUtility().updateDirectory(session, new JsonDirectoryVersion(checksum, path), jsonFolder, cascadePermissions, parameters);
        AJAXRequestResult result = new AJAXRequestResult(new JSONObject(), "json");
        if (null != parameters.getWarnings()) {
            result.addWarnings(parameters.getWarnings());
        }
        return result;
    }

}
