/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.id;

import java.io.Serializable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;

import org.hibernate.orm.test.jpa.BaseEntityManagerFunctionalTestCase;

import org.hibernate.testing.TestForIssue;
import org.junit.Test;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.Assert.assertTrue;

/**
 * @author Vlad Mihalcea
 */
@TestForIssue( jiraKey = "HHH-12251" )
public class IdClassManyToOneCascadeTest extends BaseEntityManagerFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] {
				SomeEntity.class,
				ReferencedEntity.class
		};
	}

	@Test
	public void testMergeCascadesToManyToOne() {

		doInJPA( this::entityManagerFactory, entityManager -> {
			ReferencedEntity referencedEntity = new ReferencedEntity();
			referencedEntity.setId( 42L );

			SomeEntity someEntity = new SomeEntity();
			someEntity.setId( 23L );
			someEntity.setReferencedEntity( referencedEntity );

			SomeEntity merged = entityManager.merge(someEntity);

			assertTrue( entityManager.contains( merged.getReferencedEntity() ) );
		} );
	}

	@Test
	public void testPersistCascadesToManyToOne() {

		doInJPA( this::entityManagerFactory, entityManager -> {
			ReferencedEntity referencedEntity = new ReferencedEntity();
			referencedEntity.setId( 42L );

			SomeEntity someEntity = new SomeEntity();
			someEntity.setId( 23L );
			someEntity.setReferencedEntity( referencedEntity );

			entityManager.persist( someEntity );

			assertTrue( entityManager.contains( referencedEntity ) );
		} );
	}

	@Entity(name = "SomeEntity")
	@IdClass(SomeEntityPK.class)
	public static class SomeEntity {

		@Id
		private long id;

		@Id
		@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
		private ReferencedEntity referencedEntity;

		public ReferencedEntity getReferencedEntity() {
			return referencedEntity;
		}

		public void setReferencedEntity(ReferencedEntity referencedEntity) {
			this.referencedEntity = referencedEntity;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}
	}

	public static class SomeEntityPK implements Serializable {

		private Long id;
		private Long referencedEntity;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getReferencedEntity() {
			return referencedEntity;
		}

		public void setReferencedEntity(Long referencedEntity) {
			this.referencedEntity = referencedEntity;
		}
	}

	@Entity(name = "ReferencedEntity")
	public static class ReferencedEntity {

		@Id
		private Long id;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}
	}
}
