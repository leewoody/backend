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

package com.openexchange.contact.vcard.storage.impl;

import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultVCardStorageFactory}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class DefaultVCardStorageFactory implements VCardStorageFactory {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultVCardStorageFactory.class);

    private final VCardStorageService vCardStorageService;

    public DefaultVCardStorageFactory(VCardStorageService vCardStorageService) {
        this.vCardStorageService = vCardStorageService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VCardStorageService getVCardStorageService(ConfigViewFactory configViewFactory, int contextId) {
        try {
            for (String capability : this.vCardStorageService.neededCapabilities()) {
                ConfigProperty<Boolean> filestoreCapability = configViewFactory.getView(-1, contextId).property(capability, boolean.class);
                if (!filestoreCapability.get().booleanValue()) {
                    LOG.info("Needed capability '{}' not available for context id {}. Unable to handle VCard for storage.", capability, contextId);
                    return null;
                }
            }
        } catch (OXException oxException) {
            LOG.warn("Unable to return requested VCardStorageService implementation.", oxException);
            return null;
        }
        return vCardStorageService;
    }
}