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

package com.openexchange.publish.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;


/**
 * {@link PublicationTargetCollector}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PublicationTargetCollector implements PublicationTargetDiscoveryService {

    private final Map<String, PublicationTarget> targets = new HashMap<String, PublicationTarget>();

    @Override
    public PublicationTarget getTarget(String id) {
        return targets.get(id);
    }

    @Override
    public PublicationTarget getTarget(Context context, int publicationId) throws OXException {
        for(PublicationTarget target : targets.values()) {
            if(target.getPublicationService().knows(context, publicationId)) {
                return target;
            }
        }
        return null;
    }

    @Override
    public Collection<PublicationTarget> getTargetsForEntityType(String module) {
        ArrayList<PublicationTarget> responsible = new ArrayList<PublicationTarget>();
        for(PublicationTarget target : targets.values()) {
            if(target.isResponsibleFor(module)) {
                responsible.add(target);
            }
        }
        return responsible;
    }

    @Override
    public boolean knows(String id) {
        return targets.containsKey(id);
    }

    @Override
    public Collection<PublicationTarget> listTargets() {
        return targets.values();
    }

    public void addPublicationService(PublicationService publicationService) throws OXException {
        targets.put(publicationService.getTarget().getId(), publicationService.getTarget());
    }

    public void removePublicationService(PublicationService publicationService) throws OXException {
        targets.remove(publicationService.getTarget().getId());
    }

    public void clear() {
        targets.clear();
    }

}