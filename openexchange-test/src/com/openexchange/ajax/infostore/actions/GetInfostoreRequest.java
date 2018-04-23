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

package com.openexchange.ajax.infostore.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class GetInfostoreRequest extends AbstractInfostoreRequest<GetInfostoreResponse> {

    private String id;
    private int[] columns;
    private int version;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public GetInfostoreRequest() {
        super();
    }

    public GetInfostoreRequest(String id) {
        setId(id);
    }

    public GetInfostoreRequest(String id, int... columns) {
        this(id, -1, columns);
    }

    public GetInfostoreRequest(String id, int version, int... columns) {
        setId(id);
        this.columns = columns;
        this.version = version;
    }

    @Override
    public Object getBody() throws JSONException {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() {
        if (null == columns || columns.length == 0) {
            return new Params(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET, AJAXServlet.PARAMETER_ID, String.valueOf(getId())).toArray();
        }
        final List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ID, String.valueOf(getId())));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        if(version != -1) {
            parameterList.add(new Parameter(AJAXServlet.PARAMETER_VERSION, version));
        }
        return parameterList.toArray(new Parameter[parameterList.size()]);
    }

    @Override
    public AbstractAJAXParser<GetInfostoreResponse> getParser() {
        return new AbstractAJAXParser<GetInfostoreResponse>(getFailOnError()) {

            @Override
            protected GetInfostoreResponse createResponse(final Response response) throws JSONException {
                return new GetInfostoreResponse(response);
            }
        };
    }

}
