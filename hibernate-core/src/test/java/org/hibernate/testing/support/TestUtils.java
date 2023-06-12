package org.hibernate.testing.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.util.Strings;
import org.hibernate.JDBCException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.SQLGrammarException;
import org.junit.runners.model.FrameworkMethod;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handy utility functions used by NuoDB tests.
 * 
 * @author Paul Chapman
 *
 */
public class TestUtils {

	/**
	 * The NuoDB Hibernate JAR and Hibernate 6.1-6.2 are compiled for JDK 11.
	 * Previous JAR versions support Hibernate 5.x and Java 1.8.
	 */
	public static final int MINIMUM_JAVA_VERSION = 11;

	public static boolean firstTime = true;

	static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

	/**
	 * Checks that the supplied string a valid UUID. Uses asserts internally.
	 * 
	 * @param uuid A UUID as a string.
	 * 
	 * @throws AssertionFailedError If the string is not valid.
	 */
	public static void isValidUuid(String uuid) {
		LOGGER.warn("UUID = {}", uuid);
		assertEquals(36, uuid.length());
		assertEquals(5, uuid.split("-").length);
		assertEquals('-', uuid.charAt(8));
		assertEquals('-', uuid.charAt(13));
		assertEquals('-', uuid.charAt(18));
		assertEquals('-', uuid.charAt(23));
	}

	/**
	 * Returns the innermost cause of {@code throwable}. The first throwable in a
	 * chain provides context from when the error or exception was initially
	 * detected. Example usage:
	 * 
	 * <pre>
	 * assertEquals("Unable to assign a customer id", Throwables.getRootCause(e).getMessage());
	 * </pre>
	 */
	public static Throwable getRootCause(Throwable throwable) {
		Throwable cause;

		while ((cause = throwable.getCause()) != null) {
			throwable = cause;
		}

		return throwable;
	}

	/**
	 * Generates the following error message from the supplied arguments:
	 * {@code "<msg>: e.getLocalizedMessage() (<exception-type> )"}. If there is no
	 * exception message, then the output is {@code "<msg>: <exception-type>"}.
	 *
	 * @param msg Additional message text.
	 * @param e   The exception.
	 * 
	 * @return The error message string.
	 */
	public static String getExceptionInfo(String msg, Throwable e) {
		StringBuilder sb = new StringBuilder();
		sb.append(msg).append(": ");

		String emsg = e.getLocalizedMessage();
		String exceptionType = e.getClass().getSimpleName();

		if (Strings.isBlank(emsg))
			sb.append(exceptionType);
		else
			sb.append(emsg).append(" (").append(exceptionType).append(')');

		return sb.toString();
	}

	public static void logException(Object caller, String msg, Exception exception) {
		logException(LoggerFactory.getLogger(caller.getClass()), msg, exception);
	}

	/**
	 * Log an exception, including the exception type and any error message. See
	 * {@link #getExceptionInfo(String, Throwable)}.
	 * 
	 * @param logger The logger to use.
	 * @param msg    Additional message text.
	 * @param e      The exception to be logged.
	 */
	public static void logException(Logger logger, String msg, Exception e) {
		logger.error(getExceptionInfo(msg, e));
	}

	/**
	 * Get the name of the calling method.
	 * 
	 * @return The name of the method that called this one.
	 */
	public static String getFunctionName() {
		return getFunctionName(2);
	}

