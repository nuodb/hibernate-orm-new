package org.hibernate.testing.support;

import org.hibernate.orm.test.constraint.NonRootTablePolymorphicTests;
import org.hibernate.orm.test.inheritance.TransientOverrideAsPersistentJoined;
import org.hibernate.orm.test.jpa.criteria.mapjoin.MapJoinTestWithEmbeddable;
import org.hibernate.orm.test.pagination.OraclePaginationTest;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.support.TestUtils.TestInfo;
import org.jboss.logging.Logger;
import org.junit.runners.model.FrameworkMethod;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class SkipTestInfo {

	/**
     * Hold information about a test class and its methods.
     */
    public static class TestClassInfo {
        public final String className;
        public final String comment;
        public final List<String> methods;
        public final List<String> methodComments;

        public static TestClassInfo allMethods(String className, String comment) {
            return new TestClassInfo(className, comment, true);
        }

        public TestClassInfo(String className, String comment) {
            this(className, comment, false);
        }

        private TestClassInfo(String className, String comment, boolean allMethods) {
            this.className = className;
            this.comment = comment;
            this.methods = allMethods ? ALL_METHODS : new ArrayList<>();
            this.methodComments = allMethods ? null : new ArrayList<>();
        }

        @Override
        public String toString() {
            if (methods == ALL_METHODS)
                return "All methods " + comment;

            StringBuilder sb = new StringBuilder();
            sb.append("{ ");

            for (int i = 0; i < methods.size(); i++) {
                sb.append(methods.get(i)).append(" ").append(methodComments.get(i)).append(", ");
            }

            sb.append(" }");
            return sb.toString();
        }
    }

	class Context {
	    protected final String purpose;
	    protected final File errorFile;
	    protected final File updatedFile;
	
	    protected int errors = 0;
	    protected boolean needToRewriteSource = false;
	
	    public Context(String purpose, File errorFile, File updatedFile) {
	        this.purpose = purpose;
	        this.errorFile = errorFile;
	        this.updatedFile = updatedFile;
	    }
	}

	public static final String SKIP_TESTS_FILE_NAME = "skip-tests.txt";

    public static final String GREEN_LIST_FILE_NAME = "green-list.txt";

    @SuppressWarnings("unchecked")
    public static final List<String> ALL_METHODS = Collections.EMPTY_LIST;

    private static final String SKIP_FILE_HEADING = "# Tests to skip because they generate SQL syntax not supported by NuoDB";

    private static final String NL = System.lineSeparator();

    private static SkipTestInfo instance = null;

    private static final Logger LOGGER = Logger.getLogger(SkipTestInfo.class);

    private Map<String, TestClassInfo> methodsToRun = new HashMap<>();
    private Map<String, TestClassInfo> methodsToSkip = new HashMap<>();

    private boolean initialized = false;

    private String skipListFileName = null;
    private File errorFile = null;
    private File extraTestsToSkip = null;
    boolean needToRewriteSkipFile = false;
    private File newSkipFile = null;
    private StringBuilder skipFileContents = null;
    private int errors = 0;

    private String srcFileName;

    public static SkipTestInfo instance() {
        if (instance == null)
            instance = new SkipTestInfo();

        return instance;
    }

	private static StringBuilder saveTests(Context context, File source, Map<String, TestClassInfo> methods) {
	    LOGGER.debug("Loading tests to " + context.purpose + " from " + source);
	    List<String> allLines = TestUtils.loadFile(source);
	    StringBuilder fileContents = new StringBuilder();
	    int lineNum = 0;
	
	    for (String line : allLines) {
	        lineNum++;
	        line = line.trim();
	
	        // Ignore blank lines and comments
	        if (line.length() == 0 || line.charAt(0) == '#') {
	            fileContents.append(line).append(NL);
	            continue;
	        }
	
	        // Expecting FQCN, not a path to the class.
	        if (line.indexOf('/') != -1)
	            sourceFileError(context, source, lineNum, "Line contains /");
	
	        String originalLine = line;
	
	        int ix = line.indexOf('(');
	        String comment = "";
	
	        if (ix != -1) {
	            comment = ' ' + line.substring(ix);
	            line = line.substring(0, ix).trim();
	        }
	
	        String lineWithoutComment = line;
	        ix = line.lastIndexOf('.');
	        String lastWord = ix == -1 ? line : line.substring(ix + 1);
	        line = line.substring(0, ix);
	        boolean isMethodName = false;
	
	        if (lastWord.startsWith("lambda$")) {
	            context.needToRewriteSource = true;
	            lastWord = lastWord.substring("lambda$".length());
	
	            ix = lastWord.indexOf('$');
	
	            if (ix != -1)
	                lastWord = lastWord.substring(0, ix);
	
	            isMethodName = true;
	        }
	
	        if ((!isMethodName) && TestUtils.isTestClass(lastWord, lineWithoutComment)) {
	            // Name of a test class, skip all its methods
	            line = line + '.' + lastWord;
	            methods.put(line, TestClassInfo.allMethods(line, comment));
	            fileContents.append(line).append(comment).append(NL);
	        } else {
	            // Name of test, find its class
	            String methodName = lastWord;
	            ix = line.lastIndexOf('.');
	            String className = ix == -1 ? line : line.substring(ix + 1);
	
	            if (lastWord.startsWith("test")) {
	
	                if (TestUtils.isTestClass(className, line)) {
	                    // Class name
	                    TestClassInfo testClassInfo = methods.get(line);
	
	                    if (testClassInfo == null) {
	                        testClassInfo = new TestClassInfo(line, "");
	                        methods.put(line, testClassInfo);
	                    }
	
	                    List<String> methodsList = testClassInfo.methods;
	
	                    if (methodsList == ALL_METHODS) {
	                        sourceFileError(context, source, lineNum,
	                                "This class already marked without any specific methods");
	                    } else {
	                        if (methodsList.contains(methodName))
	                            sourceFileError(context, source, lineNum,
	                                    "Duplicate entry for " + line + '.' + methodName);
	                        else {
	                            methodsList.add(methodName);
	                            testClassInfo.methodComments.add(comment);
	                            fileContents.append(line).append('.').append(methodName).append(comment)
	                                    .append(NL);
	
	                        }
	                    }
	                } else {
	                    sourceFileError(context, source, lineNum, "Expected test class for " //
	                            + methodName + " in line: " + originalLine);
	                }
	            } else {
	                boolean isTestMethod = false;
	
	                if (TestUtils.isTestClass(className, line)) {
	                    try {
	                        Class<?> testClass = Class.forName(line);
                            Method testMethod;

                            try {
                                testMethod = testClass.getDeclaredMethod(methodName, (Class<?>[]) null);
                            }
                            catch (NoSuchMethodException e) {
                                testMethod = testClass.getDeclaredMethod(methodName, SessionFactoryScope.class);
                            }

	                        if (testMethod.getAnnotation(org.junit.Test.class) != null
	                                || testMethod.getAnnotation(org.junit.jupiter.api.Test.class) != null) {
	                            fileContents.append(line).append('.').append(methodName).append(comment)
	                                    .append(NL);
	                            isTestMethod = true;
	                        }
	                    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
	                        TestUtils.logException(LOGGER, "Error validating " + originalLine, e); // Ignore
	                    }
	                }
	
	                if (!isTestMethod)
	                    sourceFileError(context, source, lineNum, "Bad line: " + originalLine);
	            }
	        }
	
	    }
	
	    // Write out modified list of tests?
	    boolean isProcessingSkipFile = context.purpose.equals("skip");
	
	    if (isProcessingSkipFile && context.needToRewriteSource) {
	    	TestUtils.initFile(context.updatedFile, fileContents.toString());
	    }
	
	    // Errors? Give up
	    if (context.errors > 0)
	        throw new RuntimeException(context.errors + " errors reading " + source);
	
	    return fileContents;
	}

	private static void sourceFileError(Context context, File source, int lineNum, String msg) {
	    String emsg = "Error in " + source + "(" + lineNum + "): " + msg;
	    LOGGER.warn(emsg);
        JOptionPane.showMessageDialog(null, emsg);
	    context.errors++;
	    context.needToRewriteSource = true;
	    TestUtils.appendToFile(context.errorFile, emsg);
	}

	public String doSkipCheck(Class<?> testClass, FrameworkMethod frameworkMethod) {

		// Run test?
		if (!methodsToRun.isEmpty()) {
			LOGGER.warn("Checking if we run green tests in " + testClass + " when using NuoDB");
			TestClassInfo testClassInfo = methodsToRun.get(testClass.getName());

			if (testClassInfo == null)
				return "true";  // Not in the list, skip

			List<String> methods = testClassInfo.methods;

			if (methods == ALL_METHODS)
				return "false";   // Don't skip, run this method

			// Skip _unless_ this method is in the list
			return methods.contains(frameworkMethod.getName()) ? "false" : "true";
		}

		// Skip test?
		LOGGER.warn("Checking if we skip tests in " + testClass + " when using NuoDB (" //
				 + methodsToSkip.size()	+ " methods marked for skip)");

		TestClassInfo testClassInfo = methodsToSkip.get(testClass.getName());

		if (testClassInfo == null) {
			return "false";
		}

		List<String> methods = testClassInfo.methods;

		if (methods == SkipTestInfo.ALL_METHODS)
			return testClassInfo.comment;

		int ix = methods.indexOf(frameworkMethod.getName());
	    return ix == -1 ? "false" : testClassInfo.methodComments.get(ix);
	}

	public String skipListFileName() {
		return skipListFileName;
	}


	protected void recordError(String message, SQLException sqlException, String sqlError, String sql) {
        TestInfo testInfo = TestUtils.findTestInfoFromStack(sqlException);

        // Ignore deliberate error
        if (sqlException.getErrorCode() == -25 //
                && (sql.contains("from NON_EXISTENT") || sql.contains("from NotExistedTable") || sql.contains("FROM tbl_no_there")))
            return;

        TestUtils.appendToFile(errorFile, message + " <-- " + sqlError + NL //
                + "Exception=" + sqlException.getClass().getSimpleName() + ", sql-state=" + sqlException.getSQLState()
                + ", sql-code=" + sqlException.getErrorCode() + " running:" + NL //
                + "   [" + sql + ']' + NL //
                + "   " + sqlException.getLocalizedMessage() + NL //
                + (testInfo.status == TestInfo.Status.TEST_FOUND ? " at " + testInfo.fullyQualifiedTestName : "") //
        );
    }

    protected void saveExtraTestToSkip(String causeOfFailure, SQLException sqlException) {
        TestInfo testInfo = TestUtils.findTestInfoFromStack(sqlException);

        if (testInfo.status == TestInfo.Status.TEST_NOT_FOUND) {
            String emsg = "# UNABLE TO DETERMINE FAILING TEST DETAILS" + NL //
                    + "failure=" + causeOfFailure + NL //
                    + "exception=" + sqlException + NL //
                    + testInfo.stackTrace.toString();

            LOGGER.error("# UNABLE TO DETERMINE FAILING TEST DETAILS", sqlException);
            TestUtils.appendToFile(errorFile, emsg);
            return;
        }

        if (testInfo.status == TestInfo.Status.IGNORE)
            return;

        String fullyQualifiedTestName = testInfo.fullyQualifiedTestName;
        TestClassInfo testClassInfo = methodsToSkip.get(testInfo.testClassName);
        List<String> methods = testClassInfo == null ? null : testClassInfo.methods;

        if (methods != null) {
            if (methods.contains(testInfo.testMethodName)) {
                // We have this method already. Why has it come up twice?
                LOGGER.warn("Duplicate test? " + fullyQualifiedTestName, sqlException);
                String emsg = "Test already in skip list: " + fullyQualifiedTestName + NL //
                        + "failure=" + causeOfFailure + NL //
                        + "exception=" + sqlException + NL //
                        + testInfo.stackTrace.toString();
                TestUtils.appendToFile(errorFile, emsg);
                return;
            }
        }

        extraTestToSkip(fullyQualifiedTestName, causeOfFailure);
    }

    private SkipTestInfo() {
	    this(SKIP_TESTS_FILE_NAME);
	}

	private SkipTestInfo(String fileName) {
	    if (instance != null)
	        throw new IllegalStateException("An instance already exists, only one is allowed");
	
	    this.srcFileName = fileName;
	    initialize(fileName);
	    instance = this;
	}

	private void extraTestToSkip(String fullyQualifiedTestName, String causeOfFailure) {
        errors++;
        LOGGER.debug("Errors = " + errors + ", needToRewriteSkipFile = " + needToRewriteSkipFile);

        // Haven't initialized the new file yet, do it now
        if (!needToRewriteSkipFile) {
        	TestUtils.initFile(newSkipFile, skipFileContents.toString());
        }

        if (errors == 1) {
            // Section heading
        	TestUtils.appendToFile(newSkipFile, NL + "# " + LocalDateTime.now());
        }

        needToRewriteSkipFile = true;
        String line = fullyQualifiedTestName + " (" + causeOfFailure + ')';
        LOGGER.warn("You need to add " + fullyQualifiedTestName + " to " + skipListFileName);
        TestUtils.appendToFile(newSkipFile, line);
        TestUtils.appendToFile(extraTestsToSkip, line);
    }

    private void initialize(String skipListFileName) {
        if (initialized)
            return;

        initialized = true;
        this.skipListFileName = skipListFileName;
        File skipListFile = getSkipFile(skipListFileName);
        File workingDir = skipListFile.getParentFile();
        File greenListFile = new File(workingDir, GREEN_LIST_FILE_NAME);
        LOGGER.trace("Using " + skipListFile + " and " + greenListFile);

        errorFile = new File(workingDir, "skip-errors.txt");
        extraTestsToSkip = new File(workingDir, "extra-tests.txt");
        newSkipFile = new File(workingDir, "updated-" + SKIP_TESTS_FILE_NAME);

        TestUtils.initFile(errorFile, "# Test errors from run " + LocalDateTime.now() + NL);
        TestUtils.initFile(extraTestsToSkip, "# Extra classes to skip " + LocalDateTime.now() + NL);

        if (skipListFile.exists()) {
            // Process the contents
            LOGGER.warn("Processing " + skipListFile);
            skipFileContents = saveTests(new Context("skip", errorFile, newSkipFile), skipListFile, methodsToSkip);
        } else {
            // No skip file yet - create it now
            LOGGER.debug("Creating " + skipListFile);
            TestUtils.initFile(skipListFile, SKIP_FILE_HEADING);
            skipFileContents.append(SKIP_FILE_HEADING).append(NL);
        }

        if (greenListFile.exists()) {
            // Process the contents
            LOGGER.warn("Processing " + greenListFile);
            File greenListErrorFile = new File(workingDir, "green-list-errors.txt");
            File updatedGreenListFile = new File(workingDir, "updated-" + GREEN_LIST_FILE_NAME);
            TestUtils.initFile(greenListErrorFile, "# Green list errors from run " + LocalDateTime.now() + NL);
            saveTests(new Context("run", greenListErrorFile, updatedGreenListFile), greenListFile, methodsToRun);
            LOGGER.warn("Tests to run: " + methodsToRun.toString());
        }
        else {
            // Create empty file
            TestUtils.initFile(errorFile, "# Test to run " + LocalDateTime.now() + NL //
                    + "#  - If this is non-empty, only these tests will be run." + NL //
                    + "#  - Leave empty unless you wish to run and debug specific test(s)." + NL  );
        }
    }

    private File getSkipFile(String skipListFileName) {
        // Is it in the local directory?
        LOGGER.trace("Looking for " + skipListFileName);
        File skipFileList = new File(skipListFileName);

        if (skipFileList.exists()) {
            return skipFileList;
        }

        // When running matrix tests, current dir is hibernate-core/target/matrix/nuodb
        File current = new File(".").getAbsoluteFile();
        LOGGER.trace("Current = " + current + " looking for hibernate-core in path");

        if (current.getPath().contains("/hibernate-core/")) {
            while (!current.getName().equals("hibernate-core")) {
                current = current.getParentFile();
                LOGGER.trace("current = " + current);
            }

            if (current != null)
                return new File(current, skipListFileName);
        }

        throw new RuntimeException(
                "Unable to find '" + skipListFileName + "' in directory " + new File(".").getAbsolutePath());
    }

    /**
     * Test program for SkipTestInfo class.
     *
     * @param args Unused.
     */
    public static void main(String[] args) {
        String[] lines = { //
                "com.nuodb.test.HereIsATest (a comment)", //
                "com.nuodb.test.MoreTests.testIgnoreMe", //
                "org.hibernate.orm.test.hql.CollectionMapWithComponentValueTest.lambda$testJoinMapKey$9 (Parentheses in SQL using JOIN)", //
                "org.hibernate.orm.test.hql.size.filter.WhereAnnotatedOneToManySizeTest.orderBy_sizeOf (Subqueries in ORDER BY clause)", //
                "org.hibernate.orm.test.jpa.criteria.mapjoin.MapJoinTestWithEmbeddable.testSelectingKeyOfMapJoin (Parentheses in SQL using JOIN)", //
        };

        List<String> listOfLines = new ArrayList<>();

        for (String line : lines)
            listOfLines.add(line);

        boolean useTestFile = false; // False to process actual skip-test.txt file

        File testSkipFile = new File("target/test-skip-file.txt");
        testSkipFile.delete();
        SkipTestInfo skipTestInfo = null;

        if (useTestFile) {
            // Create test file from listOfLines (see above)
            try {
                Files.write(testSkipFile.toPath(), listOfLines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                TestUtils.logException(LOGGER, "Failed creating " + SKIP_TESTS_FILE_NAME, e);
                throw new RuntimeException(e);
            }
        } else {
            // Make a copy of the actual file for use in this test
            try {
                File original = new File(SKIP_TESTS_FILE_NAME);
                if (original.exists()) {
                    Files.copy(original.toPath(), testSkipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                TestUtils.logException(LOGGER,
                        "Failed copying " + SKIP_TESTS_FILE_NAME + " to " + testSkipFile, e);
                throw new RuntimeException(e);
            }
        }

        // Setup a green-list file for use in this test
        File testGreenFile = new File("target/green-list.txt");

        if (useTestFile) {
            ; // Do nothing
        }
        else {
            // Make a copy of the actual green-list file for use in this test

            try {
                File original = new File(GREEN_LIST_FILE_NAME);
                if (original.exists()) {
                    Files.copy(original.toPath(), testGreenFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                TestUtils.logException(LOGGER,
                        "Failed copying " + GREEN_LIST_FILE_NAME + " to " + testGreenFile, e);
                throw new RuntimeException(e);
            }
        }

        // Now run the tests
        try {
            skipTestInfo = new SkipTestInfo(testSkipFile.getPath());

            skipTestInfo.extraTestToSkip("MyTest.testItWorks", "I just didn't like it");
            LOGGER.info("Processed " + skipTestInfo.srcFileName + ". Contents:");

            for (Map.Entry<String, TestClassInfo> entry : skipTestInfo.methodsToSkip.entrySet()) {
                LOGGER.info("    " + entry.getKey() + '=' + entry.getValue());
            }

            LOGGER.info("Skip file is now:");
            for (String line : TestUtils.loadFile(skipTestInfo.newSkipFile)) {
                LOGGER.info("    " + line);
            }

            Class<?> testClass = null;
            String testMethod = "";

            try {
                testClass = OraclePaginationTest.class;
                Method method1 = testClass.getDeclaredMethod(testMethod = "testPagination");
                FrameworkMethod fm1 = new FrameworkMethod(method1);
                TestUtils.skipTest(testClass, fm1);

                testClass = MapJoinTestWithEmbeddable.class;
                Method method2 = testClass.getDeclaredMethod(testMethod = "testSelectingKeyOfMapJoin");
                FrameworkMethod fm2 = new FrameworkMethod(method2);
                TestUtils.skipTest(testClass, fm2);
            }
            catch(Exception e) {
                // ignore
                TestUtils.logException(LOGGER, "Unable to find " + testClass + "." + testMethod, e);
            }
        } finally {
            testSkipFile.delete();
        }

    }
}