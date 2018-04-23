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

package com.openexchange.share.json.fields;

import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.anonymizer.Anonymizers;
import com.openexchange.ajax.anonymizer.Module;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.core.tools.PermissionResolver;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ExtendedPermission}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class ExtendedPermission {

    protected final PermissionResolver resolver;

    /**
     * Initializes a new {@link ExtendedPermission}.
     *
     * @param permissionResolver The permission resolver
     */
    protected ExtendedPermission(PermissionResolver permissionResolver) {
        super();
        this.resolver = permissionResolver;
    }

    protected void addGroupInfo(AJAXRequestData requestData, JSONObject jsonObject, Group group) throws JSONException, OXException {
        if (null != group) {
            /*
             * serialize anonymized or full group as needed
             */
            ServerSession session = requestData.getSession();
            if (Anonymizers.isGuest(session)) {
                addGroupInfo(jsonObject, Anonymizers.optAnonymize(group, Module.GROUP, session));
            } else {
                addGroupInfo(jsonObject, group);
            }
        }
    }

    protected void addUserInfo(AJAXRequestData requestData, JSONObject jsonObject, User user) throws JSONException, OXException {
        if (null != user) {
            Contact userContact = resolver.getUserContact(user.getId());
            if (null != userContact) {
                addContactInfo(requestData, jsonObject, userContact);
            } else {
                addContactInfo(requestData, jsonObject, user);
            }
        }
    }

    protected void addContactInfo(AJAXRequestData requestData, JSONObject jsonObject, Contact userContact) throws JSONException, OXException {
        if (null != userContact) {
            /*
             * serialize anonymized or full contact as needed
             */
            Contact toAdd = userContact;
            ServerSession session = requestData.getSession();
            if (Anonymizers.isGuest(session)) {
                if (session.getUserId() != toAdd.getInternalUserId()) {
                    Set<Integer> sharingUsers = Anonymizers.getSharingUsersFor(session.getContextId(), session.getUserId());
                    if (false == sharingUsers.contains(Integer.valueOf(toAdd.getInternalUserId()))) {
                        toAdd = Anonymizers.optAnonymize(toAdd, Module.CONTACT, session);
                    }
                }
            } else {
                if (session.getUserId() != toAdd.getInternalUserId() && Anonymizers.isNonVisibleGuest(toAdd.getInternalUserId(), session)) {
                    toAdd = Anonymizers.optAnonymize(toAdd, Module.CONTACT, session);
                }
            }
            addContactInfo(jsonObject, toAdd);
        }
    }

    protected void addContactInfo(AJAXRequestData requestData, JSONObject jsonObject, User user) throws JSONException, OXException {
        if (null != user) {
            /*
             * serialize anonymized or full user as needed
             */
            User toAdd = user;
            ServerSession session = requestData.getSession();
            if (Anonymizers.isGuest(session)) {
                if (session.getUserId() != toAdd.getId()) {
                    Set<Integer> sharingUsers = Anonymizers.getSharingUsersFor(session.getContextId(), session.getUserId());
                    if (false == sharingUsers.contains(Integer.valueOf(toAdd.getId()))) {
                        toAdd = Anonymizers.optAnonymize(toAdd, Module.CONTACT, session);
                    }
                }
            } else {
                if (session.getUserId() != toAdd.getId() && Anonymizers.isNonVisibleGuest(toAdd.getId(), session)) {
                    toAdd = Anonymizers.optAnonymize(toAdd, Module.CONTACT, session);
                }
            }
            addContactInfo(jsonObject, toAdd);
        }
    }

    protected void addShareInfo(AJAXRequestData requestData, JSONObject jsonObject, ShareInfo share) throws JSONException, OXException {
        if (null != share) {
            if (null != requestData) {
                jsonObject.putOpt("share_url", share.getShareURL(requestData.getHostData()));
            }
            Date expiryDate = share.getGuest().getExpiryDate();
            if (null != expiryDate) {
                long time = null != requestData ? addTimeZoneOffset(expiryDate.getTime(), getTimeZone(requestData)) : expiryDate.getTime();
                jsonObject.put("expiry_date", time);
            }
            jsonObject.putOpt("password", share.getGuest().getPassword());
        }
    }

    private void addContactInfo(JSONObject jsonObject, User user) throws JSONException {
        jsonObject.putOpt(ContactFields.DISPLAY_NAME, user.getDisplayName());
        JSONObject jsonContact = new JSONObject();
        jsonContact.putOpt(ContactFields.EMAIL1, user.getMail());
        jsonContact.putOpt(ContactFields.LAST_NAME, user.getSurname());
        jsonContact.putOpt(ContactFields.FIRST_NAME, user.getGivenName());
        jsonContact.putOpt(ContactFields.IMAGE1_URL, resolver.getImageURL(user.getId()));
        jsonObject.put("contact", jsonContact);
    }

    private void addContactInfo(JSONObject jsonObject, Contact contact) throws JSONException {
        jsonObject.putOpt(ContactFields.DISPLAY_NAME, contact.getDisplayName());
        JSONObject jsonContact = new JSONObject();
        jsonContact.putOpt(ContactFields.EMAIL1, contact.getEmail1());
        jsonContact.putOpt(ContactFields.TITLE, contact.getTitle());
        jsonContact.putOpt(ContactFields.LAST_NAME, contact.getSurName());
        jsonContact.putOpt(ContactFields.FIRST_NAME, contact.getGivenName());
        jsonContact.putOpt(ContactFields.IMAGE1_URL, resolver.getImageURL(contact.getInternalUserId()));
        jsonObject.put("contact", jsonContact);
    }

    private void addGroupInfo(JSONObject jsonObject, Group group) throws JSONException {
        jsonObject.put(ContactFields.DISPLAY_NAME, group.getDisplayName());
    }

    private static long addTimeZoneOffset(long date, TimeZone timeZone) {
        return null == timeZone ? date : date + timeZone.getOffset(date);
    }

    private static TimeZone getTimeZone(AJAXRequestData requestData) {
        String timeZoneID = requestData.getParameter("timezone");
        if (null == timeZoneID) {
            timeZoneID = requestData.getSession().getUser().getTimeZone();
        }
        return TimeZone.getTimeZone(timeZoneID);
    }

}
