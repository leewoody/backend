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

package com.openexchange.folderstorage.virtual.sql;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.virtual.osgi.Services;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Update} - SQL for updating a virtual folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Update {

    /**
     * Initializes a new {@link Update}.
     */
    private Update() {
        super();
    }

    /**
     * Updates specified folder.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param folder The folder
     * @throws OXException If update fails
     */
    public static void updateFolder(final int cid, final int tree, final int user, final Folder folder, final Session session) throws OXException {
        final DatabaseService databaseService = Services.getService(DatabaseService.class);
        // Get a connection
        final Connection con = databaseService.getWritable(cid);
        try {
            con.setAutoCommit(false); // BEGIN
            Delete.deleteFolder(cid, tree, user, folder.getID(), false, session, con);
            Insert.insertFolder(cid, tree, user, folder, null, session, con);
            con.commit(); // COMMIT
        } catch (final SQLException e) {
            DBUtils.rollback(con); // ROLLBACK
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            DBUtils.rollback(con); // ROLLBACK
            throw e;
        } catch (final Exception e) {
            DBUtils.rollback(con); // ROLLBACK
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            databaseService.backWritable(cid, con);
        }
    }

}
