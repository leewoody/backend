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

package com.openexchange.control.consoleold.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import com.openexchange.control.consoleold.ConsoleException;

/**
 * {@link ValueParser} - Parses passed command-line arguments.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class ValueParser {

    final List<ValuePairObject> valuePairObjectList = new ArrayList<ValuePairObject>();

    final List<ValueObject> valueList = new ArrayList<ValueObject>();

    /**
     * Initializes a new {@link ValueParser}.
     *
     * @param args The command-line arguments.
     * @param parameter The names of those parameters which should be considered as name-value-pairs in specified command-line arguments.
     * @throws ConsoleException If parsing the passed arguments fails.
     */
    public ValueParser(final String[] args, final String[] parameter) throws ConsoleException {
        final HashSet<String> parameterSet = new HashSet<String>();

        for (int a = 0; a < parameter.length; a++) {
            parameterSet.add(parameter[a]);
        }

        for (int a = 0; a < args.length; a++) {
            final String param = args[a];
            if (parameterSet.contains(param)) {
                final ValuePairObject valuePairObject = parseValuePair(param, args, a);
                valuePairObjectList.add(valuePairObject);
                a++;
            } else {
                final ValueObject valueObject = new ValueObject(param);
                valueList.add(valueObject);
            }
        }
    }

    private ValuePairObject parseValuePair(final String name, final String[] args, final int pos) throws ConsoleException {
        if (pos >= args.length - 1) {
            throw new ConsoleException("missing value for parameter: " + name);
        }
        final String value = args[pos + 1];
        final ValuePairObject valuePairObject = new ValuePairObject(name, value);
        return valuePairObject;
    }

    /**
     * Gets the name-value-pair arguments.
     *
     * @return The name-value-pair arguments.
     */
    public ValuePairObject[] getValuePairObjects() {
        return valuePairObjectList.toArray(new ValuePairObject[valuePairObjectList.size()]);
    }

    /**
     * Gets the sole values without an associated name.
     *
     * @return The sole values without an associated name.
     */
    public ValueObject[] getValueObjects() {
        return valueList.toArray(new ValueObject[valueList.size()]);
    }
}
