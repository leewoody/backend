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

package com.openexchange.dav.principals;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.dav.principals.groups.GroupPrincipalCollection;
import com.openexchange.dav.principals.resources.ResourcePrincipalCollection;
import com.openexchange.dav.principals.users.UserPrincipalCollection;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.dav.resources.DAVRootCollection;
import com.openexchange.java.Strings;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link RootPrincipalCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class RootPrincipalCollection extends DAVRootCollection {

    private final PrincipalFactory factory;

    /**
     * Initializes a new {@link RootPrincipalCollection}.
     *
     * @param factory The factory
     */
    public RootPrincipalCollection(PrincipalFactory factory) {
        super(factory, "Principals");
        this.factory = factory;
    }

    @Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        List<WebdavResource> children = new ArrayList<WebdavResource>(3);
        children.add(new UserPrincipalCollection(factory, constructPathForChildResource(UserPrincipalCollection.NAME)));
        children.add(new GroupPrincipalCollection(factory, constructPathForChildResource(GroupPrincipalCollection.NAME)));
        children.add(new ResourcePrincipalCollection(factory, constructPathForChildResource(ResourcePrincipalCollection.NAME)));
        return children;
    }

    @Override
    public DAVCollection getChild(String name) throws WebdavProtocolException {
        if (Strings.isEmpty(name)) {
            throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        if (UserPrincipalCollection.NAME.equals(name)) {
            return new UserPrincipalCollection(factory, constructPathForChildResource(UserPrincipalCollection.NAME));
        }
        if (GroupPrincipalCollection.NAME.equals(name)) {
            return new GroupPrincipalCollection(factory, constructPathForChildResource(GroupPrincipalCollection.NAME));
        }
        if (ResourcePrincipalCollection.NAME.equals(name)) {
            return new ResourcePrincipalCollection(factory, constructPathForChildResource(ResourcePrincipalCollection.NAME));
        }
        throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_NOT_FOUND);
    }

}
