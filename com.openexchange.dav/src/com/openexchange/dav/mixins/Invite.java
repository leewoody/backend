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

package com.openexchange.dav.mixins;

import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.resources.CommonFolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link Invite}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class Invite extends SingleXMLPropertyMixin {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Invite.class);

    private final CommonFolderCollection<?> collection;

    /**
     * Initializes a new {@link Invite}.
     *
     * @param collection The collection
     */
    public Invite(CommonFolderCollection<?> collection) {
        super(DAVProtocol.DAV_NS.getURI(), "invite");
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        if (null == collection || null == collection.getFolder() || null == collection.getFolder().getPermissions()) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        Permission[] permissions = collection.getFolder().getPermissions();
        if (PublicType.getInstance().equals(collection.getFolder().getType())) {

        } else {
            for (Permission permission : permissions) {
                if (permission.isAdmin()) {
                    continue; // don't include "owner"
                }
                try {
                    stringBuilder.append(getEntityElements(permission));
                } catch (OXException e) {
                    LOG.warn("error resolving permission entity from '{}'", collection.getFolder(), e);
                }
            }
        }
        return stringBuilder.toString();
    }

    private String getEntityElements(Permission permission) throws OXException {
        DAVFactory factory = collection.getFactory();
        String commonName;
        String uri;
        if (permission.isGroup()) {
            uri = PrincipalURL.forGroup(permission.getEntity());
            Group group = factory.requireService(GroupService.class).getGroup(factory.getContext(), permission.getEntity());
            commonName = " + " + group.getDisplayName();
        } else {
            uri = PrincipalURL.forUser(permission.getEntity());
            User user = factory.requireService(UserService.class).getUser(permission.getEntity(), factory.getContext());
            commonName = user.getDisplayName();
            if (Strings.isEmpty(commonName)) {
                commonName = user.getMail();
                if (Strings.isEmpty(commonName)) {
                    commonName = "User " + user.getId();
                }
            }
        }
        String shareAccess;
        if (impliesReadWritePermissions(permission)) {
            shareAccess = "read-write";
        } else if (impliesReadPermissions(permission)) {
            shareAccess = "read";
        } else {
            shareAccess = "no-access";
        }
        return new StringBuilder().append("<D:sharee>")
            .append("<D:href>").append(uri).append("</D:href>")
            .append("<D:invite-accepted />")
            .append("<D:share-access>").append(shareAccess).append("</D:share-access>")
//            .append("<CS:common-name>").append(commonName).append("</CS:common-name>")
        .append("</D:sharee>").toString();
    }

    /**
     * Gets a value indicating whether the supplied permission implies (at least) a simplified CalDAV "read" access level.
     *
     * @param permission The permission to check
     * @return <code>true</code> if "read" permissions can be assumed, <code>false</code>, otherwise
     */
    private static boolean impliesReadPermissions(Permission permission) {
        return null != permission &&
            permission.getFolderPermission() >= Permission.READ_FOLDER &&
            permission.getReadPermission() >= Permission.READ_OWN_OBJECTS
        ;
    }

    /**
     * Gets a value indicating whether the supplied permission implies (at least) a simplified CalDAV "read-write" access level.
     *
     * @param permission The permission to check
     * @return <code>true</code> if "read-write" permissions can be assumed, <code>false</code>, otherwise
     */
    private static boolean impliesReadWritePermissions(Permission permission) {
        return null != permission &&
            permission.getFolderPermission() >= Permission.CREATE_OBJECTS_IN_FOLDER &&
            permission.getWritePermission() >= Permission.WRITE_OWN_OBJECTS &&
            permission.getDeletePermission() >= Permission.DELETE_OWN_OBJECTS &&
            permission.getReadPermission() >= Permission.READ_OWN_OBJECTS
        ;
    }

}
