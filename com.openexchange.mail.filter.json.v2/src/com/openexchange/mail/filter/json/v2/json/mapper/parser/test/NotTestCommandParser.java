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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH.
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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.test;

import java.util.ArrayList;
import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.TestCommand.Commands;
import com.openexchange.mail.filter.json.v2.json.fields.GeneralField;
import com.openexchange.mail.filter.json.v2.json.fields.NotTestField;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.CommandParserRegistry;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParser;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.TestCommandParserRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NotTestCommandParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public class NotTestCommandParser extends AbstractTestCommandParser {

    /**
     * Initializes a new {@link NotTestCommandParser}.
     */
    public NotTestCommandParser(ServiceLookup services) {
        super(services, Commands.NOT);
    }

    @Override
    public TestCommand parse(JSONObject jsonObject, ServerSession session) throws JSONException, SieveException, OXException {
        final List<Object> argList = new ArrayList<Object>();
        ArrayList<TestCommand> testcommands = new ArrayList<TestCommand>();

        final JSONObject innerJsonObject = jsonObject.optJSONObject(NotTestField.test.name());
        if (null != innerJsonObject) {
            CommandParserRegistry<TestCommand, TestCommandParser<TestCommand>> parserRegistry = services.getService(TestCommandParserRegistry.class);
            CommandParser<TestCommand> parser = parserRegistry.get(innerJsonObject.optString(GeneralField.id.name()));
            testcommands.add(parser.parse(innerJsonObject, session));
        }
        return new TestCommand(TestCommand.Commands.NOT, argList, testcommands);
    }

    @Override
    public void parse(JSONObject jsonObject, TestCommand command, boolean transformToNotMatcher) throws JSONException, OXException {
        TestCommand nestedTestCommand = command.getTestCommands().get(0);
        CommandParserRegistry<TestCommand, TestCommandParser<TestCommand>> parserRegistry = services.getService(TestCommandParserRegistry.class);
        TestCommandParser<TestCommand> parser = parserRegistry.get(nestedTestCommand.getCommand().getCommandName());
        parser.parse(jsonObject, nestedTestCommand, transformToNotMatcher == false);
    }
}
