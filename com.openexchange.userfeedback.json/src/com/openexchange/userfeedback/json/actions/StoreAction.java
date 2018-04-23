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

package com.openexchange.userfeedback.json.actions;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.userfeedback.FeedbackService;
import com.openexchange.userfeedback.json.osgi.Services;

/**
 * {@link StoreAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class StoreAction implements AJAXActionService {

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        Object data = requestData.getData();
        if ((data == null) || (false == JSONObject.class.isInstance(data))) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();

        }
        JSONObject dataObject = (JSONObject) data;

        String type = requestData.getParameter("type");
        if (null == type || Strings.isEmpty(type)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("type");
        }

        FeedbackService service = Services.getService(FeedbackService.class);
        if (service == null) {
            throw ServiceExceptionCode.absentService(FeedbackService.class);
        }
        String hostname = getHostname(requestData, session);

        Map<String, String> params = new HashMap<>();
        params.put("type", type);
        params.put("hostname", hostname);

        service.store(session, dataObject, params);

        return new AJAXRequestResult();
    }

    private static String getHostname(AJAXRequestData request, ServerSession session) {
        String hostname = null;
        HostnameService hostnameService = Services.optService(HostnameService.class);

        if (hostnameService != null) {
            hostname = hostnameService.getHostname(session.getUserId(), session.getContextId());
        }

        if (hostname == null) {
            hostname = request.getHostname();
        }

        return hostname;
    }
}
