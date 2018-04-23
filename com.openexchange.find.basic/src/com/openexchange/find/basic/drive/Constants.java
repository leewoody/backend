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

package com.openexchange.find.basic.drive;

import java.util.Arrays;
import java.util.List;


/**
 * {@link Constants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Constants {

    /**
     * Initializes a new {@link Constants}.
     */
    private Constants() {
        super();
    }

    /** The virtual "global" field for file name, title and description */
    public static final String FIELD_GLOBAL = "global";

    /** The field for file name */
    public static final String FIELD_FILE_NAME = "filename";

    /** The field for file description */
    public static final String FIELD_FILE_DESC = "description";

    /** The field for file content */
    public static final String FIELD_FILE_CONTENT = "content";

    /** The field for file MIME type */
    public static final String FIELD_FILE_TYPE = "file_mimetype";

    /** The field for file size */
    public static final String FIELD_FILE_SIZE = "file_size";
    
    public static final String FIELD_FILE_EXTENSION = "file_extension";

    // ---------------------------------------------------------------------------------------------------------- //

    /** The fields to query for */
    public static final List<String> QUERY_FIELDS = Arrays.asList(new String[] { FIELD_FILE_NAME, FIELD_FILE_DESC });

    /** The patterns used match {@link Type#DOCUMENTS}. */
    public static final String[] FILETYPE_PATTERNS_DOCUMENTS = {
        "text/*plain*",
        "text/*rtf*",
        "application/*ms-word*",
        "application/*ms-excel*",
        "application/*ms-powerpoint*",
        "application/*msword*",
        "application/*excel*",
        "application/*powerpoint*",
        "application/*openxmlformats*",
        "application/*opendocument*",
        "application/*pdf*",
        "application/*rtf*"
    };

    /** The patterns used match {@link Type#IMAGES}. */
    public static final String[] FILETYPE_PATTERNS_IMAGES = {
        "image/*"
    };

    /** The patterns used match {@link Type#AUDIO}. */
    public static final String[] FILETYPE_PATTERNS_AUDIO = {
        "audio/*"
    };

    /** The patterns used match {@link Type#VIDEO}. */
    public static final String[] FILETYPE_PATTERNS_VIDEO = {
        "video/*"
    };
    
    /** The file extension used match {@link Type#DOC_TEXT}. */
    public static final String[] FILE_EXTENSION_TEXT = {
        "*.docx",
        "*.docm",
        "*.dotx",
        "*.dotm",
        "*.odt",
        "*.ott",
        "*.doc",
        "*.dot",
        "*.txt",
        "*.rtf"
    };
    
    /** The file extension used match {@link Type#DOC_SPREADSHEET}. */
    public static final String[] FILE_EXTENSION_SPREADSHEET = {
        "*.xlsx",
        "*.xlsm",
        "*.xltx",
        "*.xltm",
        "*.xlsb",
        "*.ods",
        "*.ots",
        "*.xls",
        "*.xlt",
        "*.xla"
    };
    
    /** The file extension used match {@link Type#DOC_PRESENTATION}. */    
    public static final String[] FILE_EXTENSION_PRESENTATION = {
        "*.pptx",
        "*.pptm",
        "*.potx",
        "*.potx",
        "*.ppsx",
        "*.ppsm",
        "*.ppam",
        "*.odp",
        "*.otp",
        "*.ppt",
        "*.pot",
        "*.pps",
        "*.ppa"
    };
    
    /** The file extension used match {@link Type#PDF}. */    
    public static final String[] FILE_EXTENSION_PDF = {
        "*.pdf"
    };
    
    /** The file extension used match {@link Type#IMAGE}. */    
    public static final String[] FILE_EXTENSION_IMAGE = {
        "*.png",
        "*.jpg",
        "*.jpeg",
        "*.gif",
        "*.tiff",
        "*.bmp"
    };   
    
    /** The file extension used match {@link Type#VIDEO}. */    
    public static final String[] FILE_EXTENSION_VIDEO = {
        "*.m4v",
        "*.ogv",
        "*.webm",
        "*.mov",
        "*.avi",
        "*.wmv",
        "*.wma",
        "*.mpg",
        "*.mpeg",
        "*.mp4",
        "*.mpg"
    };  
    
    /** The file extension used match {@link Type#MUSIC}. */    
    public static final String[] FILE_EXTENSION_AUDIO = {
        "*.mp3",
        "*.m4a",
        "*.m4b",
        "*.ogg",
        "*.aac",
        "*.wav",
        "*.wma",
        "*.mid",
        "*.ra",
        "*.ram",
        "*.rm",
        "*.m3u",
        "*.mp4a",
        "*.mpga"
    };  
}