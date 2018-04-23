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

package com.openexchange.ajax.spellcheck;

import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.spellcheck.actions.add.UserWordRequest;
import com.openexchange.ajax.spellcheck.actions.add.UserWordResponse;
import com.openexchange.ajax.spellcheck.actions.check.CheckRequest;
import com.openexchange.ajax.spellcheck.actions.check.CheckResponse;

/**
 * {@link UserWordTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class UserWordTest extends AbstractSpellCheckTest {

    /**
     * Initializes a new {@link UserWordTest}
     *
     * @param name
     */
    public UserWordTest() {
        super();
    }

    /**
     * Tests the <code>action=add</code> request
     *
     * @throws Throwable
     */
    @Test
    public void testCheck() throws Throwable {

        final String userWord = "clazz";
        UserWordResponse userWordResponse = (UserWordResponse) Executor.execute(getSession(), new UserWordRequest(userWord, true, true));
        assertTrue("non NULL JSON object: ", JSONObject.NULL.equals(userWordResponse.getNullObject()));

        /*
         * en
         */
        final String htmlContent = "<html><head><title>quetsche</title></head><body>This is my clazz</body></html>";
        CheckResponse checkResponse = (CheckResponse) Executor.execute(getSession(), new CheckRequest(htmlContent, "en", true));

        String[] mw = checkResponse.getMisspeltWords();
        assertTrue("Misspelt words: " + Arrays.toString(mw), mw.length == 0);

        userWordResponse = (UserWordResponse) Executor.execute(getSession(), new UserWordRequest(userWord, false, true));
        assertTrue("non NULL JSON object: ", JSONObject.NULL.equals(userWordResponse.getNullObject()));

        checkResponse = (CheckResponse) Executor.execute(getSession(), new CheckRequest(htmlContent, "en", true));
        mw = checkResponse.getMisspeltWords();
        assertTrue("No misspelt words: " + Arrays.toString(mw), mw.length == 1);

    }

}
