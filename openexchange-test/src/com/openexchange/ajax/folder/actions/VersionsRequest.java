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

package com.openexchange.ajax.folder.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.infostore.thirdparty.actions.AbstractFileRequest;
import com.openexchange.groupware.search.Order;

/**
 * 
 * {@link VersionsRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class VersionsRequest extends AbstractFileRequest<VersionsResponse> {

    private final String folderId;
    private final int[] columns;
    private final int sort;
    private final Order order;

    public VersionsRequest(String folderId, int[] fields) {
        this(folderId, fields, -1, null);
    }

    public VersionsRequest(String folderId, int[] fields, int sort, Order order) {
        super(true);
        this.sort = sort;
        this.order = order;
        this.folderId = folderId;
        this.columns = fields;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public VersionsParser getParser() {
        return new VersionsParser(true, this.columns);
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "versions"));
        if (this.folderId != null) {
            params.add(new Parameter("id", folderId));
        }
        if ((this.columns != null) && (this.columns.length > 0)) {
            String colsArray2String = com.openexchange.tools.URLParameter.colsArray2String(this.columns);
            params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, colsArray2String));
        }
        if (this.sort != -1) {
            params.add(new Parameter("sort", this.sort));
        }
        if (this.order != null) {
            params.add(new Parameter("order", OrderFields.write(this.order)));
        }
        return params.toArray(new Parameter[params.size()]);
    }
}
