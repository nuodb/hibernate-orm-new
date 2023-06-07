/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

//$Id$
package org.hibernate.orm.test.annotations;

import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import org.hibernate.testing.ServiceRegistryBuilder;
import org.hibernate.testing.util.ExceptionUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Emmanuel Bernard
 */
public class SecuredBindingTest {


	public static final String BAD_HSQLDB_JDBC_DRIVER_CLASSNAME = "org.hsqldb.jdbcDrive";

	@Test
	public void testConfigurationMethods() {
		Configuration ac = new Configuration();

		// Test should use these properties and ignore hibernate.properties on classpath.
		Properties p = new Properties();
		p.put( Environment.DIALECT, "org.hibernate.dialect.HSQLDialect" );

		// Deliberate error in classname - Drive not Driver
		p.put( "hibernate.connection.driver_class", BAD_HSQLDB_JDBC_DRIVER_CLASSNAME );

		p.put( "hibernate.connection.url", "jdbc:hsqldb:." );
		p.put( "hibernate.connection.username", "sa" );
		p.put( "hibernate.connection.password", "" );
		p.put( "hibernate.show_sql", "true" );
		ac.setProperties( p );
		ac.addAnnotatedClass( Plane.class );
		SessionFactory sf=null;
		ServiceRegistry serviceRegistry = null;
		try {
			serviceRegistry = ServiceRegistryBuilder.buildServiceRegistry( p );
			sf = ac.buildSessionFactory( serviceRegistry );
			try {
				sf.close();
			}
			catch (Exception ignore) {
			}

			Assert.fail( "Driver property overriding should work" );
		}
		catch (HibernateException he) {
			Throwable cause = ExceptionUtil.rootCause(he);
			String exceptionMessage = cause.getLocalizedMessage();
			String errorInfo = "Expected ClassNotFoundException for " + BAD_HSQLDB_JDBC_DRIVER_CLASSNAME + //
					", but got " + cause.getClass().getName() + ": " + exceptionMessage;
			Assert.assertTrue( errorInfo, cause instanceof ClassNotFoundException //
					&& exceptionMessage.contains(BAD_HSQLDB_JDBC_DRIVER_CLASSNAME));
			//success
		}
		finally {
			if(sf!=null){
				sf.close();
			}
			if ( serviceRegistry != null ) {
				ServiceRegistryBuilder.destroy( serviceRegistry );
			}
			ac.getStandardServiceRegistryBuilder().getBootstrapServiceRegistry().close();
		}
	}
}

