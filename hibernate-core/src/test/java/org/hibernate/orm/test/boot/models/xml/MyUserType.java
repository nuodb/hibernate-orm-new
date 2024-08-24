/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.models.xml;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

/**
 * @author Steve Ebersole
 */
public class MyUserType implements UserType<UUID> {

	@Override
	public int getSqlType() {
		return 0;
	}

	@Override
	public Class<UUID> returnedClass() {
		return null;
	}

	@Override
	public boolean equals(UUID x, UUID y) {
		return false;
	}

	@Override
	public int hashCode(UUID x) {
		return 0;
	}

	@Override
	public UUID nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
			throws SQLException {
		return null;
	}

	@Override
	public void nullSafeSet(PreparedStatement st, UUID value, int index, SharedSessionContractImplementor session)
			throws SQLException {

	}

	@Override
	public UUID deepCopy(UUID value) {
		return null;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(UUID value) {
		return null;
	}

	@Override
	public UUID assemble(Serializable cached, Object owner) {
		return null;
	}
}
