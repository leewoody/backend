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


package com.openexchange.spamhandler.spamassassin.exceptions;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Error codes for permission exceptions.
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.org">Dennis Sieben</a>
 */
public enum SpamhandlerSpamassassinConfigurationExceptionCode implements DisplayableOXExceptionCode {
    /**
     * The given value for mode "%s" is not a possible one
     */
    MODE_TYPE_WRONG("The value given for mode \"%s\" is invalid.", CATEGORY_CONFIGURATION, 1),

    /**
     * The parameter "%s" is not set in the property file
     */
    PARAMETER_NOT_SET("The parameter \"%s\" is not set in property file", CATEGORY_CONFIGURATION, 2),

    /**
     * The parameter "%s" must be set in the property file if spamd is true
     */
    PARAMETER_NOT_SET_SPAMD("The parameter \"%s\" must be set in the property file if spamd is true", CATEGORY_CONFIGURATION, 3),

    /**
     * The parameter "%s" must be an integer value but is "%s"
     */
    PARAMETER_NO_INTEGER("The parameter \"%s\" must be an integer value but is \"%s\"", CATEGORY_CONFIGURATION, 4),

    /**
     * The parameter "userSource" must be set in the property file if spamd is true
     */
    USERSOURCE_NOT_SET("The parameter \"userSource\" must be set in the property file if spamd is true", CATEGORY_CONFIGURATION, 5),

    /**
     * The given value for userSource "%s" is not a possible one
     */
    USERSOURCE_WRONG("The given value for userSource \"%s\" is not a possible one", CATEGORY_CONFIGURATION, 6),

    /**
     * The parameter "%s" must be numeric, but is "%s"
     */
    PARAMETER_NO_LONG("The parameter \"%s\" must be numeric, but is \"%s\"", CATEGORY_CONFIGURATION, 7);
    
    private String displayMessage;

    /**
     * Message of the exception.
     */
    private String message;

    /**
     * Category of the exception.
     */
    private Category category;

    /**
     * Detail number of the exception.
     */
    private int number;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private SpamhandlerSpamassassinConfigurationExceptionCode(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.category = category;
        this.number = detailNumber;
    }
    
    private SpamhandlerSpamassassinConfigurationExceptionCode(String message, Category category, int detailNumber) {
        this(message, null, category, detailNumber);
    }

    @Override
    public String getPrefix() {
        return "MSG";
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
    public int getNumber() {
        return number;
    }
    
    @Override
    public String getDisplayMessage() {
        return displayMessage;
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
