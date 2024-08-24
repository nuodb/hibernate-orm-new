/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sql.spi;

/**
 * Defines the "callback" process of recognizing native query parameters.
 *
 * @see org.hibernate.engine.query.spi.NativeQueryInterpreter#recognizeParameters
 */
public interface ParameterRecognizer {

	/**
	 * Called when an ordinal parameter is recognized
	 *
	 * @param sourcePosition The position within the query
	 */
	void ordinalParameter(int sourcePosition);

	/**
	 * Called when a named parameter is recognized
	 *
	 * @param name The recognized parameter name
	 * @param sourcePosition The position within the query
	 */
	void namedParameter(String name, int sourcePosition);

	/**
	 * Called when a JPA-style named parameter is recognized
	 *
	 * @param label The label (identifier) of the JPA-style parameter.  e.g. for a parameter `?2`, the label is `2`
	 * @param sourcePosition The position within the query
	 */
	void jpaPositionalParameter(int label, int sourcePosition);

	/**
	 * Called when a character that is not part of a parameter is recognized.
	 *
	 * @param character The recognized character
	 */
	void other(char character);

	/**
	 * Callback after all parsing is complete
	 */
	default void complete() {
		// by default, nothing to do
	}
}
