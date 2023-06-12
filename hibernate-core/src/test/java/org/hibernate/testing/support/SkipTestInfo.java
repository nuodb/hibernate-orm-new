package org.hibernate.testing.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.testing.orm.junit.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SkipTestInfo {
	private static final String SKIP_FILE_HEADING = "# Tests to skip because they generate SQL syntax not supported by NuoDB";

	public static final String SKIP_TESTS_FILE_NAME = "skip-tests.txt";

	@SuppressWarnings("unchecked")
	static final List<String> ALL_METHODS = Collections.EMPTY_LIST;

	private static final String[] POSSIBLE_TEST_CLASS_NAME_ENDINGS = //
			{ "Test", "Tests", "Test2" };

	private static final String NL = System.lineSeparator();

	private static SkipTestInfo instance = null;

	private static final Logger LOGGER = LoggerFactory.getLogger(SkipTestInfo.class);

	Map<String, List<String>> methodsToSkip = new HashMap<>();
	String skipListFileName = null;
	private File errorFile = null;
	private File extraTestsToSkip = null;
	boolean needToRewriteSkipFile = false;
	File newSkipFile = null;
	StringBuilder skipFileContents = new StringBuilder();
	private int errors = 0;

	private String srcFileName;

	public static SkipTestInfo instance() {
		if (instance == null)
			instance = new SkipTestInfo();

		return instance;
	}

	protected SkipTestInfo() {
		this(SKIP_TESTS_FILE_NAME);
	}

	protected SkipTestInfo(String fileName) {
		this.srcFileName = fileName;
		initialize(fileName);
	}

	protected void extraTestToSkip(String causeOfFailure, SQLException sqlException) {
		String fullyQualifiedTestName = null;
		StringBuilder sb = new StringBuilder();
		sb.append(NL);
		boolean matchFound = false;
		String testClassName = null;
		String testMethodName = null;

		for (StackTraceElement ste : sqlException.getStackTrace()) {
			String className = ste.getClassName();
			sb.append("#    ").append(className).append('.').append(ste.getMethodName()).append(NL);

			if (possibleTestClass(className)) {
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
					return;
				}

				if (!methodName.startsWith("test")) {
					if (matchFound)
						break;
					else
						return; // Not a test method
				}

				testClassName = className;
				testMethodName = methodName;
				fullyQualifiedTestName = className + '.' + methodName;
				matchFound = true;
			}
		}

		if (fullyQualifiedTestName == null) {
			String emsg = "# UNABLE TO DETERMINE FAILING TEST DETAILS" + NL //
					+ "failure=" + causeOfFailure + NL //
					+ "exception=" + sqlException + NL //
					+ sb.toString();

			TestUtils.LOGGER.error(fullyQualifiedTestName = "# UNABLE TO DETERMINE FAILING TEST DETAILS", sqlException);
			appendToFile(errorFile, emsg);
			return;
		}

		List<String> methods = methodsToSkip.get(testClassName);

		if (methods != null) {
			if (methods.contains(testMethodName)) {
				// We have this method already. Why has it come up twice?
				TestUtils.LOGGER.warn("Duplicate test? " + fullyQualifiedTestName, sqlException);
				String emsg = "Test already in skip list: " + fullyQualifiedTestName + NL //
						+ "failure=" + causeOfFailure + NL //
						+ "exception=" + sqlException + NL //
						+ sb.toString();
				appendToFile(errorFile, emsg);
				return;
			}
		}

		extraTestToSkip(fullyQualifiedTestName, causeOfFailure);
	}

	private void extraTestToSkip(String fullyQualifiedTestName, String causeOfFailure) {
		errors++;
		LOGGER.info("Errors = " + errors + " needToRewriteSkipFile = " + needToRewriteSkipFile);

		// Haven't initialized the new file yet, do it now
		if (!needToRewriteSkipFile) {
			initFile(newSkipFile, skipFileContents.toString());
		}

		if (errors == 1) {
			// Section heading
			appendToFile(newSkipFile, NL + "# " + LocalDateTime.now());
		}

		needToRewriteSkipFile = true;
		String line = fullyQualifiedTestName + " (" + causeOfFailure + ')';
		TestUtils.LOGGER.warn("You need to add " + fullyQualifiedTestName + " to " + skipListFileName);
		appendToFile(newSkipFile, line);
		appendToFile(extraTestsToSkip, line);
	}

	private void initFile(File fileToInitialize, String text) {
		try {
			// We will put these in the same directory
			Files.write(fileToInitialize.toPath(), text.getBytes(), StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			TestUtils.logException(TestUtils.LOGGER, "Failed initializing " + fileToInitialize, e);
		}
	}

	private List<String> loadFile(File file) {
		try {
			return Files.readAllLines(file.toPath());
		} catch (IOException e) {
			TestUtils.logException(TestUtils.LOGGER, "Failed reading " + file, e);
			return new ArrayList<>();
		}
	}

	private void appendToFile(File file, String line) {
		try {
			// We will put these in the same directory
			Files.write(file.toPath(), (line + NL).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			TestUtils.logException(TestUtils.LOGGER, "Failed appending to " + file, e);
		}
	}

	private void initialize(String skipListFileName) {
		if (methodsToSkip.isEmpty()) {
			this.skipListFileName = skipListFileName;

			File skipListFile = getSkipFile(skipListFileName);
			TestUtils.LOGGER.trace("Using " + skipListFile);

			File workingDir = skipListFile.getParentFile();
			errorFile = new File(workingDir, "errors.txt");
			extraTestsToSkip = new File(workingDir, "extra-tests.txt");
			newSkipFile = new File(workingDir, "updated-" + SKIP_TESTS_FILE_NAME);

			initFile(errorFile, "# Errors from run " + LocalDateTime.now() + NL);
			initFile(extraTestsToSkip, "# Extra classes to skip " + LocalDateTime.now() + NL);

			// No skip file yet - create it now
			if (!skipListFile.exists()) {
				TestUtils.LOGGER.debug("Creating " + skipListFile);
				initFile(skipListFile, SKIP_FILE_HEADING);
				skipFileContents.append(SKIP_FILE_HEADING).append(NL);
				return;
			}

			TestUtils.LOGGER.debug("Loading tests to skip from " + skipListFile);
			List<String> allLines = loadFile(skipListFile);

			int lineNum = 0;
			errors = 0;

			for (String line : allLines) {
				lineNum++;

				line = line.trim();

				// Ignore blank lines and comments
				if (line.length() == 0 || line.charAt(0) == '#') {
					skipFileContents.append(line).append(NL);
					continue;
				}

				// Expecting FQCN, not a path to the class.
				if (line.indexOf('/') != -1)
					skipFileError(skipListFile, lineNum, "Line contains /");

				int ix = line.indexOf('(');
				String comment = "";

				String originalLine = line;

				if (ix != -1) {
					comment = ' ' + line.substring(ix);
					line = line.substring(0, ix).trim();
				}

				ix = line.lastIndexOf('.');
				String lastWord = ix == -1 ? line : line.substring(ix + 1);
				line = line.substring(0, ix);

				if (lastWord.startsWith("lambda$")) {
					needToRewriteSkipFile = true;
					lastWord = lastWord.substring("lambda$".length());

					ix = lastWord.indexOf('$');

					if (ix != -1)
						lastWord = lastWord.substring(0, ix);
				}

				if (possibleTestClass(lastWord)) {
					// Name of a test class, skip all its methods
					line = line + '.' + lastWord;
					methodsToSkip.put(line, ALL_METHODS);
					skipFileContents.append(line).append(comment).append(NL);
				} else {
					if (lastWord.startsWith("test")) {
						// Name of test, find its class
						String methodName = lastWord;
						ix = line.lastIndexOf('.');
						String className = ix == -1 ? line : line.substring(ix + 1);

						if (possibleTestClass(className)) {
							// Class name
							List<String> methodsList = methodsToSkip.get(line);

							if (methodsList == ALL_METHODS) {
								skipFileError(skipListFile, lineNum,
										"This class aready marked without any specific methods");
							} else {
								if (methodsList == null) {
									methodsList = new ArrayList<>();
									methodsToSkip.put(line, methodsList);
								}
								if (methodsList.contains(methodName))
									skipFileError(skipListFile, lineNum,
											"Duplicate entry for " + line + '.' + methodName);
								else {
									methodsList.add(methodName);
									skipFileContents.append(line).append('.').append(methodName).append(comment)
											.append(NL);

								}
							}
						} else {
							skipFileError(skipListFile, lineNum, "Expected test class for " //
									+ methodName + " in line: " + originalLine);
						}
					} else {
						skipFileError(skipListFile, lineNum, "Bad line: " + originalLine);
					}
				}

			}

			if (needToRewriteSkipFile) {
				initFile(newSkipFile, skipFileContents.toString());
			}

			if (errors > 0)
				throw new RuntimeException(errors + " errors reading " + skipListFile);
		}
	}

	private boolean possibleTestClass(String className) {
		for (String word : POSSIBLE_TEST_CLASS_NAME_ENDINGS) {
			if (className.endsWith(word))
				return true;
		}

		if (className.contains("Test")) {
			try {
				Class<?> clazz = Class.forName(className);
				Class<?> superClass = clazz.getSuperclass();

				if (superClass.getSimpleName().endsWith("TestCase"))
					return true;

				if (clazz.getAnnotation(org.hibernate.testing.orm.junit.SessionFactory.class) != null)
					return true;
			} catch (Exception e) {
				; // give up
			}
		}

		return false;
	}

	private File getSkipFile(String skipListFileName) {
		// Is it in the local directory?
		TestUtils.LOGGER.trace("Looking for " + skipListFileName);
		File skipFileList = new File(skipListFileName);

		if (skipFileList.exists()) {
			return skipFileList;
		}

		// When running matrix tests, current dir is hibernate-core/target/matrix/nuodb
		File current = new File(".").getAbsoluteFile();
		TestUtils.LOGGER.trace("Current = " + current + " looking for hibernate-core in path");

		if (current.getPath().contains("/hibernate-core/")) {
			while (!current.getName().equals("hibernate-core")) {
				current = current.getParentFile();
				TestUtils.LOGGER.trace("current = " + current);
			}

			if (current != null)
				return new File(current, skipListFileName);
		}

		throw new RuntimeException(
				"Unable to find '" + skipListFileName + "' in directory " + new File(".").getAbsolutePath());
	}

	private void skipFileError(File skipFileList, int lineNum, String msg) {
		String emsg = "Error in " + skipFileList + "(" + lineNum + "): " + msg;
		TestUtils.LOGGER.warn(emsg);
		errors++;
		needToRewriteSkipFile = true;
		appendToFile(errorFile, emsg);
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
				"org.hibernate.orm.test.hql.CollectionMapWithComponentValueTest.lambda$testJoinMapKey$9 (Parentheses in SQL using JOIN)" //
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
				Files.write(testSkipFile.toPath(), listOfLines, StandardOpenOption.CREATE_NEW);
			} catch (IOException e) {
				TestUtils.logException(TestUtils.LOGGER, "Failed creating " + SKIP_TESTS_FILE_NAME, e);
				throw new RuntimeException(e);
			}
		} else {
			// Make a copy of the actual file for use in this test
			try {
				File original = new File(SKIP_TESTS_FILE_NAME);
				if (original.exists())
					Files.copy(original.toPath(), testSkipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				TestUtils.logException(TestUtils.LOGGER,
						"Failed copying " + SKIP_TESTS_FILE_NAME + " to " + testSkipFile, e);
				throw new RuntimeException(e);
			}
		}

		try {
			skipTestInfo = new SkipTestInfo(testSkipFile.getPath());

			skipTestInfo.extraTestToSkip("MyTest.testItWorks", "I just didn't like it");
			LOGGER.info("Processed " + skipTestInfo.srcFileName + ". Methods to skip are:");

			for (Map.Entry<String, List<String>> entry : skipTestInfo.methodsToSkip.entrySet()) {
				LOGGER.info("    " + entry.getKey() + '=' + entry.getValue());
			}

			LOGGER.info("Skip file is now:");
			for (String line : skipTestInfo.loadFile(skipTestInfo.newSkipFile)) {
				LOGGER.info("    " + line);
			}
		} finally {
			testSkipFile.delete();
		}

	}
}