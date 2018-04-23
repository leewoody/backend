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

package com.openexchange.tools.regex;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ParseCookiesTest {

    private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ParseCookiesTest.class);

    /**
     * @param name
     */
    public ParseCookiesTest(final String name) {
        super();
    }

    @Test
    public void testCookie() throws Throwable {

        LOG.info("Version: " + RFC2616Regex.COOKIE_VERSION);
        final String version = "$Version=0";
        assertTrue(RFC2616Regex.COOKIE_VERSION.matcher(version).matches());

        LOG.info(RFC2616Regex.COOKIE_VALUE.toString());
        final String simple = "sessionid=aaa713ea6275b42205f040c6614701b7";
        assertTrue(RFC2616Regex.COOKIE_VALUE.matcher(simple).matches());

        LOG.info(RFC2616Regex.COOKIES.toString());
        final String firstCookie = "$Version=0; sessionid=aaa713ea6275b42205f040c6614701b7";
        assertTrue(RFC2616Regex.COOKIES.matcher(firstCookie).matches());

        final String two = "sessionid=aaa713ea6275b42205f040c6614701b7, JSESSIONID=1fed47c83eb3d3d5178c510bbde887499591f014.OX1";
        assertTrue(RFC2616Regex.COOKIES.matcher(two).matches());

        final String twoWithFirstVersion = "$Version=0; sessionid=aaa713ea6275b42205f040c6614701b7, JSESSIONID=1fed47c83eb3d3d5178c510bbde887499591f014.OX1";
        assertTrue(RFC2616Regex.COOKIES.matcher(twoWithFirstVersion).matches());

        final String twoWithVersion = "$Version=0; sessionid=aaa713ea6275b42205f040c6614701b7, $Version=0; JSESSIONID=1fed47c83eb3d3d5178c510bbde887499591f014.OX1";
        assertTrue(RFC2616Regex.COOKIES.matcher(twoWithVersion).matches());

        final String valueWithPath = "JSESSIONID=1fed47c83eb3d3d5178c510bbde887499591f014.OX1; $Path=/";
        assertTrue(RFC2616Regex.COOKIES.matcher(valueWithPath).matches());

        final String headerValue = "$Version=0; sessionid=aaa713ea6275b42205f040c6614701b7, $Version=0; JSESSIONID=1fed47c83eb3d3d5178c510bbde887499591f014.OX1; $Path=/";
        assertTrue(RFC2616Regex.COOKIES.matcher(headerValue).matches());
    }
}
