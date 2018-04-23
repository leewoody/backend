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

package com.openexchange.realtime.json.fields;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.UserizedFile;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ResourceIDField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ResourceIDField implements AdditionalFileField {

    /**
     * Initializes a new {@link ResourceIDField}.
     */
    public ResourceIDField() {
        super();
    }

    @Override
    public Field[] getRequiredFields() {
        return new Field[] { Field.FOLDER_ID, Field.ID };
    }

    @Override
    public int getColumnID() {
        return 7020;
    }

    @Override
    public String getColumnName() {
        return "com.openexchange.realtime.resourceID";
    }

    @Override
    public Object getValue(File file, ServerSession session) {
        return getRealtimeID(file);
    }

    @Override
    public List<Object> getValues(List<File> files, ServerSession session) {
        if (null == files) {
            return null;
        }
        List<Object> values = new ArrayList<Object>(files.size());
        for (File file : files) {
            values.add(getRealtimeID(file));
        }
        return values;
    }

    @Override
    public Object renderJSON(AJAXRequestData requestData, Object value) {
        return value;
    }

    /**
     * Gets the realtime file identifier for a specific file. By default, this is the file's (fully qualified) identifier, while for files
     * from the <code>infostore</code> account, only the (relative) file identifier is used in order to support scenarios where the same
     * file appears in different folders for different users.
     *
     * @param file The file to get the realtime identifier for
     * @return The realtime identifier, or <code>null</code> if no identifier can be extracted from the passed file reference
     */
    private static String getRealtimeID(File file) {
        if (null != file && UserizedFile.class.isInstance(file)) {
            String originalID = ((UserizedFile) file).getOriginalId();
            return Strings.isEmpty(originalID) ? file.getId() : originalID;
        }
        return null;
    }

}
