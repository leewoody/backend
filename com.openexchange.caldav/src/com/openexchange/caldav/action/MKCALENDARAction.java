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

package com.openexchange.caldav.action;

import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.dav.DAVProperty;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.actions.DAVAction;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link MKCALENDARAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MKCALENDARAction extends DAVAction {

    /**
     * Initializes a new {@link MKCALENDARAction}.
     *
     * @param protocol The underlying protocol
     */
    public MKCALENDARAction(Protocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse res) throws WebdavProtocolException {
        DAVCollection resource = requireResource(request, DAVCollection.class);
        if (resource.exists()) {
            // https://www.ietf.org/mail-archive/web/caldav/current/msg00123.html
            throw new PreconditionException(Protocol.DAV_NS.getURI(), "resource-must-be-null", request.getUrl(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        Document requestBody = optRequestBody(request);
        if (null != requestBody) {
            /*
             * process inline PROPPATCHes
             */
            Element rootElement = requestBody.getRootElement();
            if (null == rootElement || false == CaldavProtocol.CAL_NS.equals(rootElement.getNamespace()) || false == "mkcalendar".equals(rootElement.getName())) {
                throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
            }
            for (Element element : rootElement.getChildren("set", Protocol.DAV_NS)) {
                for (Element prop : element.getChildren("prop", Protocol.DAV_NS)) {
                    for (Element propertyElement : prop.getChildren()) {
                        if (request.getFactory().getProtocol().isProtected(propertyElement.getNamespaceURI(), propertyElement.getName())) {
                            throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_FORBIDDEN);
                        } else {
                            resource.putProperty(new DAVProperty(propertyElement));
                        }
                    }
                }
            }
        }
        /*
         * create resource & return appropriate response
         */
        resource.create();
        res.setStatus(HttpServletResponse.SC_CREATED);
    }

}
