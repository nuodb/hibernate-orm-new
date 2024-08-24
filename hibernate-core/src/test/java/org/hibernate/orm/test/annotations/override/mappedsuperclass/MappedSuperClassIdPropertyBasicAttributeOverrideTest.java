package org.hibernate.orm.test.annotations.override.mappedsuperclass;

import org.hibernate.MappingException;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * @author Stanislav Gubanov
 */
@TestForIssue(jiraKey = "HHH-11771")
public class MappedSuperClassIdPropertyBasicAttributeOverrideTest {

	@Test
	public void test() {
		try (StandardServiceRegistry ssr = ServiceRegistryUtil.serviceRegistry()) {

			MetadataSources metadataSources = new MetadataSources( ssr );
			metadataSources.addAnnotatedClasses( MappedSuperClassWithUuidAsBasic.class );
			metadataSources.addAnnotatedClasses( SubclassWithUuidAsId.class );

			MetadataImplementor metadata = (MetadataImplementor) metadataSources.buildMetadata();
			metadata.buildSessionFactory();
			fail( "Should throw exception!" );
		}
		catch (MappingException expected) {
			assertEquals(
					"You cannot override the [uid] non-identifier property from the [org.hibernate.orm.test.annotations.override.mappedsuperclass.MappedSuperClassWithUuidAsBasic] base class or @MappedSuperclass and make it an identifier in the [org.hibernate.orm.test.annotations.override.mappedsuperclass.SubclassWithUuidAsId] subclass",
					expected.getMessage()
			);
		}
	}

}
