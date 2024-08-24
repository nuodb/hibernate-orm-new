/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.caching.mocked;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.cache.internal.CacheKeyImplementation;
import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Gail Badner
 */
public class CacheKeyImplementationHashCodeTest {

	@Test
	@TestForIssue( jiraKey = "HHH-12746")
	public void test() {
		try (ServiceRegistryImplementor serviceRegistry = ServiceRegistryUtil.serviceRegistry()) {
			MetadataSources ms = new MetadataSources( serviceRegistry );
			ms.addAnnotatedClass( AnEntity.class ).addAnnotatedClass( AnotherEntity.class );
			Metadata metadata = ms.buildMetadata();
			final SessionFactoryBuilder sfb = metadata.getSessionFactoryBuilder();
			try ( SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) sfb.build()) {
				CacheKeyImplementation anEntityCacheKey = createCacheKeyImplementation(
						1,
						sessionFactory.getRuntimeMetamodels()
								.getMappingMetamodel()
								.getEntityDescriptor( AnEntity.class ),
						sessionFactory
				);
				CacheKeyImplementation anotherEntityCacheKey = createCacheKeyImplementation(
						1,
						sessionFactory.getRuntimeMetamodels()
								.getMappingMetamodel()
								.getEntityDescriptor( AnotherEntity.class ),
						sessionFactory
				);
				assertFalse( anEntityCacheKey.equals( anotherEntityCacheKey ) );
			}
		}
	}

	private CacheKeyImplementation createCacheKeyImplementation(
			int id,
			EntityPersister persister,
			SessionFactoryImplementor sfi) {
		return (CacheKeyImplementation) DefaultCacheKeysFactory.staticCreateEntityKey( id, persister, sfi, "tenant" );
	}

	@Entity(name = "AnEntity")
	@Cache( usage = CacheConcurrencyStrategy.READ_WRITE)
	public static class AnEntity {
		@Id
		@GeneratedValue
		private int id;
	}

	@Entity(name = "AnotherEntity")
	@Cache( usage = CacheConcurrencyStrategy.READ_WRITE)
	public static class AnotherEntity {
		@Id
		@GeneratedValue
		private int id;
	}

}
