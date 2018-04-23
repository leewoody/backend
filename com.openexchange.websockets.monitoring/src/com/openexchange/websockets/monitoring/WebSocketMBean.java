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

package com.openexchange.websockets.monitoring;

import java.util.List;
import javax.management.MBeanException;
import com.openexchange.management.MBeanMethodAnnotation;


/**
 * {@link WebSocketMBean} - The MBean for Web Sockets.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface WebSocketMBean {

    /** The MBean's domain */
    public static final String DOMAIN = "com.openexchange.websockets";

    /**
     * Gets the number of open Web Sockets on this node
     *
     * @return The number of open Web Sockets
     * @throws MBeanException If number of open Web Sockets cannot be returned
     */
    @MBeanMethodAnnotation (description="Gets the number of open Web Sockets on this node", parameters={}, parameterDescriptions={})
    long getNumberOfWebSockets() throws MBeanException;

    /**
     * Gets the number of buffered messages that are supposed to be sent to remote cluster members.
     *
     * @return The number of buffered messages
     * @throws MBeanException If number of buffered messages cannot be returned
     */
    @MBeanMethodAnnotation (description="Gets the number of buffered messages that are supposed to be sent to remote cluster members.", parameters={}, parameterDescriptions={})
    long getNumberOfBufferedMessages() throws MBeanException;

    /**
     * Lists all available Web Socket information from whole cluster.
     * <p>
     * <div style="background-color:#FFDDDD; padding:6px; margin:0px;"><b>Expensive operation!</b></div>
     * <p>
     *
     * @return All available Web Socket information
     * @throws MBeanException If Web Socket information cannot be returned
     */
    @MBeanMethodAnnotation (description="Lists all available Web Socket information from whole cluster; each row provides context identifier, user identifier, member address/port, the path used when the socket was created, and connection identifier", parameters={}, parameterDescriptions={})
    List<List<String>> listClusterWebSocketInfo() throws MBeanException;

    /**
     * Lists Web Sockets opened on this node; each row provides:
     * <ul>
     * <li>context identifier,
     * <li>user identifier,
     * <li>the path used when the socket was created and
     * <li>connection identifier
     * </ul>
     *
     * @return The Web Sockets opened on this node
     * @throws MBeanException If Web Sockets cannot be returned
     */
    @MBeanMethodAnnotation (description="Lists Web Sockets opened on this node; each row provides context identifier, user identifier, the path used when the socket was created, and connection identifier", parameters={}, parameterDescriptions={})
    List<List<String>> listWebSockets() throws MBeanException;

    /**
     * Closes all locally available Web Sockets matching specified path filter expression (if any).
     * <p>
     * In case no path filter expression is given (<code>pathFilter == null</code>), all user-associated Web Sockets are closed.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param pathFilter The optional path filter expression or <code>null</code>
     * @throws MBeanException If closing Web Sockets fails
     */
    @MBeanMethodAnnotation (description="Closes all locally available Web Sockets matching specified path filter expression (if any).", parameters={"userId", "contextId", "pathFilter"}, parameterDescriptions={"The user identifier", "The context identifier", "The optional path filter expression; e.g. \"/socket.io/*\""})
    void closeWebSockets(int userId, int contextId, String pathFilter) throws MBeanException;

}
