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

package com.openexchange.admin.console.user;

import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Delete extends DeleteCore {

    private static final String REASSIGN_LONG = "reassign";
    private static final char REASSIGN_SHORT = 'r';
    private static final String NO_REASSIGN_LONG = "no-reassign";
    private CLIOption reassignCLI;
    private CLIOption noReassignCLI;

    public static void main(final String[] args) {
        new Delete(args);
    }

    public Delete(final String[] args2) {

        final AdminParser parser = new AdminParser("deleteuser");
        commonfunctions(parser, args2);
    }

    @Override
    protected void maincall(final AdminParser parser, final OXUserInterface oxusr, final Context ctx, final User usr, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        Integer destUser = null;
        if (parser.hasOption(noReassignCLI)) {
            destUser = 0;
        } else {
            Object o = parser.getOptionValue(reassignCLI);
            if (o != null) {
                if (o instanceof String) {
                    destUser = Integer.valueOf((String) o);
                } else {
                    destUser = Integer.valueOf((int) o);
                }
            }
        }
        oxusr.delete(ctx, usr, destUser, auth);
    }

    @Override
    protected void setFurtherOptions(AdminParser parser) {

        reassignCLI = parser.addOption(REASSIGN_SHORT, REASSIGN_LONG, "The user id shared data will be assigned to. If omitted the context admin will be used instead.", false);
        noReassignCLI = parser.addSettableBooleanOption(NO_REASSIGN_LONG, null, "If set all shared data will be deleted instead of being assigned.", false, false, false);
    }
}
