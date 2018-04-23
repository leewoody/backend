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

package com.openexchange.dovecot.doveadm.client;

/**
 * {@link DoveAdmResponse} - Represents a response from the Dovecot DoveAdm REST interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface DoveAdmResponse extends DoveAdmEntity {

    /** The command identifier to signal a regular DoveAdm data response */
    public static final String TYPE_DATA_RESPONSE = "doveadmResponse";

    /** The command identifier to signal a DoveAdm error response */
    public static final String TYPE_ERROR_RESPONSE = "error";

    /**
     * Checks if this DoveAdm response is an error.
     *
     * @return <code>true</code> if this DoveAdm response is an error; otherwise <code>false</code>
     */
    boolean isError();

    /**
     * Gets the error response representation from this response.
     * <p>
     * Only makes sense in case {@link #isError()} returns <code>true</code>.
     *
     * @return The error response representation or <code>null</code> if not applicable
     */
    DoveAdmErrorResponse asErrorResponse();

    /**
     * Gets the data response representation from this response.
     * <p>
     * Only makes sense in case {@link #isError()} returns <code>false</code>.
     *
     * @return The data response representation or <code>null</code> if not applicable
     */
    DoveAdmDataResponse asDataResponse();

}
