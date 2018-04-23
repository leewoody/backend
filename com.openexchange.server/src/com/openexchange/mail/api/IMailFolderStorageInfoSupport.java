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

package com.openexchange.mail.api;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailFolderInfo;

/**
 * {@link IMailFolderStorageInfoSupport} - Extends basic folder storage by mailbox info support.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMailFolderStorageInfoSupport extends IMailFolderStorage {

    /**
     * Indicates if mailbox info is supported.
     *
     * @return <code>true</code> if supported; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isInfoSupported() throws OXException;

    /**
     * Gets the folder information for specified full name.
     *
     * @param fullName the folder full name
     * @return The folder information
     * @throws OXException If an error occurs
     */
    MailFolderInfo getFolderInfo(String fullName) throws OXException;

    /**
     * Gets the folder information listing for this folder storage
     *
     * @param subscribedOnly <code>true</code> to return only subscribed folder information; otherwise <code>false</code> to return all regardless of subscribed flag
     * @return The folder information listing
     * @throws OXException If an error occurs
     */
    List<MailFolderInfo> getAllFolderInfos(boolean subscribedOnly) throws OXException;

    /**
     * Gets the folder information listing for this folder storage
     *
     * @param optParentFullName The optional full name of the parent; if missing complete folder information is returned
     * @param subscribedOnly <code>true</code> to return only subscribed folder information; otherwise <code>false</code> to return all regardless of subscribed flag
     * @return The folder information listing
     * @throws OXException If an error occurs
     */
    List<MailFolderInfo> getFolderInfos(String optParentFullName, boolean subscribedOnly) throws OXException;

}
