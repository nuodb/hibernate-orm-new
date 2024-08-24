/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.annotations.collectionelement;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import org.hibernate.Hibernate;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Gail Badner
 */
@DomainModel(
		annotatedClasses = {
				EmbeddableCollectionElementWithLazyManyToOneTest.Parent.class,
				EmbeddableCollectionElementWithLazyManyToOneTest.Child.class
		}
)
@SessionFactory
public class EmbeddableCollectionElementWithLazyManyToOneTest {

	@Test
	@TestForIssue(jiraKey = "???")
	public void testLazyManyToOneInEmbeddable(SessionFactoryScope scope) {
		Parent p = new Parent();
		p.containedChild = new ContainedChild( new Child() );

		scope.inTransaction(
				session ->
						session.persist( p )
		);

		scope.inTransaction(
				session -> {
					Parent pRead = session.get( Parent.class, p.id );
					assertFalse( Hibernate.isInitialized( pRead.containedChild.child ) );
				}
		);

		scope.inTransaction(
				session ->
						session.remove( p )
		);
	}

	@Test
	@TestForIssue(jiraKey = "???")
	public void testLazyManyToOneInCollectionElementEmbeddable(SessionFactoryScope scope) {
		Parent p = new Parent();
		p.containedChildren.add( new ContainedChild( new Child() ) );

		scope.inTransaction(
				session ->
						session.persist( p )
		);

		scope.inTransaction(
				session -> {
					Parent pRead = session.get( Parent.class, p.id );
					assertFalse( Hibernate.isInitialized( pRead.containedChildren ) );
					assertEquals( 1, pRead.containedChildren.size() );
					assertTrue( Hibernate.isInitialized( pRead.containedChildren ) );
					assertFalse( Hibernate.isInitialized( pRead.containedChildren.iterator().next().child ) );
				}
		);

		scope.inTransaction(
				session ->
						session.remove( p )
		);
	}

	@Test
	@TestForIssue(jiraKey = "???")
	public void testLazyBoth(SessionFactoryScope scope) {
		Parent p = new Parent();
		ContainedChild containedChild = new ContainedChild( new Child() );
		p.containedChild = containedChild;
		p.containedChildren.add( containedChild );

		scope.inTransaction(
				session ->
						session.persist( p )
		);

		scope.inTransaction(
				session -> {
					Parent pRead = session.get( Parent.class, p.id );
					assertFalse( Hibernate.isInitialized( pRead.containedChild.child ) );
					assertFalse( Hibernate.isInitialized( pRead.containedChildren ) );
					assertEquals( 1, pRead.containedChildren.size() );
					assertTrue( Hibernate.isInitialized( pRead.containedChildren ) );
					assertFalse( Hibernate.isInitialized( pRead.containedChildren.iterator().next().child ) );
				}
		);

		scope.inTransaction(
				session ->
						session.remove( p )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HHH-13045")
	public void testAccessIdOfManyToOneInEmbeddable(SessionFactoryScope scope) {
		Parent p = new Parent();
		p.containedChildren.add( new ContainedChild( new Child() ) );

		scope.inTransaction(
				session ->
						session.persist( p )
		);

		scope.inTransaction(
				session ->
						assertFalse( session.createQuery(
								"from Parent p join p.containedChildren c where c.child.id is not null" )
											 .getResultList()
											 .isEmpty() )

		);

		scope.inTransaction(
				session ->
						session.remove( p )
		);
	}

	@Entity(name = "Parent")
	public static class Parent {
		@Id
		@GeneratedValue
		private int id;

		private ContainedChild containedChild;

		@ElementCollection
		private Set<ContainedChild> containedChildren = new HashSet<>();
	}

	@Entity(name = "Child")
	public static class Child {
		@Id
		@GeneratedValue
		private int id;

	}

	@Embeddable
	public static class ContainedChild {
		@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		private Child child;

		ContainedChild() {
		}

		ContainedChild(Child child) {
			this.child = child;
		}
	}


}
