/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.dialect.sequence;

import org.hibernate.MappingException;

/**
 * Sequence support for {@link org.hibernate.dialect.MariaDBDialect}.
 *
 * @author Christian Beikov
 */
public final class MariaDBSequenceSupport extends ANSISequenceSupport {

	public static final SequenceSupport INSTANCE = new MariaDBSequenceSupport();

	@Override
	public String getCreateSequenceString(String sequenceName) throws MappingException {
		return "create sequence " + sequenceName + " nocache";
	}

	@Override
	public String getCreateSequenceString(String sequenceName, int initialValue, int incrementSize)
			throws MappingException {
		return "create sequence " + sequenceName
				+ startingValue( initialValue, incrementSize )
				+ " start with " + initialValue
				+ " increment by " + incrementSize
				+ " nocache";
	}

	@Override
	public String getSelectSequencePreviousValString(String sequenceName) throws MappingException {
		return "previous value for " + sequenceName;
	}

	@Override
	public boolean sometimesNeedsStartingValue() {
		return true;
	}
}
