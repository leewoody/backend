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

package com.openexchange.share.notification.impl;

import java.util.Date;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;

/**
 * A notification to inform arbitrary recipients about created links.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface LinkCreatedNotification<T> extends ShareNotification<T> {

    /**
     * Gets the session of the new shares creator.
     *
     * @return The session
     */
    Session getSession();

    /**
     * Gets the share target to notify the recipient about.
     *
     * @return The share target, never <code>null</code>
     */
    ShareTarget getShareTarget();

    /**
     * Gets an optional message that will be shown to the recipient if appropriate. Whether a message is shown or not depends on the
     * {@link NotificationType}.
     *
     * @return The message or <code>null</code>, if nothing was provided
     */
    String getMessage();

    /**
     * Get the id of the user something is shared to
     *
     * @return The user ID
     */
    int getTargetUserID();

    /**
     * Gets the URL to the according share.
     *
     * @return The URL
     */
    String getShareUrl();

    /**
     * If defined, gets the date when this share expires, i.e. it should be no longer accessible.
     *
     * @return The expiry date of the share, or <code>null</code> if not defined
     */
    Date getExpiryDate();

    /**
     * Gets the password that is necessary to access the link.
     *
     * @return The password or <code>null</code> if none was set.
     */
    String getPassword();

}
