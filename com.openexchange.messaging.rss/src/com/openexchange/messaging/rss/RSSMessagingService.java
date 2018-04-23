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

package com.openexchange.messaging.rss;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAction;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.generic.DefaultMessagingAccountManager;
import com.openexchange.session.Session;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;

/**
 * {@link RSSMessagingService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RSSMessagingService implements MessagingService {

    private static final String DISPLAY_NAME = "RSS Feed";
    private static final DynamicFormDescription FORM_DESCRIPTION;
    public static final String ID = "com.openexchange.messaging.rss";

    static {
        final DynamicFormDescription fd = new DynamicFormDescription();
        fd.add(FormElement.input("url", FormStrings.FORM_LABEL_URL));
        FORM_DESCRIPTION = new ReadOnlyDynamicFormDescription(fd);
    }

    private final MessagingAccountManager accountManager = new DefaultMessagingAccountManager(this);

    private final FeedFetcher fetcher = new HttpURLFeedFetcher(HashMapFeedInfoCache.getInstance());

    @Override
    public MessagingAccountAccess getAccountAccess(final int accountId, final Session session) {
        return new RSSFeedOperations(accountId, session, fetcher, accountManager);
    }

    @Override
    public MessagingAccountManager getAccountManager() {
        return accountManager;
    }

    @Override
    public MessagingAccountTransport getAccountTransport(final int accountId, final Session session) {
        return new RSSFeedOperations(accountId, session, fetcher, accountManager);
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.emptySet();
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return FORM_DESCRIPTION;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<MessagingAction> getMessageActions() {
        return Collections.emptyList();
    }

    public static int[] getStaticRootPerms() {
        return new int[] {MessagingPermission.READ_FOLDER,
            MessagingPermission.READ_ALL_OBJECTS,
            MessagingPermission.NO_PERMISSIONS,
            MessagingPermission.DELETE_OWN_OBJECTS};
    }

    @Override
    public int[] getStaticRootPermissions() {
        return getStaticRootPerms();
    }

    public static String buildFolderId(final int accountId, final String folder) {
        final StringBuilder stringBuilder = new StringBuilder(ID);
        stringBuilder.append("://").append(accountId).append('/').append(folder);
        return stringBuilder.toString();
    }
}
