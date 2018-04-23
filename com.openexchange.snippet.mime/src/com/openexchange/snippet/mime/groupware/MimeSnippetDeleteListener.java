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

package com.openexchange.snippet.mime.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.snippet.ReferenceType;
import com.openexchange.snippet.mime.MimeSnippetManagement;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link MimeSnippetDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeSnippetDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link MimeSnippetDeleteListener}.
     */
    public MimeSnippetDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER != event.getType()) {
            return;
        }
        /*
         * Writable connection
         */
        final int contextId = event.getContext().getContextId();
        PreparedStatement stmt = null;
        try {
            final int userId = event.getId();
            final List<String> ids;
            {
                ResultSet rs = null;
                try {
                    stmt =
                        writeCon.prepareStatement("SELECT id FROM snippet WHERE cid = ? AND user = ? AND refType=" + ReferenceType.FILE_STORAGE.getType());
                    int pos = 1;
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, userId);
                    rs = stmt.executeQuery();
                    ids = new LinkedList<String>();
                    while (rs.next()) {
                        ids.add(rs.getString(1));
                    }
                } finally {
                    DBUtils.closeSQLStuff(rs);
                }
            }
            DBUtils.closeSQLStuff(stmt);
            stmt = null;
            if (ids.isEmpty()) {
                return;
            }
            /*
             * Delete them
             */
            for (final String id : ids) {
                MimeSnippetManagement.deleteSnippet(id, userId, contextId, writeCon);
            }
        } catch (final SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

}
