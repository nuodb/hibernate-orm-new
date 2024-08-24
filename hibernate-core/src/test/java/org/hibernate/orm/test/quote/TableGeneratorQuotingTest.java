/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.quote;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator;
import org.hibernate.orm.test.util.DdlTransactionIsolatorTestingImpl;
import org.hibernate.tool.hbm2ddl.SchemaValidator;
import org.hibernate.tool.schema.internal.SchemaCreatorImpl;
import org.hibernate.tool.schema.internal.SchemaDropperImpl;
import org.hibernate.tool.schema.internal.exec.GenerationTarget;
import org.hibernate.tool.schema.internal.exec.GenerationTargetToDatabase;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.BaseUnitTest;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;


/**
 * @author Steve Ebersole
 */
@BaseUnitTest
public class TableGeneratorQuotingTest {
	private StandardServiceRegistry serviceRegistry;

	@BeforeEach
	public void setUp() {
		serviceRegistry = ServiceRegistryUtil.serviceRegistryBuilder()
				.applySetting( AvailableSettings.GLOBALLY_QUOTED_IDENTIFIERS, "true" )
				.build();
	}

	@AfterEach
	public void tearDown() {
		if ( serviceRegistry != null ) {
			StandardServiceRegistryBuilder.destroy( serviceRegistry );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HHH-7927")
	public void testTableGeneratorQuoting() {
		final Metadata metadata = new MetadataSources( serviceRegistry ).addAnnotatedClass( TestEntity.class )
				.buildMetadata();

		final ConnectionProvider connectionProvider = serviceRegistry.getService( ConnectionProvider.class );

		final DdlTransactionIsolatorTestingImpl ddlTransactionIsolator = new DdlTransactionIsolatorTestingImpl(
				serviceRegistry,
				new JdbcEnvironmentInitiator.ConnectionProviderJdbcConnectionAccess( connectionProvider )
		);
		try {
			final GenerationTarget target = new GenerationTargetToDatabase( ddlTransactionIsolator, false );
			try {
				new SchemaCreatorImpl( serviceRegistry ).doCreation( metadata, false, target );
				new SchemaValidator().validate( metadata );
			}
			catch (HibernateException e) {
				fail( "The identifier generator table should have validated.  " + e.getMessage() );
			}
			finally {
				new SchemaDropperImpl( serviceRegistry ).doDrop( metadata, false, target );
			}
		}
		finally {
			ddlTransactionIsolator.release();
		}
	}

	@Entity
	@Table(name = "test_entity")
	private static class TestEntity {
		@Id
		@GeneratedValue(strategy = GenerationType.TABLE)
		private int id;
	}
}
