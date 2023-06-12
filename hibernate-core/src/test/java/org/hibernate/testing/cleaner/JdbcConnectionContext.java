/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.cleaner;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

import org.hibernate.cfg.AvailableSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>NUODB OVERRIDE CLASS</b>
 * <p>
 * <b>NuoDB:</b> The default {@code hibernate.properties} has {@code @xxx@}
 * place holders that the matrix plug-in sets before each run. When running
 * tests locally (that is, when NOT running the matrix tests) code has been
 * modified to use the NuoDB specific {@code hibernate.properties} in
 * {@code ../database/nuodb}.
 * 
 * @author Christian Beikov
 */
public final class JdbcConnectionContext {
	// --- NUODB Start
	public static final String HIBERNATE_CORE_DIR_NAME = "hibernate-core";
	// --- NUODB End

	private static final Driver driver;
	private static final String url;
	private static final String user;
	private static final String password;
	private static final Properties properties;

	static {
		final Properties connectionProperties = new Properties();

		// --- NUODB Start
		// Use NuoDB's hibernate.properties when running tests locally (that is, when
		// NOT running matrix tests)
		Logger logger = LoggerFactory.getLogger(JdbcConnectionContext.class);
		logger.info("Using custom JdbcConnectionContext class");
		String hibernatePropertiesFile = "hibernate.properties";
		File projectDir = new File(".").getAbsoluteFile();

		// Matrix tests make target/matrix/<db-name> the current directory and
		// automatically setup the correct 'hibernate.properties'- file for the database
		// being tested.
		if (projectDir.getName().equals(HIBERNATE_CORE_DIR_NAME) //
				|| projectDir.getParentFile().getName().equals(HIBERNATE_CORE_DIR_NAME)) {

			// Running tests locally
			hibernatePropertiesFile = "hibernate-nuodb.properties";
			logger.info("Using 'hibernate-nuodb.properties'");
		}
		// --- NUODB End

		try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(hibernatePropertiesFile)) {
			connectionProperties.load(inputStream);
			final String driverClassName = connectionProperties.getProperty(AvailableSettings.DRIVER);
			driver = (Driver) Class.forName(driverClassName).newInstance();
			url = connectionProperties.getProperty(AvailableSettings.URL);
			user = connectionProperties.getProperty(AvailableSettings.USER);
			password = connectionProperties.getProperty(AvailableSettings.PASS);

			Properties p = new Properties();

			if (user != null) {
				p.put("user", user);
			}

			if (password != null) {
				p.put("password", password);
			}

			properties = p;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void work(ConnectionConsumer work) {
		try (Connection connection = driver.connect(url, properties)) {
			connection.setAutoCommit(false);
			work.consume(connection);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static <R> R workReturning(ConnectionFunction<R> work) {
		try (Connection connection = driver.connect(url, properties)) {
			connection.setAutoCommit(false);
			return work.apply(connection);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static interface ConnectionConsumer {
		void consume(Connection c) throws Exception;
	}

	public static interface ConnectionFunction<R> {
		R apply(Connection c) throws Exception;
	}

	private JdbcConnectionContext() {
	}
}
