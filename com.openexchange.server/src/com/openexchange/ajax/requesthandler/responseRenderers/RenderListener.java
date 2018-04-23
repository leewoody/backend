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

package com.openexchange.ajax.requesthandler.responseRenderers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.HttpErrorCodeException;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.exception.OXException;

/**
 * {@link RenderListener} - A listener which receives various call-backs before/after a {@link ResponseRenderer#write(AJAXRequestData, AJAXRequestResult, HttpServletRequest, HttpServletResponse)} processing.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public interface RenderListener {

    /**
     * Checks whether this render listener wants to receive call-backs for given request data.
     *
     * @param request The associated request data
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     */
    boolean handles(AJAXRequestData request);

    /**
     * Called before the write operation of the {@link ResponseRenderer} is invoked.
     *
     * @param request The associated request data
     * @param result The result
     * @param req The HTTP request
     * @param resp The HTTP response
     * @throws OXException If this listener signals to abort further processing
     * @see ResponseRenderer#write(AJAXRequestData, AJAXRequestResult, HttpServletRequest, HttpServletResponse)
     */
    void onBeforeWrite(AJAXRequestData request, AJAXRequestResult result, HttpServletRequest req, HttpServletResponse resp) throws OXException;

    /**
     * Called after the write operation of the {@link ResponseRenderer} is invoked.
     *
     * @param request The associated request data
     * @param result The request result that has been created
     * @param writeException The optional exception instance in case actual write yielded an error;
     *                       in case no <code>"200 - OK"</code> status was set an instance of {@link HttpErrorCodeException} is passed
     * @throws OXException If this listener signals to abort further processing
     */
    void onAfterWrite(AJAXRequestData request, AJAXRequestResult result, Exception writeException) throws OXException;
}
