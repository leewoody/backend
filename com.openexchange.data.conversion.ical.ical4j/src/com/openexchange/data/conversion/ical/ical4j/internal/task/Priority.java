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

package com.openexchange.data.conversion.ical.ical4j.internal.task;

import static net.fortuna.ical4j.model.property.Priority.HIGH;
import static net.fortuna.ical4j.model.property.Priority.LOW;
import static net.fortuna.ical4j.model.property.Priority.MEDIUM;
import static net.fortuna.ical4j.model.property.Priority.UNDEFINED;
import java.util.List;
import java.util.TimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ConversionWarning.Code;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Priority extends AbstractVerifyingAttributeConverter<VToDo, Task> {

    public Priority() {
        super();
    }

    @Override
    public boolean isSet(final Task task) {
        return task.containsPriority();
    }

    @Override
    public void emit(final Mode mode, final int index, final Task task, final VToDo vToDo, final List<ConversionWarning> warnings, final Context ctx, final Object... args) {
        final net.fortuna.ical4j.model.property.Priority prio;
        switch (task.getPriority()) {
        case Task.HIGH:
            prio = HIGH;
            break;
        case Task.NORMAL:
            prio = MEDIUM;
            break;
        case Task.LOW:
            prio = LOW;
            break;
        default:
            warnings.add(new ConversionWarning(index, Code.INVALID_PRIORITY, Integer.valueOf(task.getPriority())));
            prio = UNDEFINED;
        }
        vToDo.getProperties().add(prio);
    }

    @Override
    public boolean hasProperty(final VToDo vToDo) {
        return vToDo.getPriority() != null;
    }

    @Override
    public void parse(final int index, final VToDo todo, final Task task, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) {
        final float lowMed = (LOW.getLevel() + MEDIUM.getLevel()) >> 1;
        final float medHigh = (MEDIUM.getLevel() + HIGH.getLevel()) >> 1;
        final int priority = todo.getPriority().getLevel();
        if (priority >= lowMed) {
            task.setPriority(Task.LOW);
        } else if (priority >= medHigh) {
            task.setPriority(Task.NORMAL);
        } else if (priority >= HIGH.getLevel()) {
            task.setPriority(Task.HIGH);
        } else if (priority == UNDEFINED.getLevel()) {
            task.setPriority(Task.NORMAL); // Default to normal Bug #10401
        } else {
            warnings.add(new ConversionWarning(index, Code.INVALID_PRIORITY, Integer.valueOf(priority)));
        }
    }
}
