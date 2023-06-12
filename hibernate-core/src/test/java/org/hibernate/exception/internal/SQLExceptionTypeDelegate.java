/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.exception.internal;

import java.sql.DataTruncation;
import java.sql.SQLClientInfoException;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransactionRollbackException;
import java.sql.SQLTransientConnectionException;

import org.hibernate.JDBCException;
import org.hibernate.QueryTimeoutException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.exception.spi.AbstractSQLExceptionConversionDelegate;
import org.hibernate.exception.spi.ConversionContext;
import org.hibernate.testing.support.TestUtils;

/**
 * <b>NUODB OVERRIDE CLASS</b>
 * <p>
 * <b>NuoDB:</b> Suppress stack trace for failures when generated SQL used
 * features unsupported by NuoDB.
 * <p>
 * A {@link org.hibernate.exception.spi.SQLExceptionConverter} implementation
 * that does conversion based on the {@link SQLException} subtype hierarchy
 * defined by JDBC 4.
 *
 * @author Steve Ebersole
 */
public class SQLExceptionTypeDelegate extends AbstractSQLExceptionConversionDelegate {
	public SQLExceptionTypeDelegate(ConversionContext conversionContext) {
		super(conversionContext);
	}

	@Override
	public JDBCException convert(SQLException sqlException, String message, String sql) {
		if (sqlException instanceof SQLClientInfoException
				|| sqlException instanceof SQLInvalidAuthorizationSpecException
				|| sqlException instanceof SQLNonTransientConnectionException
				|| sqlException instanceof SQLTransientConnectionException) {
			return new JDBCConnectionException(message, sqlException, sql);
		} else if (sqlException instanceof DataTruncation || sqlException instanceof SQLDataException) {
			throw new DataException(message, sqlException, sql);
		} else if (sqlException instanceof SQLIntegrityConstraintViolationException) {
			return new ConstraintViolationException(message, sqlException, sql,
					getConversionContext().getViolatedConstraintNameExtractor().extractConstraintName(sqlException));
		} else if (sqlException instanceof SQLSyntaxErrorException) {
			// NuoDB 11-Jun-2023: If this is a known failure case, suppress unnecessary
			// stack trace. Otherwise return original exception (per line commented out).
			return TestUtils.checkForKnownException(this, message, sqlException, sql, SQLGrammarException.class);
			//return new SQLGrammarException(message, sqlException, sql);
			// NuoDB: End
		} else if (sqlException instanceof SQLTimeoutException) {
			return new QueryTimeoutException(message, sqlException, sql);
		} else if (sqlException instanceof SQLTransactionRollbackException) {
			// Not 100% sure this is completely accurate. The JavaDocs for
			// SQLTransactionRollbackException state that
			// it indicates sql states starting with '40' and that those usually indicate
			// that:
			// <quote>
			// the current statement was automatically rolled back by the database because
			// of deadlock or
			// other transaction serialization failures.
			// </quote>
			return new LockAcquisitionException(message, sqlException, sql);
		}

		return null; // allow other delegates the chance to look
	}
}
