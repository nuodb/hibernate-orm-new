/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.tool.schema.internal;

import java.util.Collection;
import java.util.Optional;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.boot.model.relational.internal.SqlStringGenerationContextImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.mapping.ForeignKey;

import org.hibernate.testing.RequiresDialect;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.util.ServiceRegistryUtil;

import org.junit.Test;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import static org.junit.Assert.assertEquals;

/**
 * @author Jan Schatteman
 */
@RequiresDialect( value = H2Dialect.class )
@TestForIssue( jiraKey = "HHH-15704")
public class StandardForeignKeyExporterTest {

	@Test
	public void testForeignKeySqlStringForCompositePK() {
		try (final StandardServiceRegistry ssr = ServiceRegistryUtil.serviceRegistry()) {
			final MetadataImplementor bootModel = (MetadataImplementor) new MetadataSources( ssr )
					.addAnnotatedClass( CompositePk.class )
					.addAnnotatedClass( Person.class )
					.buildMetadata();
			Database database = bootModel.getDatabase();
			SqlStringGenerationContext sqlStringGenerationContext =
					SqlStringGenerationContextImpl.forTests( database.getJdbcEnvironment() );

			Collection<ForeignKey>  fks = database.getDefaultNamespace().locateTable( Identifier.toIdentifier( "PERSON" ) ).getForeignKeys().values();
			assertEquals( fks.size(), 1 );
			final Optional<ForeignKey> foreignKey = fks.stream().findFirst();

			final String[] sqlCreateStrings = new H2Dialect().getForeignKeyExporter().getSqlCreateStrings(
					foreignKey.get(),
					bootModel,
					sqlStringGenerationContext
			);
			assertEquals( sqlCreateStrings.length, 1 );
			assertEquals( sqlCreateStrings[0], "alter table if exists PERSON add constraint fk_firstLastName foreign key (pkFirstName, pkLastName) references PERSON" );
		}
	}

	@Entity
	@Table(name = "PERSON")
	public static class Person {
		@Id
		private CompositePk id;

		@OneToOne
		@JoinColumns(
				value = {
						@JoinColumn(name = "pkFirstName", referencedColumnName = "firstName"),
						@JoinColumn(name = "pkLastName", referencedColumnName = "lastName")
				},
				foreignKey = @jakarta.persistence.ForeignKey(name = "fk_firstLastName")
		)
		private Person peer;
	}

	@Embeddable
	public static class CompositePk {
		private String firstName;
		private String lastName;
	}

}
