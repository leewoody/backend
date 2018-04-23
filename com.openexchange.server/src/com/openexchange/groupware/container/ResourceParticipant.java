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

package com.openexchange.groupware.container;

import com.openexchange.resource.Resource;

/**
 * {@link ResourceParticipant} - Represents a resource participant.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class ResourceParticipant implements Participant, Comparable<Participant> {

    private static final long serialVersionUID = 4133897083017380091L;

    private int id = NO_ID;

    private String displayName;

    private String emailaddress;

    private boolean ignoreNotification;

    /**
     * Default constructor.
     *
     * @param id unique identifier of the resource.
     */
    public ResourceParticipant(final int id) {
        super();
        this.id = id;
    }

    /**
     * Constructor that takes values of from a given resource
     *
     * @param res - a resource to be used as base
     */
    public ResourceParticipant(Resource res){
        this(res.getIdentifier());
        setDisplayName(res.getDisplayName());
        setEmailAddress(res.getMail());
    }

    /**
     * @deprecated Use {@link #ResourceParticipant(int)}.
     */
    @Deprecated
    public ResourceParticipant() {
        this(0);
    }

    /**
     * {@inheritDoc}
     * @deprecated
     */
    @Deprecated
    @Override
    public void setIdentifier(final int id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentifier() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailAddress() {
        return emailaddress == null ? null : emailaddress.toLowerCase();
    }

    /**
     * {@inheritDoc}
     */
    public void setEmailAddress(final String emailaddress) {
        this.emailaddress = emailaddress == null ? null : emailaddress.toLowerCase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getType() {
        return RESOURCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + RESOURCE;
        result = prime * result + id;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ResourceParticipant)) {
            return false;
        }
        final ResourceParticipant other = (ResourceParticipant) obj;
        return id == other.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Participant part) {
        final int retval;
        if (RESOURCE == part.getType()) {
            retval = Integer.valueOf(id).compareTo(Integer.valueOf(part.getIdentifier()));
        } else {
            retval = Integer.valueOf(RESOURCE).compareTo(Integer.valueOf(part.getType()));
        }
        return retval;
    }

    @Override
    public ResourceParticipant clone() throws CloneNotSupportedException {
        ResourceParticipant retval = (ResourceParticipant) super.clone();

        retval.setDisplayName(this.getDisplayName());
        retval.setEmailAddress(this.getEmailAddress());
        retval.setIdentifier(this.getIdentifier());
        retval.setIgnoreNotification(this.isIgnoreNotification());

        return retval;
    }

    @Override
    public Participant getClone() throws CloneNotSupportedException {
        return clone();
    }

    @Override
    public boolean isIgnoreNotification() {
        return ignoreNotification;
    }

    @Override
    public void setIgnoreNotification(final boolean ignoreNotification) {
        this.ignoreNotification = ignoreNotification;
    }
}
