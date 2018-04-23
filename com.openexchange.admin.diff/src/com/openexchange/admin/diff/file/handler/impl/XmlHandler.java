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

package com.openexchange.admin.diff.file.handler.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;
import com.openexchange.admin.diff.ConfigDiff;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.result.DiffResult;
import com.openexchange.admin.diff.result.domain.PropertyDiff;
import com.openexchange.admin.diff.util.ConfigurationFileSearch;

/**
 * Handler for .xml configuration files
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class XmlHandler extends AbstractFileHandler {

    private volatile static XmlHandler instance;

    private XmlHandler() {
        ConfigDiff.register(this);
    }

    public static synchronized XmlHandler getInstance() {
        if (instance == null) {
            synchronized (XmlHandler.class) {
                if (instance == null) {
                    instance = new XmlHandler();
                }
            }
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiffResult getDiff(DiffResult diffResult, List<ConfigurationFile> lOriginalFiles, List<ConfigurationFile> lInstalledFiles) {
        configureXMLUnitComparator();

        for (ConfigurationFile origFile : lOriginalFiles) {

            final String fileName = origFile.getName();
            List<ConfigurationFile> result = new ConfigurationFileSearch().search(lInstalledFiles, fileName);

            if (result.isEmpty()) {
                // Missing in installation, but already tracked in file diff
                continue;
            }

            String originalFileContent = origFile.getContent();
            String installedFileContent = result.get(0).getContent();
            try {
                DetailedDiff xmlDetailedDiff = new DetailedDiff(new Diff(originalFileContent, installedFileContent));

                if (xmlDetailedDiff.getAllDifferences().isEmpty()) {
                    continue;
                }
                String difference = "";
                Iterator<Difference> iterator = xmlDetailedDiff.getAllDifferences().iterator();
                while (iterator.hasNext()) {
                    Difference next = iterator.next();
                    difference = difference.concat(next.toString() + "\n");
                }
                diffResult.getChangedProperties().add(new PropertyDiff(origFile.getFileNameWithExtension(), difference, null));
            }
            catch (SAXException e) {
                diffResult.getProcessingErrors().add("Error while xml diff: " + e.getLocalizedMessage() + "\n");
            } catch (IOException e) {
                diffResult.getProcessingErrors().add("Error while xml diff: " + e.getLocalizedMessage() + "\n");
            }

        }
        return diffResult;
    }

    private void configureXMLUnitComparator() {
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
    }
}
