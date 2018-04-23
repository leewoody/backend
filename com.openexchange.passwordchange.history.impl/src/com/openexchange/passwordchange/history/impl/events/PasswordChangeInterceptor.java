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

package com.openexchange.passwordchange.history.impl.events;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.passwordchange.history.PasswordChangeClients;
import com.openexchange.passwordchange.history.PasswordChangeRecorderRegistryService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.user.AbstractUserServiceInterceptor;

/**
 * {@link PasswordChangeInterceptor} - Provisioning based password changes will call this interceptor
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeInterceptor extends AbstractUserServiceInterceptor {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeInterceptor.class);

    private final ServiceLookup services;
    final PasswordChangeRecorderRegistryService registry;

    /**
     * Initializes a new {@link PasswordChangeInterceptor}.
     *
     * @param registry The {@link PasswordChangeRecorderRegistryService} to get the {@link PasswordChangeRecorder} from
     * @param services The {@link ServiceLookup} to get services from
     */
    public PasswordChangeInterceptor(PasswordChangeRecorderRegistryService registry, ServiceLookup services) {
        super();
        this.registry = registry;
        this.services = services;
    }

    @Override
    public int getRanking() {
        return 10;
    }

    @Override
    public void afterUpdate(Context context, User user, Contact contactData, Map<String, Object> properties) throws OXException {
        // Check if password was changed
        if (null != context && null != user && null != user.getUserPassword()) {
            final int contextId = context.getContextId();
            final int userId = user.getId();

            // so password was changed..
            ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);
            if (null == threadPool) {
                PasswordChangeHelper.recordChangeSafe(contextId, userId, null, PasswordChangeClients.PROVISIONING.getIdentifier(), registry);
            } else {                
                threadPool.submit(new AbstractTask<Void>() {
                    @Override
                    public Void call() {
                        PasswordChangeHelper.recordChangeSafe(contextId, userId, null, PasswordChangeClients.PROVISIONING.getIdentifier(), registry);
                        return null;
                    }
                });
            }
        }
    }

    @Override
    public void afterDelete(Context context, User user, Contact contactData) throws OXException {
        final int contextId = context.getContextId();
        final int userId = user.getId();

        // Clear DB after deletion of user
        ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);
        if (null == threadPool) {
            PasswordChangeHelper.clearSafeFor(contextId, userId, 0, registry);
        } else {            
            threadPool.submit(new AbstractTask<Void>() {
                @Override
                public Void call() {
                    PasswordChangeHelper.clearSafeFor(contextId, userId, 0, registry);
                    return null;
                }
            });
        }
    }

}
