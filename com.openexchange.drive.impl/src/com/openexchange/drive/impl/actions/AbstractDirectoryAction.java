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

package com.openexchange.drive.impl.actions;

import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.impl.comparison.ThreeWayComparison;

/**
 * {@link AbstractDirectoryAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractDirectoryAction extends AbstractAction<DirectoryVersion> {

    /**
     * Initializes a new {@link AbstractDirectoryAction}.
     *
     * @param version
     * @param newVersion
     */
    protected AbstractDirectoryAction(DirectoryVersion version, DirectoryVersion newVersion, ThreeWayComparison<DirectoryVersion> comparison) {
        super(version, newVersion, comparison);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + DirectoryVersion.class.hashCode();
        Action action = getAction();
        result = prime * result + ((null == action) ? 0 : action.hashCode());
        if (null != version) {
            result = prime * result + version.getChecksum().hashCode();
            result = prime * result + version.getPath().hashCode();
        }
        if (null != newVersion) {
            result = prime * result + newVersion.getChecksum().hashCode();
            result = prime * result + newVersion.getPath().hashCode();
        }
        result = prime * result + ((null == parameters) ? 0 : parameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractDirectoryAction)) {
            return false;
        }
        AbstractDirectoryAction other = (AbstractDirectoryAction) obj;
        if (newVersion == null) {
            if (other.newVersion != null) {
                return false;
            }
        } else if (!newVersion.equals(other.newVersion)) {
            return false;
        }
        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!parameters.equals(other.parameters)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

}

