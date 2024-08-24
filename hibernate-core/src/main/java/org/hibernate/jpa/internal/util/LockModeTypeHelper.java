/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.jpa.internal.util;

import jakarta.persistence.LockModeType;

import org.hibernate.LockMode;
import org.hibernate.internal.util.LockModeConverter;

/**
 * Helper to deal with conversions between {@link LockModeType} and {@link LockMode}.
 *
 * @author Steve Ebersole
 */
public final class LockModeTypeHelper {
	private LockModeTypeHelper() {
	}

	public static LockModeType getLockModeType(LockMode lockMode) {
		return LockModeConverter.convertToLockModeType( lockMode );
	}

	public static LockMode getLockMode(LockModeType lockModeType) {
		return LockModeConverter.convertToLockMode( lockModeType );
	}

	public static LockMode interpretLockMode(Object value) {
		if ( value == null ) {
			return LockMode.NONE;
		}
		if (value instanceof LockMode) {
			return (LockMode) value;
		}
		else if (value instanceof LockModeType) {
			return getLockMode( (LockModeType) value );
		}
		else if (value instanceof String) {
			return LockMode.fromExternalForm( (String) value );
		}

		throw new IllegalArgumentException( "Unknown lock mode source: '" + value + "'; can't convert from value of type " + value.getClass() );
	}

}
