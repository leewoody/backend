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

package com.openexchange.ajax.attach.actions;

import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link GetDocumentParser}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetDocumentParser extends AbstractAJAXParser<GetDocumentResponse> {

    private int contentLength;

    private HttpResponse httpResponse;

    private String respString;

    /**
     * Initializes a new {@link GetDocumentParser}.
     */
    public GetDocumentParser(boolean failOnError) {
        super(failOnError);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ajax.framework.AbstractAJAXParser#parse(java.lang.String)
     */
    @Override
    public GetDocumentResponse parse(final String body) throws JSONException {
        final boolean isJSON = body.startsWith("{");
        if (isJSON) {
            return super.parse(body);
        }
        JSONObject json = new JSONObject();
        json.put("document", body);
        return super.parse(json.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ajax.framework.AbstractAJAXParser#checkResponse(org.apache.http.HttpResponse, org.apache.http.HttpRequest)
     */
    @Override
    public String checkResponse(HttpResponse response, HttpRequest request) throws ParseException, IOException {
        httpResponse = response;
        Header[] headers = response.getAllHeaders();
        for (Header h : headers) {
            if (h.getName().equals("Content-Length")) {
                contentLength = Integer.parseInt(h.getValue());
                break;
            }
        }
        respString = EntityUtils.toString(response.getEntity());
        return respString;
    }

    @Override
    protected GetDocumentResponse createResponse(Response response) throws JSONException {
        GetDocumentResponse r = new GetDocumentResponse(httpResponse, response, contentLength, respString);
        return r;
    }

}
