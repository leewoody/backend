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

package com.openexchange.pns.subscription.storage.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.subscription.storage.rdb.RdbPushSubscriptionRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link PnsDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PnsDeleteListener implements DeleteListener {

    private final RdbPushSubscriptionRegistry registry;

    /**
     * Initializes a new {@link PnsDeleteListener}.
     */
    public PnsDeleteListener(RdbPushSubscriptionRegistry registry) {
        super();
        this.registry = registry;
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER == event.getType()) {
            int contextId = event.getContext().getContextId();
            int userId = event.getId();
            List<byte[]> ids = getSubscriptionIds(userId, contextId, writeCon);
            if (!ids.isEmpty()) {
                for (byte[] id : ids) {
                    registry.deleteById(id, null, writeCon);
                }
            }
        } else if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            int contextId = event.getContext().getContextId();
            List<byte[]> ids = getSubscriptionIds(0, contextId, writeCon);
            if (!ids.isEmpty()) {
                for (byte[] id : ids) {
                    registry.deleteById(id, null, writeCon);
                }
            }
        }
    }

    private List<byte[]> getSubscriptionIds(int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(userId > 0 ? "SELECT id FROM pns_subscription WHERE cid=? AND user = ?" : "SELECT id FROM pns_subscription WHERE cid=?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            if (userId > 0) {
                stmt.setInt(pos++, userId);
            }
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<byte[]> ids = new LinkedList<>();
            do {
                ids.add(rs.getBytes(1));
            } while (rs.next());
            return ids;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

}
