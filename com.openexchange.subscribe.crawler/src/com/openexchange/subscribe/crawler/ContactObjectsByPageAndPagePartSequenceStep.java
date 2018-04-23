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

package com.openexchange.subscribe.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.ContactSanitizer;
import com.openexchange.subscribe.crawler.internal.Mappings;
import com.openexchange.subscribe.crawler.internal.PagePartSequence;

/**
 * {@link ContactObjectsByPageAndPagePartSequenceStep}
 *
 * This returns
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ContactObjectsByPageAndPagePartSequenceStep extends AbstractStep<Contact[], HtmlPage> {

    private static final ContactSanitizer SANITIZER = new ContactSanitizer();

    private PagePartSequence pageParts;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactObjectsByPageAndPagePartSequenceStep.class);

    public ContactObjectsByPageAndPagePartSequenceStep() {

    }

    public ContactObjectsByPageAndPagePartSequenceStep(final String description, final PagePartSequence pageParts) {
        this.description = description;
        this.pageParts = pageParts;
    }

    /* (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.AbstractStep#execute(com.gargoylesoftware.htmlunit.WebClient)
     */
    @Override
    public void execute(WebClient webClient) throws OXException {
        final List<Contact> contactObjects = new ArrayList<Contact>();
            try {
                final String pageString = StringEscapeUtils.unescapeHtml(input.getWebResponse().getContentAsString());
                pageParts.setPage(pageString);
                LOG.debug("Page evaluated is : {}", pageString);
                final Collection<HashMap<String, String>> maps = pageParts.retrieveMultipleInformation();

                for (HashMap<String, String> map : maps){
                    final Contact contact = Mappings.translateMapToContact(map);
                    SANITIZER.sanitize(contact);
                    contactObjects.add(contact);
                }

            } catch (final OXException e) {
                LOG.error("{} for Context : {}, User : {}, Folder : {}.", e.getMessage(), workflow.getSubscription().getContext().getContextId(), workflow.getSubscription().getUserId(), workflow.getSubscription().getFolderId());

                exception = e;
            }
            executedSuccessfully = true;


        if (!executedSuccessfully && input != null){

        }
        output = contactObjects.toArray(new Contact[contactObjects.size()]);
    }

    public PagePartSequence getPageParts() {
        return pageParts;
    }

    public void setPageParts(final PagePartSequence pageParts) {
        this.pageParts = pageParts;
    }
}
