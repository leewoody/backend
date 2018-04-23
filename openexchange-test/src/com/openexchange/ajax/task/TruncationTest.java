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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.exception.OXException.Truncated;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.tools.RandomString;

/**
 * Tests if too long values for task attributes are correctly catched in the
 * server and sent to the AJAX client.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TruncationTest extends AbstractTaskTest {

    /**
     * Default constructor.
     * 
     * @param name Name of the test.
     */
    public TruncationTest() {
        super();
    }

    /**
     * Creates a task with a to long title and checks if the data truncation
     * is detected.
     * 
     * @throws Throwable if an error occurs.
     */
    @Test
    public void testTruncation() throws Throwable {
        final Task task = new Task();
        // Title length in database is 256.
        task.setTitle(RandomString.generateChars(257));
        // Trip meter length in database is 255.
        task.setTripMeter(RandomString.generateChars(256));
        task.setParentFolderID(getPrivateFolder());
        final InsertResponse response = getClient().execute(new InsertRequest(task, getTimeZone(), false));
        assertTrue("Server did not detect truncated data.", response.hasError());
        assertTrue("Array of truncated attribute identifier is empty.", response.getProblematics().length > 0);
        final StringBuilder sb = new StringBuilder();
        sb.append("Truncated attribute identifier: [");
        int truncatedAttributeId = -1;
        for (final ProblematicAttribute problematic : response.getProblematics()) {
            if (problematic instanceof Truncated) {
                truncatedAttributeId = ((Truncated) problematic).getId();
                sb.append(truncatedAttributeId);
                sb.append(',');
            }
        }
        sb.setCharAt(sb.length() - 1, ']');
        assertEquals("Wrong attribute discovered as truncated.", Task.TITLE, truncatedAttributeId);
    }
}