	/**
	 * Get the name of a method in the current stack. Depth 0 returns
	 * {@code getStackTrace()}, depth 1 returns the name of this method
	 * ({@code getFunctionName()}), so depth should be 2 or greater.
	 * 
	 * @param depth How far into the stack to go to find the method of interest.
	 *
	 * @return Method name.
	 */
	public static String getFunctionName(int depth) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		return depth < stackTrace.length //
				? stackTrace[depth].getMethodName()
				: stackTrace[stackTrace.length - 1].getMethodName();
	}

	public static void popupMessage(String message) {
		JOptionPane.showMessageDialog(null, message);
	}

	/**
	 * Convert the argument (which must be a date or time type) to a common String
	 * format of the form {@code yyyy-MM-dd hh:mm:ss}.
	 * <p>
	 * Performs the following tasks:
	 * <ul>
	 * <li>Remove letter 'T' - calling toString() on any of the old Date classes
	 * puts a T between the date and the time.
	 * <li>Remove any decimal seconds - toString() on the old Data classes includes
	 * them, but LocalDateTime.toString() does not.
	 * </ul>
	 * 
	 * @param dt An instance of an 'old' date class (Java util Date, SQL Date, SQL
	 *           Time, SQL Timestamp) or one of the 'new' time classes (LocalDate,
	 *           LocalTime, LocalDataTime or OffsetDateTime).
	 * @return Converts
	 */
	public static String toDateString(Object dt) {

		String str = dt.toString();

		if (dt instanceof String)
			return str;

		// The 'old' classes derived from java.util.Date.
		if (dt instanceof java.util.Date) {
			if (dt instanceof Date) {
				// Just get date, drop the time
				return str.substring(0, 10);
			} else if (dt instanceof Time) {
				// Drop sub-second decimal
				int ix = str.indexOf('.');
				return ix == -1 ? str : str.substring(0, ix);
			} else if (dt instanceof Timestamp) {
				// Drop sub-second decimal
				int ix = str.indexOf('.');
				return str.substring(0, ix);
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				return sdf.format(dt);
			}
		}

		// The 'new' time classes.
		if (dt instanceof LocalDate)
			return str;
		else if (dt instanceof LocalTime) {
			// Drop sub-second decimal
			int ix = str.indexOf('.');
			return ix == -1 ? str : str.substring(0, ix);
		} else if (dt instanceof LocalDateTime || dt instanceof Instant) {
			// Drop sub-second decimal and T between date and time
			int ix = str.indexOf('.');
			str = str.replace('T', ' ');
			return ix == -1 ? str : str.substring(0, ix);
		} else if (dt instanceof OffsetDateTime) {
			OffsetDateTime odt = (OffsetDateTime) dt;
			str = odt.toLocalDateTime().toString();

			// Drop sub-second decimal and T between date and time
			int ix = str.indexOf('.');
			str = str.replace('T', ' ');
			str = ix == -1 ? str : str.substring(0, ix);

			ZoneOffset offset = odt.getOffset();
			return str + offset;
		}

		throw new RuntimeException("Unexpected type " + dt.getClass().getName());
	}

	public static void validateJdkVersion(Class<?> testClass) {
		final String javaVersion = System.getProperty("java.version");

		// Get major version
		int ix = javaVersion.indexOf('.');
		String majorVersionStr = ix == -1 ? javaVersion : javaVersion.substring(0, ix);
		int majorVersion = Integer.parseInt(majorVersionStr);

		// Validate version. Print error and exit on fail (less verbose than throwing an
		// exception).
		if (majorVersion < MINIMUM_JAVA_VERSION) {
			LOGGER.error("Java version is " + javaVersion //
					+ ", but " + testClass.getName() + " must run using JDK " + MINIMUM_JAVA_VERSION
					+ " or later. Test aborted.");
			LOGGER.error("If running in a IDE, configure test to use JDK " //
					+ MINIMUM_JAVA_VERSION + '.');
		} else {
			if (firstTime) {
				LOGGER.warn("Java version is: " + javaVersion);
				firstTime = false;
			}
		}
	}

	public static String skipListFileName() {
		return SkipTestInfo.instance().skipListFileName;
	}

	public static boolean skipTest(Class<?> testClass, FrameworkMethod frameworkMethod) {

		LOGGER.trace("Checking if we skip tests in " + testClass + " when using NuoDB");
		List<String> methods = SkipTestInfo.instance().methodsToSkip.get(testClass.getName());

		if (methods == null)
			return false;

		return (methods == SkipTestInfo.ALL_METHODS || methods.contains(frameworkMethod.getName()));
	}

	private static JDBCException isKnownException(Object caller, String message, SQLException sqlException,
			String sql) {
		String sqlError = TestUtils.getRootCause(sqlException).getLocalizedMessage();
		LOGGER.error("         : " + sqlError);
		String cause = "";

		if (sqlException instanceof SQLFeatureNotSupportedException) {
			cause = sqlError;
			int ix = cause.indexOf('^');
			String context = sqlError;

			if (ix != -1) {
				String l2 = cause.substring(0, ix);
				cause = cause.substring(ix + 1).trim();
				
				int ix2 = l2.indexOf('\n');
						
				if (ix2 != -1) {
					// Carot ^ is indented this many spaces
					int nSpaces = ix - ix2;
					int start = nSpaces > 20 ? nSpaces - 20 : 0;
					int end = nSpaces + 20 > l2.length() ? l2.length() : nSpaces + 20;
					context = l2.substring(start, end);
				}
			}

			SkipTestInfo.instance().extraTestToSkip(cause, sqlException);
		} else if (sqlError.contains("expected SELECT got") & sqlError.contains(" join (")) {
			// Known error: parentheses in SELECT, usually due to JOIN FETCH
			SkipTestInfo.instance().extraTestToSkip(cause = "Parentheses in SQL using JOIN", sqlException);
		} else {
			return null;
		}

		return quietException(caller, message + " (" + cause + ')' , sqlException, sql);
	}

	public static JDBCException checkForKnownException(Object caller, String message, SQLException sqlException,
			String sql, Class<? extends JDBCException> exceptionClass) {
		JDBCException je = isKnownException(caller, message, sqlException, sql);

		if (je != null)
			return je;
		else if (SQLGrammarException.class.equals(exceptionClass))
			return new SQLGrammarException(message, sqlException, sql);
		else
			return new GenericJDBCException(message, sqlException, sql);
	}

	/**
	 * An exception that suppresses its stack traces.
	 * 
	 * @author Paul Chapman
	 */
	public static class QuietException extends JDBCException {

		private static final String UNSUPPORTED_NUODB_SYNTAX //
				= "SQL syntax not supported by NuoDB. Error was";

		private static final long serialVersionUID = 1L;

		private Exception cause;

		public QuietException(String errorMessage, SQLException e) {
			super(errorMessage, e);
			this.cause = e;
		}

		@Override
		public void printStackTrace() {
			printStackTrace(System.out);
		}

		@Override
		public void printStackTrace(PrintStream s) {
			s.println(TestUtils.getExceptionInfo(UNSUPPORTED_NUODB_SYNTAX, cause));
		}

		@Override
		public void printStackTrace(PrintWriter s) {
			s.println(TestUtils.getExceptionInfo(UNSUPPORTED_NUODB_SYNTAX, cause));
		}
	}

	public static JDBCException quietException(Object caller, String message, SQLException sqlException, String sql) {
		String msg = ">>> TEST FAILED " + message //
				+ " running " + System.lineSeparator() + "    [" + sql.trim() + ']';
		logException(caller, msg, sqlException);
		return new QuietException(msg, sqlException);
	}

}
