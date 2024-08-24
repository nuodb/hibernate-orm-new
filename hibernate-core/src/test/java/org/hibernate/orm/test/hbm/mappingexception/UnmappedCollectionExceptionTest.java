package org.hibernate.orm.test.hbm.mappingexception;

import org.hibernate.MappingException;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class UnmappedCollectionExceptionTest {

	@Test
	@TestForIssue(jiraKey = "HHH-15379")
	public void mappingExceptionTest() {
		StandardServiceRegistry ssr = ServiceRegistryUtil.serviceRegistry();
		try {
			assertThrows( MappingException.class, () -> {
				new MetadataSources( ssr )
						.addResource( "org/hibernate/orm/test/hbm/mappingexception/unmapped_collection.hbm.xml" )
						.buildMetadata();

			} );
		}
		finally {
			ssr.close();
		}

	}
}
