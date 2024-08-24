/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.idgen.enhanced.table;

import java.util.Properties;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.boot.model.relational.internal.SqlStringGenerationContextImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.mapping.Table;
import org.hibernate.type.StandardBasicTypes;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class Db2GenerationTest {
	@Test
	@TestForIssue( jiraKey = "HHH-9850" )
	public void testNewGeneratorTableCreationOnDb2() {
		final StandardServiceRegistry ssr = ServiceRegistryUtil.serviceRegistryBuilder()
				.applySetting( AvailableSettings.DIALECT, DB2Dialect.class.getName() )
				.build();

		try {
			final Metadata metadata = new MetadataSources( ssr ).buildMetadata();

			assertEquals( 0, metadata.getDatabase().getDefaultNamespace().getTables().size() );

			final TableGenerator generator = new TableGenerator();

			generator.configure(
					metadata.getDatabase()
							.getTypeConfiguration()
							.getBasicTypeRegistry()
							.resolve( StandardBasicTypes.INTEGER ),
					new Properties(),
					ssr
			);

			generator.registerExportables( metadata.getDatabase() );

			assertEquals( 1, metadata.getDatabase().getDefaultNamespace().getTables().size() );

			Database database = metadata.getDatabase();
			final Table table = database.getDefaultNamespace().getTables().iterator().next();
			SqlStringGenerationContext sqlStringGenerationContext =
					SqlStringGenerationContextImpl.forTests( database.getJdbcEnvironment() );
			final String[] createCommands = new DB2Dialect().getTableExporter().getSqlCreateStrings( table, metadata,
					sqlStringGenerationContext
			);
			assertThat( createCommands[0], containsString( "sequence_name varchar(255) not null" ) );
		}
		finally {
			StandardServiceRegistryBuilder.destroy( ssr );
		}
	}

}
