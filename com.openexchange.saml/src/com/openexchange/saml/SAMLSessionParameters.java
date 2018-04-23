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

package com.openexchange.saml;


/**
 * SAML specific keys of session properties.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLSessionParameters {

    /**
     * com.openexchange.saml.Authenticated
     */
    public static final String AUTHENTICATED = "com.openexchange.saml.Authenticated";

    /**
     * com.openexchange.saml.SubjectID
     */
    public static final String SUBJECT_ID = "com.openexchange.saml.SubjectID";

    /**
     * com.openexchange.saml.SessionNotOnOrAfter
     */
    public static final String SESSION_NOT_ON_OR_AFTER = "com.openexchange.saml.SessionNotOnOrAfter";

    /**
     * com.openexchange.saml.SessionIndex
     */
    public static final String SESSION_INDEX = "com.openexchange.saml.SessionIndex";

    /**
     * com.openexchange.saml.SessionCookie
     */
    public static final String SESSION_COOKIE = "com.openexchange.saml.SessionCookie";

    /**
     * com.openexchange.saml.AccessToken
     */
    public static final String ACCESS_TOKEN = "com.openexchange.saml.AccessToken";

    /**
     * com.openexchange.saml.RefreshToken
     */
    public static final String REFRESH_TOKEN = "com.openexchange.saml.RefreshToken";

    /**
     * com.openexchange.saml.SamlPath
     */
    public static final String SAML_PATH = "com.openexchange.saml.SamlPath";

    /**
     * com.openexchange.saml.SingleLogout
     */
    public static final String SINGLE_LOGOUT = "com.openexchange.saml.SingleLogout";
}
