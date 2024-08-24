/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.exception.spi;

import java.sql.SQLException;

import org.hibernate.JDBCException;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Allow a {@link SQLExceptionConverter} to work by chaining together
 * multiple delegates. The main difference between a delegate and a
 * full-fledged converter is that a delegate may return {@code null}.
 *
 * @author Steve Ebersole
 */
@FunctionalInterface
public interface SQLExceptionConversionDelegate {
	/**
	 * Convert the given {@link SQLException} to a subtype of
	 * {@link JDBCException}, if possible.
	 *
	 * @param sqlException The {@code SQLException} to be converted
	 * @param message An optional error message
	 * @param sql The SQL statement that resulted in the exception
	 *
	 * @return The resulting {@code JDBCException}, or {@code null}
	 *         if this delegate does not know how to interpret the
	 *         given {@link SQLException}.
	 */
	@Nullable JDBCException convert(SQLException sqlException, String message, String sql);

}
