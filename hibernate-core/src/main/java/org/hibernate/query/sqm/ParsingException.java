/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm;

import org.hibernate.QueryException;

/**
 * Occurs when an unexpected condition is encountered while interpreting the
 * output of the HQL parser. This exception represents some sort of bug in
 * the parser, whereas {@link org.hibernate.query.SyntaxException} indicates
 * a problem with the query itself.
 *
 * @apiNote This exception type should not be used to report any expected
 *          kind of failure which could occur due to user error. It should
 *          only be used to assert that a condition should never occur. Of
 *          course, this exception usually occurs when a query has some sort
 *          of error. But its occurrence indicates that the query parser
 *          should have detected and reported that error earlier, in a more
 *          meaningful way, via a {@code SyntaxException}.
 *
 * @author Steve Ebersole
 *
 * @see InterpretationException
 */
public class ParsingException extends QueryException {
	public ParsingException(String message) {
		super( message );
	}
}
