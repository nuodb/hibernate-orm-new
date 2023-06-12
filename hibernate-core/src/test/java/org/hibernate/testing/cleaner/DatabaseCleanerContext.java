/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.cleaner;

/**
 * <b>NUODB OVERRIDE CLASS: Added cleaner for NuoDB.</b>
 * <p/>
 * Find the right cleaner for the current database.
 * 
 * @author Christian Beikov
 */
public final class DatabaseCleanerContext {

	public static final DatabaseCleaner CLEANER;

	// Iterate over the cleaners - should return NuoDB's cleaner.
	static {
		CLEANER = JdbcConnectionContext.workReturning(connection -> {
			final DatabaseCleaner[] cleaners = new DatabaseCleaner[] { //
					new DB2DatabaseCleaner(), //
					new H2DatabaseCleaner(), //
					new SQLServerDatabaseCleaner(), //
					new MySQL5DatabaseCleaner(), //
					new MySQL8DatabaseCleaner(), //
					new MariaDBDatabaseCleaner(), //
					new OracleDatabaseCleaner(), //
					new PostgreSQLDatabaseCleaner(), //
					new NuoDBDatabaseCleaner() //
			};

			for (DatabaseCleaner cleaner : cleaners) {
				if (cleaner.isApplicable(connection)) {
					return cleaner;
				}
			}
			return null;
		});
	}

	private DatabaseCleanerContext() {
	}
}