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

import static org.junit.Assert.assertFalse;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 *
 */
public class Bug18204Test extends AbstractAJAXSession {

    AJAXClient client;
    TimeZone tz;
    Calendar start;
    Calendar due;
    Task task;

    public Bug18204Test() throws Exception {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = client.getValues().getTimeZone();
        start = TimeTools.createCalendar(tz);
        due = (Calendar) start.clone();
        due.add(Calendar.HOUR_OF_DAY, 2);
        task = createTask();
    }

    @Test
    public void testBug18204() throws Exception {
        // Insert new recurring task with end of series set to _after_
        InsertResponse insertResponse = client.execute(new InsertRequest(task, tz, true));
        insertResponse.fillTask(task);

        // Modify task to end of series set to _on_
        task.removeOccurrence();
        due.add(Calendar.DAY_OF_MONTH, 4);
        task.setUntil(due.getTime());
        UpdateResponse updateResponse = client.execute(new UpdateRequest(task, tz, true));
        task.setLastModified(updateResponse.getTimestamp());

        // Get Task to compare
        GetResponse getResponse = client.execute(new GetRequest(task));
        Task toCompare = getResponse.getTask(tz);

        assertFalse("Task contains Occurrences although it should not.", toCompare.containsOccurrence());
    }

    @After
    public void tearDown() throws Exception {
        try {
            client.execute(new DeleteRequest(task, true));
        } finally {
            super.tearDown();
        }

    }

    private Task createTask() throws Exception {
        Task task = new Task();
        task.setTitle("Bug18204 Task");
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        task.setRecurrenceType(Task.DAILY);
        task.setInterval(1);
        task.setOccurrence(2);
        task.setStartDate(start.getTime());
        task.setEndDate(due.getTime());

        return task;
    }

}
