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

package com.openexchange.rss.util;

import static com.openexchange.java.Autoboxing.I;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import com.google.common.collect.ImmutableSet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.java.InetAddresses;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.rss.osgi.Services;
import com.openexchange.tools.stream.CountingInputStream;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FetcherEvent;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.AbstractFeedFetcher;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.fetcher.impl.SyndFeedInfo;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * {@link TimoutHttpURLFeedFetcher} - timeout-capable {@link HttpURLFeedFetcher}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.4.1
 */
public class TimoutHttpURLFeedFetcher extends AbstractFeedFetcher implements Reloadable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TimoutHttpURLFeedFetcher.class);

    private static final String MAX_FEED_SIZE_PROPERTY_NAME = "com.openexchange.messaging.rss.feed.size";

    /** The timeout value, in milliseconds, to be used when opening a communications link to the resource */
    protected final int connectTimout;

    /** The read timeout to a specified timeout, in milliseconds */
    protected final int readTimout;

    /**
     * Defines the maximum feed size for an RSS feed in bytes
     */
    private long maximumAllowedSize;

    private FeedFetcherCache feedInfoCache;

    /**
     * Initializes a new {@link TimoutHttpURLFeedFetcher}.
     *
     * @param connectTimout The timeout value, in milliseconds, to be used when opening a communications link to the resource
     * @param readTimout The read timeout to a specified timeout, in milliseconds
     */
    public TimoutHttpURLFeedFetcher(int connectTimout, int readTimout) {
        super();
        this.connectTimout = connectTimout;
        this.readTimout = readTimout;
        reloadConfiguration(Services.getService(ConfigurationService.class));
    }

    /**
     * Initializes a new {@link TimoutHttpURLFeedFetcher}.
     *
     * @param connectTimout The timeout value, in milliseconds, to be used when opening a communications link to the resource
     * @param readTimout The read timeout to a specified timeout, in milliseconds
     * @param feedInfoCache The feed cache
     */
    public TimoutHttpURLFeedFetcher(int connectTimout, int readTimout, FeedFetcherCache feedInfoCache) {
        this.connectTimout = connectTimout;
        this.readTimout = readTimout;
        setFeedInfoCache(feedInfoCache);
        reloadConfiguration(Services.getService(ConfigurationService.class));
    }

    private static final Set<Integer> REDIRECT_RESPONSE_CODES = ImmutableSet.of(I(HttpURLConnection.HTTP_MOVED_PERM), I(HttpURLConnection.HTTP_MOVED_TEMP), I(HttpURLConnection.HTTP_SEE_OTHER), I(HttpURLConnection.HTTP_USE_PROXY));

    private URL checkPossibleRedirect(int responseCode, HttpURLConnection httpConnection, boolean originalAddressIsRemote) throws FetcherException {
        if (!REDIRECT_RESPONSE_CODES.contains(I(responseCode))) {
            // No redirect response code
            return null;
        }

        // Get redirect location & disconnect current URL connection instance
        String redirectUrl = httpConnection.getHeaderField("Location");
        httpConnection.disconnect();

        // Validate redirect location
        if (Strings.isEmpty(redirectUrl)) {
            // Missing "Location" header
            throw new FetcherException("Missing redirect URL");
        }

        // Examine redirect location
        try {
            URL feedUrl = new URL(redirectUrl);
            if (RssProperties.isDenied(feedUrl.getProtocol(), feedUrl.getHost(), feedUrl.getPort())) {
                // Deny redirecting to a local address
                throw new FetcherException(feedUrl.toExternalForm() + " is not an allowed redirect URL");
            }
            if (originalAddressIsRemote) {
                try {
                    InetAddress inetAddress = InetAddress.getByName(feedUrl.getHost());
                    if (InetAddresses.isInternalAddress(inetAddress)) {
                        // Deny redirecting to a local address
                        throw new FetcherException(feedUrl.toExternalForm() + " is not an allowed redirect URL");
                    }
                } catch (UnknownHostException e) {
                    // IP address of that host could not be determined
                    LOG.warn("Unknown host: {}. Skipping retrieving feed from redirect URL {}", feedUrl.getHost(), feedUrl, e);
                    throw new IllegalArgumentException(feedUrl.toExternalForm() + " contains an unknown host", e);
                }
            }
            return feedUrl;
        } catch (MalformedURLException e) {
            LOG.warn("Redirect URL is invalid: {}", redirectUrl, e);
            throw new IllegalArgumentException("Redirect URL is invalid: " + redirectUrl, e);
        }
    }

    /**
     * Retrieve a feed over HTTP
     *
     * @param feedUrl A non-null URL of a RSS/Atom feed to retrieve
     * @return A {@link com.sun.syndication.feed.synd.SyndFeed} object
     * @throws IllegalArgumentException if the URL is null;
     * @throws IOException if a TCP error occurs
     * @throws FeedException if the feed is not valid
     * @throws FetcherException if a HTTP error occurred
     */
    @Override
    public SyndFeed retrieveFeed(URL feedUrlToRetrieve) throws IllegalArgumentException, IOException, FeedException, FetcherException {
        if (feedUrlToRetrieve == null) {
            throw new IllegalArgumentException("null is not a valid URL");
        }

        boolean originalAddressIsRemote;
        try {
            InetAddress inetAddress = InetAddress.getByName(feedUrlToRetrieve.getHost());
            originalAddressIsRemote = false == InetAddresses.isInternalAddress(inetAddress);
        } catch (UnknownHostException e) {
            // IP address of that host could not be determined
            LOG.warn("Unknown host: {}. Skipping retrieving feed from URL {}", feedUrlToRetrieve.getHost(), feedUrlToRetrieve, e);
            throw new IllegalArgumentException(feedUrlToRetrieve.toExternalForm() + " contains an unknown host", e);
        }

        URL feedUrl = feedUrlToRetrieve;
        NextUrl: while (true) {
            URLConnection connection = feedUrl.openConnection();
            if (!(connection instanceof HttpURLConnection)) {
                throw new IllegalArgumentException(feedUrl.toExternalForm() + " is not a valid HTTP Url");
            }

            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            if ("https".equals(feedUrl.getProtocol()) || 443 == feedUrl.getPort()) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) httpConnection;
                SSLSocketFactory sslSocketFactory = Services.getService(SSLSocketFactoryProvider.class).getDefault();
                HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
                httpConnection = httpsConnection;
            }

            if (connectTimout > 0) {
                httpConnection.setConnectTimeout(connectTimout);
            }
            if (readTimout > 0) {
                httpConnection.setReadTimeout(readTimout);
            }

            // Deny to automatically follow redirects
            httpConnection.setInstanceFollowRedirects(false);

            FeedFetcherCache cache = getFeedInfoCache();
            if (cache == null) {
                fireEvent(FetcherEvent.EVENT_TYPE_FEED_POLLED, connection);
                InputStream inputStream = null;
                setRequestHeaders(connection, null);
                httpConnection.connect();
                try {
                    // Check for redirect
                    int responseCode = httpConnection.getResponseCode();
                    URL redirectUrl = checkPossibleRedirect(responseCode, httpConnection, originalAddressIsRemote);
                    if (null != redirectUrl) {
                        feedUrl = redirectUrl;
                        continue NextUrl;
                    }

                    inputStream = httpConnection.getInputStream();
                    return getSyndFeedFromStream(inputStream, connection);
                } catch (java.io.IOException e) {
                    handleErrorCodes(((HttpURLConnection) connection).getResponseCode());
                } finally {
                    Streams.close(inputStream);
                    httpConnection.disconnect();
                }
                // we will never actually get to this line
                return null;
            }

            // With cache
            SyndFeedInfo syndFeedInfo = cache.getFeedInfo(feedUrl);
            setRequestHeaders(connection, syndFeedInfo);
            httpConnection.connect();
            try {
                fireEvent(FetcherEvent.EVENT_TYPE_FEED_POLLED, connection);

                // Check for redirect
                int responseCode = httpConnection.getResponseCode();
                URL redirectUrl = checkPossibleRedirect(responseCode, httpConnection, originalAddressIsRemote);
                if (null != redirectUrl) {
                    feedUrl = redirectUrl;
                    continue NextUrl;
                }

                if (syndFeedInfo == null) {
                    // this is a feed that hasn't been retrieved
                    syndFeedInfo = new SyndFeedInfo();
                    retrieveAndCacheFeed(feedUrl, syndFeedInfo, httpConnection, responseCode);
                } else {
                    // check the response code
                    if (responseCode != HttpURLConnection.HTTP_NOT_MODIFIED) {
                        // the response code is not 304 NOT MODIFIED
                        // This is either because the feed server
                        // does not support condition gets
                        // or because the feed hasn't changed
                        retrieveAndCacheFeed(feedUrl, syndFeedInfo, httpConnection, responseCode);
                    } else {
                        // the feed does not need retrieving
                        fireEvent(FetcherEvent.EVENT_TYPE_FEED_UNCHANGED, connection);
                    }
                }
                return syndFeedInfo.getSyndFeed();
            } finally {
                httpConnection.disconnect();
            }
        }
    }

    /**
     * Retrieves and caches a {@link SyndFeed}
     *
     * @param feedUrl The feed {@link URL}
     * @param syndFeedInfo The {@link SyndFeedInfo} to retrieve and cache
     * @param connection The {@link HttpURLConnection}
     * @throws IllegalArgumentException
     * @throws FeedException
     * @throws FetcherException
     * @throws IOException if an I/O error occurs
     */
    protected void retrieveAndCacheFeed(URL feedUrl, SyndFeedInfo syndFeedInfo, HttpURLConnection connection, int responseCode) throws IllegalArgumentException, FeedException, FetcherException, IOException {
        handleErrorCodes(responseCode);

        resetFeedInfo(feedUrl, syndFeedInfo, connection);
        FeedFetcherCache cache = getFeedInfoCache();
        // resetting feed info in the cache
        // could be needed for some implementations
        // of FeedFetcherCache (eg, distributed HashTables)
        if (cache != null) {
            cache.setFeedInfo(feedUrl, syndFeedInfo);
        }
    }

    /**
     * Resets the specified {@link SyndFeedInfo}
     *
     * @param orignalUrl The original URL
     * @param syndFeedInfo The {@link SyndFeedInfo} to reset
     * @param connection The {@link HttpURLConnection}
     * @throws IllegalArgumentException
     * @throws IOException if an I/O error occurs
     * @throws FeedException
     */
    protected void resetFeedInfo(URL orignalUrl, SyndFeedInfo syndFeedInfo, HttpURLConnection connection) throws IllegalArgumentException, IOException, FeedException {
        // need to always set the URL because this may have changed due to 3xx redirects
        syndFeedInfo.setUrl(connection.getURL());

        // the ID is a persistant value that should stay the same even if the URL for the
        // feed changes (eg, by 3xx redirects)
        syndFeedInfo.setId(orignalUrl.toString());

        // This will be 0 if the server doesn't support or isn't setting the last modified header
        syndFeedInfo.setLastModified(new Long(connection.getLastModified()));

        // This will be null if the server doesn't support or isn't setting the ETag header
        syndFeedInfo.setETag(connection.getHeaderField("ETag"));

        // get the contents
        InputStream inputStream = null;
        try {
            inputStream = connection.getInputStream();
            SyndFeed syndFeed = getSyndFeedFromStream(inputStream, connection);

            String imHeader = connection.getHeaderField("IM");
            if (isUsingDeltaEncoding() && (imHeader != null && imHeader.indexOf("feed") >= 0)) {
                FeedFetcherCache cache = getFeedInfoCache();
                if (cache != null && connection.getResponseCode() == 226) {
                    // client is setup to use http delta encoding and the server supports it and has returned a delta encoded response
                    // This response only includes new items
                    SyndFeedInfo cachedInfo = cache.getFeedInfo(orignalUrl);
                    if (cachedInfo != null) {
                        SyndFeed cachedFeed = cachedInfo.getSyndFeed();

                        // set the new feed to be the orginal feed plus the new items
                        syndFeed = combineFeeds(cachedFeed, syndFeed);
                    }
                }
            }

            syndFeedInfo.setSyndFeed(syndFeed);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * <p>Set appropriate HTTP headers, including conditional get and gzip encoding headers</p>
     *
     * @param connection A URLConnection
     * @param syndFeedInfo The SyndFeedInfo for the feed to be retrieved. May be null
     */
    protected void setRequestHeaders(URLConnection connection, SyndFeedInfo syndFeedInfo) {
        if (syndFeedInfo != null) {
            // set the headers to get feed only if modified
            // we support the use of both last modified and eTag headers
            if (syndFeedInfo.getLastModified() != null) {
                Object lastModified = syndFeedInfo.getLastModified();
                if (lastModified instanceof Long) {
                    connection.setIfModifiedSince(((Long) syndFeedInfo.getLastModified()).longValue());
                }
            }
            if (syndFeedInfo.getETag() != null) {
                connection.setRequestProperty("If-None-Match", syndFeedInfo.getETag());
            }

        }
        // header to retrieve feed gzipped
        connection.setRequestProperty("Accept-Encoding", "gzip");

        // set the user agent
        connection.addRequestProperty("User-Agent", getUserAgent());

        if (isUsingDeltaEncoding()) {
            connection.addRequestProperty("A-IM", "feed");
        }
    }

    /**
     * Reads the SyndFeed from the specified input stream and fires an event {@link FetcherEvent#EVENT_TYPE_FEED_RETRIEVED}
     *
     * @param inputStream The {@link InputStream}
     * @param connection The {@link URLConnection}
     * @return The {@link SyndFeed}
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException
     * @throws FeedException
     */
    private SyndFeed getSyndFeedFromStream(InputStream inputStream, URLConnection connection) throws IOException, IllegalArgumentException, FeedException {
        SyndFeed feed = readSyndFeedFromStream(inputStream, connection);
        fireEvent(FetcherEvent.EVENT_TYPE_FEED_RETRIEVED, connection, feed);
        return feed;
    }

    /**
     * Reads the SyndFeed from the specified input stream
     *
     * @param inputStream The {@link InputStream}
     * @param connection The {@link URLConnection}
     * @return The {@link SyndFeed}
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException
     * @throws FeedException
     */
    private SyndFeed readSyndFeedFromStream(InputStream inputStream, URLConnection connection) throws IOException, IllegalArgumentException, FeedException {
        CountingInputStream cis = inputStream instanceof CountingInputStream ? (CountingInputStream) inputStream : new CountingInputStream(inputStream, maximumAllowedSize);
        InputStream is;
        if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
            // handle gzip encoded content
            is = new GZIPInputStream(cis, 65536);
        } else {
            is = new BufferedInputStream(cis, 65536);
        }

        XmlReader reader = null;
        if (connection.getHeaderField("Content-Type") != null) {
            reader = new XmlReader(is, connection.getHeaderField("Content-Type"), true);
        } else {
            reader = new XmlReader(is, true);
        }

        SyndFeedInput syndFeedInput = new SyndFeedInput();
        syndFeedInput.setPreserveWireFeed(isPreserveWireFeed());

        return syndFeedInput.build(reader);
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        maximumAllowedSize = configService.getIntProperty(MAX_FEED_SIZE_PROPERTY_NAME, 4194304);
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(MAX_FEED_SIZE_PROPERTY_NAME);
    }

    /**
     * @return The FeedFetcherCache used by this fetcher (Could be null)
     */
    public synchronized FeedFetcherCache getFeedInfoCache() {
        return feedInfoCache;
    }

    /**
     * @param cache The cache to be used by this fetcher (pass null to stop using a cache)
     */
    public synchronized void setFeedInfoCache(FeedFetcherCache cache) {
        feedInfoCache = cache;
    }

}
