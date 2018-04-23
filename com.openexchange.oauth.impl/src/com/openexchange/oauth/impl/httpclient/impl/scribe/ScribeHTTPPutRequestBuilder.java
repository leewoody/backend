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

package com.openexchange.oauth.impl.httpclient.impl.scribe;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.UnsupportedCharsetException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;
import com.openexchange.java.Streams;
import com.openexchange.oauth.impl.httpclient.OAuthHTTPRequestBuilder;

public class ScribeHTTPPutRequestBuilder extends
		ScribeGenericHTTPRequestBuilder<HTTPPutRequestBuilder> implements
		HTTPPutRequestBuilder {

	private String payload = "";
	private String contentType = "application/octet-stream";

	public ScribeHTTPPutRequestBuilder(OAuthHTTPRequestBuilder coreBuilder) {
		super(coreBuilder);
	}

	@Override
	public Verb getVerb() {
		return Verb.PUT;
	}

	@Override
	public HTTPPutRequestBuilder body(String body) {
		payload = body;
		return this;
	}

	@Override
	public HTTPPutRequestBuilder body(InputStream body) throws OXException {
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(new BufferedInputStream(body, 65536), com.openexchange.java.Charsets.UTF_8);
		} catch (UnsupportedCharsetException e1) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		int ch = -1;
		try {
			while ((ch = isr.read()) != -1) {
				b.append((char) ch);
			}
		} catch (IOException e) {
			throw OxHttpClientExceptionCodes.IO_ERROR.create(e.getMessage());
		} finally {
		    Streams.close(isr);
		}
		return this;
	}

	@Override
	public HTTPPutRequestBuilder contentType(String ctype) {
		this.contentType = ctype;
		return this;
	}

	@Override
	protected void modify(OAuthRequest request) {
		request.addPayload(payload);
		if (contentType != null) {
			request.addHeader("Content-Type", contentType);
		}
	}

}
