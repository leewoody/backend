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

package com.openexchange.sessiond.mbean;

import java.util.Set;
import javax.management.MBeanException;
import com.openexchange.management.MBeanMethodAnnotation;

/**
 * {@link SessiondMBean} - The MBean for sessiond
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SessiondMBean {

    public static final String SESSIOND_DOMAIN = "com.openexchange.sessiond";

    /**
     * Clears all sessions belonging to the user identified by given user ID in specified context
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The number of removed sessions belonging to the user or <code>-1</code> if an error occurred
     */
    @MBeanMethodAnnotation (description="Clears all sessions on running node belonging to the user identified by given user ID in specified context", parameters={"userId", "contextId"}, parameterDescriptions={"The user identifier", "The context identifier"})
    int clearUserSessions(int userId, int contextId);

    /**
     * Clears all sessions from cluster belonging to the user identified by given user ID in specified context
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The number of removed sessions belonging to the user or <code>-1</code> if an error occurred
     * @throws MBeanException If operation fails
     */
    @MBeanMethodAnnotation (description="Clears all sessions from cluster on running node belonging to the user identified by given user ID in specified context", parameters={"userId", "contextId"}, parameterDescriptions={"The user identifier", "The context identifier"})
    void clearUserSessionsGlobally(int userId, int contextId) throws MBeanException;

    /**
     * Gets the number of short-term sessions associated with specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The number of user-associated sessions
     * @throws MBeanException If number of user-associated sessions cannot be returned
     */
    @MBeanMethodAnnotation (description="Gets the number of short-term sessions associated with specified user", parameters={"userId", "contextId"}, parameterDescriptions={"The user identifier", "The context identifier"})
    int getNumberOfUserSessons(int userId, int contextId) throws MBeanException;

    /**
     * Clears all sessions belonging to specified context
     *
     * @param contextId The context ID
     */
    @MBeanMethodAnnotation (description="Clears all sessions belonging to specified context", parameters={"contextId"}, parameterDescriptions={"The context identifier"})
    void clearContextSessions(int contextId);

    /**
     * Clears all sessions belonging to given contexts.
     *
     * @param contextId The context identifiers to remove sessions for
     */
    @MBeanMethodAnnotation (description="Clears all sessions in whole cluster belonging to specified context identifiers", parameters={"contextIds"}, parameterDescriptions={"The context identifiers"})
    void clearContextSessionsGlobal(Set<Integer> contextIds) throws MBeanException;

    /**
     * Gets the number of short-term sessions.
     *
     * @return The number of short-term sessions
     */
    @MBeanMethodAnnotation (description="Gets the number of short-term sessions.", parameters={}, parameterDescriptions={})
    int[] getNumberOfShortTermSessions();

    /**
     * Gets the number of long-term sessions.
     *
     * @return The number of long-term sessions
     */
    @MBeanMethodAnnotation (description="Gets the number of long-term sessions.", parameters={}, parameterDescriptions={})
    int[] getNumberOfLongTermSessions();

    /**
     * Clear all sessions in central session storage. This does not affect the local short term session container.
     */
    @MBeanMethodAnnotation (description="Clear all sessions in central session storage. This does not affect the local short term session container.", parameters={}, parameterDescriptions={})
    void clearSessionStorage() throws MBeanException;

}
