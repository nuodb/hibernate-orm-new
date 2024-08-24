/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2014, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.orm.test.mapping.inheritance.discriminator;

import java.util.Set;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.JiraKey;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test cases for joined inheritance with eager fetching.
 *
 * @author Christian Beikov
 */
@DomainModel(
		annotatedClasses = {
				JoinedInheritanceEagerTest.BaseEntity.class,
				JoinedInheritanceEagerTest.EntityA.class,
				JoinedInheritanceEagerTest.EntityB.class,
				JoinedInheritanceEagerTest.EntityC.class,
				JoinedInheritanceEagerTest.EntityD.class
		}
)
@SessionFactory
public class JoinedInheritanceEagerTest {

	@BeforeEach
	public void setUp(SessionFactoryScope scope) {
		scope.inTransaction( session -> {
			EntityC entityC = new EntityC( 1L );
			EntityD entityD = new EntityD( 2L );

			EntityB entityB = new EntityB( 3L );
			entityB.setRelation( entityD );

			EntityA entityA = new EntityA( 4L );
			entityA.setRelation( entityC );

			session.persist( entityC );
			session.persist( entityD );
			session.persist( entityA );
			session.persist( entityB );
		} );
	}

	@AfterEach
	public void cleanUp(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					EntityA entityA = session.get( EntityA.class, 4L );
					EntityB entityB = session.get( EntityB.class, 3L );
					EntityD entityD = session.get( EntityD.class, 2L );
					EntityC entityC = session.get( EntityC.class, 1L );

					session.remove( entityD );
					session.remove( entityC );
					session.remove( entityA );
					session.remove( entityB );
				}
		);
	}

	@Test
	@JiraKey("HHH-12375")
	public void joinUnrelatedCollectionOnBaseType(SessionFactoryScope scope) {
		scope.inSession(
				session -> {
					session.getTransaction().begin();

					try {
						session.createQuery( "from BaseEntity b join b.attributes" ).list();
						fail( "Expected a resolution exception for property 'attributes'!" );
					}
					catch (IllegalArgumentException ex) {
						Assert.assertTrue( ex.getCause().getMessage().contains( "Could not resolve attribute 'attributes' "));
					}
					finally {
						session.getTransaction().commit();
					}
				}
		);

	}

	@Entity(name = "BaseEntity")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class BaseEntity {
		@Id
		private Long id;

		public BaseEntity() {
		}

		public BaseEntity(Long id) {
			this.id = id;
		}
	}

	@Entity(name = "EntityA")
	public static class EntityA extends BaseEntity {
		@OneToMany(fetch = FetchType.LAZY)
		private Set<EntityC> attributes;
		@ManyToOne(fetch = FetchType.EAGER)
		private EntityC relation;

		public EntityA() {
		}

		public EntityA(Long id) {
			super( id );
		}

		public void setRelation(EntityC relation) {
			this.relation = relation;
		}

		public EntityC getRelation() {
			return relation;
		}

		public Set<EntityC> getAttributes() {
			return attributes;
		}
	}

	@Entity(name = "EntityB")
	public static class EntityB extends BaseEntity {
		@OneToMany(fetch = FetchType.LAZY)
		private Set<EntityD> attributes;
		@ManyToOne(fetch = FetchType.EAGER)
		private EntityD relation;

		public EntityB() {
		}

		public EntityB(Long id) {
			super( id );
		}

		public void setRelation(EntityD relation) {
			this.relation = relation;
		}

		public EntityD getRelation() {
			return relation;
		}

		public Set<EntityD> getAttributes() {
			return attributes;
		}
	}

	@Entity(name = "EntityC")
	public static class EntityC {
		@Id
		private Long id;

		public EntityC() {
		}

		public EntityC(Long id) {
			this.id = id;
		}
	}

	@Entity(name = "EntityD")
	public static class EntityD {
		@Id
		private Long id;

		public EntityD() {
		}

		public EntityD(Long id) {
			this.id = id;
		}
	}
}
