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

package com.openexchange.groupware.infostore.webdav;

import static com.openexchange.file.storage.FileStorageEventHelper.createDebugMessage;
import static com.openexchange.file.storage.FileStorageEventHelper.extractObjectId;
import static com.openexchange.file.storage.FileStorageEventHelper.extractSession;
import static com.openexchange.file.storage.FileStorageEventHelper.isInfostoreEvent;
import static com.openexchange.file.storage.FileStorageEventHelper.isUpdateEvent;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.event.impl.FolderEventInterface;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

public class PropertyCleaner implements FolderEventInterface, EventHandler {

	private final PropertyStore infoProperties;
	private final PropertyStore folderProperties;

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PropertyCleaner.class);

	public PropertyCleaner(final PropertyStore folderProperties, final PropertyStore infoProperties){
		this.folderProperties = folderProperties;
		this.infoProperties = infoProperties;

	}

	@Override
    public void folderCreated(final FolderObject folderObj, final Session sessionObj) {
        // Nothing to do
	}

	@Override
    public void folderDeleted(final FolderObject folderObj, final Session session) {
		try {
            final ServerSession sessionObj = ServerSessionAdapter.valueOf(session);
            folderProperties.startTransaction();
			folderProperties.removeAll(folderObj.getObjectID(), sessionObj.getContext());
			folderProperties.commit();
		} catch (final OXException e) {
			LOG.error("", e); // What shall we do with the drunken Exception? what shall we do with the drunken Exception? What shall we do with the drunken Exception early in the morning?
		} finally {
			try {
				folderProperties.finish();
			} catch (final OXException e) {
			    LOG.error("", e);
			}
		}
	}

	@Override
    public void folderModified(final FolderObject folderObj, final Session sessionObj) {
	    // Nothing to do
	}

    @Override
    public void handleEvent(final Event event) {
        if (isInfostoreEvent(event) && isUpdateEvent(event)) {
            try {
                FileID fileID = new FileID(extractObjectId(event));
                if (FileID.INFOSTORE_SERVICE_ID.equals(fileID.getService()) && FileID.INFOSTORE_ACCOUNT_ID.equals(fileID.getAccountId())) {
                    int objectID = Integer.parseInt(fileID.getFileId());
                    ServerSession session = ServerSessionAdapter.valueOf(extractSession(event));
                    infoProperties.startTransaction();
                    infoProperties.removeAll(objectID, session.getContext());
                    infoProperties.commit();
                }
            } catch (OXException e) {
                LOG.error("Couldn't remove locks from infoitem. Run the consistency tool.", e);
            } catch (NumberFormatException e) {
                // Obviously no numeric identifier; therefore not related to InfoStore file storage
                LOG.debug("", e);
            } finally {
                try {
                    infoProperties.finish();
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
            LOG.debug("{}", new Object() { @Override public String toString() { return createDebugMessage("UpdateEvent", event);}});
        }
    }
}
