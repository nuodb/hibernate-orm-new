/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.dialect;

import jakarta.persistence.EnumType;

import static org.hibernate.type.SqlTypes.NAMED_ORDINAL_ENUM;

/**
 * Represents a named {@code enum} type on PostgreSQL.
 * <p>
 * Hibernate does <em>not</em> automatically use this for enums
 * mapped as {@link EnumType#ORDINAL}, and
 * instead this type must be explicitly requested using:
 * <pre>
 * &#64;JdbcTypeCode(SqlTypes.NAMED_ORDINAL_ENUM)
 * </pre>
 *
 * @see org.hibernate.type.SqlTypes#NAMED_ORDINAL_ENUM
 * @see PostgreSQLDialect#getEnumTypeDeclaration(String, String[])
 * @see PostgreSQLDialect#getCreateEnumTypeCommand(String, String[])
 */
public class PostgreSQLOrdinalEnumJdbcType extends PostgreSQLEnumJdbcType {

	public static final PostgreSQLOrdinalEnumJdbcType INSTANCE = new PostgreSQLOrdinalEnumJdbcType();

	@Override
	public int getDefaultSqlTypeCode() {
		return NAMED_ORDINAL_ENUM;
	}
}
