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

package com.openexchange.snippet;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link SnippetExceptionCodes} - Enumeration of all {@link OXException}s known in snippet module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum SnippetExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR, 2),
    /**
     * No such snippet found for identifier: %1$s
     */
    SNIPPET_NOT_FOUND("No such snippet found for identifier: %1$s", CATEGORY_ERROR, 3),
    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", CATEGORY_ERROR, 4),
    /**
     * Illegal state: %1$s
     */
    ILLEGAL_STATE("Illegal state: %1$s", CATEGORY_ERROR, 5),
    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR("A SQL error occurred: %1$s", CATEGORY_ERROR, 6),
    /**
     * No such snippet attachment found for identifier %1$s in snippet %2$s
     */
    ATTACHMENT_NOT_FOUND("No such snippet attachment found for identifier %1$s in snippet %2$s", CATEGORY_ERROR, 7),
    /**
     * Maximum number of '%1$s' for signature images reached.
     */
    MAXIMUM_IMAGES_COUNT("Maximum number of '%1$s' for signature images reached.", SnippetStrings.MAXIMUM_IMAGES_COUNT_MSG, CATEGORY_ERROR, 8),
    /**
     * The signature image exceeds the maximum allowed size of '%1$s' (%2$s bytes).
     */
    MAXIMUM_IMAGE_SIZE("The signature image exceeds the maximum allowed size of '%1$s' (%2$s bytes).", SnippetStrings.MAXIMUM_IMAGE_SIZE_MSG, CATEGORY_ERROR, 9),
    /**
     * Invalid or harmful image data detected.
     */
    INVALID_IMAGE_DATA("Invalid or harmful image data detected.", SnippetStrings.INVALID_IMAGE_DATA_MSG, CATEGORY_ERROR, 10),
    /**
     * Snippet %1$s must not be changed by user %2$s in context %3$s
     */
    UPDATE_DENIED("Snippet %1$s must not be changed by user %2$s in context %3$s", SnippetStrings.UPDATE_DENIED_MSG, CATEGORY_ERROR, 11),

    ;

    /**
     * The error code prefix for snippet module.
     */
    public static String PREFIX = "SNIPPET";

    private Category category;

    private int detailNumber;

    private String message;

    private String displayMessage;

    private SnippetExceptionCodes(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    private SnippetExceptionCodes(String message, Category category, int detailNumber) {
        this(message, null, category, detailNumber);
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
