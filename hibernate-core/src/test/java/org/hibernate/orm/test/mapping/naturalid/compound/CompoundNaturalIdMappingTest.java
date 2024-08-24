/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.mapping.naturalid.compound;

import java.util.List;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.NaturalIdMapping;
import org.hibernate.metamodel.mapping.SingularAttributeMapping;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Steve Ebersole
 */
@TestForIssue(jiraKey = "HHH-11255")
public class CompoundNaturalIdMappingTest {

	@Test
	public void test() {
		final StandardServiceRegistry ssr = ServiceRegistryUtil.serviceRegistry();

		try {
			Metadata meta = new MetadataSources( ssr )
					.addAnnotatedClass( PostalCarrier.class )
					.addAnnotatedClass( Country.class )
					.buildMetadata();
			( (MetadataImplementor) meta ).orderColumns( false );
			( (MetadataImplementor) meta ).validate();

			final SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) meta.buildSessionFactory();

			try {
				final EntityMappingType entityMappingType = sessionFactory.getRuntimeMetamodels().getEntityMappingType( PostalCarrier.class );
				final NaturalIdMapping naturalIdMapping = entityMappingType.getNaturalIdMapping();
				final List<SingularAttributeMapping> naturalIdAttributes = naturalIdMapping.getNaturalIdAttributes();

				assertThat( naturalIdAttributes.size(), is( 2 ) );
				assertThat( naturalIdAttributes.get( 0 ).getAttributeName(), is( "code" ) );
				assertThat( naturalIdAttributes.get( 1 ).getAttributeName(), is( "country" ) );
			}
			finally {
				sessionFactory.close();
			}
		}
		finally {
			StandardServiceRegistryBuilder.destroy( ssr );
		}
	}
}
