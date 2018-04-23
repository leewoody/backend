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

package com.openexchange.client.onboarding;

import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.Service;
import com.openexchange.session.Session;

/**
 * {@link OnboardingProvider} - Represents an on-boarding provider suitable for configuring/integrating a client for communicating with the
 * Open-Xchange Middleware.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
@Service
public interface OnboardingProvider {

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Gets this provider's description
     *
     * @return The description
     */
    String getDescription();

    /**
     * Gets the supported devices.
     *
     * @return The supported devices
     */
    Set<Device> getSupportedDevices();

    /**
     * Gets the on-boarding types, which are supported by this provider
     *
     * @return The supported on-boarding types
     */
    Set<OnboardingType> getSupportedTypes();

    /**
     * Executes specified on-boarding scenario according to given action.
     *
     * @param request The on-boarding request
     * @param previousResult The optional previous result or <code>null</code>
     * @param session The session
     * @return The execution result
     * @throws OXException If execution fails
     */
    Result execute(OnboardingRequest request, Result previousResult, Session session) throws OXException;

    /**
     * Checks if this provider is enabled for session-associated user.
     *
     * @param session The session
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If availability cannot be checked
     */
    AvailabilityResult isAvailable(Session session) throws OXException;

    /**
     * Checks if this provider is enabled for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If availability cannot be checked
     */
    AvailabilityResult isAvailable(int userId, int contextId) throws OXException;

}
