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

package com.openexchange.ajax.mail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.java.Charsets;

/**
 * {@link Bug19696Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Bug19696Test extends AbstractMailTest {

    private String folder;
    private String address;
    private String[] ids;

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public Bug19696Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        folder = getClient().getValues().getInboxFolder();
        address = getClient().getValues().getSendAddress();
        final String mail = TestMails.BUG_19696_MAIL;
        final ImportMailRequest request = new ImportMailRequest(folder, 0, Charsets.UTF_8, mail);
        final ImportMailResponse response = getClient().execute(request);
        ids = response.getIds()[0];
    }

    @After
    public void tearDown() throws Exception {
        try {
            getClient().execute(new DeleteRequest(ids, true));
        } finally {
            super.tearDown();
        }
    }

    /**
     * Tests the <code>action=get</code> request on INBOX folder
     *
     * @throws Throwable
     */
    @Test
    public void testGet() throws Throwable {
        {
            final GetRequest request = new GetRequest(folder, ids[1]);
            request.setUnseen(true);
            request.setSource(true);
            request.setSave(true);
            getClient().execute(request);
        }
    }

}
