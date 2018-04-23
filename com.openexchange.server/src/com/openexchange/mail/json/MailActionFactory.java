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

package com.openexchange.mail.json;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXState;
import com.openexchange.ajax.requesthandler.AJAXStateHandler;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.json.actions.AbstractMailAction;
import com.openexchange.mail.json.actions.AllAction;
import com.openexchange.mail.json.actions.AllSeenAction;
import com.openexchange.mail.json.actions.ArchiveAction;
import com.openexchange.mail.json.actions.ArchiveFolderAction;
import com.openexchange.mail.json.actions.AutosaveAction;
import com.openexchange.mail.json.actions.BounceAction;
import com.openexchange.mail.json.actions.ClearAction;
import com.openexchange.mail.json.actions.CopyAction;
import com.openexchange.mail.json.actions.DeleteAction;
import com.openexchange.mail.json.actions.EditAction;
import com.openexchange.mail.json.actions.ExamineAction;
import com.openexchange.mail.json.actions.ExpungeAction;
import com.openexchange.mail.json.actions.GetAction;
import com.openexchange.mail.json.actions.GetAttachmentAction;
import com.openexchange.mail.json.actions.GetAttachmentTokenAction;
import com.openexchange.mail.json.actions.GetForwardAction;
import com.openexchange.mail.json.actions.GetMailCountAction;
import com.openexchange.mail.json.actions.GetMultipleAttachmentAction;
import com.openexchange.mail.json.actions.GetMultipleMessagesAction;
import com.openexchange.mail.json.actions.GetReplyAction;
import com.openexchange.mail.json.actions.GetReplyAllAction;
import com.openexchange.mail.json.actions.GetStructureAction;
import com.openexchange.mail.json.actions.GetUpdatesAction;
import com.openexchange.mail.json.actions.GetVersitAction;
import com.openexchange.mail.json.actions.ImportAction;
import com.openexchange.mail.json.actions.ListAction;
import com.openexchange.mail.json.actions.MoveAllAction;
import com.openexchange.mail.json.actions.NewAction;
import com.openexchange.mail.json.actions.ReceiptAckAction;
import com.openexchange.mail.json.actions.ResendAction;
import com.openexchange.mail.json.actions.ResolveShareReference;
import com.openexchange.mail.json.actions.SearchAction;
import com.openexchange.mail.json.actions.SendDataAction;
import com.openexchange.mail.json.actions.SimpleThreadStructureAction;
import com.openexchange.mail.json.actions.ThreadReferencesAction;
import com.openexchange.mail.json.actions.TransportMailAction;
import com.openexchange.mail.json.actions.TrashAction;
import com.openexchange.mail.json.actions.UpdateAction;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MailActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OAuthModule
public class MailActionFactory implements AJAXActionServiceFactory, AJAXStateHandler, MailActionConstants {

    private static final AtomicReference<MailActionFactory> INSTANCE_REFERENCE = new AtomicReference<MailActionFactory>();

    /**
     * Gets the action factory
     *
     * @return The action factory or <code>null</code>
     */
    public static MailActionFactory getActionFactory() {
        return INSTANCE_REFERENCE.get();
    }

    /**
     * Initializes the action factory instance
     *
     * @param services The service look-up
     * @return The initialized instance
     */
    public static MailActionFactory initializeActionFactory(ServiceLookup services) {
        MailActionFactory actionFactory = new MailActionFactory(services);
        INSTANCE_REFERENCE.set(actionFactory);
        return actionFactory;
    }

    /**
     * Releases the action factory instance.
     */
    public static void releaseActionFactory() {
        INSTANCE_REFERENCE.set(null);
    }

    // ----------------------------------------------------------------------------------------------

    private final Map<String, AbstractMailAction> actions;

    /**
     * Initializes a new {@link MailActionFactory}.
     *
     * @param services The service look-up
     */
    private MailActionFactory(final ServiceLookup services) {
        super();
        ImmutableMap.Builder<String, AbstractMailAction> builder = ImmutableMap.builder();
        builder.put("all", new AllAction(services));
        builder.put("threadedAll", new SimpleThreadStructureAction(services));
        builder.put("get", new GetAction(services));
        builder.put("get_structure", new GetStructureAction(services));
        builder.put("count", new GetMailCountAction(services));
        builder.put("copy", new CopyAction(services));
        builder.put("move_all", new MoveAllAction(services));
        builder.put("archive", new ArchiveAction(services));
        builder.put("archive_folder", new ArchiveFolderAction(services));
        builder.put("reply", new GetReplyAction(services));
        builder.put("replyall", new GetReplyAllAction(services));
        builder.put("updates", new GetUpdatesAction(services));
        builder.put("forward", new GetForwardAction(services));
        builder.put("bounce", new BounceAction(services));
        builder.put("resend", new ResendAction(services));
        builder.put("attachment", new GetAttachmentAction(services));
        builder.put("attachmentToken", new GetAttachmentTokenAction(services));
        builder.put("zip_attachments", new GetMultipleAttachmentAction(services));
        builder.put("zip_messages", new GetMultipleMessagesAction(services));
        builder.put("saveVersit", new GetVersitAction(services));

        builder.put("list", new ListAction(services));
        builder.put("search", new SearchAction(services));
        builder.put("update", new UpdateAction(services));
        builder.put("delete", new DeleteAction(services));
        builder.put("transport", new TransportMailAction(services));
        builder.put("receipt_ack", new ReceiptAckAction(services));
        builder.put("clear", new ClearAction(services));
        builder.put("expunge", new ExpungeAction(services));
        NewAction newAction = new NewAction(services);
        builder.put("new", newAction);
        builder.put("send_data", new SendDataAction(newAction, services));
        builder.put("import", new ImportAction(services));
        builder.put("edit", new EditAction(services));
        builder.put("autosave", new AutosaveAction(services));

        builder.put("all_seen", new AllSeenAction(services));
        builder.put("resolve_share_reference", new ResolveShareReference(services));
        builder.put("examine", new ExamineAction(services));
        builder.put("thread_references", new ThreadReferencesAction(services));
        builder.put("trash", new TrashAction(services));

        this.actions = builder.build();
    }

    @Override
    public Collection<? extends AJAXActionService> getSupportedServices() {
        return java.util.Collections.unmodifiableCollection(actions.values());
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }

    @Override
    public void initialize(final AJAXState state) throws OXException {
        // Nope
    }

    @Override
    public void cleanUp(final AJAXState state) throws OXException {
        /*
         * Drop possibly opened mail access instances
         */
        final MailServletInterface mailInterface = state.removeProperty(PROPERTY_MAIL_IFACE);
        if (null != mailInterface) {
            mailInterface.close(true);
        }
        Streams.close(state.<Collection<Closeable>>removeProperty(PROPERTY_CLOSEABLES));
    }

}
