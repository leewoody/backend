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

package com.openexchange.sessiond;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.test.TestInit;

public class SessiondTest {

    protected static final String sessiondPropertiesFile = "sessiondPropertiesFile";

    protected static String testUser1 = null;

    protected static String testUser2 = null;

    protected static String testUser3 = null;

    protected static String defaultContext = null;

    protected static String notExistingUser = "notexistinguser";

    protected static String notActiveUser = "notactiveuser";

    protected static String passwordExpiredUser = "passwordexpireduser";

    protected static String userWithoutContext = "user@withoutcontext.de";

    protected static String invalidPassword = "qwertz";

    private static boolean isInit = false;

    @Before
    public void setUp() throws Exception {
        if (isInit) {
            return;
        }
        TestConfig config = new TestConfig();
        defaultContext = config.getContextName();
        testUser1 = config.getUser();
        testUser2 = config.getSecondUser();
        testUser3 = config.getThirdUser();

        Init.startServer();

        final Properties prop = TestInit.getTestProperties();

        final String propfile = prop.getProperty(sessiondPropertiesFile);

        if (propfile == null) {
            throw new Exception("no sessiond propfile given!");
        }

        final Properties p = new Properties();

        p.load(new FileInputStream(propfile));

        notExistingUser = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.notExistingUser", notExistingUser);
        notActiveUser = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.notActiveUser", notActiveUser);
        passwordExpiredUser = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.passwordExpiredUser", passwordExpiredUser);
        userWithoutContext = AbstractConfigWrapper.parseProperty(p, "com.openexchange.session.userWithoutContext", userWithoutContext);

        isInit = true;
    }

    /**
     * {@inheritDoc}
     */
    @After
    public void tearDown() throws Exception {
        if (isInit) {
            isInit = false;
            Init.stopServer();
        }
    }

    @Test
    public void testAddSession() throws Exception {
        final int contextId = ContextStorage.getInstance().getContextId(defaultContext);
        final Context context = ContextStorage.getInstance().getContext(contextId);
        final int userId = UserStorage.getInstance().getUserId(testUser1, context);
        final SessiondService sessiondCon = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        sessiondCon.addSession(new AddSessionParameter() {

            @Override
            public String getClientIP() {
                return "localhost";
            }

            @Override
            public Context getContext() {
                return context;
            }

            @Override
            public String getFullLogin() {
                return testUser1 + '@' + context.getContextId();
            }

            @Override
            public String getPassword() {
                return "secret";
            }

            @Override
            public int getUserId() {
                return userId;
            }

            @Override
            public String getUserLoginInfo() {
                return testUser1;
            }

            @Override
            public String getAuthId() {
                return UUIDs.getUnformattedString(UUID.randomUUID());
            }

            @Override
            public String getHash() {
                return "123";
            }

            @Override
            public String getClient() {
                return "test";
            }

            @Override
            public String getClientToken() {
                return "testToken";
            }

            @Override
            public boolean isTransient() {
                return false;
            }

            @Override
            public SessionEnhancement getEnhancement() {
                return null;
            }
        });
    }

    @Test
    public void testRefreshSession() throws Exception {
        final int contextId = ContextStorage.getInstance().getContextId(defaultContext);
        final Context context = ContextStorage.getInstance().getContext(contextId);
        final int userId = UserStorage.getInstance().getUserId(testUser1, context);
        final SessiondService sessiondCon = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        final Session session = sessiondCon.addSession(new AddSessionParameter() {

            @Override
            public String getClientIP() {
                return "localhost";
            }

            @Override
            public Context getContext() {
                return context;
            }

            @Override
            public String getFullLogin() {
                return testUser1 + '@' + context.getContextId();
            }

            @Override
            public String getPassword() {
                return "secret";
            }

            @Override
            public int getUserId() {
                return userId;
            }

            @Override
            public String getUserLoginInfo() {
                return testUser1;
            }

            @Override
            public String getAuthId() {
                return UUIDs.getUnformattedString(UUID.randomUUID());
            }

            @Override
            public String getHash() {
                return "123";
            }

            @Override
            public String getClient() {
                return "test";
            }

            @Override
            public String getClientToken() {
                return "testToken";
            }

            @Override
            public boolean isTransient() {
                return false;
            }

            @Override
            public SessionEnhancement getEnhancement() {
                return null;
            }
        });
        sessiondCon.getSession(session.getSessionID());
    }

    @Test
    public void testDeleteSession() throws Exception {
        final int contextId = ContextStorage.getInstance().getContextId(defaultContext);
        final Context context = ContextStorage.getInstance().getContext(contextId);
        final int userId = UserStorage.getInstance().getUserId(testUser1, context);
        final SessiondService sessiondCon = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        final Session session = sessiondCon.addSession(new AddSessionParameter() {

            @Override
            public String getClientIP() {
                return "localhost";
            }

            @Override
            public Context getContext() {
                return context;
            }

            @Override
            public String getFullLogin() {
                return testUser1 + '@' + context.getContextId();
            }

            @Override
            public String getPassword() {
                return "secret";
            }

            @Override
            public int getUserId() {
                return userId;
            }

            @Override
            public String getUserLoginInfo() {
                return testUser1;
            }

            @Override
            public String getAuthId() {
                return UUIDs.getUnformattedString(UUID.randomUUID());
            }

            @Override
            public String getHash() {
                return "123";
            }

            @Override
            public String getClient() {
                return "test";
            }

            @Override
            public String getClientToken() {
                return "testToken";
            }

            @Override
            public boolean isTransient() {
                return false;
            }

            @Override
            public SessionEnhancement getEnhancement() {
                return null;
            }
        });
        sessiondCon.removeSession(session.getSessionID());
    }

    @Test
    public void testGetSession() throws Exception {
        final int contextId = ContextStorage.getInstance().getContextId(defaultContext);
        final Context context = ContextStorage.getInstance().getContext(contextId);
        final int userId = UserStorage.getInstance().getUserId(testUser1, context);
        final SessiondService sessiondCon = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        final Session session = sessiondCon.addSession(new AddSessionParameter() {

            @Override
            public String getClientIP() {
                return "localhost";
            }

            @Override
            public Context getContext() {
                return context;
            }

            @Override
            public String getFullLogin() {
                return testUser1 + '@' + context.getContextId();
            }

            @Override
            public String getPassword() {
                return "secret";
            }

            @Override
            public int getUserId() {
                return userId;
            }

            @Override
            public String getUserLoginInfo() {
                return testUser1;
            }

            @Override
            public String getAuthId() {
                return UUIDs.getUnformattedString(UUID.randomUUID());
            }

            @Override
            public String getHash() {
                return "123";
            }

            @Override
            public String getClient() {
                return "test";
            }

            @Override
            public String getClientToken() {
                return "testToken";
            }

            @Override
            public boolean isTransient() {
                return false;
            }

            @Override
            public SessionEnhancement getEnhancement() {
                return null;
            }
        });
        sessiondCon.getSession(session.getSessionID());
    }
}
