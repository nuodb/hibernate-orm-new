package org.hibernate.testing.cleaner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.testing.support.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nuodb.jdbc.DataSource;

/**
 * NuoDB specific class for removing schemas, typically before running a test.
 * 
 * @author Paul Chapman
 */
public class NuoDBDatabaseCleaner implements DatabaseCleaner {

	private static Logger LOGGER = LoggerFactory.getLogger(NuoDBDatabaseCleaner.class);

	@Override
	public void addIgnoredTable(String tableName) {
		// None to ignore
	}

	@Override
	public boolean isApplicable(Connection connection) {
		try {
			boolean applicable = connection.getMetaData().getDriverName().contains("NuoDB");

			if (applicable)
				LOGGER.info("Using database cleaner for NuoDB");

			return applicable;
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public void clearAllSchemas(Connection connection) {
		try {
			ResultSet results = connection //
					.createStatement().executeQuery("show schemas");
			if (results.next()) {
				String schemas = results.getString(1);
				String[] lines = schemas.split("\\r?\\n");

				for (String line : lines) {
					line = line.trim();

					if (line.length() == 0 || line.startsWith("Found") || line.equalsIgnoreCase("System"))
						continue;

					clearSchema(connection, line);
				}

			} else {
				LOGGER.error("No schema list returned from NuoDB");
			}
		} catch (SQLException e) {
			TestUtils.logException(LOGGER, "Failed running 'show schemas'", e);
		}

	}

	@Override
	public void clearSchema(Connection connection, String schemaName) {
		LOGGER.info("  Dropping schema " + schemaName);

		try {
			connection.createStatement() //
					.execute("DROP SCHEMA " + schemaName + " CASCADE");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void clearAllData(Connection connection) {
		// Nothing to do?
	}

	@Override
	public void clearData(Connection connection, String schemaName) {
		// Nothing to do?
	}

	/**
	 * Test program.
	 * 
	 * @param args Unused
	 */
	public static void main(String[] args) {
		DataSource ds = new DataSource();
		ds.setUrl("jdbc:com.nuodb.hib://localhost/hibernate_orm_test");
		ds.setUser("hibernate_orm_test");
		ds.setPassword("hibernate_orm_test");

		try (Connection conn = ds.getConnection()) {
			NuoDBDatabaseCleaner cleaner = new NuoDBDatabaseCleaner();

			if (cleaner.isApplicable(conn))
				cleaner.clearAllSchemas(conn);
		} catch (SQLException e) {
			TestUtils.logException(LOGGER, "Error", e);
		}
	}
}
