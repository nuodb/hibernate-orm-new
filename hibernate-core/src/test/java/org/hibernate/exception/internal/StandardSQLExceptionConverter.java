/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.exception.internal;

import org.hibernate.JDBCException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.exception.spi.SQLExceptionConverter;
import org.hibernate.testing.support.TestUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <b>NUODB OVERRIDE CLASS</b>
 * <p>
 * <b>NuoDB:</b> Suppress stack trace for failures when generated SQL used
 * features unsupported by NuoDB.
 * <p>
 * A {@link SQLExceptionConverter} that delegates to a chain of
 * {@link SQLExceptionConversionDelegate}.
 *
 * @author Steve Ebersole
 */
@SuppressWarnings("serial")
public class StandardSQLExceptionConverter implements SQLExceptionConverter {

	private final List<SQLExceptionConversionDelegate> delegates;

	public StandardSQLExceptionConverter(SQLExceptionConversionDelegate... delegates) {
		this.delegates = Arrays.asList(delegates);
	}

	/**
	 * @deprecated use
	 *             {@link #StandardSQLExceptionConverter(SQLExceptionConversionDelegate...)}
	 */
	@Deprecated(since = "6.0")
	public StandardSQLExceptionConverter() {
		delegates = new ArrayList<>();
	}

	/**
	 * Add a delegate.
	 *
	 * @deprecated use
	 *             {@link #StandardSQLExceptionConverter(SQLExceptionConversionDelegate...)}
	 */
	@Deprecated(since = "6.0")
	public void addDelegate(SQLExceptionConversionDelegate delegate) {
		if (delegate != null) {
			this.delegates.add(delegate);
		}
	}

	@Override
	public JDBCException convert(SQLException sqlException, String message, String sql) {
		for (SQLExceptionConversionDelegate delegate : delegates) {
			final JDBCException jdbcException = delegate.convert(sqlException, message, sql);
			if (jdbcException != null) {
				return jdbcException;
			}
		}

		// NuoDB 11-Jun-2023: If this is a known failure case, suppress unnecessary
		// stack trace. Otherwise return original exception (per line commented out).
		return TestUtils.checkForKnownException(this, message, sqlException, sql, GenericJDBCException.class);
		// return new GenericJDBCException(message, sqlException, sql);
		// NuoDB: End
	}
}
