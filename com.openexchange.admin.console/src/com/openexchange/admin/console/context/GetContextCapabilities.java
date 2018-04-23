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

package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.TreeSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;


/**
 * {@link GetContextCapabilities} - Determines the capabilities for a context.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.0
 */
public class GetContextCapabilities extends ContextAbstraction {

    /**
     * Initializes a new {@link GetContextCapabilities}.
     *
     * @param args The CLI arguments
     */
    public GetContextCapabilities(String[] args) {
        final AdminParser parser = new AdminParser("getcontextcapabilities");
        commonfunctions(parser, args);
    }

    protected void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setContextOption(parser, NeededQuadState.eitheror);
        setContextNameOption(parser, NeededQuadState.eitheror);
    }

    protected final void commonfunctions(final AdminParser parser, final String[] args) {
        setOptions(parser);

        String successtext = null;
        try {
            Context ctx = null;
            Credentials auth = null;
            try {
                parser.ownparse(args);

                ctx = contextparsing(parser);

                auth = credentialsparsing(parser);

                parseAndSetContextName(parser, ctx);

                successtext = nameOrIdSetInt(this.ctxid, this.contextname, "context");

            } catch (final RuntimeException e) {
                printError(null, null, e.getClass().getSimpleName() + ": " + e.getMessage(), parser);
                sysexit(1);
            }
            Set<String> caps = maincall(ctx, auth);

            if (null == caps || caps.isEmpty()) {
                System.out.println("There are no capabilities set for context " + ctx.getId());
            } else {
                final String lf = System.getProperty("line.separator");

                final StringBuilder sb = new StringBuilder(2048);
                sb.append("Capabilities for context ").append(ctx.getId()).append(":").append(lf);

                for (final String cap : new TreeSet<String>(caps)) {
                    sb.append(cap).append(lf);
                }

                System.out.println(sb.toString());
            }

        } catch (final Exception e) {
            printErrors(successtext, null, e, parser);
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        }
    }

    private Set<String> maincall(Context ctx, Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, InvalidCredentialsException, StorageException, NoSuchContextException, InvalidDataException {
        final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);
        return oxres.getCapabilities(ctx, auth);
    }

    public static void main(String[] args) {
        new GetContextCapabilities(args);
    }
}
