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

package com.openexchange.realtime.events;

import java.util.Collection;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;


/**
 * {@link RTEventMatcher}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RTEventMatcher extends BaseMatcher<Collection<PayloadTree>> {

    private String eventName;
    private Object payload;
    
    public static RTEventMatcher isRTEvent(String eventName, Object payload) {
        return new RTEventMatcher(eventName, payload);
    }

    public RTEventMatcher(String eventName, Object payload) {
        super();
        this.eventName = eventName;
        this.payload = payload;
    }

    @Override
    public boolean matches(Object arg0) {
        Collection<PayloadTree> payloads = (Collection<PayloadTree>) arg0;
        
        if (payloads.size() != 1) {
            return false;
        }
        
        PayloadTree payloadTree = payloads.iterator().next();
        
        boolean foundName = false;
        boolean foundData = false;
        
        Collection<PayloadTreeNode> children = payloadTree.getRoot().getChildren();
        for (PayloadTreeNode child : children) {
            if (child.getNamespace().equals("event") && child.getElementName().equals("name")) {
                foundName = true;
                if (!child.getData().equals(eventName)) {
                    return false;
                }
            } else if (child.getNamespace().equals("event") && child.getElementName().equals("data")) {
                foundData = true;
                if (payload instanceof Matcher) {
                    Matcher payloadMatcher = (Matcher) payload;
                    if (!payloadMatcher.matches(child.getPayloadElement())) {
                        return false;
                    }
                } else {
                    if (!child.getPayloadElement().getData().equals(payload)) {
                        return false;
                    }
                }
            }
            
        }
        
        return foundData && foundName;
    }

    @Override
    public void describeTo(Description description) {

    }

}