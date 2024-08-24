/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.jdbc;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.util.function.Supplier;

import org.hibernate.HibernateException;
import org.hibernate.Internal;
import org.hibernate.StaleStateException;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;

import static java.sql.Statement.EXECUTE_FAILED;
import static java.sql.Statement.SUCCESS_NO_INFO;

/**
 * Useful operations for dealing with {@link Expectation}s.
 *
 * @author Steve Ebersole
 */
public class Expectations {
	private static final CoreMessageLogger LOG = CoreLogging.messageLogger( Expectations.class );

	static final SqlExceptionHelper sqlExceptionHelper = new SqlExceptionHelper( false );

	/**
	 * Create an instance of the given class implementing {@link Expectation}.
	 * @param expectation a class which implements {@code Expectation}
	 * @param callable true if the {@code Expectation} will be called with {@link CallableStatement}s.
	 * @return a new instance of the given class
	 *
	 * @since 6.5
	 */
	@Internal
	public static Expectation createExpectation(Supplier<? extends Expectation> expectation, boolean callable) {
		final Expectation instance = instantiate( expectation, callable );
		instance.validate( callable );
		return instance;
	}

	private static Expectation instantiate(Supplier<? extends Expectation> supplier, boolean callable) {
		if ( supplier == null ) {
			return callable
					? new Expectation.OutParameter()
					: new Expectation.RowCount();
		}
		else {
			return supplier.get();
		}
	}

	static CallableStatement toCallableStatement(PreparedStatement statement) {
		if ( statement instanceof CallableStatement ) {
			return (CallableStatement) statement;
		}
		else {
			throw new HibernateException( "Expectation.OutParameter operates exclusively on CallableStatements: "
					+ statement.getClass() );
		}
	}

	static void checkBatched(int expectedRowCount, int rowCount, int batchPosition, String sql) {
		switch (rowCount) {
			case EXECUTE_FAILED:
				throw new BatchFailedException( "Batch update failed: " + batchPosition );
			case SUCCESS_NO_INFO:
				LOG.debugf( "Success of batch update unknown: %s", batchPosition );
				break;
			default:
				if ( expectedRowCount > rowCount ) {
					throw new StaleStateException(
							"Batch update returned unexpected row count from update ["
									+ batchPosition + "]; actual row count: " + rowCount
									+ "; expected: " + expectedRowCount + "; statement executed: "
									+ sql
					);
				}
				else if ( expectedRowCount < rowCount ) {
					throw new BatchedTooManyRowsAffectedException(
							"Batch update returned unexpected row count from update [" +
							batchPosition + "]; actual row count: " + rowCount +
							"; expected: " + expectedRowCount,
							expectedRowCount, rowCount, batchPosition );
				}
		}
	}

	static void checkNonBatched(int expectedRowCount, int rowCount, String sql) {
		if ( expectedRowCount > rowCount ) {
			throw new StaleStateException(
					"Unexpected row count: " + rowCount + "; expected: " + expectedRowCount
							+ "; statement executed: " + sql
			);
		}
		if ( expectedRowCount < rowCount ) {
			throw new TooManyRowsAffectedException(
					"Unexpected row count: " + rowCount + "; expected: " + expectedRowCount,
					1, rowCount
			);
		}
	}

	// Various Expectation instances ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * @deprecated Use {@link Expectation.None}
	 */
	@Deprecated(since = "6.5")
	public static final Expectation NONE = new Expectation.None();

	/**
	 * @deprecated Use {@link Expectation.RowCount}
	 */
	@Deprecated(since = "6.5")
	public static final Expectation BASIC = new Expectation.RowCount();

	/**
	 * @deprecated Use {@link Expectation.OutParameter}
	 */
	@Deprecated(since = "6.5")
	public static final Expectation PARAM = new Expectation.OutParameter();

	private Expectations() {
	}
}
