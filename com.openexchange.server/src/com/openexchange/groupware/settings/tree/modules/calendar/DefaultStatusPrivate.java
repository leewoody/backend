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

package com.openexchange.groupware.settings.tree.modules.calendar;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingExceptionCodes;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.session.Session;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class DefaultStatusPrivate implements PreferencesItemService {

    private static final String[] PATH = new String[] { "modules", "calendar", "defaultStatusPrivate" };

    @Override
    public String[] getPath() {
        return PATH;
    }

    @Override
    public IValueHandler getSharedValue() {
        return new IValueHandler() {

            @Override
            public int getId() {
                return -1;
            }

            @Override
            public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                Integer value = ServerUserSetting.getInstance().getDefaultStatusPrivate(ctx.getContextId(), user.getId());
                if (value == null) {
                    value = Integer.valueOf(0);
                }
                setting.setSingleValue(value);
            }

            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasCalendar();
            }

            @Override
            public boolean isWritable() {
                return true;
            }

            @Override
            public void writeValue(final Session session, final Context ctx, final User user, final Setting setting) throws OXException {
                Integer value;
                try {
                    value = new Integer(String.valueOf(setting.getSingleValue()));
                } catch (final NumberFormatException e) {
                    throw SettingExceptionCodes.INVALID_VALUE.create(e, setting.getSingleValue());
                }
                if (value < 0 || value > 3) {
                    throw SettingExceptionCodes.INVALID_VALUE.create(setting.getSingleValue());
                }
                ServerUserSetting.getInstance().setDefaultStatusPrivate(ctx.getContextId(), user.getId(), value);
            }

        };
    }

}
