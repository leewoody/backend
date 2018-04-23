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

package com.openexchange.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * <p>
 * {@link UnsynchronizedByteArrayOutputStream} - an implementation of {@link ByteArrayOutputStream} that does not use synchronized methods
 * </p>
 * <p>
 * This class implements an output stream in which the data is written into a byte array. The buffer automatically grows as data is written
 * to it. The data can be retrieved using <code>toByteArray()</code> and <code>toString()</code>.
 * <p>
 * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in this class can be called after the stream has been closed without
 * generating an <tt>IOException</tt>.
 * </p>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnsynchronizedByteArrayOutputStream extends ByteArrayOutputStream {

    /**
     * Creates a new byte array output stream. The buffer capacity is initially 32 bytes, though its size increases if necessary.
     */
    public UnsynchronizedByteArrayOutputStream() {
        this(32);
    }

    /**
     * Creates a new byte array output stream, with a buffer capacity of the specified size, in bytes.
     *
     * @param size The initial size.
     * @exception IllegalArgumentException If size is negative.
     */
    public UnsynchronizedByteArrayOutputStream(final int size) {
        super(size);
    }

    /**
     * Creates a new byte array output stream, containing given bytes.
     *
     * @param bytes The initial bytes.
     * @exception IllegalArgumentException If bytes is null
     */
    public UnsynchronizedByteArrayOutputStream(final byte[] bytes) {
        super(0);
        if (null == bytes) {
            throw new IllegalArgumentException("bytes is null.");
        }
        buf = bytes;
        count = bytes.length;
    }

    /**
     * Creates a new byte array output stream, containing given bytes.
     *
     * @param bytes The initial bytes.
     * @exception IllegalArgumentException If bytes is null
     */
    public UnsynchronizedByteArrayOutputStream(final ByteArrayOutputStream bytes) {
        super(0);
        if (null == bytes) {
            throw new IllegalArgumentException("bytes is null.");
        }
        buf = bytes instanceof UnsynchronizedByteArrayOutputStream ? ((UnsynchronizedByteArrayOutputStream) bytes).buf : bytes.toByteArray();
        count = buf.length;
    }

    /**
     * Gets the string value for valid bytes.
     *
     * @param charset The charset
     * @return The string value
     */
    public String toString(final Charset charset) {
        final Charset cs = null == charset ? detectCharset() : charset;
        if ("US-ASCII".equals(cs.name())) {
            return Charsets.toAsciiString(buf, 0, count);
        }
        return new String(buf, 0, count, cs);
    }

    private Charset detectCharset() {
        String charset = CharsetDetector.detectCharset(buf, count);
        if ("US-ASCII".equalsIgnoreCase(charset)) {
            return Charsets.ISO_8859_1;
        }
        return Charsets.forName(charset);
    }

    /**
     * Gets the number of valid bytes in the buffer.
     *
     * @return The number of valid bytes
     */
    public int getCount() {
        return count;
    }

    /**
     * Gets the direct byte buffer; <b>not</b> a copy!
     *
     * @return The byte buffer
     */
    public byte[] getBuf() {
        return buf;
    }

    /**
     * Writes the specified byte to this byte array output stream.
     *
     * @param b The byte to be written.
     */
    @Override
    public void write(final int b) {
        final int newcount = count + 1;
        if (newcount > buf.length) {
            final byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
        buf[count] = (byte) b;
        count = newcount;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array starting at offset <code>off</code> to this byte array output stream.
     *
     * @param b The data.
     * @param off The start offset in the data.
     * @param len The number of bytes to write.
     */
    @Override
    public void write(final byte b[], final int off, final int len) {
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        final int newcount = count + len;
        if (newcount > buf.length) {
            final byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
        System.arraycopy(b, off, buf, count, len);
        count = newcount;
    }

    /**
     * Writes the complete contents of this byte array output stream to the specified output stream argument, as if by calling the output
     * stream's write method using <code>out.write(buf, 0, count)</code>.
     *
     * @param out The output stream to which to write the data.
     * @exception IOException If an I/O error occurs.
     */
    @Override
    public void writeTo(final OutputStream out) throws IOException {
        if (null != out) {
            out.write(buf, 0, count);
        }
    }

    /**
     * Resets the <code>count</code> field of this byte array output stream to zero, so that all currently accumulated output in the output
     * stream is discarded. The output stream can be used again, reusing the already allocated buffer space.
     */
    @Override
    public void reset() {
        count = 0;
    }

    /**
     * Writes buffer's content to specified stream.
     *
     * @param outputStream The stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writeToStream(final OutputStream outputStream) throws IOException {
        outputStream.write(buf, 0, count);
    }

    /**
     * Discards <code>discardSize</code> bytes.
     *
     * @param discardSize The number of bytes to discard
     */
    public void discard(final int discardSize) {
        if ((discardSize < 0) || (discardSize > count)) {
            throw new IndexOutOfBoundsException();
        }
        if (discardSize == 0) {
            return;
        }
        final byte newbuf[] = new byte[count - discardSize];
        System.arraycopy(buf, discardSize, newbuf, 0, newbuf.length);
        buf = newbuf;
        count = newbuf.length;
    }

    /**
     * Creates a newly allocated <tt>ByteArrayInputStream</tt>. Its size is the current size of this output stream and the valid contents of
     * the buffer have been copied into it.
     *
     * @return The current contents of this output stream, as a <tt>ByteArrayInputStream</tt>.
     */
    public ByteArrayInputStream toByteArrayInputStream() {
        return new UnsynchronizedByteArrayInputStream(buf, 0, count);
    }

    /**
     * Creates a newly allocated byte array. Its size is the current size of this output stream and the valid contents of the buffer have
     * been copied into it.
     *
     * @return The current contents of this output stream, as a byte array.
     */
    @Override
    public byte toByteArray()[] {
        final byte newbuf[] = new byte[count];
        System.arraycopy(buf, 0, newbuf, 0, count);
        return newbuf;
    }

    /**
     * Creates a newly allocated byte array. Its size is specified <code>size</code> and the valid contents starting from specified offset
     * <code>off</code> are going to be copied into it.
     *
     * @param off The offset in valid contents
     * @param size The demanded size
     * @return The current contents of this output stream, as a byte array.
     */
    public byte toByteArray(final int off, final int size)[] {
        if ((off < 0) || (off > count) || (size < 0) || ((off + size) > count) || ((off + size) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (size == 0) {
            return new byte[0];
        }
        final byte newbuf[] = new byte[size];
        System.arraycopy(buf, off, newbuf, 0, size);
        return newbuf;
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return The value of the <code>count</code> field, which is the number of valid bytes in this output stream.
     */
    @Override
    public int size() {
        return count;
    }

    /**
     * Converts the buffer's contents into a string, translating bytes into characters according to the platform's default character
     * encoding.
     *
     * @return A string translated from the buffer's contents.
     */
    @Override
    public String toString() {
        return new String(buf, 0, count);
    }

    /**
     * Converts the buffer's contents into a string, translating bytes into characters according to the specified character encoding.
     *
     * @param enc The character-encoding name.
     * @return A string translated from the buffer's contents.
     * @throws UnsupportedEncodingException If the named encoding is not supported.
     */
    @Override
    public String toString(final String enc) throws UnsupportedEncodingException {
        if (count <= 0) {
            return "";
        }
        return toString(null == enc ? null : Charsets.forName(enc));
    }

    /**
     * Creates a newly allocated string. Its size is the current size of the output stream and the valid contents of the buffer have been
     * copied into it. Each character <i>c</i> in the resulting string is constructed from the corresponding element <i>b</i> in the byte
     * array such that: <blockquote>
     *
     * <pre>
     * c == (char) (((hibyte &amp; 0xff) &lt;&lt; 8) | (b &amp; 0xff))
     * </pre>
     *
     * </blockquote>
     *
     * @deprecated This method does not properly convert bytes into characters. As of JDK&nbsp;1.1, the preferred way to do this is via the
     *             <code>toString(String enc)</code> method, which takes an encoding-name argument, or the <code>toString()</code> method,
     *             which uses the platform's default character encoding.
     * @param hibyte The high byte of each resulting Unicode character.
     * @return The current contents of the output stream, as a string.
     */
    @Override
    @Deprecated
    public String toString(final int hibyte) {
        return new String(buf, hibyte, 0, count);
    }

    /**
     * Closing a byte array output stream has no effect. The methods in this class can be called after the stream has been closed without
     * generating an <tt>IOException</tt>.
     */
    @Override
    public void close() throws IOException {
        // Nothing to do
    }

}
