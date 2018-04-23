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

package com.openexchange.folderstorage.mail;

import com.openexchange.folderstorage.SortableId;

/**
 * {@link MailId} - A mail ID.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailId implements SortableId {

    private final String fullName;
    private final int ordinal;
    private String name;

    /**
     * Initializes a new {@link MailId}.
     *
     * @param fullName The full name
     * @param ordinal The ordinal
     */
    public MailId(final String fullName, final int ordinal) {
        super();
        this.fullName = fullName;
        this.ordinal = ordinal;
    }

    /**
     * Applies specified name.
     *
     * @param name The name
     * @return This {@link MailId} instance with new name applied
     */
    public MailId setName(final String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return fullName;
    }

    @Override
    public Priority getPriority() {
        /*
         * Mail folders come first
         */
        return Priority.HIGH;
    }

    @Override
    public int compareTo(final SortableId o) {
        if (o instanceof MailId) {
            final int thisVal = ordinal;
            final int anotherVal = ((MailId) o).ordinal;
            return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
        }
        final int thisPrio = getPriority().ordinal();
        final int anotherPrio = (o).getPriority().ordinal();
        return (thisPrio < anotherPrio ? 1 : (thisPrio == anotherPrio ? 0 : -1));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ordinal;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MailId)) {
            return false;
        }
        final MailId other = (MailId) obj;
        if (ordinal != other.ordinal) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("{folderId=").append(fullName).append(", ordinal=").append(ordinal).append('}').toString();
    }

}
