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

package com.openexchange.ajax.requesthandler.responseRenderers;

import static com.openexchange.ajax.requesthandler.AJAXRequestDataTools.parseBoolParameter;
import static com.openexchange.tools.servlet.http.Tools.getWriterFrom;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link APIResponseRenderer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class APIResponseRenderer implements ResponseRenderer {

    /**
     * The logger constant.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(APIResponseRenderer.class);

    private static final String JSONP = "jsonp";

    private static final String CALLBACK = "callback";

    private static final String PLAIN_JSON = AJAXServlet.PARAM_PLAIN_JSON;

    private static final String INCLUDE_STACK_TRACE_ON_ERROR = com.openexchange.ajax.AJAXServlet.PARAMETER_INCLUDE_STACK_TRACE_ON_ERROR;

    private static final String CONTENTTYPE_HTML = com.openexchange.ajax.AJAXServlet.CONTENTTYPE_HTML;

    /**
     * Initializes a new {@link APIResponseRenderer}.
     */
    public APIResponseRenderer() {
        super();
    }

    @Override
    public int getRanking() {
        return 0;
    }

    @Override
    public boolean handles(final AJAXRequestData request, final AJAXRequestResult result) {
        return result.getResultObject() instanceof Response;
    }

    @Override
    public void write(final AJAXRequestData request, final AJAXRequestResult result, final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        Boolean plainJson = (Boolean) result.getParameter(PLAIN_JSON);
        if (null == plainJson) {
            boolean b = AJAXRequestDataTools.parseBoolParameter(PLAIN_JSON, request);
            plainJson = b ? Boolean.TRUE : null;
        }

        Response response = (Response) result.getResultObject();
        response.setContinuationUUID(result.getContinuationUuid());
        if (parseBoolParameter(INCLUDE_STACK_TRACE_ON_ERROR, request) ) {
            response.setIncludeStackTraceOnError(true);
        }

        // Try to obtain writer instance and then output response
        PrintWriter writer = getWriterFrom(resp);
        if (null != writer) {
            writeResponse(response, request.getAction(), writer, req, resp, null == plainJson ? false : plainJson.booleanValue());
        }
    }

    private static final String SESSION_KEY = SessionServlet.SESSION_KEY;

    /**
     * Returns the remembered session.
     *
     * @param req The Servlet request.
     * @return The remembered session
     */
    protected static ServerSession getSession(final ServletRequest req) {
        final Object attribute = req.getAttribute(SESSION_KEY);
        if (attribute != null) {
            return (ServerSession) req.getAttribute(SESSION_KEY);
        }
        return null;
    }

    /**
     * Gets the locale for given HTTP request
     *
     * @param req The request
     * @return The locale
     */
    protected static Locale localeFrom(final HttpServletRequest req) {
        return localeFrom(getSession(req));
    }

    /**
     * Gets the locale for given server session
     *
     * @param session The server session
     * @return The locale
     */
    protected static Locale localeFrom(final ServerSession session) {
        if (null == session) {
            return Locale.US;
        }
        User user = session.getUser();
        if (user == null) {
            return Locale.US;
        }

        return user.getLocale();
    }

    /**
     * Write specified response to Servlet output stream either as HTML callback or as JSON data.
     * <p>
     * The response is considered as HTML callback if one of these conditions is met:
     * <ul>
     * <li>The HTTP request indicates <i>multipart/*</i> content type</li>
     * <li>The HTTP request has the <code>"respondWithHTML"</code> parameter set to <code>"true"</code></li>
     * <li>The HTTP request contains non-<code>null</code> <code>"callback"</code> parameter</li>
     * </ul>
     *
     * @param response The response to write
     * @param action The request's action
     * @param req The HTTP request
     * @param resp The HTTP response
     * @return <code>true</code> if response has been successfully written; otherwise <code>false</code>
     * @throws IOException If an I/O error occurs
     */
    public static boolean writeResponse(Response response, String action, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Try to obtain writer instance
        PrintWriter writer = getWriterFrom(resp);

        if (null == writer) {
            return false;
        }

        return writeResponse(response, action, writer, req, resp);
    }

    /**
     * Write specified response to Servlet output stream either as HTML callback or as JSON data.
     * <p>
     * The response is considered as HTML callback if one of these conditions is met:
     * <ul>
     * <li>The HTTP request indicates <i>multipart/*</i> content type</li>
     * <li>The HTTP request has the <code>"respondWithHTML"</code> parameter set to <code>"true"</code></li>
     * <li>The HTTP request contains non-<code>null</code> <code>"callback"</code> parameter</li>
     * </ul>
     *
     * @param response The response to write
     * @param action The request's action
     * @param writer The writer to use
     * @param req The HTTP request
     * @param resp The HTTP response
     * @return <code>true</code> if response has been successfully written; otherwise <code>false</code>
     * @throws IOException If an I/O error occurs
     */
    public static boolean writeResponse(Response response, String action, PrintWriter writer, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String plainJson = req.getParameter(PLAIN_JSON);
        return writeResponse(response, action, writer, req, resp, AJAXRequestDataTools.parseBoolParameter(plainJson));
    }

    private static final char[] JS_FRAGMENT_PART1 = ("<!DOCTYPE html><html><head>"
        + "<META http-equiv=\"Content-Type\" "
        + "content=\"text/html; charset=UTF-8\">"
        + "<script type=\"text/javascript\">"
        + "(parent[\"callback_").toCharArray();

    private static final char[] JS_FRAGMENT_PART2 = "\"] || window.opener && window.opener[\"callback_".toCharArray();

    private static final char[] JS_FRAGMENT_PART3 = ")</script></head></html>".toCharArray();

    private static final Pattern PATTERN_QUOTE = Pattern.compile("(^|[^\\\\])\"");

    private static boolean writeResponse(Response response, String action, PrintWriter writer, HttpServletRequest req, HttpServletResponse resp, boolean plainJson) throws IOException {
        try {
            if (plainJson) {
                ResponseWriter.write(response, writer, localeFrom(req));
                // Successfully written...
                return true;
            }

            if (expectsJsCallback(req)) {
                // Regular HTML call-back...
                writeJsCallback(response, action, writer, req, resp);
                // Successfully written...
                return true;
            }

            String jsonp = req.getParameter(JSONP);
            if (false == Strings.isEmpty(jsonp)) {
                String callback = AJAXUtility.sanitizeParam(jsonp);
                if (false == validateCallbackName(Strings.asciiLowerCase(callback))) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid call-back name");
                    return true;
                }

                // Write: <call> + "(" + <json> + ")"
                resp.setContentType("text/javascript");
                writer.write(callback);
                writer.write('(');
                ResponseWriter.write(response, writer, localeFrom(req));
                writer.write(')');
                // Successfully written...
                return true;
            }

            ResponseWriter.write(response, writer, localeFrom(req));
            // Successfully written...
            return true;
        } catch (JSONException e) {
            LOG.error("", e);
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "A JSON error occurred: " + e.getMessage());
            } catch (IOException ioe) {
                LOG.error("", ioe);
            }
        }
        return false;
    }

    /**
     * Validates specified call-back name.
     *
     * @param lowerCaseCallback The call-back name (in lower-case) to validate
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    private static boolean validateCallbackName(String lowerCaseCallback) {
        int length = lowerCaseCallback.length();
        for (int i = length; i-- > 0;) {
            char ch = lowerCaseCallback.charAt(i);
            if (('a' > ch || 'z' < ch) && ('0' > ch || '9' < ch) && '-' != ch && '_' != ch && '$' != ch) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a JavaScript call-back is expected for given HTTP request
     *
     * @param req The HTTP request to check
     * @return <code>true</code> if a JavaScript call-back is expected; otherwise <code>false</code>
     */
    public static boolean expectsJsCallback(HttpServletRequest req) {
        return isPlainJson(req) ? false : (isMultipartContent(req) || isRespondWithHTML(req) || req.getParameter(CALLBACK) != null);
    }

    /**
     * Writes common JavaScript call-back for given response.
     *
     * @param response The response to output JavaScript call-back for
     * @param action The associated action
     * @param writer The writer to output to
     * @param req The HTTP request
     * @param resp The HTTP response
     * @throws IOException If an I/O error occurs
     * @throws JSONException If a JSON error occurs
     * @throws IllegalStateException If writer instance cannot be obtained from HTTP response
     */
    public static void writeJsCallback(Response response, String action, HttpServletRequest req, HttpServletResponse resp) throws IOException, JSONException {
        writeJsCallback(response, action, resp.getWriter(), req, resp);
    }

    /**
     * Writes common JavaScript call-back for given response.
     *
     * @param response The response to output JavaScript call-back for
     * @param action The associated action
     * @param writer The writer to output to
     * @param req The HTTP request
     * @param resp The HTTP response
     * @throws IOException If an I/O error occurs
     * @throws JSONException If a JSON error occurs
     */
    public static void writeJsCallback(Response response, String action, Writer writer, HttpServletRequest req, HttpServletResponse resp) throws IOException, JSONException {
        String callback = req.getParameter(CALLBACK);
        if (callback == null) {
            callback = action;
        } else {
            if (callback.indexOf('"') >= 0) {
                callback = PATTERN_QUOTE.matcher(callback).replaceAll("$1\\\\\"");
            }
        }
        callback = AJAXUtility.sanitizeParam(callback);
        if (false == validateCallbackName(Strings.asciiLowerCase(callback))) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid call-back name");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENTTYPE_HTML);
        resp.setHeader("Content-Disposition", "inline");
        writer.write(JS_FRAGMENT_PART1);
        writer.write(callback);
        writer.write(JS_FRAGMENT_PART2);
        writer.write(callback);
        writer.write("\"])(");
        ResponseWriter.write(response, new EscapingWriter(writer), localeFrom(req));
        writer.write(JS_FRAGMENT_PART3);
    }

    /**
     * Part of HTTP content type header.
     */
    private static final String MULTIPART = "multipart/";

    /**
     * Utility method that determines whether the request contains multipart content
     *
     * @param request The request to be evaluated.
     * @return <code>true</code> if the request is multipart; <code>false</code> otherwise.
     */
    private static final boolean isMultipartContent(HttpServletRequest request) {
        final String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        if (contentType.toLowerCase().startsWith(MULTIPART)) {
            return true;
        }
        return false;
    }

    private static boolean isPlainJson(final HttpServletRequest req) {
        return Boolean.parseBoolean(req.getParameter("plainJson"));
    }

    private static boolean isRespondWithHTML(final HttpServletRequest req) {
        return Boolean.parseBoolean(req.getParameter("respondWithHTML"));
    }

    /**
     * Escapes <tt>"&lt;/"</tt> char sequence to <tt>"&lt;\/"</tt>.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    private static final class EscapingWriter extends Writer {

        private int prev;
        private final Writer writer;

        protected EscapingWriter(final Writer writer) {
            super();
            this.writer = writer;
            prev = 0;
        }

        @Override
        public void write(final int c) throws IOException {
            if ('<' == c) {
                prev = c;
            } else if ('/' == c) {
                if (prev > 0) {
                    //  </   -->   <\/
                    writer.write("<\\/");
                    prev = 0;
                } else {
                    writer.write(c);
                }
            } else {
                if (prev > 0) {
                    writer.write('<');
                    prev = 0;
                }
                writer.write(c);
            }
        }

        @Override
        public void write(final char[] cbuf) throws IOException {
            write(cbuf, 0, cbuf.length);
        }

        @Override
        public void write(final char[] cbuf, final int off, final int len) throws IOException {
            for (int i = off, end = off + len; i < end; i++) {
                write(cbuf[i]);
            }
        }

        @Override
        public void write(final String str) throws IOException {
            write(str, 0, str.length());
        }

        @Override
        public void write(final String str, final int off, final int len) throws IOException {
            for (int i = off, end = off + len; i < end; i++) {
                write(str.charAt(i));
            }
        }

        @Override
        public Writer append(final CharSequence csq) throws IOException {
            if (csq == null) {
                write("null");
            } else {
                write(csq.toString());
            }
            return this;
        }

        @Override
        public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
            CharSequence cs = (csq == null ? "null" : csq);
            write(cs.subSequence(start, end).toString());
            return this;
        }

        @Override
        public Writer append(final char c) throws IOException {
            write(c);
            return this;
        }

        @Override
        public void flush() throws IOException {
            if ('<' == prev) {
                writer.write('<');
                prev = '\0';
            }
            writer.flush();
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }

        @Override
        public String toString() {
            return writer.toString();
        }

    }

}
