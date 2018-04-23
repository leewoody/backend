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

package com.openexchange.mail.config;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.DefaultInterests.Builder;
import com.openexchange.exception.OXException;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MailReloadable} - Collects reloadables for mail module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class MailReloadable implements Reloadable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MailReloadable.class);

    private static final MailReloadable INSTANCE = new MailReloadable();

    private static final String[] PROPERTIES = new String[] {"com.openexchange.mail.loginSource","com.openexchange.mail.passwordSource",
        "com.openexchange.mail.mailServerSource", "com.openexchange.mail.masterPassword", "com.openexchange.mail.mailServer",
        "com.openexchange.mail.transportServer", "com.openexchange.mail.adminMailLoginEnabled"};

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static MailReloadable getInstance() {
        return INSTANCE;
    }

    // --------------------------------------------------------------------------------------------------- //

    private final List<Reloadable> reloadables;

    /**
     * Initializes a new {@link MailReloadable}.
     */
    private MailReloadable() {
        super();
        reloadables = new CopyOnWriteArrayList<Reloadable>();
    }

    /**
     * Adds given {@link Reloadable} instance.
     *
     * @param reloadable The instance to add
     */
    public void addReloadable(final Reloadable reloadable) {
        reloadables.add(reloadable);
    }

    @Override
    public void reloadConfiguration(final ConfigurationService configService) {
        try {
            final MailProperties mailProperties = MailProperties.getInstance();
            if (null != mailProperties) {
                mailProperties.resetProperties();
                mailProperties.loadProperties();
            }

            // Clear capabilities cache as "com.openexchange.mail.adminMailLoginEnabled" affects them
            final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                try {
                    final Cache cache = cacheService.getCache("Capabilities");
                    cache.clear();
                } catch (final Exception x) {
                    // Ignore
                }
            }
        } catch (final OXException e) {
            LOGGER.warn("Failed to reload mail properties", e);
        }

        for (final Reloadable reloadable : reloadables) {
            reloadable.reloadConfiguration(configService);
        }
    }

    @Override
    public Interests getInterests() {
        Set<String> properties = new TreeSet<>();
        properties.addAll(Arrays.asList(PROPERTIES));
        Set<String> fileNames = new TreeSet<>();
        fileNames.add("mail.properties");

        for (Reloadable reloadable : reloadables) {
            Interests interests = reloadable.getInterests();
            if (null != interests) {
                String[] propertiesOfInterest = interests.getPropertiesOfInterest();
                if (null != propertiesOfInterest) {
                    properties.addAll(Arrays.asList(propertiesOfInterest));
                }
                String[] configFileNames = interests.getConfigFileNames();
                if (null != configFileNames) {
                    fileNames.addAll(Arrays.asList(configFileNames));
                }
            }
        }

        Builder builder = DefaultInterests.builder();
        if (!properties.isEmpty()) {
            builder.propertiesOfInterest(properties.toArray(new String[properties.size()]));
        }
        if (!fileNames.isEmpty()) {
            builder.configFileNames(fileNames.toArray(new String[fileNames.size()]));
        }
        return builder.build();
    }

}