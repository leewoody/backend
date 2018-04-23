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

package com.openexchange.folderstorage.internal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ImmutablePermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.java.Strings;
import com.openexchange.mail.utils.ImmutableReference;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.UserAndContext;

/**
 * {@link ConfiguredDefaultPermissions} - Checks for possibly configured default permissions for a new folder that is supposed to be created below a certain parent.
 * <pre>
 *   expressions := expression ("|" expression)*
 *
 *    expression := folder "=" permission ("," permission)*
 *
 *    permission := ("admin_")? ("group_" | "user_") entity(int) "@" rights
 *
 *        rights := (folder-permission(int) "." read-permission(int) "." write-permission(int) "." delete-permission(int)) | ("viewer" | "writer" | "author")
 * </pre>
 * Examples:
 * <pre>
 *   2=group_2@2.4.0.0,admin_user_5@8.4.4.4|15=admin_group_2@8.8.8.8
 *
 *   2=group_2@viewer,admin_user_5@author|15=admin_group_2@writer
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ConfiguredDefaultPermissions {

    private static final String PROP_DEFAULT_PERMISSIONS = "com.openexchange.folderstorage.defaultPermissions";

    private static final ConfiguredDefaultPermissions INSTANCE = new ConfiguredDefaultPermissions();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static ConfiguredDefaultPermissions getInstance() {
        return INSTANCE;
    }

    // --------------------------------------------------------------------------------------------------

    private final Cache<UserAndContext, ImmutableReference<Map<String, List<Permission>>>> cache;

    /**
     * Initializes a new {@link ConfiguredDefaultPermissions}.
     */
    private ConfiguredDefaultPermissions() {
        super();
        cache = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();
    }

    /**
     * Invalidates the cache.
     */
    public void invalidateCache() {
        cache.invalidateAll();
    }

    /**
     * Gets the configured default permissions for a new folder that is supposed to be created below specified parent.
     *
     * @param parentId The identifier of the parent folder
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The configured default permissions or <code>null</code>
     * @throws OXException If look-up for configured default permissions fails
     */
    public Permission[] getConfiguredDefaultPermissionsFor(String parentId, int userId, int contextId) throws OXException {
        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        ImmutableReference<Map<String, List<Permission>>> ref = cache.getIfPresent(key);
        if (null == ref) {
            ConfigViewFactory viewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
            if (null == viewFactory) {
                return null;
            }

            Map<String, List<Permission>> configuredDefaultPermissions = getConfiguredDefaultPermissionsFor(userId, contextId, viewFactory);
            ref = new ImmutableReference<Map<String, List<Permission>>>(configuredDefaultPermissions);
            cache.put(key, ref);
        }

        Map<String, List<Permission>> map = ref.getValue();
        if (null == map) {
            return null;
        }
        List<Permission> perms = map.get(parentId);
        return null == perms ? null : perms.toArray(new Permission[perms.size()]);
    }

    private Map<String, List<Permission>> getConfiguredDefaultPermissionsFor(int userId, int contextId, ConfigViewFactory viewFactory) throws OXException {
        ConfigView view = viewFactory.getView(userId, contextId);

        ComposedConfigProperty<String> property = view.property(PROP_DEFAULT_PERMISSIONS, String.class);
        if (null == property || !property.isDefined()) {
            return null;
        }

        String expressionsLine = property.get();
        if (Strings.isEmpty(expressionsLine)) {
            return null;
        }

        return parseExpressionsLine(expressionsLine);
    }

    private Map<String, List<Permission>> parseExpressionsLine(String expressionsLine) throws OXException {
        // E.g. 2=group_2@2.4.0.0,admin_user_5@8.4.4.4|15=admin_group_2@author
        List<String> expressions = Strings.splitAndTrim(expressionsLine, Pattern.quote("|"));
        ImmutableMap.Builder<String, List<Permission>> mapBuilder = ImmutableMap.builder();
        ImmutablePermission.Builder permissionBuilder = ImmutablePermission.builder();
        for (String expression : expressions) {
            // E.g. 2=group_2@2.4.0.0,admin_user_5@8.4.4.4
            parseExpression(expression, permissionBuilder, mapBuilder);
        }

        return mapBuilder.build();
    }

    private void parseExpression(String expression, ImmutablePermission.Builder permissionBuilder, ImmutableMap.Builder<String, List<Permission>> mapBuilder) throws OXException {
        // E.g. 2=group_2@2.4.0.0,admin_user_5@8.4.4.4
        int pos = expression.indexOf('=');
        if (pos < 1) {
            throw OXException.general("Invalid value for property \"" + PROP_DEFAULT_PERMISSIONS + "\". Missing '=' character in expression: " + expression);
        }

        String folderId = expression.substring(0, pos).trim();
        ImmutableList.Builder<Permission> listBuilder = ImmutableList.builder();
        int off;
        for (String permExpression : Strings.splitByComma(expression.substring(pos + 1).trim())) {
            // E.g. group_2@2.4.0.0
            boolean admin = false;
            boolean group = false;
            off = 0;

            if (permExpression.startsWith("admin_", off)) {
                admin = true;
                off += 6;
            }

            if (permExpression.startsWith("group_", off)) {
                group = true;
                off += 6;
            } else if (permExpression.startsWith("user_", off)) {
                off += 5;
            } else {
                throw OXException.general("Invalid value for property \"" + PROP_DEFAULT_PERMISSIONS + "\". Permission expression is required to start with either \"admin_user_\", \"user_\", \"admin_group_\" or \"group_\" prefix: " + expression);
            }

            pos = permExpression.indexOf('@', off);
            if (pos < 0) {
                throw OXException.general("Invalid value for property \"" + PROP_DEFAULT_PERMISSIONS + "\". Missing '@' character in expression: " + expression);
            }
            int entityId = Integer.parseInt(permExpression.substring(off, pos));
            off = pos + 1;

            permissionBuilder.reset();
            permissionBuilder.setAdmin(admin).setGroup(group).setEntity(entityId);

            int fp, rp, wp, dp;
            if (permExpression.startsWith("viewer", off)) {
                fp = Permission.READ_FOLDER;
                rp = Permission.READ_ALL_OBJECTS;
                wp = Permission.NO_PERMISSIONS;
                dp = Permission.NO_PERMISSIONS;
            } else if (permExpression.startsWith("writer", off)) {
                fp = Permission.READ_FOLDER;
                rp = Permission.READ_ALL_OBJECTS;
                wp = Permission.WRITE_ALL_OBJECTS;
                dp = Permission.NO_PERMISSIONS;
            } else if (permExpression.startsWith("author", off)) {
                fp = Permission.CREATE_SUB_FOLDERS;
                rp = Permission.READ_ALL_OBJECTS;
                wp = Permission.WRITE_ALL_OBJECTS;
                dp = Permission.DELETE_ALL_OBJECTS;
            } else {
                pos = permExpression.indexOf('.', off);
                if (pos < 0) {
                    throw OXException.general("Invalid value for property \"" + PROP_DEFAULT_PERMISSIONS + "\". Expected a '.' delimiter in rights expression: " + expression);
                }
                fp = Integer.parseInt(permExpression.substring(off, pos));
                off = pos + 1;

                pos = permExpression.indexOf('.', off);
                if (pos < 0) {
                    throw OXException.general("Invalid value for property \"" + PROP_DEFAULT_PERMISSIONS + "\". Expected a '.' delimiter in rights expression: " + expression);
                }
                rp = Integer.parseInt(permExpression.substring(off, pos));
                off = pos + 1;

                pos = permExpression.indexOf('.', off);
                if (pos < 0) {
                    throw OXException.general("Invalid value for property \"" + PROP_DEFAULT_PERMISSIONS + "\". Expected a '.' delimiter in rights expression: " + expression);
                }
                wp = Integer.parseInt(permExpression.substring(off, pos));
                off = pos + 1;

                dp = Integer.parseInt(permExpression.substring(off));
                //off = pos + 1;
            }

            permissionBuilder.setFolderPermission(fp).setReadPermission(rp).setWritePermission(wp).setDeletePermission(dp).setSystem(0);
            listBuilder.add(permissionBuilder.build());
        }

        mapBuilder.put(folderId, listBuilder.build());
    }

}
