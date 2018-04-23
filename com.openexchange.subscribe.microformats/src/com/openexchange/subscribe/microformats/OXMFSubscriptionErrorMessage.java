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

package com.openexchange.subscribe.microformats;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link OXMFSubscriptionErrorMessage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public enum OXMFSubscriptionErrorMessage implements DisplayableOXExceptionCode {

    /**
     * A parsing error occurred: %1$s.
     */
    ParseException(CATEGORY_ERROR, 2, OXMFErrorStrings.PARSE_EXCEPTION_HELP, OXMFErrorStrings.PARSE_EXCEPTION),
    IOException(CATEGORY_SERVICE_DOWN, 3, "An I/O error occurred: %1$s. Was trying to get the service at %2$s."),
    HttpException(CATEGORY_SERVICE_DOWN, 4, "An HTTP Error occurred: %1$s. Was trying to get the service at %2$s."),
    CAN_ONLY_POST_TO_EXTERNAL_SUBSCRIPTION_SOURCES(CATEGORY_ERROR, 5, OXMFErrorStrings.CAN_ONLY_POST_TO_EXTERNAL_SUBSCRIPTION_SOURCES, "Can only post to external subscription sources."),
    ERROR_LOADING_SUBSCRIPTION(CATEGORY_SERVICE_DOWN, 6, OXMFErrorStrings.ERROR_LOADING_SUBSCRIPTION, "Could not fetch site at '%1$s'. Please check the spelling or whether you can reach the site in your browser."),
    /**
     * The string cannot be parsed to a valid URL.
     */
    INVALID_URL(CATEGORY_USER_INPUT, 7, OXMFErrorStrings.INVALID_URL, "The string cannot be parsed to a valid URL."),

    ;

    private Category category;

    private int errorCode;

    private String message;

    private final String displayMessage;

    private OXMFSubscriptionErrorMessage(final Category category, final int errorCode, final String message) {
        this(category, errorCode, null, message);
    }

    private OXMFSubscriptionErrorMessage(final Category category, final int errorCode, final String displayMessage, final String message) {
        this.category = category;
        this.errorCode = errorCode;
        this.message = message;
        this.displayMessage = (displayMessage == null) ? OXExceptionStrings.MESSAGE : displayMessage;
    }

    @Override
    public String getPrefix() {
        return "MFS";
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(final OXException e) {
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
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.exception.DisplayableOXExceptionCode#getDisplayMessage()
     */
    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }
}
