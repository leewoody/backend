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

package com.openexchange.file.storage.search;

import static com.openexchange.java.Strings.toLowerCase;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.java.Strings;


/**
 * {@link AbstractStringSearchTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractStringSearchTerm implements SearchTerm<String> {

    /** The pattern */
    protected final String pattern;

    /** Whether to compare ignore-case or case-sensitive */
    protected final boolean ignoreCase;

    /** Whether to perform a substring or equals check */
    protected final boolean substringSearch;

    /**
     * Initializes a new {@link AbstractStringSearchTerm}.
     */
    protected AbstractStringSearchTerm(final String pattern, final boolean ignoreCase, final boolean substringSearch) {
        super();
        this.pattern = pattern;
        this.ignoreCase = ignoreCase;
        this.substringSearch = substringSearch;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    /**
     * Checks whether to perform a substring or equals check
     *
     * @return The substring-search flag
     */
    public boolean isSubstringSearch() {
        return substringSearch;
    }

    /**
     * Gets the ignore-case flag
     *
     * @return The ignore-case flag
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    @Override
    public boolean matches(final File file) throws OXException {
        final String str = getString(file);
        if (Strings.isEmpty(str)) {
            return false;
        }

        if (substringSearch) {
            return ignoreCase ? (toLowerCase(str).indexOf(toLowerCase(pattern)) >= 0) : (str.indexOf(pattern) >= 0);
        }

        return ignoreCase ? (toLowerCase(str).equals(toLowerCase(pattern))) : (str.equals(pattern));
    }

    /**
     * Gets the string to compare with.
     *
     * @param file The file to retrieve the string from
     * @return The string
     */
    protected abstract String getString(File file);

}
