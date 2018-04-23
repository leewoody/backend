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
package com.openexchange.admin.storage.sqlStorage;

import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.storage.interfaces.OXGroupStorageInterface;
import java.sql.Connection;

/**
 * This class implements the global storage interface and creates a layer
 * between the abstract storage definition and a storage in a SQL accessible
 * database
 *
 * @author d7
 * @author cutmasta
 */
/**
 * @author d7
 *
 */
public abstract class OXGroupSQLStorage extends OXGroupStorageInterface {

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#addMember(com.openexchange.admin.rmi.dataobjects.Context,
     *      int, User[])
     */
    @Override
    abstract public void addMember(final Context ctx, final int grp_id, final User[] members) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#change(com.openexchange.admin.rmi.dataobjects.Context,
     *      com.openexchange.admin.rmi.dataobjects.Group)
     */
    @Override
    abstract public void change(final Context ctx, final Group grp) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#create(com.openexchange.admin.rmi.dataobjects.Context,
     *      com.openexchange.admin.rmi.dataobjects.Group)
     */
    @Override
    abstract public int create(final Context ctx, final Group grp) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#getMembers(com.openexchange.admin.rmi.dataobjects.Context,
     *      int)
     */
    @Override
    abstract public User[] getMembers(final Context ctx, final int grp_id) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#list(com.openexchange.admin.rmi.dataobjects.Context,
     *      java.lang.String)
     */
    @Override
    abstract public Group[] list(final Context ctx, final String pattern) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#removeMember(com.openexchange.admin.rmi.dataobjects.Context,
     *      int, User[])
     */
    @Override
    abstract public void removeMember(final Context ctx, final int grp_id, final User[] members) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#deleteRecoveryData(com.openexchange.admin.rmi.dataobjects.Context,
     *      int, java.sql.Connection)
     */
    @Override
    abstract public void deleteRecoveryData(final Context ctx, final int group_id, Connection con) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#deleteAllRecoveryData(com.openexchange.admin.rmi.dataobjects.Context,
     *      java.sql.Connection)
     */
    @Override
    abstract public void deleteAllRecoveryData(final Context ctx, Connection con) throws StorageException;

}
