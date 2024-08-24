/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.mapping.naturalid;

import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class ValidationTests {
	@Test
	void checkManyToOne(ServiceRegistryScope registryScope) {
		final StandardServiceRegistry registry = registryScope.getRegistry();
		final MetadataSources metadataSources = new MetadataSources( registry )
				.addAnnotatedClass( Thing1.class )
				.addAnnotatedClass( Thing2.class );
		try (final SessionFactory sessionFactory = metadataSources.buildMetadata().buildSessionFactory(); ) {
			fail( "Expecting an exception" );
		}
		catch (MappingException expected) {
			assertThat( expected.getMessage() )
					.startsWith( "Attribute marked as natural-id can not also be a not-found association - " );
		}
	}

	@Test
	void checkEmbeddable(ServiceRegistryScope registryScope) {
		final StandardServiceRegistry registry = registryScope.getRegistry();
		final MetadataSources metadataSources = new MetadataSources( registry )
				.addAnnotatedClass( Thing1.class )
				.addAnnotatedClass( Thing3.class )
				.addAnnotatedClass( Container.class );
		try (final SessionFactory sessionFactory = metadataSources.buildMetadata().buildSessionFactory(); ) {
			fail( "Expecting an exception" );
		}
		catch (MappingException expected) {
			assertThat( expected.getMessage() )
					.startsWith( "Attribute marked as natural-id can not also be a not-found association - " );
		}
	}

	@Entity(name="Thing1")
	@Table(name="thing_1")
	public static class Thing1 {
		@Id
		private Integer id;
		private String name;
	}

	@Entity(name="Thing2")
	@Table(name="thing_2")
	public static class Thing2 {
		@Id
		private Integer id;
		private String name;
		@NaturalId
		@ManyToOne
		@NotFound(action = NotFoundAction.IGNORE)
		private Thing1 thing1;
	}

	@Embeddable
	public static class Container {
		@NaturalId
		@ManyToOne
		@NotFound(action = NotFoundAction.IGNORE)
		private Thing1 thing1;
	}

	@Entity(name="Thing2")
	@Table(name="thing_2")
	public static class Thing3 {
		@Id
		private Integer id;
		private String name;
		@NaturalId
		@Embedded
		private Container container;
	}
}
