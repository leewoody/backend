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

package com.openexchange.client.onboarding.clt;

import java.rmi.RemoteException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.cli.OutputHelper;
import com.openexchange.client.onboarding.rmi.RemoteOnboardingService;

/**
 * {@link ListOnboardingProvidersTool}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ListOnboardingProvidersTool extends AbstractRmiCLI<Void> {

    /**
     * Invokes this command-line tool
     *
     * @param args The arguments
     */
    public static void main(String[] args) {
        new ListOnboardingProvidersTool().execute(args);
    }

    // --------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ListOnboardingProvidersTool}.
     */
    private ListOnboardingProvidersTool() {
        super();
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        // No more options needed
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        RemoteOnboardingService rmiService = getRmiStub(optRmiHostName, RemoteOnboardingService.RMI_NAME);
        List<List<String>> data = rmiService.getAllProviders();
        OutputHelper.doOutput(new String[] { "l", "l", "l", "l" }, new String[] { "Provider", "Description", "Supported types", "Supported devices" }, data);
        return null;
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected void checkOptions(CommandLine cmd, Options options) {
        // Nothing to check
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        checkOptions(cmd, null);
    }

    @Override
    protected String getFooter() {
        return "The command-line tool for listing available on-boarding providers";
    }

    @Override
    protected String getName() {
        return "listonboardingproviders";
    }

}
