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

package com.openexchange.database.internal.wrapping;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import com.openexchange.database.Databases;
import com.openexchange.database.IncorrectStringSQLException;

/**
 * {@link JDBC4PreparedStatementWrapper}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class JDBC4PreparedStatementWrapper extends JDBC4StatementWrapper implements PreparedStatement {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JDBC4PreparedStatementWrapper.class);

    protected final PreparedStatement preparedStatementDelegate;

    /**
     * Initializes a new {@link JDBC4PreparedStatementWrapper}.
     *
     * @param delegate The delegate statement
     * @param con The connection returner instance
     */
    public JDBC4PreparedStatementWrapper(final PreparedStatement delegate, final JDBC4ConnectionReturner con) {
        super(delegate, con);
        this.preparedStatementDelegate = delegate;
    }

    @Override
    public void addBatch() throws SQLException {
        preparedStatementDelegate.addBatch();
    }

    @Override
    public void clearParameters() throws SQLException {
        preparedStatementDelegate.clearParameters();
    }

    @Override
    public boolean execute() throws SQLException {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} executes: {}", Thread.currentThread(), Databases.getSqlStatement(preparedStatementDelegate, "<unknown>"));
            }
            boolean retval = preparedStatementDelegate.execute();
            con.updatePerformed();
            return retval;
        } catch (java.sql.SQLSyntaxErrorException syntaxError) {
            logSyntaxError(syntaxError, preparedStatementDelegate, con);
            throw syntaxError;
        } catch (java.sql.SQLException sqlException) {
            IncorrectStringSQLException incorrectStringError = IncorrectStringSQLException.instanceFor(sqlException);
            if (null != incorrectStringError) {
                throw incorrectStringError;
            }
            logReadTimeoutError(sqlException, preparedStatementDelegate, con);
            throw sqlException;
        }
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} executes: {}", Thread.currentThread(), Databases.getSqlStatement(preparedStatementDelegate, "<unknown>"));
            }
            JDBC41ResultSetWrapper retval = new JDBC41ResultSetWrapper(preparedStatementDelegate.executeQuery(), this);
            con.touch();
            return retval;
        } catch (java.sql.SQLSyntaxErrorException syntaxError) {
            logSyntaxError(syntaxError, preparedStatementDelegate, con);
            throw syntaxError;
        } catch (java.sql.SQLException sqlException) {
            IncorrectStringSQLException incorrectStringError = IncorrectStringSQLException.instanceFor(sqlException);
            if (null != incorrectStringError) {
                throw incorrectStringError;
            }
            logReadTimeoutError(sqlException, preparedStatementDelegate, con);
            throw sqlException;
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} executes: {}", Thread.currentThread(), Databases.getSqlStatement(preparedStatementDelegate, "<unknown>"));
            }
            int retval = preparedStatementDelegate.executeUpdate();
            con.updatePerformed();
            return retval;
        } catch (java.sql.SQLSyntaxErrorException syntaxError) {
            logSyntaxError(syntaxError, preparedStatementDelegate, con);
            throw syntaxError;
        } catch (java.sql.SQLException sqlException) {
            IncorrectStringSQLException incorrectStringError = IncorrectStringSQLException.instanceFor(sqlException);
            if (null != incorrectStringError) {
                throw incorrectStringError;
            }
            logReadTimeoutError(sqlException, preparedStatementDelegate, con);
            throw sqlException;
        }
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return preparedStatementDelegate.getMetaData();
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return preparedStatementDelegate.getParameterMetaData();
    }

    @Override
    public void setArray(final int i, final Array x) throws SQLException {
        preparedStatementDelegate.setArray(i, x);
    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        preparedStatementDelegate.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        preparedStatementDelegate.setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        preparedStatementDelegate.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setBlob(final int i, final Blob x) throws SQLException {
        preparedStatementDelegate.setBlob(i, x);
    }

    @Override
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        preparedStatementDelegate.setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(final int parameterIndex, final byte x) throws SQLException {
        preparedStatementDelegate.setByte(parameterIndex, x);
    }

    @Override
    public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
        preparedStatementDelegate.setBytes(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
        preparedStatementDelegate.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setClob(final int i, final Clob x) throws SQLException {
        preparedStatementDelegate.setClob(i, x);
    }

    @Override
    public void setDate(final int parameterIndex, final Date x) throws SQLException {
        preparedStatementDelegate.setDate(parameterIndex, x);
    }

    @Override
    public void setDate(final int parameterIndex, final Date x, final Calendar cal) throws SQLException {
        preparedStatementDelegate.setDate(parameterIndex, x, cal);
    }

    @Override
    public void setDouble(final int parameterIndex, final double x) throws SQLException {
        preparedStatementDelegate.setDouble(parameterIndex, x);
    }

    @Override
    public void setFloat(final int parameterIndex, final float x) throws SQLException {
        preparedStatementDelegate.setFloat(parameterIndex, x);
    }

    @Override
    public void setInt(final int parameterIndex, final int x) throws SQLException {
        preparedStatementDelegate.setInt(parameterIndex, x);
    }

    @Override
    public void setLong(final int parameterIndex, final long x) throws SQLException {
        preparedStatementDelegate.setLong(parameterIndex, x);
    }

    @Override
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        preparedStatementDelegate.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setNull(final int paramIndex, final int sqlType, final String typeName) throws SQLException {
        preparedStatementDelegate.setNull(paramIndex, sqlType, typeName);
    }

    @Override
    public void setObject(final int parameterIndex, final Object x) throws SQLException {
        preparedStatementDelegate.setObject(parameterIndex, x);
    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        preparedStatementDelegate.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scale) throws SQLException {
        preparedStatementDelegate.setObject(parameterIndex, x, targetSqlType, scale);
    }

    @Override
    public void setRef(final int i, final Ref x) throws SQLException {
        preparedStatementDelegate.setRef(i, x);
    }

    @Override
    public void setShort(final int parameterIndex, final short x) throws SQLException {
        preparedStatementDelegate.setShort(parameterIndex, x);
    }

    @Override
    public void setString(final int parameterIndex, final String x) throws SQLException {
        preparedStatementDelegate.setString(parameterIndex, x);
    }

    @Override
    public void setTime(final int parameterIndex, final Time x) throws SQLException {
        preparedStatementDelegate.setTime(parameterIndex, x);
    }

    @Override
    public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
        preparedStatementDelegate.setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
        preparedStatementDelegate.setTimestamp(parameterIndex, x);
    }

    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
        preparedStatementDelegate.setTimestamp(parameterIndex, x, cal);
    }

    @Override
    public void setURL(final int parameterIndex, final URL x) throws SQLException {
        preparedStatementDelegate.setURL(parameterIndex, x);
    }

    @Override
    @Deprecated
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        preparedStatementDelegate.setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public String toString() {
        return preparedStatementDelegate.toString();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        preparedStatementDelegate.setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        preparedStatementDelegate.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        preparedStatementDelegate.setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        preparedStatementDelegate.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        preparedStatementDelegate.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        preparedStatementDelegate.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        preparedStatementDelegate.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        preparedStatementDelegate.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        preparedStatementDelegate.setClob(parameterIndex, reader);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        preparedStatementDelegate.setClob(parameterIndex, reader, length);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        preparedStatementDelegate.setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        preparedStatementDelegate.setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        preparedStatementDelegate.setNClob(parameterIndex, value);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        preparedStatementDelegate.setNClob(parameterIndex, reader);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        preparedStatementDelegate.setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        preparedStatementDelegate.setNString(parameterIndex, value);
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        preparedStatementDelegate.setRowId(parameterIndex, x);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        preparedStatementDelegate.setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public boolean isClosed() throws SQLException {
        return preparedStatementDelegate.isClosed();
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return preparedStatementDelegate.isPoolable();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        preparedStatementDelegate.setPoolable(poolable);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isAssignableFrom(preparedStatementDelegate.getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(preparedStatementDelegate.getClass())) {
            return iface.cast(preparedStatementDelegate);
        }
        throw new SQLException("Not a wrapper for: " + iface.getName());
    }
}
