/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.jpa.persistenceunit;

import java.util.Collections;
import java.util.Map;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SharedCacheMode;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jpa.boot.spi.Bootstrap;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.hibernate.orm.test.jpa.BaseEntityManagerFunctionalTestCase;
import org.hibernate.persister.entity.EntityPersister;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Gail Badner
 */
public class TwoPersistenceUnits2LCDisabledEnabled {

	@Test
	@TestForIssue( jiraKey = "HHH-11516" )
	public void testDisabledEnabled() {
		final Map<Object, Object> config = Environment.getProperties();
		config.put( AvailableSettings.LOADED_CLASSES, Collections.singletonList( AnEntity.class ) );
		config.put( "javax.persistence.sharedCache.mode", SharedCacheMode.ENABLE_SELECTIVE );
		config.put( AvailableSettings.USE_SECOND_LEVEL_CACHE, "false" );
		ServiceRegistryUtil.applySettings( config );

		testIt( config );

		config.put( AvailableSettings.USE_SECOND_LEVEL_CACHE, "true" );

		testIt( config );
	}

	private void testIt(Map config) {
		EntityManagerFactoryBuilder entityManagerFactoryBuilder = Bootstrap.getEntityManagerFactoryBuilder(
				new BaseEntityManagerFunctionalTestCase.TestingPersistenceUnitDescriptorImpl( getClass().getSimpleName() ),
				config
		);
		SessionFactoryImplementor sf = entityManagerFactoryBuilder.build().unwrap( SessionFactoryImplementor.class );
		final EntityPersister persister = sf.getMappingMetamodel().getEntityDescriptor( AnEntity.class.getName() );

		try {
			if ( config.get( AvailableSettings.USE_SECOND_LEVEL_CACHE ).equals( "true" ) ) {
				assertNotNull( persister.getCacheAccessStrategy() );
			}
			else {
				assertNull( persister.getCacheAccessStrategy() );
			}
		}
		finally {
			sf.close();
		}
	}


	@Cacheable
	@Entity( name = "AnEntity" )
	public static class AnEntity {
		@Id
		private Long id;
	}
}
