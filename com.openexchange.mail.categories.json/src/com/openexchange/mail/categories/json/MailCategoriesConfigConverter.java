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

package com.openexchange.mail.categories.json;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.categories.MailCategoryConfig;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link MailCategoriesConfigConverter}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class MailCategoriesConfigConverter implements ResultConverter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailCategoriesConfigConverter.class);
    
    private static final MailCategoriesConfigConverter INSTANCE = new MailCategoriesConfigConverter();
    
    public static MailCategoriesConfigConverter getInstance(){
        return INSTANCE;
    }
    
    @Override
    public String getInputFormat() {
        return "mailCategoriesConfig";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        try {
            convert2JSON(requestData, result, session);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }
    

    /**
     * Converts to JSON output format.
     *
     * @param requestData The AJAX request data
     * @param result The AJAX result
     * @param session The associated session
     * @throws OXException If an error occurs
     * @throws JSONException 
     */
    public void convert2JSON(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException, JSONException {
        
        Object resultObject = result.getResultObject();
        if (null == resultObject) {
            LOG.warn("Result object is null.");
            result.setResultObject(JSONObject.NULL, "json");
            return;
        }
        
        if(resultObject instanceof List){ 
            @SuppressWarnings("unchecked")
            List<MailCategoryConfig> list = (List<MailCategoryConfig>) resultObject;
            
            if(list==null || list.isEmpty()){
                result.setResultObject(JSONObject.NULL, "json");
                return;
            }
            
            final OXJSONWriter jsonWriter = new OXJSONWriter();
            jsonWriter.array();
            for(MailCategoryConfig config: list){
                jsonWriter.object();
                jsonWriter.key("category").value(config.getCategory());
                jsonWriter.key("flag").value(config.getFlag());
                jsonWriter.key("active").value(config.isActive());
                jsonWriter.key("name").value(config.getName());
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
            result.setResultObject(jsonWriter.getObject(), "json");
            
        } else {
            
            MailCategoryConfig config = (MailCategoryConfig) resultObject;
            
            final OXJSONWriter jsonWriter = new OXJSONWriter();
            jsonWriter.object();
            jsonWriter.key("category").value(config.getCategory());
            jsonWriter.key("flag").value(config.getFlag());
            jsonWriter.key("active").value(config.isActive());
            jsonWriter.key("name").value(config.getName());
            jsonWriter.endObject();
            result.setResultObject(jsonWriter.getObject(), "json");
        }
    }

}
