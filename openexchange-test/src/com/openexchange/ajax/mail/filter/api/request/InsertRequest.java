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

package com.openexchange.ajax.mail.filter.api.request;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.parser.InsertParser;
import com.openexchange.ajax.mail.filter.api.response.InsertResponse;

/**
 * {@link InsertRequest}. Stores the parameters for inserting the appointment.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class InsertRequest extends AbstractMailFilterRequest<InsertResponse> {

    /**
     * Rule to insert.
     */
    final Rule rule;

    /**
     * The affected user
     */
    final String forUser;

    /**
     * Should the parser fail on error in server response.
     */
    final boolean failOnError;

    /**
     * Initialises a new {@link InsertRequest}.
     * 
     * @param rule The {@link Rule} to insert
     */
    public InsertRequest(Rule rule) {
        this(rule, null, true);
    }

    /**
     * Initialises a new {@link InsertRequest}.
     * 
     * @param rule The {@link Rule} to insert
     * @param failOnError The fail on error flag
     */
    public InsertRequest(Rule rule, boolean failOnError) {
        this(rule, null, failOnError);
    }

    /**
     * default constructor.
     *
     * @param rule Rule to insert. <code>true</code> to check the response for error messages.
     */
    public InsertRequest(final Rule rule, final String forUser) {
        this(rule, forUser, true);
    }

    /**
     * More detailed constructor.
     *
     * @param rule Rule to insert.
     * @param failOnError <code>true</code> to check the response for error messages.
     */
    public InsertRequest(final Rule rule, final String forUser, final boolean failOnError) {
        super();
        this.rule = rule;
        this.forUser = forUser;
        this.failOnError = failOnError;
    }

    /*
     * @Override(non-Javadoc)
     * 
     * @see com.openexchange.ajax.framework.AJAXRequest#getBody()
     */
    public Object getBody() throws JSONException {
        return convert(rule);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ajax.framework.AJAXRequest#getMethod()
     */
    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ajax.framework.AJAXRequest#getParameters()
     */
    @Override
    public Parameter[] getParameters() {
        List<Parameter> parameters = new LinkedList<Parameter>();
        parameters.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW));
        if (forUser != null) {
            parameters.add(new Parameter("username", forUser));
        }
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ajax.framework.AJAXRequest#getParser()
     */
    @Override
    public AbstractAJAXParser<InsertResponse> getParser() {
        return new InsertParser(failOnError);
    }
}
