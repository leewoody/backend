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

package com.openexchange.oauth;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.oauth.scope.OAuthScope;

/**
 * {@link DefaultOAuthAccount}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DefaultOAuthAccount extends DefaultOAuthToken implements OAuthAccount {

    private int id;
    private String displayName;
    private OAuthServiceMetaData metaData;
    private Set<OAuthScope> enabledScopes;
    private boolean enabledScopesSet;

    /**
     * Initializes a new {@link DefaultOAuthAccount}.
     */
    public DefaultOAuthAccount() {
        super();
        enabledScopes = Collections.emptySet();
        enabledScopesSet = false;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public OAuthServiceMetaData getMetaData() {
        return metaData;
    }

    /**
     * Sets the identifier
     *
     * @param id The identifier to set
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Sets the display name
     *
     * @param displayName The display name to set
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Sets the meta data
     *
     * @param metaData The meta data to set
     */
    public void setMetaData(final OAuthServiceMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public String toString() {
        final String delim = ", ";
        return new StringBuilder(64).append("( id = ").append(this.id).append(delim).append("displayName = ").append(this.displayName).append(delim).append("metaData = ").append(this.metaData).append(delim).append(" )").toString();
    }

    @Override
    public API getAPI() {
        return metaData.getAPI();
    }

    @Override
    public Set<OAuthScope> getEnabledScopes() {
        return enabledScopes;
    }

    /**
     * Adds specified scope to the set of enabled of enabled scopes for this OAuth account
     *
     * @param enabledScope The scope to add
     */
    public void addEnabledScope(OAuthScope enabledScope) {
        if (null != enabledScope) {
            Set<OAuthScope> enabledScopes = new LinkedHashSet<>(this.enabledScopes);
            enabledScopes.add(enabledScope);
            this.enabledScopes = ImmutableSet.copyOf(enabledScopes);
            this.enabledScopesSet = true;
        }
    }

    /**
     * Sets the enabled scopes
     *
     * @param enabledScopes The enabled scopes to set
     */
    public void setEnabledScopes(Set<OAuthScope> enabledScopes) {
        this.enabledScopes = null == enabledScopes ? Collections.<OAuthScope> emptySet() : ImmutableSet.copyOf(enabledScopes);
        this.enabledScopesSet = true;
    }

    /**
     * Checks whether enabled scopes are set in this instance.
     *
     * @return <code>true</code> if set; otherwise <code>false</code>
     */
    public boolean isEnabledScopesSet() {
        return enabledScopesSet;
    }
}
