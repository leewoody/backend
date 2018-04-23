
package com.openexchange.spamhandler.spamexperts.servlets;

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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.DataServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.HttpClients.ClientConfig;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.spamexperts.management.SpamExpertsConfig;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.version.Version;

/**
 *
 * Servlet which returns needed Data for the Spamexperts Iframe Plugin to redirect
 * and authenticate to an external GUI.
 *
 * Also does jobs for the other GUI Plugin
 *
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 *
 */
public final class SpamExpertsServlet extends DataServlet {

    private static final long serialVersionUID = -8914926421736440078L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SpamExpertsServlet.class);

    private final SpamExpertsConfig config;
    private final CloseableHttpClient httpClient;

    /**
     * Initializes a new {@link SpamExpertsServlet}.
     *
     * @param config The configuration to use
     */
    public SpamExpertsServlet(SpamExpertsConfig config) {
        super();
        this.config = config;

        String versionString = Version.getInstance().optVersionString();
        if (null == versionString) {
            versionString = "<unknown version>";
        }
        httpClient = HttpClients.getHttpClient(ClientConfig.newInstance()
            .setUserAgent("OX Spam Experts Client v" + versionString)
            .setMaxTotalConnections(32)
            .setMaxConnectionsPerRoute(32)
            .setConnectionTimeout(5000)
            .setSocketReadTimeout(30000)
        );
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return true;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        Session session = getSessionObject(req);

        Response response = new Response();
        try {
            String action = parseMandatoryStringParameter(req, PARAMETER_ACTION);

            JSONObject jsonObj;
            try {
                jsonObj = convertParameter2JSONObject(req);
            } catch (final JSONException e) {
                LOG.error("", e);
                response.setException(OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e));
                writeResponse(response, resp, session);
                return;
            }

            SpamExpertsServletRequest proRequest = new SpamExpertsServletRequest(session, config, httpClient);
            Object responseObj = proRequest.action(action, jsonObj);
            response.setData(responseObj);
        } catch (OXException e) {
            LOG.error("", e);
            response.setException(e);
        }

        writeResponse(response, resp, session);
    }

    /**
     * Shuts-down this servlet instance.
     */
    public void shutDown() {
        try {
            httpClient.close();
        } catch (Exception e) {
            // Ignore
        }
    }

}
