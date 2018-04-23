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

package com.openexchange.rest.services.configuration;

import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * The {@link ConfigurationRESTService} allows clients to retrieve configuration values.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.0
 */
@Path("/preliminary/configuration/v1")
@RoleAllowed(Role.BASIC_AUTHENTICATED)
public class ConfigurationRESTService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ConfigurationRESTService}.
     *
     * @param services
     */
    public ConfigurationRESTService(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * GET /rest/configuration/property/com.openexchange.some.property
     * Retrieves the value of a named property. Return an Object with a property to value mapping or a status 404 if a property is not set.
     */
    @GET
    @Path("/property/{property}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getProperty(@PathParam("property") String property) throws OXException {
        return getProperty(property, -1, -1);
    }

    /**
     * GET /rest/configuration/property/com.openexchange.some.property/[contextId]/[userId]
     * Retrieves the value of a named property as configured for the given context and user.
     * Return an Object with a property to value mapping or a status 404 if a property is not set.
     */
    @GET
    @Path("/property/{property}/{context}/{user}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getProperty(@PathParam("property") String property, @PathParam("context") int context, @PathParam("user") int user) throws OXException {
        ConfigViewFactory factory = getConfigViewFactory();
        ConfigView view = factory.getView(user, context);

        ComposedConfigProperty<String> p = view.property(property, String.class);
        try {
            if (p.isDefined()) {
                return new JSONObject().put(property, p.get());
            } else {
                throw new NotFoundException();
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        } catch (OXException e) {
            throw handleNotFoundException(e);
        }
    }

    /**
     * GET /rest/configuration/withPrefix/com.openexchange.mymodule
     * Retrieves all properties with a given prefix, e.g. all properties starting with com.openexchange.mymodule as an Object.
     */
    @GET
    @Path("/withPrefix/{prefix}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getWithPrefix(@PathParam("prefix") String prefix) throws OXException {
        return getWithPrefix(prefix, -1, -1);
    }

    /**
     * GET /rest/configuration/withPrefix/com.openexchange.mymodule/[contextId]/[userId]
     * Retrieves all properties with a given prefix for a given context and user, e.g. all properties starting with com.openexchange.mymodule as an Object.
     */
    @GET
    @Path("/withPrefix/{prefix}/{context}/{user}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getWithPrefix(@PathParam("prefix") String prefix, @PathParam("context") int context, @PathParam("user") int user) throws OXException {
        ConfigViewFactory factory = getConfigViewFactory();
        ConfigView view = factory.getView(user, context);
        JSONObject json = new JSONObject();
        try {
            Map<String, ComposedConfigProperty<String>> all = view.all();

            for (Map.Entry<String, ComposedConfigProperty<String>> entry : all.entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    json.put(entry.getKey(), entry.getValue().get());
                }
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        } catch (OXException e) {
            throw handleNotFoundException(e);
        }

        return json;
    }

    /**
     * Get the ConfigViewFactory
     *
     * @return the ConfigViewFactory
     */
    private ConfigViewFactory getConfigViewFactory() {
        ConfigViewFactory configViewFactory = services.getService(ConfigViewFactory.class);
        if (configViewFactory == null) {
            throw new ServiceUnavailableException("ConfigViewFactory is absent!");
        }

        return configViewFactory;
    }

    /**
     * Handle the UserNotFound or the ContextNotFound exceptions.
     *
     * @param The OXException
     * @return NotFoundException or InternalServerExcetpion
     */
    private WebApplicationException handleNotFoundException(OXException e) {
        if (UserExceptionCode.USER_NOT_FOUND.equals(e) || ContextExceptionCodes.NOT_FOUND.equals(e)) {
            return new NotFoundException();
        } else {
            return new InternalServerErrorException();
        }
    }
}
