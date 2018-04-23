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

import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Mapping;
import com.openexchange.groupware.tasks.Task;

/**
 * Tests actual and target duration and costs set to 0.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug10071Test extends AbstractTaskTest {

    private static final int[] ATTRIBUTE_IDS = { Task.TARGET_DURATION, Task.ACTUAL_DURATION, Task.TARGET_COSTS, Task.ACTUAL_COSTS };

    private AJAXClient client;

    private int folderId;

    private TimeZone tz;

    private Task task;

    public Bug10071Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folderId = client.getValues().getPrivateTaskFolder();
        tz = client.getValues().getTimeZone();
        task = Create.createWithDefaults(folderId, "Bug 10071 test task");
        for (int attributeId : ATTRIBUTE_IDS) {
            setToZero(Mapping.getMapping(attributeId));
        }
        final InsertResponse insertR = client.execute(new InsertRequest(task, tz));
        insertR.fillTask(task);
    }

    @SuppressWarnings("unchecked")
    private void setToZero(Mapper mapper) {
        for (Object value : new Object[] { L(0), new BigDecimal(0) }) {
            try {
                mapper.set(task, value);
            } catch (ClassCastException e) {
                // Try next.
            }
        }
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
    public void testDurationAndCostsSetToZero() throws OXException, IOException, JSONException, OXException {
        GetRequest request = new GetRequest(task);
        GetResponse response = client.execute(request);
        Task toTest = response.getTask(tz);
        ListIDs ids = ListIDs.l(new int[] { task.getParentFolderID(), task.getObjectID() });
        ListRequest request2 = new ListRequest(ids, ATTRIBUTE_IDS);
        CommonListResponse response2 = client.execute(request2);
        for (int attributeId : ATTRIBUTE_IDS) {
            Mapper<?> mapper = Mapping.getMapping(attributeId);
            assertTrue("Attribute " + mapper.getDBColumnName() + " is missing in GET.", mapper.isSet(toTest));
            assertEquals("Attribute " + mapper.getDBColumnName() + " has wrong value in GET.", mapper.get(task), mapper.get(toTest));
            Object value = response2.getValue(0, attributeId);
            if (Long.class.equals(mapper.get(toTest).getClass())) {
                value = Long.valueOf((String) value);
            } else if (BigDecimal.class.equals(mapper.get(toTest).getClass())) {
                value = new BigDecimal(value.toString());
            }
            assertNotNull("Attribute " + mapper.getDBColumnName() + " is missing in LIST.", value);
            assertEquals("Attribute " + mapper.getDBColumnName() + " has wrong value in LIST.", mapper.get(task), value);
        }
    }
}
