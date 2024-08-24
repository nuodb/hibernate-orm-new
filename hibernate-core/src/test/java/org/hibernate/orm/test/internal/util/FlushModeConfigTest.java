/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.internal.util;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import org.hibernate.jpa.HibernateHints;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.BaseUnitTest;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nathan Xu
 */
@TestForIssue( jiraKey = "HHH-13677" )
@BaseUnitTest
public class FlushModeConfigTest {
	@ParameterizedTest
	@EnumSource( FlushMode.class )
	public void testFlushModeSettingTakingEffect(FlushMode flushMode) {
		final StandardServiceRegistry serviceRegistry = ServiceRegistryUtil.serviceRegistryBuilder()
				.applySetting(HibernateHints.HINT_FLUSH_MODE, flushMode.name() )
				.build();
		try ( final SessionFactory sessionFactory = new MetadataSources(serviceRegistry).buildMetadata().buildSessionFactory() ) {
			try ( final Session session = sessionFactory.openSession() ) {
				assertThat( session.getHibernateFlushMode() ).isEqualTo( flushMode );
			}
		}
		finally {
			StandardServiceRegistryBuilder.destroy( serviceRegistry );
		}
	}
}
