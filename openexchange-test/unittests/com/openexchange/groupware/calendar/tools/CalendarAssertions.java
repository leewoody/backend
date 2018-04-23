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

package com.openexchange.groupware.calendar.tools;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.setuptools.TestContextToolkit;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CalendarAssertions {

    public static void assertUserParticipants(final CalendarDataObject cdao, final String... users) {
        assertParticipants(cdao, users, new String[0]);
    }

    public static void assertResourceParticipants(final CalendarDataObject cdao, final String... resources) {
        assertParticipants(cdao, new String[0], resources);
    }

    public static void assertParticipants(final CalendarDataObject cdao, final String[] users, final String[] resources) {
        assertNotNull("Participants should be set! ", cdao.getParticipants());

        final TestContextToolkit tools = new TestContextToolkit();

        final Set<UserParticipant> userParticipants = new HashSet<UserParticipant>(tools.users(cdao.getContext(), users));
        final Set<ResourceParticipant> resourceParticipants = new HashSet<ResourceParticipant>(tools.resources(cdao.getContext(), resources));
        final Set<Participant> unexpected = new HashSet<Participant>();
        for (final Participant participant : cdao.getParticipants()) {
            if (!(userParticipants.remove(participant)) && !(resourceParticipants.remove(participant))) {
                unexpected.add(participant);
            }
        }
        final StringBuilder problems = new StringBuilder();
        boolean mustFail = false;
        if (!unexpected.isEmpty()) {
            mustFail = true;
            problems.append("Didn't expect: ").append(stringify(unexpected)).append(". ");
        }
        if (!userParticipants.isEmpty()) {
            mustFail = true;
            problems.append("Missing user participants: ").append(stringify(userParticipants)).append(". ");
        }
        if (!resourceParticipants.isEmpty()) {
            mustFail = true;
            problems.append("Missing resource participants: ").append(stringify(resourceParticipants)).append(". ");
        }
        if (mustFail) {
            fail(problems.toString());
        }
    }

    public static void assertInPrivateFolder(final CommonAppointments appointments, final Appointment appointment) throws OXException {
        for (final Appointment currentAppointment : appointments.getPrivateAppointments()) {
            if (appointment.getObjectID() == currentAppointment.getObjectID()) {
                return;
            }
        }
        fail("Couldn't find " + appointment.getObjectID() + " in private appointments");
    }

    private static String stringify(final Set<? extends Participant> unexpected) {
        final StringBuilder bob = new StringBuilder();
        for (final Participant p : unexpected) {
            bob.append(p.getIdentifier()).append(": ").append(p.getDisplayName()).append(" | ");
        }
        return bob.toString();
    }

}
