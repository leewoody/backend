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

package com.openexchange.webdav.xml.appointment.actions;

import static com.openexchange.webdav.xml.framework.Constants.NS_DAV;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.webdav.xml.AppointmentWriter;
import com.openexchange.webdav.xml.framework.RequestTools;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class InsertRequest extends AbstractAppointmentRequest<InsertResponse> {

    private final Appointment appointment;

    public InsertRequest(final Appointment appointment) {
        super();
        this.appointment = appointment;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    protected Element createProp() throws OXException, IOException {
        appointment.removeObjectID();
        final Element eProp = new Element("prop", NS_DAV);

        final AppointmentWriter appointmentWriter = new AppointmentWriter();
        appointmentWriter.addContent2PropElement(eProp, appointment, false, true);
        return eProp;
    }

    @Override
    public RequestEntity getEntity() throws OXException, IOException {
        final Document doc = RequestTools.createPropertyUpdate(createProp());
        final XMLOutputter xo = new XMLOutputter();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        xo.output(doc, baos);

        return new ByteArrayRequestEntity(baos.toByteArray());
    }

    @Override
    public InsertParser getParser() {
        return new InsertParser();
    }
}