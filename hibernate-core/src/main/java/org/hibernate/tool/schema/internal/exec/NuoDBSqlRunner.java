package org.hibernate.tool.schema.internal.exec;

import org.hibernate.tool.schema.spi.CommandAcceptanceException;
import org.jboss.logging.Logger;

import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Runs a SQL command and tries to ignore exceptions during test setup and cleanup
 * that do not affect the test.  See {@link #runCommand(SqlRunner)} for details.
 */
public class NuoDBSqlRunner {

    enum TestInfoState {
        TEST_NOT_FOUND, IGNORE, TEST_FOUND
    }

    protected static class TestInfo {
        public static final TestInfo IGNORE = new TestInfo();

        public final TestInfoState state;
        public final StringBuilder stackTrace;
        public final String fullyQualifiedTestName;
        public final String testClassName;
        public final String testMethodName;

        private TestInfo() {
            this.state = TestInfoState.IGNORE;
            this.stackTrace = null;
            this.fullyQualifiedTestName = null;
            this.testClassName = null;
            this.testMethodName = null;
        }

        public TestInfo(StringBuilder stackTrace) {
            this.state = TestInfoState.TEST_NOT_FOUND;
            this.stackTrace = stackTrace;
            this.fullyQualifiedTestName = null;
            this.testClassName = null;
            this.testMethodName = null;
        }

        public TestInfo(StringBuilder stackTrace, String fullyQualifiedTestName, String testClassName,
                        String testMethodName) {
            this.state = TestInfoState.TEST_FOUND;
            this.stackTrace = stackTrace;
            this.fullyQualifiedTestName = fullyQualifiedTestName;
            this.testClassName = testClassName;
            this.testMethodName = testMethodName;
        }
    }

    public interface SqlRunner {
        public void runCommand(Statement jdbcStatement, String command) throws SQLException;
    }

    private static final String NL = System.lineSeparator();

    private static final Logger LOGGER = Logger.getLogger(NuoDBSqlRunner.class);

    private static final String[] POSSIBLE_TEST_CLASS_NAME_ENDINGS = //
            {"Test", "Tests", "Test2"};

    private final Statement jdbcStatement;
    private String command;

    private static boolean isRunningTest(SQLException sqlException) {
        TestInfo testInfo = findTestInfoFromStack(sqlException);
        return testInfo.state == TestInfoState.TEST_FOUND;
    }

    private static boolean possibleTestClass(String className, String fqcn) {
        if (className.startsWith("test"))
            return false; // Is a method name

        for (String word : POSSIBLE_TEST_CLASS_NAME_ENDINGS) {
            if (className.endsWith(word))
                return true;
        }

        if (className.contains("Test")) {
            try {
                Class<?> clazz = Class.forName(fqcn);
                Class<?> superClass = clazz.getSuperclass();

                if (superClass.getSimpleName().endsWith("TestCase"))
                    return true;

                for (Annotation a : clazz.getAnnotations()) {
                    if (a.getClass().getName().equals("org.hibernate.testing.orm.junit.SessionFactory"))
                        return true;
                }

            } catch (Exception e) {
                LOGGER.info(e.getClass().getSimpleName() + " finding " + className); // give up
            }
        }

        return false;
    }

    private static TestInfo findTestInfoFromStack(SQLException sqlException) {
        String fullyQualifiedTestName = null;
        boolean matchFound = false;
        String testClassName = null;
        String testMethodName = null;
        StringBuilder sb = new StringBuilder();
        sb.append(NL);

        for (StackTraceElement ste : sqlException.getStackTrace()) {
            String className = ste.getClassName();
            sb.append("#    ").append(className).append('.').append(ste.getMethodName()).append(NL);

            if (possibleTestClass(simpleClassName(className), className)) {
                String methodName = ste.getMethodName();

                if (methodName.startsWith("lambda$"))
                    methodName = methodName.substring("lambda$".length());

                int ix = methodName.indexOf('$');

                if (ix != -1)
                    methodName = methodName.substring(0, ix);

                if (className.equals("org.hibernate.testing.junit4.BaseCoreFunctionalTestCase")
                        && methodName.equals("cleanupTestData")) {
                    // This is invoked in an @AfterTest method, not sure why it is generating
                    // multi-column syntax. Ignore for now.
                    return TestInfo.IGNORE;
                }

                if (!methodName.startsWith("test")) {
                    if (matchFound)
                        break;
                    else
                        return new TestInfo(sb, fullyQualifiedTestName, testClassName, testMethodName);
                }

                testClassName = className;
                testMethodName = methodName;
                fullyQualifiedTestName = className + '.' + methodName;
                matchFound = true;
            } else if (matchFound) {
                break;
            }
        }

        return matchFound ? new TestInfo(sb, fullyQualifiedTestName, testClassName, testMethodName) : new TestInfo(sb);
    }

    private static String simpleClassName(String fqcn) {
        int ix = fqcn.lastIndexOf('.');
        return ix == -1 ? fqcn : fqcn.substring(ix + 1);
    }

    public NuoDBSqlRunner(Statement jdbcStatement, String command) {
        this.jdbcStatement = jdbcStatement;
        this.command = command;
    }

    /**
     * Run sql command but ignore or workaround spurious exceptions during test setup.
     * <li>DROP ... CASCADE not supported so a table will not be dropped if a foreign
     * key constraint won't let it be dropped. Truncate the table instead.
     * <li>If a CREATE TABLE fails because table already exists, drop the table and
     * try again.
     * <li> Ignore error trying to drop an FK constraint from a table that has already
     * been deleted.
     *
     * @param sqlRunner Allows the code to run the SQL to be passed as a lambda function.
     */
    public void runCommand(SqlRunner sqlRunner) {
        // Allow any command to be run and, if necessary, retried one more time
        for (int i = 0; i < 2; i++) {
            try {
                sqlRunner.runCommand(jdbcStatement, command);
                break;
            } catch (SQLException e) {
                // If handled, don't retry
                if (handleException(e))
                    break;  // Stop iterating
            }
        }
    }

    /**
     * Handle the exception if possible.
     *
     * @param e A SQL exception
     * @return True if handled.  Return false to retry the command.
     */
    private boolean handleException(SQLException e) {
        // If actually running a test, allow exception to propagate as part of the test
        if (NuoDBSqlRunner.isRunningTest(e))
            throw new CommandAcceptanceException(
                    "Error executing DDL \"" + command + "\" via JDBC Statement",
                    e);

        // Hide or fix spurious errors during setup and cleanup - such as dropping constraints on
        // non-existent tables, failing to create an existing table or truncating tables that
        // cannot be dropped.
        String emsg = e.getLocalizedMessage();

        if (command.contains("DROP CONSTRAINT") && (emsg.contains("can't find table") || emsg.contains("has no constraint")))
            return true;  // Constraint or its table no longer exists. Ignore error and carry on
        else if (command.startsWith("drop ")) {
            // Change drop command to truncate and try again
            command = "truncate " + command.substring(5);
        } else if (command.contains(" drop "))
            // Change drop command to truncate and try again
            command.replaceAll(" drop ", " truncate ");
        else if (command.contains("create table") && emsg.contains("already exists")) {
            // Table exists, remove it first
            LOGGER.info("Failed running \"" + command + "\" because table already exists.  Will try to drop it first.");
            int ix = command.indexOf('(');
            String command2 = command.substring(0, ix);
            command2 = command2.replaceAll("create", "drop");

            try {
                jdbcStatement.execute(command2);
            } catch (SQLException ex) {
                // Failed to drop table
                throw new CommandAcceptanceException(
                        "Create table failed, because table already exists. Unable to drop it using \"" + command2 + "\"",
                        ex);
            }
        } else
            // Failed
            throw new CommandAcceptanceException(
                    "Error executing DDL \"" + command + "\" via JDBC Statement",
                    e);

        return false;
    }

}
