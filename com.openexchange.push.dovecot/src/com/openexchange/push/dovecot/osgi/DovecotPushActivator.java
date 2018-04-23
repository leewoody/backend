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

package com.openexchange.push.dovecot.osgi;

import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.dovecot.doveadm.client.DoveAdmClient;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.lock.LockService;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.dovecot.DefaultRegistrationPerformer;
import com.openexchange.push.dovecot.DovecotPushConfiguration;
import com.openexchange.push.dovecot.DovecotPushDeleteListener;
import com.openexchange.push.dovecot.DovecotPushListener;
import com.openexchange.push.dovecot.DovecotPushManagerService;
import com.openexchange.push.dovecot.locking.DovecotPushClusterLock;
import com.openexchange.push.dovecot.registration.RegistrationPerformer;
import com.openexchange.push.dovecot.rest.DovecotPushRESTService;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;


/**
 * {@link DovecotPushActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.2
 */
public class DovecotPushActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link DovecotPushActivator}.
     */
    public DovecotPushActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, TimerService.class, MailService.class, ConfigurationService.class, ConfigViewFactory.class,
            SessiondService.class, ThreadPoolService.class, ContextService.class, UserService.class, PushListenerService.class, ObfuscatorService.class,
            LockService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);

        trackService(PushNotificationService.class);
        trackService(DoveAdmClient.class);
        trackService(MailAccountStorageService.class);

        DovecotPushListener.setIfHigherRanked(new DefaultRegistrationPerformer(this));

        track(RegistrationPerformer.class, new RegistrationPerformerTracker(context));

        DovecotPushConfiguration configuration = new DovecotPushConfiguration();
        configuration.init(this);

        // Check Hazelcast-based locking is enabled
        if (DovecotPushClusterLock.Type.HAZELCAST.equals(configuration.getClusterLock().getType())) {
            // Start tracking for Hazelcast
            DovecotRegisteringTracker registeringTracker = new DovecotRegisteringTracker(true, configuration, this, context);
            track(registeringTracker.getFilter(), registeringTracker);
        } else {
            // Register PushManagerService instance
            DovecotRegisteringTracker registeringTracker = new DovecotRegisteringTracker(false, configuration, this, context);
            track(registeringTracker.getFilter(), registeringTracker);
            trackService(HazelcastInstance.class);
        }
        openTrackers();

        registerService(DeleteListener.class, new DovecotPushDeleteListener());

        registerService(DovecotPushRESTService.class, new DovecotPushRESTService(this));
    }

    @Override
    protected void stopBundle() throws Exception {
        DovecotPushManagerService.dropInstance();
        Services.setServiceLookup(null);
        super.stopBundle();
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

}
