package org.hibernate.testing.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.util.Strings;
import org.hibernate.JDBCException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.testing.orm.junit.DomainModel;
import org.jboss.logging.Logger;
import org.junit.runners.model.FrameworkMethod;
import org.opentest4j.AssertionFailedError;

/**
 * Handy utility functions used by NuoDB to manage tests.
 * 
 * @author Paul Chapman
 */
public class TestUtils {

    /**
     * Holds the name of the current test, found by looking up the stack-trace of an
     * exception raised by that test. Looks for classes with {@code Test} in their
     * name and methods starting with {@code test}.
     */
    protected static class TestInfo {
        enum Status {
            TEST_NOT_FOUND, IGNORE, TEST_FOUND, OTHER
        }

        public final Status status;
        public final StringBuilder stackTrace;
        public final String fullyQualifiedTestName;
        public final String testClassName;
        public final String testMethodName;

        /**
         * After checking the stack, the exception was found to be ignorable.
         */
        public static TestInfo ignore() {
            return new TestInfo(Status.IGNORE, null, null, null, null);
        }

        /**
         * After checking the stack, could not find a class whose name indicated a JUnit
         * test.
         */
        public static TestInfo notFound(StringBuilder stackTrace) {
            return new TestInfo(Status.TEST_NOT_FOUND, stackTrace, null, null, null);
        }

        /**
         * Test class and method found successfully.
         *
         * @param stackTrace             Exception stack trace.
         * @param fullyQualifiedTestName Fully qualfied class name of test.
         * @param testClassName          Test classes simple name (no package).
         * @param testMethodName         The Test method that was being run.
         */
        public static TestInfo found(StringBuilder stackTrace, String fullyQualifiedTestName, String testClassName,
                String testMethodName) {
            return new TestInfo(Status.TEST_FOUND, stackTrace, fullyQualifiedTestName, testClassName, testMethodName);
        }

        /**
         * A method in a test class that is not an {@code @Test} method.
         * 
         * @param stackTrace
         * @param fullyQualifiedTestName
         * @param testClassName
         * @param testMethodName
         */
        public static TestInfo other(StringBuilder stackTrace, String fullyQualifiedTestName, String testClassName,
                String testMethodName) {
            return new TestInfo(Status.OTHER, stackTrace, fullyQualifiedTestName, testClassName, testMethodName);
        }

        private TestInfo(Status status, StringBuilder stackTrace, String fullyQualifiedTestName, String testClassName,
                String testMethodName) {
            this.status = status;
            this.stackTrace = stackTrace;
            this.fullyQualifiedTestName = fullyQualifiedTestName;
            this.testClassName = testClassName;
            this.testMethodName = testMethodName;
        }
    }

    /**
     * An exception that suppresses its stack traces and only outputs the exception
     * message. For exceptions that are ignorable.
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

    /**
     * The NuoDB Hibernate JAR and Hibernate 6.1-6.2 are compiled for JDK 11.
     * Previous JAR versions support Hibernate 5.x and Java 1.8.
     */
    public static final int MINIMUM_JAVA_VERSION = 11;

    public static final String[] POSSIBLE_TEST_CLASS_NAME_ENDINGS = { "Test", "Tests", "Test2" };

    public static final String NL = System.lineSeparator();

    private static boolean firstTime = true;

    private static final Logger LOGGER = Logger.getLogger(TestUtils.class);

