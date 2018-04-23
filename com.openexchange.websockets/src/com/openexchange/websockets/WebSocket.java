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

package com.openexchange.websockets;

import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link WebSocket} - The representation of a session-bound Web Socket to send and receive data through a {@link WebSocketListener#onMessage(WebSocket, String) onMessage() call-back}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface WebSocket {

    /**
     * Gets the path that was used while this Web Socket was created; e.g. <code>"/websockets/foo/bar"</code>.
     *
     * @return The path
     */
    String getPath();

    /**
     * Gets the immutable map view for the available query parameters while this Web Socket was created; e.g. <code>"param1=foo&amp;param2=bar"</code>.
     *
     * @return The parameters (as immutable map)
     */
    Map<String, String> getParameters();

    /**
     * Gets the value for the denoted query parameter.
     *
     * @param parameterName The parameter name
     * @return The parameters value or <code>null</code> (if no such parameter was available while this Web Socket was created)
     */
    String getParameter(String parameterName);

    /**
     * gets this Web Socket's connection identifier.
     *
     * @return The connection identifier
     */
    ConnectionId getConnectionId();

    /**
     * Gets the identifier of the session currently associated with this Web Socket.
     *
     * @return The session identifier
     */
    String getSessionId();

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    int getUserId();

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    int getContextId();

    /**
     * Gets the Web Socket session to store states.
     *
     * @return The Web Socket session
     */
    WebSocketSession getWebSocketSession();

    /**
     * Applies a certain message transcoder to this Web Socket.
     * <p>
     * Every inbound and outbound messages are routed through that transcoder.
     *
     * @param transcoder The transcode to set
     */
    void setMessageTranscoder(MessageTranscoder transcoder);

    /**
     * Gets the scheme identifier for the currently active message transcoder.
     *
     * @return The scheme identifier or <code>null</code> if no trancoder is in place
     */
    String getMessageTranscoderScheme();

    /**
     * Sends a message to the remote end-point, blocking until all of the message has been transmitted.
     * <p>
     * A previously set {@link MessageTranscoder transcoder} kicks-in.
     *
     * @param message The message to be sent
     * @return The handler which will be notified of progress
     * @throws OXException If there is a problem delivering the message.
     */
    SendControl sendMessage(String message) throws OXException;

    /**
     * Sends a message to the remote end-point, blocking until all of the message has been transmitted.
     *
     * @param message The message to be sent
     * @return The handler which will be notified of progress
     * @throws OXException If there is a problem delivering the message.
     */
    SendControl sendMessageRaw(String message) throws OXException;

    /**
     * Closes this {@link WebSocket}.
     */
    void close();

}
