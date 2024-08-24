/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.community.dialect;

import org.hibernate.LockMode;
import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.ComparisonOperator;
import org.hibernate.sql.ast.tree.Statement;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.expression.Literal;
import org.hibernate.sql.ast.tree.from.FunctionTableReference;
import org.hibernate.sql.ast.tree.from.NamedTableReference;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.ast.tree.select.QueryPart;
import org.hibernate.sql.exec.spi.JdbcOperation;

import static org.hibernate.community.dialect.DB2zLegacyDialect.DB2_LUW_VERSION9;


/**
 * A SQL AST translator for DB2z.
 *
 * @author Christian Beikov
 */
public class DB2zLegacySqlAstTranslator<T extends JdbcOperation> extends DB2LegacySqlAstTranslator<T> {

	private final DatabaseVersion version;

	public DB2zLegacySqlAstTranslator(SessionFactoryImplementor sessionFactory, Statement statement, DatabaseVersion version) {
		super( sessionFactory, statement );
		this.version = version;
	}

	@Override
	protected boolean shouldEmulateFetchClause(QueryPart queryPart) {
		// Percent fetches or ties fetches aren't supported in DB2 z/OS
		// Also, variable limit isn't supported before 12.0
		return getQueryPartForRowNumbering() != queryPart && (
				useOffsetFetchClause( queryPart ) && !isRowsOnlyFetchClauseType( queryPart )
						|| version.isBefore(12) && queryPart.isRoot() && hasLimit()
						|| version.isBefore(12) && queryPart.getFetchClauseExpression() != null && !( queryPart.getFetchClauseExpression() instanceof Literal )
		);
	}

	@Override
	protected boolean supportsOffsetClause() {
		return version.isSameOrAfter(12);
	}

	@Override
	protected void renderComparison(Expression lhs, ComparisonOperator operator, Expression rhs) {
		// Supported at least since DB2 z/OS 9.0
		renderComparisonStandard( lhs, operator, rhs );
	}

	@Override
	protected boolean renderPrimaryTableReference(TableGroup tableGroup, LockMode lockMode) {
		if ( shouldInlineCte( tableGroup ) ) {
			inlineCteTableGroup( tableGroup, lockMode );
			return false;
		}
		final TableReference tableReference = tableGroup.getPrimaryTableReference();
		if ( tableReference instanceof NamedTableReference ) {
			return renderNamedTableReference( (NamedTableReference) tableReference, lockMode );
		}
		// DB2 z/OS we need the "table" qualifier for table valued functions or lateral sub-queries
		append( "table " );
		tableReference.accept( this );
		return false;
	}

	@Override
	public void visitFunctionTableReference(FunctionTableReference tableReference) {
		// For the table qualifier we need parenthesis on DB2 z/OS
		append( OPEN_PARENTHESIS );
		tableReference.getFunctionExpression().accept( this );
		append( CLOSE_PARENTHESIS );
		renderDerivedTableReference( tableReference );
	}

	@Override
	protected String getNewTableChangeModifier() {
		// On DB2 zOS, `final` also sees the trigger data
		return "final";
	}

	@Override
	public DatabaseVersion getDB2Version() {
		return DB2_LUW_VERSION9;
	}
}
