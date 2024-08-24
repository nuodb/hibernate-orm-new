/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast;

import org.hibernate.internal.log.SubSystemLogging;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
@SubSystemLogging(
		name = SqlTreeCreationLogger.LOGGER_NAME,
		description = "Logging related to the creation of SQL AST trees"
)
public interface SqlTreeCreationLogger {
	String LOGGER_NAME = SubSystemLogging.BASE + ".sql.ast.create";

	Logger LOGGER = Logger.getLogger( LOGGER_NAME );
}
