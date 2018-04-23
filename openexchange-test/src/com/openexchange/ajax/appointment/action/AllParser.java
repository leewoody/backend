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

import java.util.Iterator;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonAllParser;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.ListIDInt;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Appointment;

/**
 * TODO: This is buggy when given FolderObject.ALL_COLUMNS
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AllParser extends CommonAllParser {

    /**
     * Default constructor.
     */
    public AllParser(final boolean failOnError, final int[] columns) {
        super(failOnError, columns);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CommonAllResponse instantiateResponse(final Response response) {
        return new CommonAllResponse(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CommonAllResponse createResponse(final Response response) throws JSONException {
        final CommonAllResponse retval = super.createResponse(response);
        final Iterator<Object[]> iter = retval.iterator();
        final ListIDs list = new ListIDs();
        final int folderPos = retval.getColumnPos(Appointment.FOLDER_ID);
        final int identifierPos = retval.getColumnPos(Appointment.OBJECT_ID);
        while (iter.hasNext()) {
            final Object[] row = iter.next();
            list.add(new ListIDInt(toInt(row[folderPos]), toInt(row[identifierPos])));
        }
        retval.setListIDs(list);
        return retval;
    }

    private int toInt(Object thingie) {
        if (Long.class.isInstance(thingie)) {
            return ((Long) thingie).intValue();
        } else if (Integer.class.isInstance(thingie)) {
            return ((Integer) thingie).intValue();
        } else {
            return Integer.parseInt(thingie.toString());
        }
    }
}
