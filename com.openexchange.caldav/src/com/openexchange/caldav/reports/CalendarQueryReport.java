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

package com.openexchange.caldav.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.query.Filter;
import com.openexchange.caldav.query.FilterParser;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.actions.PROPFINDAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.xml.resources.ResourceMarshaller;


/**
 * {@link CalendarQueryReport}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CalendarQueryReport extends PROPFINDAction {

    public static final String NAMESPACE = CaldavProtocol.CAL_NS.getURI();
    public static final String NAME = "calendar-query";

    /**
     * Initializes a new {@link CalendarQueryReport}.
     *
     * @param protocol The protocol
     */
    public CalendarQueryReport(DAVProtocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        Element multistatusElement = prepareMultistatusElement();
        Document requestBody = optRequestBody(request);
        ResourceMarshaller marshaller = getMarshaller(request, requestBody);
        List<Element> elements = new ArrayList<Element>();
        for (WebdavResource resource : getMatching(request, requestBody)) {
            elements.addAll(marshaller.marshal(resource));
        }
        multistatusElement.addContent(elements);
        sendMultistatusResponse(response, multistatusElement);
    }

    private List<WebdavResource> getMatching(final WebdavRequest req, final Document requestBody) throws WebdavProtocolException {
        if (requestBody == null) {
            return Collections.emptyList();
        }
        final Element filterDef = requestBody.getRootElement().getChild("filter", CaldavProtocol.CAL_NS);
        if (filterDef == null) {
            return Collections.emptyList();
        }

        final Filter filter = new FilterParser().parse(filterDef);
        try {
            return ((FilteringResource) req.getResource()).filter(filter);
        } catch (final ClassCastException x) {
            throw WebdavProtocolException.generalError(req.getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
