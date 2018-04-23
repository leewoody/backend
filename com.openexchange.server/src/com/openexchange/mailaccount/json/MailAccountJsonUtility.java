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

package com.openexchange.mailaccount.json;

import static com.openexchange.java.Strings.isEmpty;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Tools;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link MailAccountJsonUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class MailAccountJsonUtility {

    /**
     * Initializes a new {@link MailAccountJsonUtility}.
     */
    private MailAccountJsonUtility() {
        super();
    }

    /**
     * Parses the attributes from passed comma-separated list.
     *
     * @param colString The comma-separated list
     * @return The parsed attributes
     */
    public static List<Attribute> getColumns(String colString) {
        List<Attribute> attributes = null;
        if (Strings.isNotEmpty(colString)) {
            if ("all".equalsIgnoreCase(colString)) {
                // All columns
                return Arrays.asList(Attribute.values());
            }

            attributes = new LinkedList<Attribute>();
            for (String col : Strings.splitByComma(colString)) {
                if (Strings.isNotEmpty(col)) {
                    int id = parseInt(col);
                    Attribute attr = id > 0 ? Attribute.getById(id) : null;
                    if (null != attr) {
                        attributes.add(attr);
                    }
                }
            }
            return attributes;
        }

        // All columns
        return Arrays.asList(Attribute.values());
    }

    private static int parseInt(String col) {
        return Tools.getUnsignedInteger(col);
    }

    /**
     * Checks validity of values for needed fields.
     *
     * @param accountDescription The account description
     * @throws OXException If a needed field's value is invalid
     */
    public static void checkNeededFields(final MailAccountDescription accountDescription) throws OXException {
        // Check needed fields
        if (isEmpty(accountDescription.getMailServer())) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(MailAccountFields.MAIL_URL);
        }
        if (isEmpty(accountDescription.getLogin())) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(MailAccountFields.LOGIN);
        }
        if (isEmpty(accountDescription.getPassword()) && false == accountDescription.isMailOAuthAble()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(MailAccountFields.PASSWORD);
        }
    }

}
