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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.update.Tools.createIndex;
import static com.openexchange.tools.update.Tools.existsIndex;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import org.slf4j.Logger;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.groupware.update.WorkingLevel;

/**
 * {@link ContactsAddDepartmentIndex4AutoCompleteSearch}
 *
 * (Re-)adds department index in prg_contacts for "auto-complete" queries
 * 
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class ContactsAddDepartmentIndex4AutoCompleteSearch extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link ContactsAddIndex4AutoCompleteSearchV2}.
     */
    public ContactsAddDepartmentIndex4AutoCompleteSearch() {
        super();
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING, WorkingLevel.SCHEMA);
    }

    @Override
    public String[] getDependencies() {
        return new String[] { MakeFolderIdPrimaryForDelContactsTable.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Logger log = org.slf4j.LoggerFactory.getLogger(ContactsAddDepartmentIndex4AutoCompleteSearch.class);
        log.info("Performing update task {}", ContactsAddDepartmentIndex4AutoCompleteSearch.class.getSimpleName());
        Connection connection = Database.getNoTimeout(params.getContextId(), true);
        boolean committed = false;
        try {
            connection.setAutoCommit(false);
            createIndexIfNeeded(log, connection, new String[] { "cid", "field19" }, "department");
            connection.commit();
            committed = true;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (false == committed) {
                rollback(connection);
            }
            autocommit(connection);
            Database.backNoTimeout(params.getContextId(), true, connection);
        }
        log.info("{} successfully performed.", ContactsAddDepartmentIndex4AutoCompleteSearch.class.getSimpleName());
    }

    private static void createIndexIfNeeded(Logger log, Connection connection, String[] columns, String indexName) throws SQLException {
        String existingIndex = existsIndex(connection, "prg_contacts", columns);
        if (null == existingIndex) {
            log.info("Creating new index named \"{}\" with columns ({}) on table \"prg_contacts\".", indexName, Arrays.toString(columns));
            createIndex(connection, "prg_contacts", indexName, columns, false);
        } else {
            log.info("Found existing index named \"{}\" with columns ({}) on table \"prg_contacts\".", indexName, Arrays.toString(columns));
        }
    }
}
