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

package com.openexchange.net;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.math.NumberRange;
import org.apache.commons.lang.math.Range;
import org.apache.commons.validator.routines.InetAddressValidator;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6AddressRange;
import com.openexchange.net.utils.Strings;
import edazdarevic.commons.net.CIDRUtils;

/**
 *
 * {@link IPRange}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class IPRange {

    /**
     * Parses specified string to an IP range.
     *
     * @param string The string to parse
     * @return The resulting IP range or <code>null</code> if passed string is empty
     * @throws IllegalArgumentException If parsing fails
     */
    public static IPRange parseRange(final String string) {
        if (Strings.isEmpty(string)) {
            return null;
        }
        if (string.indexOf('-') > 0) {  // Range with '-'
            return handleWithSlash(string);
        } else if (string.indexOf('/') > 0) { // Range as CIDR
            return handleCIDR(string);
        }
        return handleSingleAddress(string);
    }

    private static IPRange handleSingleAddress(String address) {
        if (InetAddressValidator.getInstance().isValidInet4Address(address)) {
            byte[] octets = IPAddressUtil.textToNumericFormatV4(address);
            return new IPRange(new LongRange(ipToLong(octets), ipToLong(octets)), null);
        }
        final IPv6Address iPv6Address = IPv6Address.fromString(address);
        return new IPRange(null, new NumberRange(iPv6Address.toBigInteger(), iPv6Address.toBigInteger()));

    }

    private static IPRange handleCIDR(String cidrRange) {
        try {
            CIDRUtils cidrUtils = new CIDRUtils(cidrRange);
            InetAddress startAddress = cidrUtils.getStartAddress();
            InetAddress endAddress = cidrUtils.getEndAddress();
            if (InetAddressValidator.getInstance().isValidInet4Address(startAddress.getHostAddress())) { //handle v4
                byte[] octetsStart = IPAddressUtil.textToNumericFormatV4(startAddress.getHostName());
                byte[] octetsEnd = IPAddressUtil.textToNumericFormatV4(endAddress.getHostName());

                final LongRange ipv4Range = new LongRange(ipToLong(octetsStart), ipToLong(octetsEnd));
                return new IPRange(ipv4Range, null);
            }

            final NumberRange ipv6Range = new NumberRange(IPv6Address.fromString(startAddress.toString().replaceAll("/", "")).toBigInteger(), IPv6Address.fromString(endAddress.toString().replaceAll("/", "")).toBigInteger());
            return new IPRange(null, ipv6Range);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            //LOG.
        }
        return null;
    }

    private static IPRange handleWithSlash(String range) {
        final String[] addresses = range.split("\\s*-\\s*");

        if (InetAddressValidator.getInstance().isValidInet4Address(addresses[0])) {
            byte[] octetsStart = IPAddressUtil.textToNumericFormatV4(addresses[0]);
            final byte[] octetsEnd = IPAddressUtil.textToNumericFormatV4(addresses[1]);
            final LongRange ipv4Range = new LongRange(ipToLong(octetsStart), ipToLong(octetsEnd));

            return new IPRange(ipv4Range, null);
        }
        final IPv6AddressRange rangeObj = IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString(addresses[0]), IPv6Address.fromString(addresses[1]));

        final NumberRange ipv6Range = new NumberRange(rangeObj.getFirst().toBigInteger(), rangeObj.getLast().toBigInteger());
        return new IPRange(null, ipv6Range);
    }

    private static long ipToLong(final byte[] octets) {
        long result = 0;
        for (int i = 0; i < octets.length; i++) {
            result |= octets[i] & 0xff;
            if (i < octets.length - 1) {
                result <<= 8;
            }
        }
        return result;
    }

    private static byte[] longToIP(long value) {
        final List<Byte> retval = new ArrayList<Byte>();
        while (value != 0) {
            retval.add(Byte.valueOf((byte) (value & 0xff)));
            value >>= 8;
        }
        Collections.reverse(retval);
        return B2b(retval);
    }

    private static byte[] B2b(final Collection<Byte> byteCollection) {
        byte[] byteArray = new byte[byteCollection.size()];
        int pos = 0;
        for (final Byte b : byteCollection) {
            if (null != b) {
                byteArray[pos++] = b.byteValue();
            }
        }
        if (pos != byteArray.length) {
            final byte[] tmpArray = new byte[pos];
            System.arraycopy(byteArray, 0, tmpArray, 0, pos);
            byteArray = tmpArray;
        }
        return byteArray;
    }

    /** The {@link #ipToLong(byte[]) ipToLong()} result for localhost IP address <code>"127.0.0.1"</code> */
    private static final long LONG_IPv4_LOCALHOST = 2130706433L;

    // ---------------------------------------------------------------------------------------------------------------------------

    private final Cache<String, Boolean> cache;
    private final Range ipv4Range;
    private final IPv6AddressRange ipv6Range;

    /**
     * Initializes a new {@link IPRange}. Use {@link IPRange#parseRange(String)} to get an instance
     *
     * @param ipv4Range The IPv4 address range
     * @param ipv6Range The IPv6 address range
     */
    private IPRange(final Range ipv4Range, final Range ipv6Range) {
        super();
        this.ipv4Range = ipv4Range;
        this.ipv6Range = ipv6Range == null ? null : IPv6AddressRange.fromFirstAndLast(IPv6Address.fromBigInteger((BigInteger) ipv6Range.getMinimumNumber()), IPv6Address.fromBigInteger((BigInteger) ipv6Range.getMaximumNumber()));
        this.cache = CacheBuilder.newBuilder().initialCapacity(512).maximumSize(65536).expireAfterAccess(4, TimeUnit.HOURS).build();
    }

    /**
     * Checks if specified IP address is covered by configured IP range.
     *
     * @param ipAddress The IP address to check
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean contains(String ipAddress) {
        // Check for cached entry
        {
            Boolean cached = cache.getIfPresent(ipAddress);
            if (null != cached) {
                return cached.booleanValue();
            }
        }

        // Calculate...
        byte[] octets = IPAddressUtil.textToNumericFormatV4(ipAddress);
        if (null != octets) {
            /*
             * IPv4
             */
            boolean ret = null != this.ipv4Range && this.ipv4Range.containsLong(ipToLong(octets));
            this.cache.put(ipAddress, Boolean.valueOf(ret));
            return ret;
        }
        return containsIPv6(ipAddress);
    }

    /**
     * Checks if specified IPv4 octets are covered by configured IP range.
     *
     * @param octets The IPv4 octets to check
     * @param ipAddress The octets' IPv4 string representation; might be <code>null</code>
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean containsIPv4(byte[] octets, String ipAddress) {
        // Check for cached entry
        if (null != ipAddress) {
            Boolean cached = this.cache.getIfPresent(ipAddress);
            if (null != cached) {
                return cached.booleanValue();
            }
        }

        if (null != octets) {
            long longValue = ipToLong(octets);
            if (longValue == 0) {
                // All IPv4; consider as contained
                this.cache.put(ipAddress, Boolean.TRUE);
                return true;
            }
            boolean ret = null != this.ipv4Range && this.ipv4Range.containsLong(longValue);
            if (null != ipAddress) {
                this.cache.put(ipAddress, Boolean.valueOf(ret));
            }
            return ret;
        }
        return false;
    }

    /**
     * Checks if specified IPv6 octets are covered by configured IP range.
     *
     * @param octets The IPv6 octets to check
     * @param ipAddress The octets' IPv6 string representation; might be <code>null</code>
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean containsIPv6(byte[] octets, String ipAddress) {
        if (null != ipAddress) {
            Boolean cached = this.cache.getIfPresent(ipAddress);
            if (null != cached) {
                return cached.booleanValue();
            }
        }
        IPv6Address v6Address = null == octets ? IPv6Address.fromString(ipAddress) : IPv6Address.fromByteArray(octets);
        if (v6Address.getHighBits() == 0) {
            if (v6Address.getLowBits() == 0) {
                // All IPv6; consider as contained
                this.cache.put(ipAddress, Boolean.TRUE);
                return true;
            }
            if (v6Address.getLowBits() == 1) {
                // Localhost IPv6; consider as contained
                if (null != this.ipv4Range && this.ipv4Range.containsLong(LONG_IPv4_LOCALHOST)) {
                    this.cache.put(ipAddress, Boolean.TRUE);
                    return true;
                }
                return checkAgainstIPv6Range(v6Address, ipAddress);
            }
        }
        return checkAgainstIPv6Range(v6Address, ipAddress);
    }

    private boolean checkAgainstIPv6Range(IPv6Address v6Address, String ipAddress) {
        boolean ret = null != this.ipv6Range && this.ipv6Range.contains(v6Address);
        if (null != ipAddress) {
            this.cache.put(ipAddress, Boolean.valueOf(ret));
        }
        return ret;
    }

    public boolean containsIPv6(String ipAddress) {
        if (null != ipAddress) {
            Boolean cached = this.cache.getIfPresent(ipAddress);
            if (null != cached) {
                return cached.booleanValue();
            }
        }
        IPv6Address v6Address = IPv6Address.fromString(ipAddress);
        boolean ret = null != this.ipv6Range && this.ipv6Range.contains(v6Address);
        if (null != ipAddress) {
            this.cache.put(ipAddress, Boolean.valueOf(ret));
        }
        return ret;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (null != this.ipv4Range) {
            for (final byte b : longToIP(this.ipv4Range.getMinimumLong())) {
                sb.append(b < 0 ? 256 + b : b);
                sb.append('.');
            }
            sb.setCharAt(sb.length() - 1, '-');
            for (final byte b : longToIP(this.ipv4Range.getMaximumLong())) {
                sb.append(b < 0 ? 256 + b : b);
                sb.append('.');
            }
            sb.setLength(sb.length() - 1);
        }
        if (null != this.ipv6Range) {
            sb.append(this.ipv6Range.toString());
        }
        return sb.toString();
    }
}
