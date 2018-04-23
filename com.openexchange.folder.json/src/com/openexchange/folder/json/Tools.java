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

package com.openexchange.folder.json;

import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * {@link Tools} - A utility class for folder storage processing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Tools {

    /**
     * Initializes a new {@link Tools}.
     */
    private Tools() {
        super();
    }

    private static final ConcurrentMap<String, Future<TimeZone>> TZ_MAP = new ConcurrentHashMap<String, Future<TimeZone>>();

    /**
     * Gets the <code>TimeZone</code> for the given ID.
     *
     * @param timeZoneID The ID for a <code>TimeZone</code>, either an abbreviation such as "PST", a full name such as
     *            "America/Los_Angeles", or a custom ID such as "GMT-8:00".
     * @return The specified <code>TimeZone</code>, or the GMT zone if the given ID cannot be understood.
     * @throws IllegalStateException If this method fails to return an appropriate time zone
     */
    public static TimeZone getTimeZone(final String timeZoneID) {
        if (null == timeZoneID) {
            return null;
        }
        Future<TimeZone> future = TZ_MAP.get(timeZoneID);
        if (null == future) {
            final FutureTask<TimeZone> ft = new FutureTask<TimeZone>(new Callable<TimeZone>() {

                @Override
                public TimeZone call() throws Exception {
                    return TimeZone.getTimeZone(timeZoneID);
                }
            });
            future = TZ_MAP.putIfAbsent(timeZoneID, ft);
            if (null == future) {
                future = ft;
                ft.run();
            }
        }
        try {
            return future.get();
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            final IllegalStateException ise = new IllegalStateException(e.getMessage());
            ise.initCause(e);
            throw ise;
        } catch (final CancellationException e) {
            final IllegalStateException ise = new IllegalStateException(e.getMessage());
            ise.initCause(e);
            throw ise;
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                final IllegalStateException ise = new IllegalStateException(e.getMessage());
                ise.initCause(e);
                throw ise;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        }
    }
}
