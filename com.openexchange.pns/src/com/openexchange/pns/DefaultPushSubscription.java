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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns;

import java.util.List;
import com.google.common.collect.ImmutableList;

/**
 * {@link DefaultPushSubscription} - The default implementation for {@code PushSubscription}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DefaultPushSubscription implements PushSubscription {

    /**
     * Gets the appropriate {@link DefaultPushSubscription} instance from specified subscription.
     *
     * @param subscription The subscription
     * @return The appropriate {@link DefaultPushSubscription} instance
     */
    public static DefaultPushSubscription instanceFor(PushMatch match) {
        Builder builder = new Builder()
            .contextId(match.getContextId())
            .token(match.getToken())
            .transportId(match.getTransportId())
            .userId(match.getUserId())
            .client(match.getClient());

        return builder.build();
    }

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for a <code>DefaultPushSubscription</code> instance */
    public static class Builder {

        int userId;
        int contextId;
        List<String> topics;
        String transportId;
        String token;
        String client;

        /** Creates a new builder */
        Builder() {
            super();
        }

        /**
         * Sets the client identifier
         * @param client The client identifier
         * @return This builder
         */
        public Builder client(String client) {
            this.client = client;
            return this;
        }

        /**
         * Sets the user identifier
         * @param userId The user identifier
         * @return This builder
         */
        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Sets the context identifier
         * @param contextId The context identifier
         * @return This builder
         */
        public Builder contextId(int contextId) {
            this.contextId = contextId;
            return this;
        }

        /**
         * Sets the topics
         * @param topics The topics
         * @return This builder
         * @throws IllegalArgumentException If a topic name is invalid.
         */
        public Builder topics(List<String> topics) {
            if (null != topics) {
                if (topics.isEmpty()) {
                    throw new IllegalArgumentException("empty topics");
                }
                for (String topic : topics) {
                    PushNotifications.validateTopicName(topic);
                }
            }
            this.topics = topics;
            return this;
        }

        /**
         * Sets the transport identifier
         * @param transportId The transport identifier
         * @return This builder
         */
        public Builder transportId(String transportId) {
            this.transportId = transportId;
            return this;
        }

        /**
         * Sets the token
         * <p>
         * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
         * Note: A non-empty token is required to be set in case nature is set to {@link Nature#PERSISTENT}
         * </div>
         * @param token The token
         * @return This builder
         */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        /**
         * Builds the <code>DefaultPushSubscription</code> instance.
         * @return The resulting <code>DefaultPushSubscription</code> instance
         */
        public DefaultPushSubscription build() {
            return new DefaultPushSubscription(this);
        }
    }

    // --------------------------------------------------------------------------------------------------

    private final int userId;
    private final int contextId;
    private final String client;
    private final List<String> topics;
    private final String transportId;
    private final String token;

    /**
     * Initializes a new {@link DefaultPushSubscription}.
     */
    DefaultPushSubscription(Builder builder) {
        super();
        this.topics = null == builder.topics ? null : ImmutableList.copyOf(builder.topics);
        this.contextId = builder.contextId;
        this.token = builder.token;
        this.transportId = builder.transportId;
        this.userId = builder.userId;
        this.client = builder.client;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public List<String> getTopics() {
        return topics;
    }

    @Override
    public String getTransportId() {
        return transportId;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + userId;
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        result = prime * result + ((transportId == null) ? 0 : transportId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PushSubscription)) {
            return false;
        }
        PushSubscription other = (PushSubscription) obj;
        if (contextId != other.getContextId()) {
            return false;
        }
        if (userId != other.getUserId()) {
            return false;
        }
        if (token == null) {
            if (other.getToken() != null) {
                return false;
            }
        } else if (!token.equals(other.getToken())) {
            return false;
        }
        if (transportId == null) {
            if (other.getTransportId() != null) {
                return false;
            }
        } else if (!transportId.equals(other.getTransportId())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(96);
        sb.append("{userId=").append(userId).append(", contextId=").append(contextId).append(", ");
        if (client != null) {
            sb.append("client=").append(client).append(", ");
        }
        if (topics != null) {
            sb.append("topics=").append(topics).append(", ");
        }
        if (transportId != null) {
            sb.append("transportId=").append(transportId).append(", ");
        }
        if (token != null) {
            sb.append("token=").append(token).append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

}
