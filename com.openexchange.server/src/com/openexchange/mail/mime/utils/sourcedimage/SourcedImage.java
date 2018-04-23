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

package com.openexchange.mail.mime.utils.sourcedimage;

/**
 * {@link SourcedImage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SourcedImage {

    private final String contentType;

    private final String transferEncoding;

    private final String data;

    private final String contentId;

    /**
     * Initializes a new {@link SourcedImage}.
     *
     * @param contentType The Content-Type
     * @param transferEncoding The transfer encoding; e.g. <code>"base64"</code>
     * @param contentId The content identifier
     * @param data The (encoded) image data
     */
    protected SourcedImage(final String contentType, final String transferEncoding, final String contentId, final String data) {
        super();
        this.contentType = contentType;
        this.transferEncoding = transferEncoding;
        this.contentId = contentId;
        this.data = data;
    }

    /**
     * Gets the content identifier.
     *
     * @return The content identifier
     */
    public String getContentId() {
        return contentId;
    }

    /**
     * Gets the content type.
     *
     * @return The content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the transfer encoding.
     *
     * @return The transfer encoding
     */
    public String getTransferEncoding() {
        return transferEncoding;
    }

    /**
     * Gets the data.
     *
     * @return The data
     */
    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("SourcedImage [contentType=").append(contentType).append(", transferEncoding=").append(transferEncoding).append(
            ", contentId=").append(contentId).append(", data=");
        if (data.length() <= 10) {
            builder.append(data);
        } else {
            builder.append(data.substring(0, 10)).append("...");
        }

        builder.append(']');
        return builder.toString();
    }

}
