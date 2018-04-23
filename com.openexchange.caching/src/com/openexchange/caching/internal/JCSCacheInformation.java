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

package com.openexchange.caching.internal;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.jcs.admin.CountingOnlyOutputStream;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.memory.behavior.IMemoryCache;
import com.google.common.collect.ImmutableSet;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheInformationMBean;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link JCSCacheInformation} - The {@link CacheInformationMBean} implementation of <a href="http://jakarta.apache.org/jcs/">JCS</a>
 * caching system.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JCSCacheInformation extends StandardMBean implements CacheInformationMBean {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JCSCacheInformation.class);

    private final CompositeCacheManager cacheHub;
    private final JCSCacheService cacheService;

    /**
     * Initializes a new {@link JCSCacheInformation}.
     *
     * @param cacheService The JCS cache service instance
     * @throws NotCompliantMBeanException
     */
    public JCSCacheInformation(JCSCacheService cacheService) throws NotCompliantMBeanException {
        super(CacheInformationMBean.class);
        this.cacheService = cacheService;
        cacheHub = CompositeCacheManager.getInstance();
    }

    @Override
    public void clear(String name, boolean localOnly) throws MBeanException {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JCSCacheInformation.class);

        if ("*".equals(name)) {
            List<String> failees = new LinkedList<>();
            for (String cacheName : cacheHub.getCacheNames()) {
                try {
                    Cache cache = cacheService.getCache(cacheName);
                    if (localOnly) {
                        cache.localClear();
                    } else {
                        cache.clear();
                    }
                } catch (Exception e) {
                    logger.error("", e);
                    failees.add(cacheName);
                }
            }
            if (false == failees.isEmpty()) {
                StringBuilder sb = new StringBuilder("Failed to clear the following cache regions: ");
                boolean first = true;
                for (String cacheName : failees) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(cacheName);
                }
                String message = sb.toString();
                sb = null;
                throw new MBeanException(new Exception(message), message);
            }
        } else {
            if (Strings.isEmpty(name)) {
                String message = "Invalid or missing cache name";
                throw new MBeanException(new Exception(message), message);
            }
            if (false == ImmutableSet.copyOf(cacheHub.getCacheNames()).contains(name)) {
                String message = "No suche cache: " + name;
                throw new MBeanException(new Exception(message), message);
            }

            try {
                Cache cache = cacheService.getCache(name);
                if (localOnly) {
                    cache.localClear();
                } else {
                    cache.clear();
                }
            } catch (Exception e) {
                logger.error("", e);
                String message = e.getMessage();
                throw new MBeanException(new Exception(message), message);
            }
        }
    }

    @Override
    public long getMemoryCacheCount(final String name) throws MBeanException {
        if (Strings.isEmpty(name)) {
            String message = "Invalid or missing cache name";
            throw new MBeanException(new Exception(message), message);
        }
        if (false == ImmutableSet.copyOf(cacheHub.getCacheNames()).contains(name)) {
            String message = "No such cache: " + name;
            throw new MBeanException(new Exception(message), message);
        }

        return cacheHub.getCache(name).getMemoryCache().getKeyArray().length;
    }

    @Override
    public String getCacheStatistics(final String name) throws MBeanException {
        if ("*".equals(name)) {
            final String[] cacheNames = cacheHub.getCacheNames();
            final StringBuilder sb = new StringBuilder(512 * cacheNames.length);
            for (final String cacheName : cacheNames) {
                sb.append(cacheHub.getCache(cacheName).getStats()).append("\r\n\r\n");
            }
            return sb.toString();
        }

        if (Strings.isEmpty(name)) {
            String message = "Invalid or missing cache name";
            throw new MBeanException(new Exception(message), message);
        }
        if (false == ImmutableSet.copyOf(cacheHub.getCacheNames()).contains(name)) {
            String message = "No suche cache: " + name;
            throw new MBeanException(new Exception(message), message);
        }
        return cacheHub.getCache(name).getStats();
    }

    @Override
    public long getMemoryCacheDataSize(final String name) throws MBeanException {
        if (Strings.isEmpty(name)) {
            String message = "Invalid or missing cache name";
            throw new MBeanException(new Exception(message), message);
        }
        if (false == ImmutableSet.copyOf(cacheHub.getCacheNames()).contains(name)) {
            String message = "No suche cache: " + name;
            throw new MBeanException(new Exception(message), message);
        }

        final IMemoryCache memCache = cacheHub.getCache(name).getMemoryCache();

        final Iterator<?> iter = memCache.getIterator();

        final CountingOnlyOutputStream counter = new CountingOnlyOutputStream();
       ObjectOutputStream out= null;
        try {
            out = new ObjectOutputStream(counter);
        } catch (final IOException e) {
            LOG.error("", e);
            Streams.close(out);
            return 0;
        }
        try {
            while (iter.hasNext()) {
                final ICacheElement ce = (ICacheElement) ((Map.Entry<?, ?>) iter.next()).getValue();
                out.writeObject(ce.getVal());
            }
            out.flush();
        } catch (final Exception e) {
            LOG.info("Problem getting byte count. Likely cause is a non serializable object.{}", e.getMessage());
        } finally {
            Streams.close(out);
        }
        // 4 bytes lost for the serialization header
        return counter.getCount() - 4;
    }

    @Override
    public String[] listRegionNames() {
        return cacheHub.getCacheNames();
    }

}
