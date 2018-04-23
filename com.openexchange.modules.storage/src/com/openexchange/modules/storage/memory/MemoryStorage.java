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

package com.openexchange.modules.storage.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.modules.model.Attribute;
import com.openexchange.modules.model.AttributeHandler;
import com.openexchange.modules.model.Metadata;
import com.openexchange.modules.model.Model;
import com.openexchange.modules.model.Tools;


/**
 * {@link MemoryStorage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MemoryStorage<T extends Model<T>> {

    private Map<Object, T> db;


    public MemoryStorage(Map<String, Map<String, Object>> database, Metadata<T> metadata) {
        this(database, metadata, AttributeHandler.DO_NOTHING);
    }

    public MemoryStorage(Map<String, Map<String, Object>> database, Metadata<T> metadata, AttributeHandler<T> overrides) {
        super();
        db  = new HashMap<Object, T>();
        if(database == null) {
            return;
        }
        for (Map.Entry<String, Map<String, Object>> pair : database.entrySet()) {
            T thing = metadata.create();
            Map<String, Object> simpleRep = pair.getValue();
            for(Attribute<T> attribute : metadata.getPersistentFields()) {
                Object value = simpleRep.get(attribute.getName());
                Object overridden = overrides.handle(attribute, value, thing, simpleRep);
                thing.set(attribute, (overridden != null) ? overridden : value);
            }
            thing.set(metadata.getIdField(), pair.getKey());
            db.put(thing.get(metadata.getIdField()), thing);
        }

    }

    public List<T> list() {
        ArrayList<T> list = new ArrayList<T>(db.values());
        return Tools.copy(list);
    }

    public T get(Object id) {
        return Tools.copy(db.get(id));
    }

}
