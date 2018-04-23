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

package com.openexchange.ms.internal;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageInbox;

/**
 * {@link MessageInboxImpl}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageInboxImpl implements MessageInbox {

    private static final MessageInboxImpl INSTANCE = new MessageInboxImpl();

    /**
     * Gets the Inbox instance.
     * 
     * @return The instance
     */
    public static MessageInboxImpl getInstance() {
        return INSTANCE;
    }

    private final BlockingQueue<Message<?>> blockingQueue;

    /**
     * Initializes a new {@link MessageInboxImpl}.
     */
    private MessageInboxImpl() {
        super();
        blockingQueue = new LinkedBlockingQueue<Message<?>>();
    }

    @Override
    public Iterator<Message<?>> iterator() {
        return blockingQueue.iterator();
    }

    @Override
    public boolean isEmpty() {
        return blockingQueue.isEmpty();
    }

    /**
     * Inserts the specified message into this Inbox if it is possible to do so immediately. Returning <tt>true</tt> upon success.
     * 
     * @param e The message to add
     * @return <tt>true</tt> if the message was added to this Inbox, else <tt>false</tt>
     */
    public boolean offer(final Message<?> e) {
        return blockingQueue.offer(e);
    }

    @Override
    public Message<?> poll() {
        return blockingQueue.poll();
    }

    @Override
    public Message<?> peek() {
        return blockingQueue.peek();
    }

    @Override
    public Message<?> take() throws InterruptedException {
        return blockingQueue.take();
    }

    @Override
    public Message<?> poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return blockingQueue.poll(timeout, unit);
    }

    @Override
    public void clear() {
        blockingQueue.clear();
    }

}
