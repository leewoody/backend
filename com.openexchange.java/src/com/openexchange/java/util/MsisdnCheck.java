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

package com.openexchange.java.util;

import java.util.regex.Pattern;
import com.openexchange.java.Strings;

/**
 * {@link MsisdnCheck} - Checks for valid MSISDN numbers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MsisdnCheck {

    /**
     * Initializes a new {@link MsisdnCheck}.
     */
    private MsisdnCheck() {
        super();
    }

    private static final Pattern PATTERN_VALIDATE = Pattern.compile("(^\\+[0-9]{2}|^\\+[0-9]{2}\\(0\\)|^\\(\\+[0-9]{2}\\)\\(0\\)|^00[0-9]{2}|^0)([0-9]{9}$|[0-9\\-\\s]{10}$)");

    /**
     * Validates specified MSISDN number.
     * <p>
     * Matches:
     * <ul>
     * <li><code>+31235256677</code></li>
     * <li><code>+31(0)235256677</code></li>
     * <li><code>023-5256677</code></li>
     * </ul>
     *
     * @param number The possible MSISDN number to validate
     * @return <code>true</code> if valid MSISDN number; otherwise <code>false</code>
     */
    public static boolean validate(final String number) {
        if (com.openexchange.java.Strings.isEmpty(number)) {
            return false;
        }
        return PATTERN_VALIDATE.matcher(number).matches();
    }

    /**
     * Checks if the given MSISDN number only consists of digits.
     *
     * @param number the mobile phone number to check
     * @return returns either "invalid" if the given number is not valid (contains non-digits) or the phone number in the format xxyyyzzzzzzzz
     **/
    public static boolean checkMsisdn(final String number) {
        if (com.openexchange.java.Strings.isEmpty(number)) {
            return false;
        }
        String num = number;
        {
            final int pos = num.indexOf('/');
            if (pos > 0) {
                num = num.substring(0, pos);
            }
        }
        num = cleanup(num);
        final int len = num.length();
        boolean isDigit = true;
        for (int i = 0; isDigit && i < len; i++) {
            isDigit = Strings.isDigit(num.charAt(i));
        }
        return isDigit;
    }

    private static final Pattern PATTERN_CLEANUP = Pattern.compile("[+()/ ]");

    /**
     * Cleans-up specified MSISDN number.
     *
     * @param number The MSISDN number
     * @return The cleaned-up MSISDN number
     */
    public static String cleanup(String number) {
        return PATTERN_CLEANUP.matcher(number).replaceAll("");
    }
}