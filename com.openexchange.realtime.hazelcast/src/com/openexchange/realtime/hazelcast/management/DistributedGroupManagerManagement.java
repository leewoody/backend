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

package com.openexchange.realtime.hazelcast.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementObject;
import com.openexchange.realtime.group.SelectorChoice;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.packet.ID;


/**
 * {@link DistributedGroupManagerManagement}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class DistributedGroupManagerManagement extends ManagementObject<DistributedGroupManagerMBean> implements DistributedGroupManagerMBean {

    private ObjectName objectName;
    private String clientMapName;
    private String groupMapName;

    public DistributedGroupManagerManagement(String clientMapName, String groupMapName) {
        super(DistributedGroupManagerMBean.class);
        this.clientMapName = clientMapName;
        this.groupMapName = groupMapName; 
    }

    @Override
    public Map<String, List<String>> getClientMapping() throws OXException {
        MultiMap<ID, SelectorChoice> clientToGroupsMapping = getClientToGroupsMapping();
        return createMapping(clientToGroupsMapping);
    }
    
    @Override
    public Map<String, List<String>> getGroupMapping() throws OXException {
        MultiMap<ID, SelectorChoice> roupToMembersMapping = getGroupToMembersMapping();
        return createMapping(roupToMembersMapping);
    }
    
    private Map<String, List<String>> createMapping(MultiMap<ID, SelectorChoice> multimap) throws OXException {
        Map<String, List<String>> jmxMap = new HashMap<String, List<String>>();
        MultiMap<ID, SelectorChoice> incoming = multimap;
        Set<Entry<ID,SelectorChoice>> entrySet = incoming.entrySet();
        for (Entry<ID, SelectorChoice> entry : entrySet) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            List<String> collected = jmxMap.get(key);
            if(collected == null) {
                collected = new ArrayList<String>();
                jmxMap.put(key, collected);
            }
            collected.add(value);
        }
        return jmxMap;
    }

    public ObjectName getObjectName() {
        if (objectName == null) {
            String directoryName = "DistributedGroupManger";
            try {
                objectName = new ObjectName("com.openexchange.realtime", "name", directoryName);
            } catch (MalformedObjectNameException e) {
                // can't happen: valid domain and no missing parameters
            } catch (NullPointerException e) {
                // can't happen: valid domain and no missing parameters
            }
        }
        return objectName;
    }

    /**
     * Get mapping of one client to many groups
     * 
     * @return A {@link MultiMap} of one client to many groups
     */
    private MultiMap<ID, SelectorChoice> getClientToGroupsMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMultiMap(clientMapName);
    }
    
    /**
     * Get mapping of one group to many members
     * 
     * @return A {@link MultiMap} of one client to many groups
     */
    private MultiMap<ID, SelectorChoice> getGroupToMembersMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMultiMap(groupMapName);
    }
}
