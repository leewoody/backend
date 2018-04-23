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

package com.openexchange.socketio.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.socketio.server.SocketIOManager;
import com.openexchange.socketio.websocket.WsSocketIOServlet;
import com.openexchange.socketio.websocket.WsTransport;
import com.openexchange.socketio.websocket.WsTransportConnectionRegistry;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.websockets.WebSocketListener;


/**
 * {@link SocketIoActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class SocketIoActivator extends HousekeepingActivator {

    private WsTransportConnectionRegistry connectionRegistry;
    private ServiceTracker<ManagementService, ManagementService> mgmtTracker;

    /**
     * Initializes a new {@link SocketIoActivator}.
     */
    public SocketIoActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, TimerService.class, ThreadPoolService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        TimerService timerService = getService(TimerService.class);

        WsTransportConnectionRegistry connectionRegistry = new WsTransportConnectionRegistry();
        this.connectionRegistry = connectionRegistry;
        registerService(WebSocketListener.class, connectionRegistry);

        WsTransport transport = new WsTransport(connectionRegistry);
        connectionRegistry.setTransport(transport);

        Dictionary<String, String> initParams = new Hashtable<>(2);
        initParams.put("allowAllOrigins", "true");
        WsSocketIOServlet servlet = new WsSocketIOServlet(transport, timerService);
        getService(HttpService.class).registerServlet("/socket.io", servlet, initParams, null);

        SocketIOManager socketIOManager = servlet.getSocketIOManager();
        ServiceTracker<ManagementService, ManagementService> mgmtTracker = new ServiceTracker<>(context, ManagementService.class, new ManagementTracker(socketIOManager, connectionRegistry, context));
        this.mgmtTracker = mgmtTracker;
        mgmtTracker.open();
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        ServiceTracker<ManagementService, ManagementService> mgmtTracker = this.mgmtTracker;
        if (null != mgmtTracker) {
            this.mgmtTracker = null;
            mgmtTracker.close();
        }

        WsTransportConnectionRegistry connectionRegistry = this.connectionRegistry;
        if (null != connectionRegistry) {
            this.connectionRegistry = null;
            connectionRegistry.shutDown();
        }

        HttpService httpService = getService(HttpService.class);
        if (null != httpService) {
            httpService.unregister("/socket.io");
        }

        super.stopBundle();
    }

}
