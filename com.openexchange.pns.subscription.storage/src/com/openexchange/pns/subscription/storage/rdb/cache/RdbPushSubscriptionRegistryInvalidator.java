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

package com.openexchange.pns.subscription.storage.rdb.cache;

import static com.openexchange.java.Autoboxing.I;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.java.util.Pair;

/**
 * {@link RdbPushSubscriptionRegistryInvalidator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class RdbPushSubscriptionRegistryInvalidator implements CacheListener, ServiceTrackerCustomizer<CacheEventService, CacheEventService> {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RdbPushSubscriptionRegistryInvalidator.class);

    private static final String REGION = RdbPushSubscriptionRegistryCache.REGION;

    // ---------------------------------------------------------------------------------------------------------------

    private final BundleContext context;
    private final RdbPushSubscriptionRegistryCache cache;

    /**
     * Initializes a new {@link RdbPushSubscriptionRegistryInvalidator}.
     *
     * @param context The bundle context
     */
    public RdbPushSubscriptionRegistryInvalidator(RdbPushSubscriptionRegistryCache cache, BundleContext context) {
        super();
        this.cache = cache;
        this.context = context;
    }

    @Override
    public void onEvent(Object sender, CacheEvent cacheEvent, boolean fromRemote) {
        if (fromRemote) {
            // Remotely received
            LOGGER.debug("Handling incoming remote cache event: {}", cacheEvent);

            if (REGION.equals(cacheEvent.getRegion())) {
                List<Serializable> keys = cacheEvent.getKeys();
                Set<Pair<Integer, Integer>> pairs = new LinkedHashSet<Pair<Integer,Integer>>(keys.size());
                for (Serializable key : keys) {
                    if (key instanceof CacheKey) {
                        CacheKey ckey = (CacheKey) key;
                        int contextId = getUnsignedInteger(cacheEvent.getGroupName());
                        if (contextId > 0) {
                            int userId = Integer.parseInt(ckey.getKeys()[0].toString());
                            if (pairs.add(new Pair<Integer, Integer>(I(contextId), I(userId)))) {
                                cache.dropFor(userId, contextId, false);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public CacheEventService addingService(ServiceReference<CacheEventService> reference) {
        CacheEventService service = context.getService(reference);
        service.addListener(REGION, this);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<CacheEventService> reference, CacheEventService service) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<CacheEventService> reference, CacheEventService service) {
        service.removeListener(REGION, this);
        context.ungetService(reference);
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     * The radix for base <code>10</code>.
     */
    private static final int RADIX = 10;

    /**
     * Parses a positive <code>int</code> value from passed {@link String} instance.
     *
     * @param s The string to parse
     * @return The parsed positive <code>int</code> value or <code>-1</code> if parsing failed
     */
    private static final int getUnsignedInteger(final String s) {
        if (s == null) {
            return -1;
        }

        final int max = s.length();

        if (max <= 0) {
            return -1;
        }
        if (s.charAt(0) == '-') {
            return -1;
        }

        int result = 0;
        int i = 0;

        final int limit = -Integer.MAX_VALUE;
        final int multmin = limit / RADIX;
        int digit;

        if (i < max) {
            digit = digit(s.charAt(i++));
            if (digit < 0) {
                return -1;
            }
            result = -digit;
        }
        while (i < max) {
            /*
             * Accumulating negatively avoids surprises near MAX_VALUE
             */
            digit = digit(s.charAt(i++));
            if (digit < 0) {
                return -1;
            }
            if (result < multmin) {
                return -1;
            }
            result *= RADIX;
            if (result < limit + digit) {
                return -1;
            }
            result -= digit;
        }
        return -result;
    }

    private static int digit(final char c) {
        switch (c) {
        case '0':
            return 0;
        case '1':
            return 1;
        case '2':
            return 2;
        case '3':
            return 3;
        case '4':
            return 4;
        case '5':
            return 5;
        case '6':
            return 6;
        case '7':
            return 7;
        case '8':
            return 8;
        case '9':
            return 9;
        default:
            return -1;
        }
    }

}
