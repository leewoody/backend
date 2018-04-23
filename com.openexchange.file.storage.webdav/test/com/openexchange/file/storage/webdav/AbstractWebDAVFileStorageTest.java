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

package com.openexchange.file.storage.webdav;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.file.storage.SimFileStorageAccount;
import com.openexchange.session.SimSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * {@link AbstractWebDAVFileStorageTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractWebDAVFileStorageTest {
    /**
     * Initializes a new {@link AbstractWebDAVFileStorageTest}.
     */
    protected AbstractWebDAVFileStorageTest() {
        super();
    }

    protected WebDAVFileStorageAccountAccess getAccountAccess() {
        final SimFileStorageAccount account;
        {
            account = new SimFileStorageAccount();
            /*
             * TODO: Configurable
             */
            final Map<String, Object> conf = new HashMap<String, Object>(4);
            conf.put(WebDAVConstants.WEBDAV_URL, "http://localhost:80/files/");
            conf.put(WebDAVConstants.WEBDAV_TIMEOUT, "60000");
            conf.put(WebDAVConstants.WEBDAV_LOGIN, "thorben");
            conf.put(WebDAVConstants.WEBDAV_PASSWORD, "secret");
            account.setConfiguration(conf);
            account.setId("0");
        }

        final SimSession session = new SimSession();
        session.setUserId(17);
        session.setContextId(1337);
        session.setLoginName("thorben@1337");
        session.setPassword("netline");

        return new WebDAVFileStorageAccountAccess(null, account, session);
    }

}
