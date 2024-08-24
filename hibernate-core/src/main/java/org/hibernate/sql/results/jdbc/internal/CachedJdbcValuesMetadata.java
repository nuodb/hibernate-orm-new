/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.jdbc.internal;

import java.io.Serializable;

import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.sql.results.jdbc.spi.JdbcValuesMetadata;
import org.hibernate.type.BasicType;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.spi.TypeConfiguration;

public final class CachedJdbcValuesMetadata implements JdbcValuesMetadata, Serializable {
	private final String[] columnNames;
	private final BasicType<?>[] types;

	public CachedJdbcValuesMetadata(String[] columnNames, BasicType<?>[] types) {
		this.columnNames = columnNames;
		this.types = types;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int resolveColumnPosition(String columnName) {
		final int position = ArrayHelper.indexOf( columnNames, columnName ) + 1;
		if ( position == 0 ) {
			throw new IllegalStateException( "Unexpected resolving of unavailable column: " + columnName );
		}
		return position;
	}

	@Override
	public String resolveColumnName(int position) {
		final String name = columnNames[position - 1];
		if ( name == null ) {
			throw new IllegalStateException( "Unexpected resolving of unavailable column at position: " + position );
		}
		return name;
	}

	@Override
	public <J> BasicType<J> resolveType(
			int position,
			JavaType<J> explicitJavaType,
			TypeConfiguration typeConfiguration) {
		final BasicType<?> type = types[position - 1];
		if ( type == null ) {
			throw new IllegalStateException( "Unexpected resolving of unavailable column at position: " + position );
		}
		if ( explicitJavaType == null || type.getJavaTypeDescriptor() == explicitJavaType ) {
			//noinspection unchecked
			return (BasicType<J>) type;
		}
		else {
			return typeConfiguration.getBasicTypeRegistry().resolve(
					explicitJavaType,
					type.getJdbcType()
			);
		}
	}

}
