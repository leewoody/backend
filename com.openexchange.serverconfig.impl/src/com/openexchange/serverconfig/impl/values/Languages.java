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

package com.openexchange.serverconfig.impl.values;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.ConfigurationService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ComputedServerConfigValueService;
import com.openexchange.session.Session;

/**
 * {@link Languages}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class Languages implements ComputedServerConfigValueService {

    private final List<SimpleEntry<String, String>> languages;

    /**
     * Initializes a new {@link Languages}.
     *
     * @param services The service look-up
     */
    public Languages(ServiceLookup services) {
        super();

        ConfigurationService config = services.getService(ConfigurationService.class);
        Properties properties = config.getPropertiesInFolder("languages" + File.separatorChar + "appsuite");

        List<SimpleEntry<String, String>> languages = new ArrayList<SimpleEntry<String,String>>(properties.size());
        for (Object key : properties.keySet()) {
            String propName = (String) key;
            String languageName = properties.getProperty(propName);

            int index = propName.lastIndexOf('/');
            if (index > 0) {
                propName = propName.substring(index + 1);
            }
            languages.add(new SimpleEntry<String, String>(propName, languageName));
        }

        if (languages.isEmpty()) {
            // Assume american english
            languages.add(new SimpleEntry<String, String>("en_US", "English"));
        }

        // Sort it alphabetically
        Collections.sort(languages, new Comparator<SimpleEntry<String, String>>() {

            @Override
            public int compare(SimpleEntry<String, String> arg0, SimpleEntry<String, String> arg1) {
                String language1 = arg0.getValue();
                if (null == language1) {
                    return null == arg1.getValue() ? 0 : 1;
                }

                String language2 = arg1.getValue();
                if (null == language2) {
                    return -1;
                }
                return language1.compareToIgnoreCase(language2);
            }
        });

        this.languages = ImmutableList.copyOf(languages);
    }

    @Override
    public void addValue(Map<String, Object> serverConfig, String hostName, int userId, int contextId, Session optSession) {

        Object existingLanguages = serverConfig.get("languages");
        if (existingLanguages == null || existingLanguages.equals("all")) {
            serverConfig.put("languages", languages);
        }
    }

}
