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

package com.openexchange.ajax.mail.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link MoveMailToCategoryRequest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MoveMailToCategoryRequest extends AbstractMailCategoriesRequest<MoveMailToCategoryResponse> {

    private static final String PARAMETER_CATEGORY_ID = "category_id";
    private static final String ACTION_MOVE = "move";

    private final String categoryId;
    private List<JSONObject> body;

    /**
     * Initializes a new {@link MoveMailToCategoryRequest}.
     */
    public MoveMailToCategoryRequest(String categoryId) {
        super();
        this.categoryId = categoryId;
        this.body = new ArrayList<>();
    }

    public void addMail(String id, String folder) throws JSONException {
        JSONObject obj = new JSONObject(2);
        obj.put("id", id);
        obj.put("folder_id", folder);
        body.add(obj);
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> list = new LinkedList<Parameter>();
        list.add(new Parameter(PARAMETER_ACTION, ACTION_MOVE));
        list.add(new Parameter(PARAMETER_CATEGORY_ID, categoryId));
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends MoveMailToCategoryResponse> getParser() {
        return new AbstractAJAXParser<MoveMailToCategoryResponse>(true) {

            @Override
            protected MoveMailToCategoryResponse createResponse(final Response response) {
                return new MoveMailToCategoryResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return new JSONArray(body);
    }

}
