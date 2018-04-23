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

package com.openexchange.oauth.impl.osgi;

import java.util.List;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link OSGiContextService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OSGiContextService extends AbstractOSGiDelegateService<ContextService> implements ContextService {

    /**
     * Initializes a new {@link OSGiContextService}.
     */
    public OSGiContextService() {
        super(ContextService.class);
    }

    @Override
    public void setAttribute(String name, String value, int contextId) throws OXException {
        getService().setAttribute(name, value, contextId);
    }

    @Override
    public List<Integer> getAllContextIds() throws OXException {
        return getService().getAllContextIds();
    }

    @Override
    public List<Integer> getDistinctContextsPerSchema() throws OXException {
        return getService().getDistinctContextsPerSchema();
    }

    @Override
    public Map<PoolAndSchema, List<Integer>> getSchemaAssociations() throws OXException {
        return getService().getSchemaAssociations();
    }

    @Override
    public Map<PoolAndSchema, List<Integer>> getSchemaAssociationsFor(List<Integer> contextIds) throws OXException {
        return getService().getSchemaAssociationsFor(contextIds);
    }

    @Override
    public Context getContext(int contextId) throws OXException {
        return getService().getContext(contextId);
    }

    @Override
    public int getContextId(final String loginContextInfo) throws OXException {
        return getService().getContextId(loginContextInfo);
    }

    @Override
    public void invalidateContext(final int contextId) throws OXException {
        getService().invalidateContext(contextId);
    }

    @Override
    public void invalidateContexts(int[] contextIDs) throws OXException {
        try {
            getService().invalidateContexts(contextIDs);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public void invalidateLoginInfo(final String loginContextInfo) throws OXException {
        getService().invalidateLoginInfo(loginContextInfo);
    }

    @Override
    public Context loadContext(final int contextId) throws OXException {
        return getService().loadContext(contextId);
    }

}
