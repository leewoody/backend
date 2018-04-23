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

package com.openexchange.groupware.filestore;

import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.sql.DBUtils;

public class RdbFilestoreStorage extends FilestoreStorage {

    @Override
    public Filestore getFilestore(int id) throws OXException {
        Connection con = DBPool.pickup();
        try {
            return getFilestore(con, id);
        } finally {
            DBPool.closeReaderSilent(con);
        }
    }

    @Override
    public Filestore getFilestore(Connection con, int id) throws OXException {
        if (null == con) {
            return getFilestore(id);
        }

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT uri, size, max_context FROM filestore WHERE id = ?");
            stmt.setInt(1,id);
            result = stmt.executeQuery();
            if (!result.next()) {
                throw FilestoreExceptionCodes.NO_SUCH_FILESTORE.create(I(id));
            }

            FilestoreImpl filestore = new FilestoreImpl();
            filestore.setId(id);
            String tmp = null;
            try {
                tmp = result.getString("uri");
                filestore.setUri(new URI(tmp));
            } catch (final URISyntaxException e) {
                throw FilestoreExceptionCodes.URI_CREATION_FAILED.create(e, tmp);
            }

            filestore.setSize(result.getLong("size"));
            filestore.setMaxContext(result.getLong("max_context"));
            return filestore;
        } catch (final SQLException e) {
            throw FilestoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
        }
    }

    @Override
    public Filestore getFilestore(URI uri) throws OXException {
        Connection con = DBPool.pickup();
        try {
            return getFilestore(con, uri);
        } finally {
            DBPool.closeReaderSilent(con);
        }
    }

    /**
     * Gets the file store for given URI using specified connection.
     *
     * @param con The connection to use
     * @param uri The URI to resolve
     * @return The associated file store
     * @throws OXException If file store cannot be resolved
     */
    public Filestore getFilestore(Connection con, URI uri) throws OXException {
        if (null == con) {
            return getFilestore(uri);
        }

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id, uri, size, max_context FROM filestore WHERE (? LIKE CONCAT(uri, '%'))");
            stmt.setString(1, uri.toString());
            result = stmt.executeQuery();
            if (!result.next()) {
                throw FilestoreExceptionCodes.NO_SUCH_FILESTORE.create(uri);
            }

            FilestoreImpl filestore = new FilestoreImpl();
            filestore.setId(result.getInt(1));
            String tmp = null;
            try {
                tmp = result.getString("uri");
                filestore.setUri(new URI(tmp));
            } catch (final URISyntaxException e) {
                throw FilestoreExceptionCodes.URI_CREATION_FAILED.create(e, tmp);
            }

            filestore.setSize(result.getLong("size"));
            filestore.setMaxContext(result.getLong("max_context"));
            return filestore;
        } catch (final SQLException e) {
            throw FilestoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
        }

    }
}
