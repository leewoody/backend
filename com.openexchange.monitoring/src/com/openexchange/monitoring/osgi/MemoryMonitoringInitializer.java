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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.monitoring.osgi;

import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.monitoring.internal.memory.MemoryMonitoring;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link MemoryMonitoringInitializer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MemoryMonitoringInitializer implements ServiceTrackerCustomizer<TimerService, TimerService> {

    private final BundleContext context;
    private final int periodMinutes;
    private final double threshold;
    private ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link MemoryMonitoringInitializer}.
     */
    public MemoryMonitoringInitializer(int periodMinutes, double threshold, BundleContext context) {
        super();
        this.periodMinutes = periodMinutes;
        this.threshold = threshold;
        this.context = context;
    }

    @Override
    public synchronized TimerService addingService(ServiceReference<TimerService> reference) {
        TimerService timerService = context.getService(reference);

        try {
            Runnable task = new MemoryMonitoring(periodMinutes, threshold);
            timerTask = timerService.scheduleAtFixedRate(task, periodMinutes, periodMinutes, TimeUnit.MINUTES);
            return timerService;
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MemoryMonitoringInitializer.class);
            logger.warn("Failed to initialize memory monitoring task", e);
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<TimerService> reference, TimerService service) {
        // Don't care
    }

    @Override
    public synchronized void removedService(ServiceReference<TimerService> reference, TimerService service) {
        ScheduledTimerTask timerTask = this.timerTask;
        if (null != timerTask) {
            this.timerTask = null;
            timerTask.cancel();
        }
        service.purge();

        context.ungetService(reference);
    }

}
