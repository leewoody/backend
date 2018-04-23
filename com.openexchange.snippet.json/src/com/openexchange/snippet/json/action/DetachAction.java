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

package com.openexchange.snippet.json.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.DefaultAttachment;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.Property;
import com.openexchange.snippet.Snippet;
import com.openexchange.snippet.SnippetExceptionCodes;
import com.openexchange.snippet.SnippetManagement;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.json.SnippetJsonParser;
import com.openexchange.snippet.json.SnippetRequest;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DetachAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DetachAction extends SnippetAction {

    /**
     * Initializes a new {@link DetachAction}.
     */
    public DetachAction(final ServiceLookup services, final ServiceListing<SnippetService> snippetServices, final Map<String, SnippetAction> actions) {
        super(services, snippetServices, actions);
    }

    @Override
    protected AJAXRequestResult perform(final SnippetRequest snippetRequest) throws OXException, JSONException {
        String id = snippetRequest.checkParameter("id");
        ServerSession session = snippetRequest.getSession();
        SnippetService snippetService = getSnippetService(session);
        SnippetManagement management = snippetService.getManagement(session);

        {
            Snippet snippetToChange = management.getSnippet(id);
            if (!snippetToChange.isShared() && snippetToChange.getCreatedBy() != session.getUserId()) {
                throw SnippetExceptionCodes.UPDATE_DENIED.create(id, session.getUserId(), session.getContextId());
            }
        }

        final DefaultSnippet snippet = new DefaultSnippet().setId(id);
        final JSONArray jsonAttachments = (JSONArray) snippetRequest.getRequestData().requireData();
        int length = jsonAttachments.length();
        final List<Attachment> attachments = new ArrayList<Attachment>(length);
        for (int i = 0; i < length; i++) {
            final DefaultAttachment attachment = new DefaultAttachment();
            SnippetJsonParser.parse(jsonAttachments.getJSONObject(i), attachment);
            attachments.add(attachment);
        }
        /*
         * Update
         */
        String newId = management.updateSnippet(id, snippet, Collections.<Property> emptySet(), Collections.<Attachment> emptyList(), attachments);
        return new AJAXRequestResult(newId, "string");
    }

    @Override
    public String getAction() {
        return "detach";
    }

}
