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

package com.openexchange.pns;

import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * {@link KnownTopic} - An enumeration for well-known topics.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public enum KnownTopic {

    /**
     * <code>*</code>
     * <p>
     * The special topic matching all topic identifiers.
     */
    ALL("*"),

    // ------------------------------------------------ MAIL ----------------------------------------------------

    /**
     * <code>ox:mail:new</code>
     * <p>
     * The topic for a newly arrived mail.
     */
    MAIL_NEW("ox:mail:new"),

    // ------------------------------------------------ CALENDAR ------------------------------------------------

    /**
     * <code>ox:calendar:new</code>
     * <p>
     * The topic for a newly created appointment.
     */
    CALENDAR_NEW("ox:calendar:new"),

    ;

    private final String name;

    private KnownTopic(final String name) {
        this.name = name;
    }

    /**
     * Gets the topic name
     *
     * @return The topic name
     */
    public String getName() {
        return name;
    }

    private static final Map<String, KnownTopic> STRING2NAME;
    static {
        final KnownTopic[] values = KnownTopic.values();
        final Map<String, KnownTopic> m = new HashMap<String, KnownTopic>(values.length);
        for (final KnownTopic name : values) {
            m.put(name.getName(), name);
        }
        STRING2NAME = ImmutableMap.copyOf(m);
    }

    /**
     * Gets the associated {@code KnownTopic} enum.
     *
     * @param sName The name string
     * @return The {@code KnownTopic} enum or <code>null</code>
     */
    public static KnownTopic nameFor(final String sName) {
        return null == sName ? null : STRING2NAME.get(sName);
    }
}
