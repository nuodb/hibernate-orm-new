package org.hibernate.testing.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SkipTestInfo {
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
	StringBuilder sb = new StringBuilder();
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
		errors++;
		needToRewriteSkipFile = true;
		
		StringBuilder sb = new StringBuilder();
		sb.append(NL);
		
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

				if (!methodName.startsWith("test"))
					return;  // Not a test method

				fullyQualifiedTestName = className + '.' + methodName;
				break;
			}
		}

		String context = "";
		
		if (fullyQualifiedTestName == null) {
			TestUtils.LOGGER.error(fullyQualifiedTestName = "# UNABLE TO DETERMINE FAILING TEST DETAILS", sqlException);
			context = sb.toString();
		}

		TestUtils.LOGGER.warn("Add " + fullyQualifiedTestName + " to " + skipListFileName);

		try {
			String line = fullyQualifiedTestName + " (" + causeOfFailure + ')' + NL;
			sb.append(line);
			Files.write(extraTestsToSkip.toPath(), (line + context).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			TestUtils.logException(TestUtils.LOGGER, "Unable to write to " + extraTestsToSkip, e);
		}
	}

	private void initialize(String skipListFileName) {
		if (methodsToSkip.isEmpty()) {
			this.skipListFileName = skipListFileName;

			File skipListFile = getSkipFile(skipListFileName);
			TestUtils.LOGGER.trace("Using " + skipListFile);
			File fileToInitialize = null;

			try {
				// We will put these in the same directory
				fileToInitialize = errorFile = new File(skipListFile.getParentFile(), "errors.txt");
				Files.write(errorFile.toPath(),
						("# Errors from run " + LocalDateTime.now() + NL).getBytes(),
						StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

				fileToInitialize = extraTestsToSkip = new File(skipListFile.getParentFile(), "extra-tests.txt");
				Files.write(extraTestsToSkip.toPath(),
						("# Extra classes to skip. " + LocalDateTime.now() + NL).getBytes(),
						StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				TestUtils.logException(TestUtils.LOGGER, "Failed initializing " + fileToInitialize, e);
			}

			TestUtils.LOGGER.debug("Loading tests to skip from " + skipListFile);
			int lineNum = 0;
			List<String> allLines = null;

			try {
				allLines = Files.readAllLines(skipListFile.toPath());
			} catch (IOException e) {
				TestUtils.logException(TestUtils.LOGGER, "Failed reading " + skipListFile, e);
			}

			errors = 0;

			for (String line : allLines) {
				lineNum++;

				line = line.trim();

				// Ignore blank lines and comments
				if (line.length() == 0 || line.charAt(0) == '#') {
					sb.append(line).append(NL);
					continue;
				}

				// Expecting FQCN, not a path to the class.
				if (line.indexOf('/') != -1)
					skipFileError(skipListFile, lineNum, "Line contains /");

				int ix = line.indexOf('(');
				String comment = "";
				
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
					methodsToSkip.put(line, ALL_METHODS);
				    sb.append(lastWord).append(comment).append(NL);
				}
				else {
					if (lastWord.startsWith("test")) {
						// Name of test, find its class
						String methodName = lastWord;
						ix = line.lastIndexOf('.');
						lastWord = ix == -1 ? line : line.substring(ix + 1);

						if (possibleTestClass(lastWord)) {
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
									skipFileError(skipListFile, lineNum, "Duplicate entry for this class and method");
								else {
									methodsList.add(methodName);
									sb.append(lastWord).append('.').append(methodName).append(comment).append(NL);

								}
							}
						} else {
							skipFileError(skipListFile, lineNum, "Unexpected class name: " + lastWord);
						}
					} else {
						skipFileError(skipListFile, lineNum, "Unexpected class/method: " + lastWord);
					}
				}

			}

			if (errors > 0)
				throw new RuntimeException(errors + " errors reading " + skipListFile);
		}
	}

	private boolean possibleTestClass(String lastWord) {
		for (String word : POSSIBLE_TEST_CLASS_NAME_ENDINGS) {
			if (lastWord.endsWith(word))
				return true;
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

		try {
			Files.write(errorFile.toPath(), (emsg + NL).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			TestUtils.logException(TestUtils.LOGGER, "Unable to write to " + errorFile, e);
		}
	}

	/**
	 * Test program for SkipTestInfo class.
	 * 
	 * @param args Unused.
	 */
	public static void main(String[] args) {
		String[] lines = { //
				"com.nuodb.test.HereIsATest", //
				"com.nuodb.test.MoreTests.testIgnoreMe", //
				"org.hibernate.orm.test.hql.CollectionMapWithComponentValueTest.lambda$testJoinMapKey$9 (Parentheses in SQL using JOIN)" //
		};

		List<String> listOfLines = new ArrayList<>();

		for (String line : lines)
			listOfLines.add(line);

		boolean useTestFile = false;
		File testSkipFile = new File(SKIP_TESTS_FILE_NAME);
		SkipTestInfo skipTestInfo = null;
		
		try {
			if (useTestFile) {
				testSkipFile = new File("target/test-skip-file.txt");
				Files.write(testSkipFile.toPath(), listOfLines, StandardOpenOption.CREATE_NEW);
				skipTestInfo = new SkipTestInfo(testSkipFile.getPath());
			}
			else {
				skipTestInfo = new SkipTestInfo();
			}
				
			LOGGER.info("Reading " + skipTestInfo.srcFileName + ", methods to skip:");

			for (Map.Entry<String, List<String>> entry : skipTestInfo.methodsToSkip.entrySet()) {
				LOGGER.info("    " + entry.getKey() + '=' + entry.getValue());
			}
		} catch (IOException e) {
			TestUtils.logException(LOGGER, "Unable to write to " + testSkipFile, e);
		} finally {
			testSkipFile.delete();
		}

	}
}