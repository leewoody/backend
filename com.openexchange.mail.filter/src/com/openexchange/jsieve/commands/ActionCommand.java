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

package com.openexchange.jsieve.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;
import com.openexchange.jsieve.commands.test.IActionCommand;

/**
 * An {@link ActionCommand} is an identifier followed by zero or more arguments,
 * terminated by a semicolon. Action commands do not take tests or blocks as
 * arguments.
 *
 * "<code>keep</code>", "<code>discard</code>", and "<code>redirect</code>" these require a require: "<code>reject</code>" and
 * "<code>fileinto</code>"
 *
 * <pre>reject &lt;reason: string&gt; fileinto &lt;folder: string&gt; redirect &lt;address: string&gt; keep discard<pre>
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ActionCommand extends ControlOrActionCommand {

    /**
     * <p>
     * Enum Arguments:
     * </p>
     * <ul>
     * <li>Command name</li>
     * <li>Minimum number of arguments</li>
     * <li>Tag arguments</li>
     * <li>JSON name</li>
     * <li>Required directive</li>
     * </ul>
     */
    public enum Commands implements IActionCommand {
        /**
         * <p>The "keep" action is whatever action is taken in lieu of all other
         * actions, if no filtering happens at all; generally, this simply means
         * to file the message into the user's main mailbox.</p>
         * <code>keep</code>
         * <p><a href="https://tools.ietf.org/html/rfc5228#section-4.3">RFC-5228: Action keep</a></p>
         */
        KEEP("keep", 0, new Hashtable<String, Integer>(), "keep", new ArrayList<String>()),
        /**
         * <p>Discard is used to silently throw away the message. It does so by
         * simply cancelling the <a href="https://tools.ietf.org/html/rfc5228#section-2.10.2">implicit keep</a>.</p>
         * <code>discard</code>
         * <p><a href="https://tools.ietf.org/html/rfc5228#section-4.4">RFC-5228: Action discard</a></p>
         */
        DISCARD("discard", 0, new Hashtable<String, Integer>(), "discard", new ArrayList<String>()),
        /**
         * <p>The "redirect" action is used to send the message to another user at
         * a supplied address, as a mail forwarding feature does.</p>
         * <code>redirect &lt;address: string&gt;</code>
         * <p><a href="https://tools.ietf.org/html/rfc5228#section-4.2">RFC-5228: Action redirect</p>
         */
        REDIRECT("redirect", 1, redirectTags(), "redirect", new ArrayList<String>()),
        /**
         * <p>The "fileinto" action delivers the message into the specified mailbox.</p>
         * <code>fileinto &lt;mailbox: string&gt;</code>
         * <p><a href="https://tools.ietf.org/html/rfc5228#section-4.1">RFC-5228: Action fileinto</p>
         */
        FILEINTO("fileinto", 1, fileintoTags(), "move", Collections.singletonList("fileinto")),
        /**
         * <p>The "reject" action cancels the implicit keep and refuses delivery of a message.</p>
         * <code>reject &lt;reason: string&gt;</code>
         * <p><a href="https://tools.ietf.org/html/rfc5429#section-2.2">RFC-5429: Action reject</a></p>
         */
        REJECT("reject", 1, new Hashtable<String, Integer>(), "reject", Collections.singletonList("reject")),
        /**
         * <p>The "stop" action ends all processing. If the implicit keep has not
         * been cancelled, then it is taken.</p>
         * <code>stop</code>
         * <p><a href="https://tools.ietf.org/html/rfc5228#section-3.3">RFC-5228: Control stop</a></p>
         */
        STOP("stop", 0, new Hashtable<String, Integer>(), "stop", new ArrayList<String>()),
        /**
         * <p>The "vacation" action implements a vacation autoresponder similar to
         * the vacation command available under many versions of Unix.</p>
         * <code>vacation [":days" number] [":subject" string] [":from" string] [":addresses" string-list] [":mime"] [":handle" string] &lt;reason: string&gt;</code>
         * <p><a href="https://tools.ietf.org/html/rfc5230#section-4">RFC-5230: Action vacation</a></p>
         */
        VACATION("vacation", 1, vacationTags(), "vacation", Collections.singletonList("vacation")),
        /**
         * <p>The "notify" action specifies that a notification should be sent to a user.</p>
         * <code>notify [":from" string] [":importance" &lt;"1" / "2" / "3"&gt;] [":options" string-list] [":message" string] &lt;method: string&gt;</code>
         * <p><a href="https://tools.ietf.org/html/rfc5435#section-3">RFC-5435: Action notify</a></p>
         */
        ENOTIFY("notify", 1, enotifyTags(), "notify", Collections.singletonList("enotify")),
        /**
         * <p>Setflag is used for setting [IMAP] system flags or keywords.
         * Setflag replaces any previously set flags.
         * <code>setflag [&lt;variablename: string&gt;] &lt;list-of-flags: string-list&gt;</code>
         * <p><a href="https://tools.ietf.org/html/rfc5232#section-3.1">RFC-5232: Action setflag</a></p>
         */
        SETFLAG("setflag", 1, new Hashtable<String, Integer>(), "setflags", Arrays.asList("imap4flags", "imapflags")),
        /**
         * <p>Addflag is used to add flags to a list of [IMAP] flags. It doesn't
         * replace any previously set flags. This means that multiple
         * occurrences of addflag are treated additively.</p>
         * <code>addflag [&lt;variablename: string&gt;] &lt;list-of-flags: string-list&gt;</code>
         * <p><a href="https://tools.ietf.org/html/rfc5232#section-3.2">RFC-5232: Action addflag</a></p>
         */
        ADDFLAG("addflag", 1, new Hashtable<String, Integer>(), "addflags", Arrays.asList("imap4flags", "imapflags")),
        /**
         * <p>Addflag is used to add flags to a list of [IMAP] flags. It doesn't
         * replace any previously set flags. This means that multiple
         * occurrences of addflag are treated additively.</p>
         * <code>removeflag [&lt;variablename: string&gt;] &lt;list-of-flags: string-list&gt;</code>
         * <p><a href="https://tools.ietf.org/html/rfc5232#section-3.3">RFC-5232: Action removeflag</a></p>
         */
        REMOVEFLAG("removeflag", 1, new Hashtable<String, Integer>(), "removeflags", Arrays.asList("imap4flags", "imapflags")),
        PGP_ENCRYPT("pgp_encrypt", 0, pgpEncryptTags(), "pgp", Collections.singletonList("vnd.dovecot.pgp-encrypt")),
        /**
         * <p>The addheader action adds a header field to the existing message header.</p>
         * <code>addheader [":last"] &lt;field-name: string&gt; &lt;value: string&gt;</code>
         * <p><a href="https://tools.ietf.org/html/rfc5293#section-4">RFC-5293: Action addheader</a></p>
         */
        ADDHEADER("addheader", 2, addHeaderTags(), "addheader", Collections.singletonList("editheader")),
        /**
         * <p>By default, the deleteheader action deletes all occurrences of the
         * named header field. The deleteheader action does not affect Sieve's
         * implicit keep.</p>
         * <code>deleteheader [":index" &lt;fieldno: number&gt; [":last"]] [COMPARATOR] [MATCH-TYPE] &lt;field-name: string&gt; [&lt;value-patterns: string-list&gt;]</code>
         * <p><a href="https://tools.ietf.org/html/rfc5293#section-5">RFC-5293: Action deleteheader</a></p>
         */
        DELETEHEADER("deleteheader", 1, deleteHeaderTags(), "deleteheader", Collections.singletonList("editheader")),
        /**
         * <p> The "set" action stores the specified value in the variable identified by name.</p>
         * <code>set [MODIFIER] &lt;name: string&gt; &lt;value: string&gt;</code>
         * <p><a href="https://tools.ietf.org/html/rfc5229#section-4">RFC-5229: Action set</a></p>
         */
        SET("set", 2, variablesTags(), "set", Collections.singletonList("variables"));

        /**
         * <p>
         * Syntax of the '<code>addheader</code>' action command and the position of the '<code>:last</code>'
         * tag as described in <a href="https://tools.ietf.org/html/rfc5293#secion-4">https://tools.ietf.org/html/rfc5293#section-4</a>:
         * </p>
         * <pre>"addheader" [":last"] &lt;field-name: string&gt; &lt;value: string&gt;</pre>
         *
         * @return a {@link Hashtable} With the ':last' tag
         */
        private static Hashtable<String, Integer> addHeaderTags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            retval.put(":last", Integer.valueOf(0));
            return retval;
        }

        /**
         * <p>
         * Syntax of the '<code>deleteheader</code>' action command and the position of the '<code>:last</code>'
         * tag as described in <a href="https://tools.ietf.org/html/rfc5293#secion-5">https://tools.ietf.org/html/rfc5293#section-5</a>:
         * </p>
         *
         * <!-- @formatter:off -->
         * <pre>
         * "deleteheader" [":index" &lt;fieldno: number&gt; [":last"]]
         *          [COMPARATOR] [MATCH-TYPE]
         *          &lt;field-name: string&gt;
         *          [&lt;value-patterns: string-list&gt;]
         * </pre>
         * <!-- @formatter:on -->
         *
         * @return an empty {@link Hashtable}
         */
        private static Hashtable<String, Integer> deleteHeaderTags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            return retval;
        }

        /**
         * <p>
         * Syntax of the modifiers as described in
         * <a href="https://tools.ietf.org/html/rfc5229#section-4.1">https://tools.ietf.org/html/rfc5229#section-4.1</a>:
         * </p>
         *
         * <pre>Usage: ":lower" / ":upper" / ":lowerfirst" / ":upperfirst" / ":quotewildcard" / ":length"</pre>
         *
         * @return an empty {@link Hashtable}
         */
        private static Hashtable<String, Integer> variablesTags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            return retval;
        }

        /**
         * <p>
         * Syntax of the '<code>vacation</code>' action command and the positions of the '<code>:days</code>',
         * '<code>:addresses</code>', '<code>:subject</code>' and '<code>:from</code>' tags
         * tag as described in <a href="https://tools.ietf.org/html/rfc5230#section-4">https://tools.ietf.org/html/rfc5230#section-4</a>:
         * </p>
         *
         * <!-- @formatter:off -->
         * <pre>
         *   Usage:   vacation [":days" number] [":subject" string]
         *           [":from" string] [":addresses" string-list]
         *           [":mime"] [":handle" string] &lt;reason: string&gt;
         *</pre>
         *<!-- @formatter:on -->
         *
         * Note: The tags :handle and :mime are intentionally left out because there's no way to deal with that
         * later in the frontend
         *
         * @return
         */
        private static Hashtable<String, Integer> vacationTags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            // The second parameter given the number of parameters which are
            // allowed after this tag
            // The tags :handle and :mime are intentionally left out because
            // there's no way to deal with that
            // later in the frontend
            retval.put(":days", Integer.valueOf(1));
            retval.put(":addresses", Integer.valueOf(1));
            retval.put(":subject", Integer.valueOf(1));
            retval.put(":from", Integer.valueOf(1));
            return retval;
        }

        /**
         * <p>
         * Syntax of the '<code>notify</code>' action command and the positions of the '<code>:message</code>'
         * tag as described in <a href="https://tools.ietf.org/html/rfc5435#section-3">https://tools.ietf.org/html/rfc5435#section-3</a>:
         * </p>
         *
         * <!-- @formatter:off -->
         * <pre>
         *    Usage:  notify [":from" string]
         *          [":importance" &lt;"1" / "2" / "3"&gt;]
         *          [":options" string-list]
         *          [":message" string]
         *          &lt;method: string&gt;
         * </pre>
         * <!-- @formatter:on -->
         * @return a {@link Hashtable} with the '<code>:message</code>' tag
         */
        private static Hashtable<String, Integer> enotifyTags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            retval.put(":message", Integer.valueOf(1));
            return retval;
        }

        private static Hashtable<String, Integer> redirectTags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            retval.put(":copy", Integer.valueOf(0));
            return retval;
        }

        private static Hashtable<String, Integer> fileintoTags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            retval.put(":copy", Integer.valueOf(0));
            return retval;
        }

        /**
         * @return a {@link Hashtable} with the '<code>:keys</code>' tag
         */
        private static Hashtable<String, Integer> pgpEncryptTags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            retval.put(":keys", Integer.valueOf(1));
            return retval;
        }

        /**
         * The number of arguments which this command takes at least
         */
        private final int minNumberOfArguments;

        /**
         * The name of the command
         */
        private final String commandName;

        /**
         * Defines if this command can take a match-type argument or not
         */
        private final Hashtable<String, Integer> tagArgs;

        /**
         * Defines what must be included for this command to run
         */
        private final List<String> required;

        /**
         * Stores the name of the parameter for the json object
         */
        private final String jsonName;

        /**
         * Initialises a new {@link Commands}.
         *
         * @param commandName The action command's name
         * @param minNumberOfArguments The minimum number of arguments
         * @param tagArgs The tag arguments
         * @param jsonName The json mappings
         * @param required The 'required'
         */
        Commands(final String commandName, final int minNumberOfArguments, final Hashtable<String, Integer> tagArgs, final String jsonName, final List<String> required) {
            this.commandName = commandName;
            this.minNumberOfArguments = minNumberOfArguments;
            this.tagArgs = tagArgs;
            this.required = (null == required || required.isEmpty()) ? new ArrayList<String>() : required;
            this.jsonName = jsonName;
        }

        /**
         * Returns the amount of minimum allowed arguments
         *
         * @return the amount of minimum allowed arguments
         */
        @Override
        public final int getMinNumberOfArguments() {
            return minNumberOfArguments;
        }

        /**
         * Returns the command's name
         *
         * @return the command's name
         */
        @Override
        public final String getCommandName() {
            return commandName;
        }

        /**
         * The JSON mapping of the command
         *
         * @return the jsonname
         */
        public final String getJsonName() {
            return jsonName;
        }

        /**
         * Returns a {@link List} with the required plugins
         *
         * @return a {@link List} with the required plugins
         */
        @Override
        public final List<String> getRequired() {
            return required;
        }

        /**
         * Returns a {@link Hashtable} with all the tag arguments
         *
         * @return a {@link Hashtable} with all the tag arguments
         */
        @Override
        public final Hashtable<String, Integer> getTagArgs() {
            return tagArgs;
        }

    }

    private final Commands command;

    private final List<String> optRequired = new ArrayList<>();

    /**
     * This Hashtable contains the tagargument and the corresponding value
     */
    private final Hashtable<String, List<String>> tagArguments;

    /**
     * Provides all types of arguments. The object here can either be a
     * TagArgument, a NumberArgument or an ArrayList<String>
     */
    private final ArrayList<Object> arguments;

    /**
     * Initialises a new {@link ActionCommand}.
     *
     * @param command The {@link Commands}
     * @param arguments An {@link ArrayList} with arguments
     * @throws SieveException if the arguments are incorrect
     */
    public ActionCommand(final Commands command, final ArrayList<Object> arguments) throws SieveException {
        this.command = command;
        this.arguments = arguments;
        this.tagArguments = new Hashtable<String, List<String>>();
        final int size = arguments.size();
        for (int i = 0; i < size; i++) {
            final Object object = arguments.get(i);
            if (object instanceof TagArgument) {
                final TagArgument tagArg = (TagArgument) object;
                final String tag = tagArg.getTag();
                // Check if an argument is allowed for this tag
                final Hashtable<String, Integer> tagArgs = this.command.getTagArgs();
                if (null != tagArgs && tagArgs.containsKey(tag) && 0 < tagArgs.get(tag)) {
                    // Get next element check if it is a list and insert
                    final Object object2 = arguments.get(++i);
                    if (object2 instanceof List) {
                        final List<String> list = (List<String>) object2;
                        this.tagArguments.put(tag, list);
                    } else if (object2 instanceof NumberArgument) {
                        final ArrayList<String> arrayList = new ArrayList<String>();
                        arrayList.add((String.valueOf(((NumberArgument) object2).getInteger())));
                        this.tagArguments.put(tag, arrayList);
                    } else {
                        throw new SieveException("No right argument for tag " + tag + " found.");
                    }
                } else {
                    this.tagArguments.put(tag, new ArrayList<String>());
                }
            } else {
                for (String tag : command.getTagArgs().keySet()) {
                    if (command.getTagArgs().get(tag) > 0 && i == 0) {
                        throw new SieveException("The main arguments have to stand after the tag argument in the rule: " + this.toString());
                    }
                }
            }
        }
        checkCommand();
    }

    /**
     * Checks if this command has the right arguments
     *
     * @throws SieveException
     */
    private void checkCommand() throws SieveException {
        if (null != this.tagArguments) {
            // This complicated copying is needed because there's no way in java
            // to clone a set. And because the set
            // is backed up by the Hshtable we would otherwise delete the
            // elements in the Hashtable.
            final Set<String> tagArray = this.tagArguments.keySet();
            final Set<String> tagArgs = this.command.getTagArgs().keySet();
            final ArrayList<String> rest = new ArrayList<String>();
            for (final String string : tagArray) {
                if (!tagArgs.contains(string)) {
                    rest.add(string);
                }
            }
            // if (null != tagarray) {
            // tagarray.removeAll(tagargs);
            // }
            if (!rest.isEmpty()) {
                throw new SieveException("One of the tagarguments: " + rest + " is not valid for " + this.command.getCommandName());
            }
        }
        final int countTags = countTags();

        if (null != this.arguments && this.command.getMinNumberOfArguments() >= 0 && (this.arguments.size() - countTags) < this.command.getMinNumberOfArguments()) {
            throw new SieveException("The number of arguments for " + this.command.getCommandName() + " is not valid. ; " + this.toString());
        }
    }

    /**
     * This method count the arguments inside the tags
     *
     * @return
     */
    private int countTags() {
        int i = 0;
        for (final List<String> list : this.tagArguments.values()) {
            if (list.isEmpty()) {
                i++;
            } else {
                i = i + 2;
            }
        }
        return i;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " : " + this.command.getCommandName() + " : " + this.tagArguments + " : " + this.arguments;
    }

    public final Commands getCommand() {
        return command;
    }

    public final Hashtable<String, List<String>> getTagArguments() {
        return tagArguments;
    }

    /**
     * With this method you can get the argument to the given tag. E.g. for
     * <code>vacation :days 1 "Test"</code>
     *
     * The following <code>getArgumentToTag(":days");</code> returns
     * <code>["1"]</code>
     *
     *
     * @param tag
     * @return
     */
    public final List<String> getArgumentToTag(final String tag) {
        return this.tagArguments.get(tag);
    }

    public final ArrayList<Object> getArguments() {
        return arguments;
    }

    @Override
    public HashSet<String> getRequired() {
        HashSet<String> result = new HashSet<String>();
        result.addAll(optRequired);
        result.addAll(this.command.getRequired() == null ? new HashSet<String>() : this.command.getRequired());
        return result;
    }

    @Override
    public void addOptionalRequired(String required) {
        this.optRequired.add(required);
    }
}
