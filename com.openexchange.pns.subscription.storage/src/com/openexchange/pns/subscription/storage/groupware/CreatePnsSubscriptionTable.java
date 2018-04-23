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

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link CreatePnsSubscriptionTable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CreatePnsSubscriptionTable extends AbstractCreateTableImpl {

    /**
     * Gets the <code>CREATE TABLE</code> statement for <code>pns_subscription</code> table.
     *
     * @return The <code>CREATE TABLE</code> statement
     */
    public static String getTableSubscription() {
        return "CREATE TABLE pns_subscription (" +
            "id BINARY(16) NOT NULL," +
            "cid INT4 UNSIGNED NOT NULL," +
            "user INT4 UNSIGNED NOT NULL," +
            "token VARCHAR(255) CHARACTER SET latin1 NOT NULL," +
            "client VARCHAR(64) CHARACTER SET latin1 NOT NULL," +
            "transport VARCHAR(32) CHARACTER SET latin1 NOT NULL," +
            "last_modified BIGINT(64) NOT NULL," +
            "all_flag TINYINT UNSIGNED NOT NULL default '0'," +
            "PRIMARY KEY (cid, user, token, client)," +
            "UNIQUE KEY `subscription_id` (`id`)" +
            // "INDEX `affiliationIndex` (cid, user, affiliation)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
    }

    /**
     * Gets the <code>CREATE TABLE</code> statement for <code>pns_subscription_topic_wildcard</code> table.
     *
     * @return The <code>CREATE TABLE</code> statement
     */
    public static String getTableTopicWildcard() {
        return "CREATE TABLE pns_subscription_topic_wildcard (" +
            "id BINARY(16) NOT NULL," +
            "cid INT4 UNSIGNED NOT NULL," +
            "topic VARCHAR(255) CHARACTER SET latin1 NOT NULL," +
            "PRIMARY KEY (id, topic)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
    }

    /**
     * Gets the <code>CREATE TABLE</code> statement for <code>pns_subscription_topic_exact</code> table.
     *
     * @return The <code>CREATE TABLE</code> statement
     */
    public static String getTableTopicExact() {
        return "CREATE TABLE pns_subscription_topic_exact (" +
            "id BINARY(16) NOT NULL," +
            "cid INT4 UNSIGNED NOT NULL," +
            "topic VARCHAR(255) CHARACTER SET latin1 NOT NULL," +
            "PRIMARY KEY (id, topic)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CreatePnsSubscriptionTable() {
        super();
    }

    @Override
    public String[] getCreateStatements() {
        return new String[] { getTableSubscription(), getTableTopicWildcard(), getTableTopicExact() };
    }

    @Override
    public String[] requiredTables() {
        return new String[] { "user" };
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { "pns_subscription", "pns_subscription_topic_wildcard", "pns_subscription_topic_exact" };
    }

}
