/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.cache;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.AnnotationException;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.internal.EntityBinder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.cache.CachingRegionFactory;
import org.hibernate.testing.logger.LoggerInspectionRule;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.Rule;
import org.junit.Test;

import org.jboss.logging.Logger;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.SharedCacheMode;

import static org.junit.Assert.fail;

/**
 * @author Gail Badner
 */
@TestForIssue( jiraKey = "HHH-11143")
public class NonRootEntityWithCacheAnnotationTest {

	@Rule
	public LoggerInspectionRule logInspection = new LoggerInspectionRule(
			Logger.getMessageLogger( CoreMessageLogger.class, EntityBinder.class.getName() )
	);

	@Test
	public void testCacheOnNonRootEntity() {
		Map<String,Object> settings = new HashMap<>();
		settings.put( Environment.CACHE_REGION_FACTORY, CachingRegionFactory.class.getName() );
		settings.put( AvailableSettings.JPA_SHARED_CACHE_MODE, SharedCacheMode.ENABLE_SELECTIVE );

		try (ServiceRegistryImplementor serviceRegistry = (ServiceRegistryImplementor) ServiceRegistryUtil.serviceRegistryBuilder()
				.applySettings( settings )
				.build()) {
			try {
				new MetadataSources( serviceRegistry )
						.addAnnotatedClass( ABase.class )
						.addAnnotatedClass( AEntity.class )
						.buildMetadata();
				fail("No error for @Cache on subclass entity");
			}
			catch (AnnotationException ae) {
				//exception required
			}
		}
	}

	@Entity
	@Inheritance
	public static class ABase {
		@Id
		private Long id;
	}

	@Entity
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public static class AEntity extends ABase {
		private String name;
	}
}
