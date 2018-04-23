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

package com.openexchange.ajax;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.request.ResourceRequest;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Resource} - The servlet handling requests to "resource"
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class Resource extends DataServlet {

	private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Resource.class);

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -8381608654367561643L;

	@Override
	protected void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
			throws ServletException, IOException {
	    final ServerSession session = getSessionObject(httpServletRequest);
        final Response response = new Response(session);
		try {
			final String action = parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);
			JSONObject jsonObj = null;
			try {
				jsonObj = convertParameter2JSONObject(httpServletRequest);
			} catch (final JSONException e) {
				LOG.error("", e);
				response.setException(OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e));
				writeResponse(response, httpServletResponse, session);
				return;
			}

			final ResourceRequest resourceRequest = new ResourceRequest(session);
			final Object responseObj = resourceRequest.action(action, jsonObj);
			response.setTimestamp(resourceRequest.getTimestamp());
			response.setData(responseObj);
		} catch (final OXException e) {
			LOG.error(_doGet, e);
			response.setException(e);
		} catch (final JSONException e) {
			final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
			LOG.error("", oje);
			response.setException(oje);
		}

		writeResponse(response, httpServletResponse, session);
	}

	@Override
	protected void doPut(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
			throws ServletException, IOException {
	    final ServerSession session = getSessionObject(httpServletRequest);
        final Response response = new Response(session);

		try {
			final String action = parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);

			final String data = getBody(httpServletRequest);
			if (data.charAt(0) == '[') {
				JSONArray jData = null;
				try {
					jData = new JSONArray(data);
				} catch (final JSONException e) {
					final OXException exc = OXJSONExceptionCodes.JSON_READ_ERROR.create(e, data);
					response.setException(exc);
					writeResponse(response, httpServletResponse, session);
					LOG.error("", exc);
					return;
				}
				JSONObject jsonObj = null;
				try {
					jsonObj = convertParameter2JSONObject(httpServletRequest);
				} catch (final JSONException e) {
					LOG.error(_doPut, e);
					response.setException(OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e));
					writeResponse(response, httpServletResponse, session);
					return;
				}

				jsonObj.put(PARAMETER_DATA, jData);

				final ResourceRequest resourceRequest = new ResourceRequest(session);
				final Object responseObj = resourceRequest.action(action, jsonObj);
				response.setTimestamp(resourceRequest.getTimestamp());
				response.setData(responseObj);
			} else {
				JSONObject jData = null;
				JSONObject jsonObj = null;
				try {
					jData = new JSONObject(data);
					jsonObj = convertParameter2JSONObject(httpServletRequest);
				} catch (final JSONException e) {
					LOG.error(_doPut, e);
					response.setException(OXJSONExceptionCodes.JSON_READ_ERROR.create(e));
					writeResponse(response, httpServletResponse, session);
					return;
				}

				jsonObj.put(PARAMETER_DATA, jData);

				final ResourceRequest resourceRequest = new ResourceRequest(session);
				final Object responseObj = resourceRequest.action(action, jsonObj);
				response.setTimestamp(resourceRequest.getTimestamp());
				response.setData(responseObj);
			}
		} catch (final OXException e) {
			LOG.error(_doPut, e);
			response.setException(e);
		} catch (final JSONException e) {
			final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
			LOG.error("", oje);
			response.setException(oje);
		}

		writeResponse(response, httpServletResponse, session);
	}

	@Override
	protected boolean hasModulePermission(final ServerSession session) {
		return true;
	}
}
