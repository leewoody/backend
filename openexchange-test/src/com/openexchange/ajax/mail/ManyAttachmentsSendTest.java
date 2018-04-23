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

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;

/**
 * {@link ManyAttachmentsSendTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - tests with manager
 */
public final class ManyAttachmentsSendTest extends AbstractMailTest {

    private MailTestManager manager;

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public ManyAttachmentsSendTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        manager = new MailTestManager(getClient(), false);
    }

    @After
    public void tearDown() throws Exception {
        try {
            manager.cleanUp();
        } finally {
            super.tearDown();
        }
    }

    /**
     * Tests the <code>action=new</code> request on INBOX folder
     *
     * @throws Throwable
     */
    @Test
    public void testSend() throws Throwable {
        /*
         * Create JSON mail object
         */
        final String mailObject_25kb = createSelfAddressed25KBMailObject().toString();
        final SendRequest sendRequest = new SendRequest(mailObject_25kb);

        class FooInputStream extends InputStream {

            private final long size;

            private long read = 0L;

            public FooInputStream(long size) {
                super();
                this.size = size;
            }

            @Override
            public int read() throws IOException {
                return read++ < size ? (read % 76 == 0 ? '\n' : 'a') : -1;
            }

        }

        for (int i = 0; i < 100; i++) {
            sendRequest.addUpload(new FooInputStream(8192));
        }

        final SendResponse response = Executor.execute(getSession(), sendRequest);
        assertTrue("Send request failed", response.getFolderAndID() != null && response.getFolderAndID().length > 0);
    }

}
