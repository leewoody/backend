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

package com.openexchange.drive.client.windows.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.drive.BrandedDriveVersionService;
import com.openexchange.drive.client.windows.files.UpdateFilesProvider;
import com.openexchange.drive.client.windows.files.UpdateFilesProviderImpl;
import com.openexchange.drive.client.windows.service.Constants;
import com.openexchange.drive.client.windows.service.DriveUpdateService;
import com.openexchange.drive.client.windows.service.internal.BrandingConfigurationRemoteImpl;
import com.openexchange.drive.client.windows.service.internal.DriveUpdateServiceImpl;
import com.openexchange.drive.client.windows.service.internal.Services;
import com.openexchange.drive.client.windows.service.rmi.BrandingConfigurationRemote;
import com.openexchange.drive.client.windows.servlet.DownloadServlet;
import com.openexchange.drive.client.windows.servlet.InstallServlet;
import com.openexchange.drive.client.windows.servlet.UpdatesXMLServlet;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

/**
 *
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class Activator extends HousekeepingActivator {

    private String downloadServletAlias;
    private String updateServletAlias;
    private String installServletAlias;
    private ServiceRegistration<Remote> serviceRegistration;

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { TemplateService.class, ConfigurationService.class, ContextService.class, UserService.class,
            UserConfigurationService.class, HttpService.class, CapabilityService.class, DispatcherPrefixService.class,
            ConfigViewFactory.class, BrandedDriveVersionService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        Services.setServiceLookup(this);
        DriveUpdateService updateService = new DriveUpdateServiceImpl();

        //register files provider
        final ConfigurationService config = getService(ConfigurationService.class);
        String path = config.getProperty(Constants.BRANDINGS_PATH);
        UpdateFilesProvider fileProvider = UpdateFilesProviderImpl.getInstance().init(path);
        updateService.init(fileProvider);

        registerService(DriveUpdateService.class, updateService, null);

        //register download servlet
        DownloadServlet downloadServlet = new DownloadServlet(updateService, fileProvider);
        String prefix = getService(DispatcherPrefixService.class).getPrefix();
        downloadServletAlias = prefix + Constants.DOWNLOAD_SERVLET;
        getService(HttpService.class).registerServlet(downloadServletAlias, downloadServlet, null, null);

        //register update servlet
        updateServletAlias = prefix + Constants.UPDATE_SERVLET;
        final TemplateService templateService = getService(TemplateService.class);
        getService(HttpService.class).registerServlet(updateServletAlias, new UpdatesXMLServlet(templateService, updateService), null, null);

        //register install servlet
        installServletAlias = prefix + Constants.INSTALL_SERVLET;
        getService(HttpService.class).registerServlet(installServletAlias, new InstallServlet(updateService), null, null);

        //register rmi interface
        Dictionary<String, Object> props = new Hashtable<String, Object>(2);
        props.put("RMIName", BrandingConfigurationRemote.RMI_NAME);
        serviceRegistration = context.registerService(Remote.class, new BrandingConfigurationRemoteImpl(), props);

    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        HttpService httpService = getService(HttpService.class);
        if (httpService != null) {
            if (downloadServletAlias != null) {
                httpService.unregister(downloadServletAlias);
                downloadServletAlias = null;
            }
            if (updateServletAlias != null) {
                httpService.unregister(updateServletAlias);
                updateServletAlias = null;
            }
            if (installServletAlias != null) {
                httpService.unregister(installServletAlias);
                installServletAlias = null;
            }
        }
        Services.setServiceLookup(null);
        super.stopBundle();
    }
}
