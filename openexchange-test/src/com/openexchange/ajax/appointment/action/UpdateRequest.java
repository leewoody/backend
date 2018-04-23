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

package com.openexchange.ajax.appointment.action;

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.container.Appointment;

/**
 * Implements creating the necessary values for a appointment update request. All necessary values are read from the appointment object. The
 * appointment must contain the folder and object identifier and the last modification timestamp.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class UpdateRequest extends AbstractAppointmentRequest<UpdateResponse> {

    private final Appointment appointmentObj;

    private final TimeZone timeZone;

    private final boolean failOnError;

    private final int originFolder;

    /**
     * Default constructor.
     *
     * @param appointment Appointment object with updated attributes. This appointment must contain the attributes parent folder
     *            identifier, object identifier and last modification timestamp.
     */
    public UpdateRequest(final Appointment appointment, final TimeZone timeZone) {
        this(appointment, timeZone, true);
    }

    public UpdateRequest(final Appointment appointment, final TimeZone timezone, final boolean failOnError) {
        this(appointment.getParentFolderID(), appointment, timezone, failOnError);

    }

    /**
     * Special constructor for moving appointment.
     * 
     * @param originFolder folder where the appointment is located currently.
     * @param appointment destination appointment object.
     * @param timezone time zone for correctly shifting times.
     * @param failOnError <code>false</code> if exception from backend should be ignored.
     */
    public UpdateRequest(final int originFolder, final Appointment appointment, final TimeZone timezone, final boolean failOnError) {
        super();
        this.appointmentObj = appointment;
        this.timeZone = timezone;
        this.failOnError = failOnError;
        this.originFolder = originFolder;
    }

    @Override
    public JSONObject getBody() throws JSONException {
        return convert(appointmentObj, timeZone);
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE), new Parameter(AJAXServlet.PARAMETER_INFOLDER, String.valueOf(this.originFolder)), new Parameter(AJAXServlet.PARAMETER_ID, String.valueOf(appointmentObj.getObjectID())), new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(appointmentObj.getLastModified().getTime())) };
    }

    @Override
    public UpdateParser getParser() {
        return new UpdateParser(failOnError);
    }

    protected Appointment getAppointment() {
        return appointmentObj;
    }
}
