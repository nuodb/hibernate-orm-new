/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.jpa.boot;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.jpa.HibernatePersistenceConfiguration;

import org.hibernate.testing.env.TestingDatabaseInfo;
import org.hibernate.testing.orm.junit.RequiresDialect;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceConfiguration;

import static jakarta.persistence.PersistenceConfiguration.JDBC_PASSWORD;
import static jakarta.persistence.PersistenceConfiguration.JDBC_URL;
import static jakarta.persistence.PersistenceConfiguration.JDBC_USER;

/**
 * Simple tests for {@linkplain PersistenceConfiguration} and {@linkplain HibernatePersistenceConfiguration}.
 *
 * @author Steve Ebersole
 */
public class PersistenceConfigurationTests {
	@Test
	@RequiresDialect( H2Dialect.class )
	void testBaseJpa() {
		try (EntityManagerFactory emf = new PersistenceConfiguration( "emf" ).createEntityManagerFactory()) {
			assert emf.isOpen();
		}
		try (EntityManagerFactory emf = new HibernatePersistenceConfiguration( "emf" ).createEntityManagerFactory()) {
			assert emf.isOpen();
		}
	}

	@Test
	@RequiresDialect( H2Dialect.class )
	void testCallPersistence() {
		final PersistenceConfiguration cfg1 = new PersistenceConfiguration( "emf" );
		try (EntityManagerFactory emf = Persistence.createEntityManagerFactory( cfg1 )) {
			assert emf.isOpen();
		}

		final HibernatePersistenceConfiguration cfg2 = new HibernatePersistenceConfiguration( "emf" );
		try (EntityManagerFactory emf = Persistence.createEntityManagerFactory( cfg2 )) {
			assert emf.isOpen();
		}
	}

	@Test
	@RequiresDialect( H2Dialect.class )
	void testJdbcData() {
		final PersistenceConfiguration cfg = new PersistenceConfiguration( "emf" );
		TestingDatabaseInfo.forEachSetting( cfg::property );
		try (EntityManagerFactory emf = cfg.createEntityManagerFactory()) {
			assert emf.isOpen();
		}

		final PersistenceConfiguration cfg2 = new PersistenceConfiguration( "emf" );
		TestingDatabaseInfo.forEachSetting( cfg2::property );
		try (EntityManagerFactory emf = cfg2.createEntityManagerFactory()) {
			assert emf.isOpen();
		}

		final PersistenceConfiguration cfg3 = new HibernatePersistenceConfiguration( "emf" );
		TestingDatabaseInfo.forEachSetting( cfg3::property );
		try (EntityManagerFactory emf = cfg3.createEntityManagerFactory()) {
			assert emf.isOpen();
		}

		final PersistenceConfiguration cfg4 = new HibernatePersistenceConfiguration( "emf" )
				.jdbcDriver( TestingDatabaseInfo.DRIVER )
				.jdbcUrl( TestingDatabaseInfo.URL )
				.jdbcUsername( TestingDatabaseInfo.USER )
				.jdbcPassword( TestingDatabaseInfo.PASS );
		try (EntityManagerFactory emf = cfg4.createEntityManagerFactory()) {
			assert emf.isOpen();
		}
	}

	@Test
	@RequiresDialect( H2Dialect.class )
	public void testForUserGuide() {
		{
			//tag::example-bootstrap-standard-PersistenceConfiguration[]
			final PersistenceConfiguration cfg = new PersistenceConfiguration( "emf" )
					.property( JDBC_URL, "jdbc:h2:mem:db1" )
					.property( JDBC_USER, "sa" )
					.property( JDBC_PASSWORD, "" );
			try (EntityManagerFactory emf = cfg.createEntityManagerFactory()) {
				assert emf.isOpen();
			}
			//end::example-bootstrap-standard-PersistenceConfiguration[]
		}

		{
			//tag::example-bootstrap-standard-HibernatePersistenceConfiguration[]
			final PersistenceConfiguration cfg = new HibernatePersistenceConfiguration( "emf" )
					.jdbcUrl( "jdbc:h2:mem:db1" )
					.jdbcUsername( "sa" )
					.jdbcPassword( "" );
			try (EntityManagerFactory emf = cfg.createEntityManagerFactory()) {
				assert emf.isOpen();
			}
			//end::example-bootstrap-standard-HibernatePersistenceConfiguration[]
		}
	}
}
