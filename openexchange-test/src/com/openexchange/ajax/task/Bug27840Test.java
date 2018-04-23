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

package com.openexchange.ajax.task;

import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;
import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;

/**
 * Verifies that the issue described in bug 27840 does not appear again.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug27840Test extends AbstractTaskTest {

    private AJAXClient client;
    private Task task;
    private TimeZone tz;

    public Bug27840Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = getTimeZone();
        task = Create.createWithDefaults(getPrivateFolder(), "Task to test for bug 27840");
        // NUMERIC(12,2) in the database. The following should be the corresponding maximal and minimal possible values.
        task.setTargetCosts(new BigDecimal("9999999999.99"));
        task.setActualCosts(new BigDecimal("-9999999999.99"));
    }

    @After
    public void tearDown() throws Exception {
        try {
            client.execute(new DeleteRequest(task));
        } finally {
            super.tearDown();
        }

    }

    @Test
    public void testForBug() throws OXException, IOException, JSONException {
        client.execute(new InsertRequest(task, tz)).fillTask(task);
        GetResponse response = client.execute(new GetRequest(task));
        Task test = response.getTask(tz);
        assertThat("Actual costs not equal", test.getActualCosts(), CoreMatchers.equalTo(task.getActualCosts()));
        assertThat("Target costs not equal", test.getTargetCosts(), CoreMatchers.equalTo(task.getTargetCosts()));
        task = test;
    }
}