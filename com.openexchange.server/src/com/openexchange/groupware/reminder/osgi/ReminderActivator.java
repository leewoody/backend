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

package com.openexchange.groupware.reminder.osgi;

import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.reminder.ReminderService;
import com.openexchange.groupware.reminder.ReminderServiceImpl;
import com.openexchange.groupware.reminder.TargetService;
import com.openexchange.groupware.reminder.json.ReminderActionFactory;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.oauth.provider.resourceserver.scope.AbstractScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;

/**
 * {@link ReminderActivator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ReminderActivator extends AJAXModuleActivator {

    public ReminderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {AppointmentSqlFactoryService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        rememberTracker(new ServiceTracker<TargetService, TargetService>(context, TargetService.class.getName(), new TargetRegistryCustomizer(context)));
        openTrackers();
        ReminderService reminderService = new ReminderServiceImpl();
        registerService(ReminderService.class, reminderService);
        registerModule(new ReminderActionFactory(new ExceptionOnAbsenceServiceLookup(this)), "reminder");
        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(ReminderActionFactory.OAUTH_READ_SCOPE, OAuthScopeDescription.READ_ONLY) {
            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.TASKS.getCapabilityName()) || capabilities.contains(Permission.CALENDAR.getCapabilityName());
            }
        });
        registerService(OAuthScopeProvider.class, new AbstractScopeProvider(ReminderActionFactory.OAUTH_WRITE_SCOPE, OAuthScopeDescription.WRITABLE) {
            @Override
            public boolean canBeGranted(CapabilitySet capabilities) {
                return capabilities.contains(Permission.TASKS.getCapabilityName()) || capabilities.contains(Permission.CALENDAR.getCapabilityName());
            }
        });
    }

}
