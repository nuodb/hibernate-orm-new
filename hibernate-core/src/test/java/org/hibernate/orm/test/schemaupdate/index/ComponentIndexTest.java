/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.schemaupdate.index;

import java.util.List;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.internal.SchemaCreatorImpl;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Andrea Boriero
 */
@TestForIssue( jiraKey = "HHH-11815")
public class ComponentIndexTest {
	private StandardServiceRegistry ssr;
	private Metadata metadata;

	@Before
	public void setUp(){
		ssr = ServiceRegistryUtil.serviceRegistry();
		metadata = new MetadataSources( ssr )
				.addAnnotatedClass( User.class )
				.buildMetadata();
	}

	@Test
	public void testTheIndexIsGenerated() {
		final List<String> commands = new SchemaCreatorImpl( ssr ).generateCreationCommands(
				metadata,
				false
		);

		assertThatCreateIndexCommandIsGenerated( commands );
	}

	private void assertThatCreateIndexCommandIsGenerated(List<String> commands) {
		boolean createIndexCommandIsGenerated = false;
		for ( String command : commands ) {
			if ( command.toLowerCase().contains( "create index city_index" ) ) {
				createIndexCommandIsGenerated = true;
			}
		}
		assertTrue(
				"Expected create index command not found",
				createIndexCommandIsGenerated
		);
	}

	@After
	public void tearDown(){
		StandardServiceRegistryBuilder.destroy( ssr );
	}

	@Entity(name = "user")
	@Table(indexes = @Index(name = "city_index", columnList = "city"))
	public class User {
		@Id
		private Long id;
		@Embedded
		private Address address;
	}

	@Embeddable
	public class Address {
		private String city;
		private String street;
		private String postalCode;
	}
}
