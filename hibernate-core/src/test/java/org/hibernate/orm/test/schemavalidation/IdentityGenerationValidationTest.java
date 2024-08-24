/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.schemavalidation;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.tool.hbm2ddl.SchemaValidator;

import org.hibernate.testing.RequiresDialect;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Jan Schatteman
 */
@TestForIssue( jiraKey = "HHH-13106" )
@RequiresDialect( value = PostgreSQLDialect.class )
public class IdentityGenerationValidationTest extends BaseCoreFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] {TestEntity.class};
	}

	@Test
	public void testSynonymUsingIndividuallySchemaValidator() {
		StandardServiceRegistry ssr = ServiceRegistryUtil.serviceRegistry();
		try {
			final MetadataSources metadataSources = new MetadataSources( ssr );
			metadataSources.addAnnotatedClass( TestEntity.class );

			new SchemaValidator().validate( metadataSources.buildMetadata() );
		}
		finally {
			StandardServiceRegistryBuilder.destroy( ssr );
		}
	}

	@Entity
	@Table(name = "test_entity")
	private static class TestEntity {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;
	}

}
