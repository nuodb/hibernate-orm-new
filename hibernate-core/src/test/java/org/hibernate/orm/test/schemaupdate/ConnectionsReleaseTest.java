/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.schemaupdate;

import java.util.EnumSet;
import java.util.Properties;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.internal.util.PropertiesHelper;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.hbm2ddl.SchemaValidator;
import org.hibernate.tool.schema.TargetType;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.jdbc.SharedDriverManagerConnectionProviderImpl;
import org.hibernate.testing.junit4.BaseUnitTestCase;
import org.hibernate.testing.orm.junit.RequiresDialect;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HHH-10443")
@RequiresDialect( H2Dialect.class )
public class ConnectionsReleaseTest extends BaseUnitTestCase {

	private StandardServiceRegistry ssr;
	private MetadataImplementor metadata;
	private SharedDriverManagerConnectionProviderImpl connectionProvider;

	@Before
	public void setUp() {
		connectionProvider = SharedDriverManagerConnectionProviderImpl.getInstance();

		ssr = ServiceRegistryUtil.serviceRegistryBuilder()
				.addService( ConnectionProvider.class, connectionProvider )
				.build();
		metadata = (MetadataImplementor) new MetadataSources( ssr )
				.addAnnotatedClass( Thing.class )
				.buildMetadata();
		metadata.orderColumns( false );
		metadata.validate();
	}

	@After
	public void tearDown() {
		StandardServiceRegistryBuilder.destroy( ssr );
	}

	@Test
	public void testSchemaUpdateReleasesAllConnections() {
		new SchemaUpdate().execute( EnumSet.of( TargetType.DATABASE ), metadata );
		assertThat( connectionProvider.getOpenConnections(), is( 0 ) );
	}

	@Test
	public void testSchemaValidatorReleasesAllConnections() {
		new SchemaValidator().validate( metadata );
		assertThat( connectionProvider.getOpenConnections(), is( 0 ) );
	}

	@Entity(name = "Thing")
	@Table(name = "Thing")
	public static class Thing {
		@Id
		public Integer id;
	}

}