    /**
     * Checks that the supplied string a valid UUID. Uses asserts internally.
     * 
     * @param uuid A UUID as a string.
     * 
     * @throws AssertionFailedError If the string is not valid.
     */
    public static void isValidUuid(String uuid) {
        LOGGER.warn("UUID = " + uuid);
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
        logException(Logger.getLogger(caller.getClass()), msg, exception);
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

    public static void logException(org.slf4j.Logger logger, String msg, Exception e) {
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

    /**
     * Popup a message dialog.
     * 
     * @param message Message to show.
     */
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

    /**
     * Create a ne wfile containing the specified text.
     * 
     * @param fileToInitialize File to create (overwrites any existing file).
     * @param text             Text to write.
     */
    public static void initFile(File fileToInitialize, String text) {
        try {
            // We will put these in the same directory
            Files.write(fileToInitialize.toPath(), text.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            TestUtils.logException(LOGGER, "Failed initializing " + fileToInitialize, e);
        }
    }

    /**
     * Load the contents of a file and return all the lines in it.
     * 
     * @param file File to load.
     * @return List containing every line in order.
     */
    public static List<String> loadFile(File file) {
        try {
            return Files.readAllLines(file.toPath());
        } catch (IOException e) {
            TestUtils.logException(LOGGER, "Failed reading " + file, e);
            return new ArrayList<>();
        }
    }

    /**
     * Append a line to the specified file. File is flushed and closed each time.
     * 
     * @param file File to append to.
     * @param line A line of text.
     */
    public static void appendToFile(File file, String line) {
        try {
            // We will put these in the same directory
            Files.write(file.toPath(), (line + NL).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            TestUtils.logException(LOGGER, "Failed appending to " + file, e);
        }
    }

    /**
     * Get the simple class name (without any package).
     * 
     * @param fqcn Fully qualified name of class.
     *
     * @return Its simple name - the text after the last full-stop/period.
     */
    public static String simpleClassName(String fqcn) {
        int ix = fqcn.lastIndexOf('.');
        return ix == -1 ? fqcn : fqcn.substring(ix + 1);
    }

    /**
     * Is he specified class a JUnit test? Uses simple heuristics first (does the
     * name follow the common conventions for a JUnit test). Then it uses reflection
     * to look for the {@code @Test} annotation.
     * 
     * @param className Name of the class.
     * @param fqcn      The fully qualified name (with package) of the class.
     * @return True if a JUnit test class.
     */
    public static boolean isTestClass(String className, String fqcn) {
        if (className.startsWith("test"))
            return false; // It's a method name

        for (String word : POSSIBLE_TEST_CLASS_NAME_ENDINGS) {
            if (className.endsWith(word))
                return true;
        }

        try {
            Class<?> clazz = Class.forName(fqcn);
            Class<?> superClass = clazz.getSuperclass();

            if (superClass != null && superClass.getSimpleName().endsWith("TestCase"))
                return true;

            if (clazz.getAnnotation(org.hibernate.testing.orm.junit.SessionFactory.class) != null
                    || clazz.getAnnotation(DomainModel.class) != null)
                return true;
        } catch (ClassNotFoundException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(e.getClass().getSimpleName() + " finding " + className); // give up
        }

        return false;
    }

    /**
     * Is the specified method a JUnit test method?
     * 
     * @param testClassName A verified JUnit test class.
     * @param methodName    One of its methods.
     * @return True if a JUnit test method.
     */
    public static boolean isTestMethod(String testClassName, String methodName) {
        if (methodName.startsWith("test"))
            return true;

        // Is this a test method?
        Class<?> testClass;
        try {
            testClass = Class.forName(testClassName);
            for (Method m : testClass.getMethods()) {
                if (!m.getName().equals(methodName))
                    continue;

                return m.getAnnotation(org.junit.Test.class) != null || m.getAnnotation(org.junit.Test.class) != null;
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Given the SQL Exception, search up the stack for a class and method that is
     * likely to be a JUnit test.
     * 
     * @param sqlException A SQL exception.
     * @return The result of the search - see {@link TestInfo}.
     */
    protected static TestInfo findTestInfoFromStack(SQLException sqlException) {
        String fullyQualifiedTestName = null;
        boolean matchFound = false;
        String testClassName = null;
        String testMethodName = null;
        boolean isTestMethod = false;
        StringBuilder sb = new StringBuilder();
        sb.append(NL);

        for (StackTraceElement ste : sqlException.getStackTrace()) {
            String className = ste.getClassName();
            sb.append("#    ").append(className).append('.').append(ste.getMethodName()).append(NL);

            if (TestUtils.isTestClass(TestUtils.simpleClassName(className), className)) {
                // Found a class that looks like a JUnit test class
                String methodName = ste.getMethodName();

                if (methodName.startsWith("lambda$"))
                    methodName = methodName.substring("lambda$".length());

                int ix = methodName.indexOf('$');

                if (ix != -1)
                    methodName = methodName.substring(0, ix);

                // Special case
                if (className.equals("org.hibernate.testing.junit4.BaseCoreFunctionalTestCase")
                        && methodName.equals("cleanupTestData")) {
                    // This is invoked in an @AfterTest method, not sure why it is generating
                    // multi-column syntax. Ignore for now.
                    return TestInfo.ignore();
                }

                if (!isTestMethod(className, methodName)) {
                    // Could be a Before or After method, or a method called from a test method.
                    if (matchFound)
                        break;
                    else
                        continue; // Can this happen?
                }

                // Save details
                testClassName = className;
                testMethodName = methodName;
                fullyQualifiedTestName = className + '.' + methodName;
                isTestMethod = isTestMethod(className, methodName);

                // Probably the test, but we will go up the stack one more level to be sure.
                matchFound = true;
            } else if (matchFound) {
                break;
            }
        }

        if (matchFound) {
            if (isTestMethod)
                return TestInfo.found(sb, fullyQualifiedTestName, testClassName, testMethodName);
            else
                return TestInfo.other(sb, fullyQualifiedTestName, testClassName, testMethodName);
        } else
            return TestInfo.notFound(sb);
    }

    public static String skipListFileName() {
        return SkipTestInfo.instance().skipListFileName();
    }

    public static boolean skipTest(Class<?> testClass, FrameworkMethod frameworkMethod) {
        String skip = SkipTestInfo.instance().doSkipCheck(testClass, frameworkMethod);
        boolean skipTest;
        String reasonToSkip = "";

        if ("false".equalsIgnoreCase(skip))
            skipTest = false;
        else if ("true".equalsIgnoreCase(skip)) {
            skipTest = true;
        } else {
            skipTest = true;
            reasonToSkip = skip.length() == 0 ? " (Reason not given)" : " (Skip-check returned '" + skip + "')";
        }

        if (skipTest)
            LOGGER.warn("Skip " + testClass.getSimpleName() + '.' + frameworkMethod.getName() //
                    + " when using NuoDB" + reasonToSkip);
        else
            LOGGER.warn("Run " + testClass.getSimpleName() + '.' + frameworkMethod.getName() + " when using NuoDB ");

        return skipTest;
    }

    private static JDBCException isKnownException(Object caller, String message, SQLException sqlException,
            String sql) {
        String sqlError = TestUtils.getRootCause(sqlException).getLocalizedMessage();
        LOGGER.error("         : " + sqlError);
        String cause = sqlError;
        SkipTestInfo skipTestInfo = SkipTestInfo.instance();

        sql = extractSql(sql, sqlError);

        if (sqlException instanceof SQLFeatureNotSupportedException) {

            int ix = cause.indexOf('^');
            String context = sql;

            if (ix != -1) {
                String l2 = cause.substring(0, ix);
                cause = cause.substring(ix + 1).trim();

                if (cause.contains("RIGHTOUTER") || cause.contains("FULLOUTER"))
                    ; // Known limitation, no more info needed
                else if (sql.contains("is distinct from") || sql.contains("is not distinct from"))
                    // Error message 'expected NOT or NULL got DISTINCT' not obvious
                    cause = "IS [NOT] DISTINCT FROM not supported";
                else {
                    int ix2 = l2.lastIndexOf('\n');
                    int offset = 30;

                    // Mark location of error with >>> in SQL string
                    if (ix2 != -1) {
                        // Caret ^ is indented this many spaces
                        int nSpaces = ix - ix2;
                        int start = nSpaces > offset ? nSpaces - offset : 0;

                        if ((start + offset) - sql.length() < 3)
                            context = sql + " <<<";
                        else {
                            int offset2 = start + offset - 1;

                            if (offset2 >= sql.length())
                                context = sql + " <<<@@";
                            else
                                context = sql.substring(0, offset2) + " >>>" + sql.substring(offset2);
                        }
                    }

                    cause = cause + " in '" + context + '\'';
                }
            }

            // popupMessage(cause);
        } else if (sqlException instanceof SQLSyntaxErrorException) {

            if (sqlError.contains("can't find") || sqlError.contains("not found")
                    || sqlError.contains("undefined reference")) {
                // Missing/undefined table, schema or field error
                skipTestInfo.recordError(message, sqlException, sqlError, sql);
                return null;
            } else if (sqlError.contains("expected SELECT got") && sqlError.contains(" join ("))
                // Known error: parentheses in SELECT, usually due to JOIN FETCH
                cause = "Parentheses in SQL using JOIN";
            else if (sqlError.contains("subqueries are not allowed in the ORDER BY clause"))
                cause = "Subqueries in ORDER BY clause";
            else if (sqlError.contains("^")) {
                cause = sqlError.substring(sqlError.indexOf('^') + 1).trim();
            } else {
                // Some SQL syntax exceptions are part of the test
                return null;
            }
        } else if (sqlError.toUpperCase().contains("INTERNAL ERROR")) {
            cause = "Internal error";
            skipTestInfo.recordError(message, sqlException, sqlError, sql);
        } else {
            // NOT an error known to upset NuoDB SQL parser
            skipTestInfo.recordError(message, sqlException, sqlError, sql);
            return null;
        }

        // Save details, so we can skip it next time.
        skipTestInfo.saveExtraTestToSkip(cause, sqlException);

        return quietException(caller, message + " (" + cause + ')', sqlException, sql);
    }

    protected static String extractSql(String sql, String sqlError) {
        // Sometimes the SQL is in the sqlError but sql = [n/a].
        sql = sql.trim();

        if (sql.contains("n/a") && LOGGER.isDebugEnabled()) {
            LOGGER.debug("sql was  \"" + sql + '"');
            LOGGER.debug("sqlError \"" + sqlError + '"');
        }

        if (sql.equals("n/a")) {
            String marker = " executing SQL [";
            int ix = sqlError.indexOf(marker);

            if (ix != -1) {
                int end = sqlError.lastIndexOf(']');
                sql = sqlError.substring(ix + marker.length(), end == -1 ? sqlError.length() : end);
            } else if (sqlError.startsWith("error on line ")) {
                ix = sqlError.indexOf("select ");

                if (ix != -1) {
                    int end = sqlError.lastIndexOf('^');

                    sql = sqlError.substring(ix, end == -1 ? sqlError.length() : end).trim();
                }
            }
        }

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("sql is now \"" + sql + '"');

        return sql;
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

    public static boolean isRunningTest(SQLException sqlException) {
        TestInfo testInfo = findTestInfoFromStack(sqlException);
        return testInfo.status == TestInfo.Status.TEST_FOUND;
    }

    protected static JDBCException quietException(Object caller, String message, SQLException sqlException,
            String sql) {

        String msg = ">>> TEST FAILED " + message //
                + (sql != null && (!sql.contains("[n/a]"))
                        ? " running " + System.lineSeparator() + "    [" + sql.trim() + ']'
                        : "");

        logException(caller, msg, sqlException);
        return new QuietException(msg, sqlException);
    }

    // Currently no longer used
    protected static String findSyntaxErrorContext(String sqlError) {
        String[] bits = sqlError.trim().split(System.lineSeparator());

        if (bits.length == 3) { // error msg, sql, carat+error details
            String sql2 = bits[1];
            String causeLine = bits[2];
            int ix = causeLine.indexOf('^');
            // cause = causeLine.substring(ix + 1);
            int start = ix > 6 ? ix - 6 : 0;
            String sqlFragment = sql2.substring(start, ix + 6);
            return sqlFragment;
        }

        return "";
    }

}
