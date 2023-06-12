/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.tool.schema.internal.exec;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import org.hibernate.engine.jdbc.internal.DDLFormatterImpl;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.resource.transaction.spi.DdlTransactionIsolator;
import org.hibernate.tool.schema.spi.CommandAcceptanceException;

/**
 * GenerationTarget implementation for handling generation directly to the
 * database
 *
 * @author Steve Ebersole
 */
public class GenerationTargetToDatabase implements GenerationTarget {
	private static final CoreMessageLogger log = CoreLogging.messageLogger(GenerationTargetToDatabase.class);

	private final DdlTransactionIsolator ddlTransactionIsolator;
	private final boolean releaseAfterUse;

	private Statement jdbcStatement;

	public GenerationTargetToDatabase(DdlTransactionIsolator ddlTransactionIsolator) {
		this(ddlTransactionIsolator, true);
	}

	public GenerationTargetToDatabase(DdlTransactionIsolator ddlTransactionIsolator, boolean releaseAfterUse) {
		this.ddlTransactionIsolator = ddlTransactionIsolator;
		this.releaseAfterUse = releaseAfterUse;
	}

	@Override
	public void prepare() {
	}

	@Override
	public void accept(String command) {
		ddlTransactionIsolator.getJdbcContext().getSqlStatementLogger().logStatement(command,
				DDLFormatterImpl.INSTANCE);

		// NuoDB 9-Jun-2023: DROP ... CASCADE not supported so a table will not be
		// dropped if a foreign key constraint won't let it be dropped. Truncate the
		// table instead.  Similarly, if a CREATE TABLE fails because table already
		// exists, drop the table and try again.
		for (int i = 0; i < 2; i++) {
			try {
				final Statement jdbcStatement = jdbcStatement();
				jdbcStatement.execute(command);

				try {
					SQLWarning warnings = jdbcStatement.getWarnings();
					if (warnings != null) {
						ddlTransactionIsolator.getJdbcContext().getSqlExceptionHelper()
								.logAndClearWarnings(jdbcStatement);
					}
				} catch (SQLException e) {
					log.unableToLogSqlWarnings(e);
				}
				break;
			} catch (SQLException e) {
				String emsg = e.getLocalizedMessage();
				if (command.contains("DROP CONSTRAINT") && (emsg.contains("can't find table") || emsg.contains("has no constraint")))
					break;  // Constraint or its table no longer exists. Ignore error and carry on
				else if (command.startsWith("drop ")) {
					// Change drop command to truncate and try again
					command = "truncate " + command.substring(5);
				}
				else if (command.contains(" drop "))
					// Change drop command to truncate and try again
					command.replaceAll(" drop ", " truncate ");
				else if (command.contains("create table") && emsg.contains("already exists")) {
					// Table exists, remove it first
					log.info("Failed running \"" + command + "\" because table already exists.  Will try to drop it first.");
					int ix = command.indexOf('(');
					String command2 = command.substring(0, ix);
					command2 = command2.replaceAll("create", "drop");

					try {
						jdbcStatement.execute(command2);
					} catch (SQLException ex) {
						// Failed to drop table
						throw new CommandAcceptanceException(
								"Create table failed, because table already exists. Unable to drop it using \"" + command2 + "\"",
								ex);
					}
				}
				else
					// Failed
					throw new CommandAcceptanceException(
							"Error executing DDL \"" + command + "\" via JDBC Statement",
							e);
			}
		}
		// NuoDB: End
	}

	private Statement jdbcStatement() {
		if (jdbcStatement == null) {
			try {
				this.jdbcStatement = ddlTransactionIsolator.getIsolatedConnection().createStatement();
			} catch (SQLException e) {
				throw ddlTransactionIsolator.getJdbcContext().getSqlExceptionHelper().convert(e,
						"Unable to create JDBC Statement for DDL execution");
			}
		}

		return jdbcStatement;
	}

	@Override
	public void release() {
		if (jdbcStatement != null) {
			try {
				jdbcStatement.close();
				jdbcStatement = null;
			} catch (SQLException e) {
				throw ddlTransactionIsolator.getJdbcContext().getSqlExceptionHelper().convert(e,
						"Unable to close JDBC Statement after DDL execution");
			}
		}
		if (releaseAfterUse) {
			ddlTransactionIsolator.release();
		}
	}
}
