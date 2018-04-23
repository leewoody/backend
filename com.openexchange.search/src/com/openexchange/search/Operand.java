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

package com.openexchange.search;

/**
 * {@link Operand} - Represents a operand within a search term.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Operand<V> {

    /**
     * The operand type.
     */
    public static enum Type {
        /**
         * A constant operand.
         */
        CONSTANT("constant"),
        /**
         * A column/field within a data object.
         */
        COLUMN("column"),
        /**
         * A attachment operand.
         */
        ATTACHMENT("attachment"),
        /**
         * A header within a data object.
         */
        HEADER("header"),
        ;

        private final String str;

        private Type(final String str) {
            this.str = str;
        }

        /**
         * Gets this type's string representation.
         *
         * @return The type's string representation.
         */
        public String getType() {
            return str;
        }

        /**
         * Checks if specified string equals this type's string representation.
         *
         * @param other The other string to check
         * @return <code>true</code> if specified string equals this type's string representation; otherwise <code>false</code>.
         */
        public boolean isType(final String other) {
            return str.equalsIgnoreCase(other);
        }
    }

    /**
     * Gets this operand's type.
     *
     * @return The operand's type.
     */
    public Type getType();

    /**
     * Gets this operand's value.
     *
     * @return The operand's value.
     */
    public V getValue();
}
