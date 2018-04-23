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

package com.openexchange.groupware.update.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * {@link LocalUpdateTaskMonitor}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class LocalUpdateTaskMonitor {

    private static final LocalUpdateTaskMonitor INSTANCE = new LocalUpdateTaskMonitor();

    private final ConcurrentMap<String, Thread> statesBySchema = new ConcurrentHashMap<String, Thread>();

    private LocalUpdateTaskMonitor() {
        super();
    }

    public static LocalUpdateTaskMonitor getInstance() {
        return INSTANCE;
    }

    /**
     * Adds a schema to this monitor. This indicates that one or more update tasks for
     * this schema have been scheduled and are going to be executed by the same thread that
     * performs this call.
     *
     * @param schema The schema
     * @return Whether the schema was added or not. If the same thread already added
     * a schema, <code>false</code> is returned and the schema is not added.
     */
    public boolean addState(String schema) {
        return statesBySchema.putIfAbsent(schema, Thread.currentThread()) == null;
    }

    /**
     * Removes the given schema if it has been added
     * by this thread.
     *
     * @param schema The schema
     * @return Whether a schema has been removed or not (i.e. wasn't added before).
     */
    public boolean removeState(String schema) {
        return statesBySchema.remove(schema, Thread.currentThread());
    }

    /**
     * Returns a list schemas. Every item indicates that one or more update tasks for this schema
     * have been scheduled and are going to be executed or are currently running.
     *
     * @return A list of schemas
     */
    public Collection<String> getScheduledStates() {
        return new ArrayList<String>(statesBySchema.keySet());
    }

}
