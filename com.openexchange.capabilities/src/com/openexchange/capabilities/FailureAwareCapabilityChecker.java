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

package com.openexchange.capabilities;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * A {@link FailureAwareCapabilityChecker} that extends common <code>CapabilityChecker</code> to deal with possible errors while checking.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class FailureAwareCapabilityChecker implements CapabilityChecker {

    /**
     * The possible results for a capability check.
     */
    public static enum Result {
        /**
         * Capability check passed successfully.
         */
        ENABLED,
        /**
         * Signals that the capability must not be granted.
         */
        DISABLED,
        /**
         * Signals that capability check could not be performed due to an error.
         */
        FAILURE,
        ;
    }

    /**
     * Initializes a new {@link FailureAwareCapabilityChecker}.
     */
    protected FailureAwareCapabilityChecker() {
        super();
    }

    /**
     * Check whether the capability should be awarded for a certain user
     *
     * @param capability The capability to check
     * @param session Provides the users session for which to check
     * @return The result
     * @throws OXException If check fails
     */
    public abstract Result checkEnabled(String capability, Session session) throws OXException;

    /**
     * Check whether the capability should be awarded for a certain user
     *
     * @param capability The capability to check
     * @param session Provides the users session for which to check
     * @return Whether to award this capability or not
     * @throws OXException If check fails
     */
    @Override
    public final boolean isEnabled(String capability, Session session) throws OXException {
        Result result = checkEnabled(capability, session);
        return Result.ENABLED == result ? true : false;
    }
}
