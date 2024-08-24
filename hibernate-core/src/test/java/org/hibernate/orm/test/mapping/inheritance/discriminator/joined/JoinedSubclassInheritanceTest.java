/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.mapping.inheritance.discriminator.joined;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.After;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Chris Cranford
 */
@TestForIssue(jiraKey = "HHH-11133")
@DomainModel(
		xmlMappings = "org/hibernate/orm/test/mapping/inheritance/discriminator/joined/JoinedSubclassInheritance.hbm.xml"
)
@SessionFactory
public class JoinedSubclassInheritanceTest {

	@After
	public void tearDown(SessionFactoryScope scope) {
		scope.inTransaction(
				session ->
						session.createQuery( "delete from ChildEntity" ).executeUpdate()
		);
	}

	@Test
	public void testConfiguredDiscriminatorValue(SessionFactoryScope scope) {
		final ChildEntity childEntity = new ChildEntity( 1, "Child" );
		scope.inTransaction( session -> session.persist( childEntity ) );

		scope.inTransaction( session -> {
			ChildEntity ce = session.find( ChildEntity.class, 1 );
			assertThat( ce.getType(), is( "ce" ) );
		} );
	}

}
