/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.query.hql;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.community.dialect.AltibaseDialect;
import org.hibernate.dialect.SybaseASEDialect;
import org.hibernate.jpa.spi.MutableJpaCompliance;

import org.hibernate.testing.jdbc.SQLStatementInspector;
import org.hibernate.testing.orm.domain.StandardDomainModel;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.Setting;
import org.hibernate.testing.orm.junit.SkipForDialect;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Here, {@value AvailableSettings#JPA_QUERY_COMPLIANCE} should force the translation
 * to treat the comma as an implicit cross join instead of 2 roots as Hibernate itself does
 *
 * @author Steve Ebersole
 */
@ServiceRegistry(
		settings = @Setting( name = AvailableSettings.JPA_QUERY_COMPLIANCE, value = "true" )
)
@DomainModel( standardModels = StandardDomainModel.RETAIL )
@SessionFactory( useCollectingStatementInspector = true )
@SkipForDialect(
		dialectClass = SybaseASEDialect.class,
		reason = "Sybase Adaptive Server does not support SQL cross-joins; this query resorts to " +
				"multiple roots at the SQL level and so the cross reference is invalid"
)
public class JpaCrossJoinTests {
	@Test
	public void testCrossJoin(SessionFactoryScope scope) {
		final String qry = "select i from LineItem i cross join Order o join o.salesAssociate a on i.quantity = a.id";

		scope.inTransaction( (session) -> session.createQuery( qry ).list() );
	}

	@Test
	@SkipForDialect( dialectClass = AltibaseDialect.class, reason = "Altibase dialect emulate cross join with inner join")
	public void test2Roots(SessionFactoryScope scope) {
		final SQLStatementInspector statementInspector = scope.getCollectingStatementInspector();
		statementInspector.clear();

		final String qry = "select i from LineItem i, Order o join o.salesAssociate a on i.quantity = a.id";
		scope.inTransaction( (session) -> session.createQuery( qry ).list() );

		assertThat( statementInspector.getSqlQueries() ).hasSize( 1 );
		assertThat( statementInspector.getSqlQueries().get( 0 ) ).containsIgnoringCase( " cross join orders " );
	}

	@Test
	@SkipForDialect( dialectClass = AltibaseDialect.class, reason = "Altibase dialect emulate cross join with inner join")
	public void test2Roots2(SessionFactoryScope scope) {
		final SQLStatementInspector statementInspector = scope.getCollectingStatementInspector();
		statementInspector.clear();

		final String qry = "select i from LineItem i, Order o join o.salesAssociate a on i.product.vendor.name = a.name.familyName";
		scope.inTransaction( (session) -> session.createQuery( qry ).list() );

		assertThat( statementInspector.getSqlQueries() ).hasSize( 1 );
		assertThat( statementInspector.getSqlQueries().get( 0 ) ).containsIgnoringCase( " cross join orders " );
		assertThat( statementInspector.getSqlQueries().get( 0 ) ).containsIgnoringCase( " join associate " );
	}
}
