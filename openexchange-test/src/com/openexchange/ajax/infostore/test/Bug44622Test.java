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

package com.openexchange.ajax.infostore.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.Date;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetDocumentRequest;
import com.openexchange.ajax.infostore.actions.GetDocumentResponse;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.test.TestInit;

/**
 * {@link Bug44622Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class Bug44622Test extends AbstractInfostoreTest {

    private String fileID;

    /**
     * Initializes a new {@link Bug44622Test}.
     * 
     * @param name
     */
    public Bug44622Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        DefaultFile file = new DefaultFile();
        file.setFileName("Bug 44622 Test");
        file.setFolderId(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()));
        NewInfostoreRequest req = new NewInfostoreRequest(file, upload);
        NewInfostoreResponse resp = getClient().execute(req);
        fileID = resp.getID();
    }

    @After
    public void tearDown() throws Exception {
        try {
            DeleteInfostoreRequest req = new DeleteInfostoreRequest(fileID, String.valueOf(getClient().getValues().getPrivateInfostoreFolder()), new Date());
            getClient().execute(req);
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testBug44622() throws Exception {
        GetDocumentRequest req = new GetDocumentRequest(String.valueOf(getClient().getValues().getPrivateInfostoreFolder()), fileID);
        req.setAdditionalParameters(new Parameter("content_disposition", ""));
        GetDocumentResponse resp = getClient().execute(req);
        assertFalse(resp.hasError());
        HttpResponse httpResp = resp.getHttpResponse();
        Header[] headers = httpResp.getHeaders("Content-Disposition");
        for (Header header : headers) {
            assertNotNull(header.getValue());
            assertTrue(header.getValue().contains("attachment;"));
        }
    }

}
