/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.boot.query;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.boot.internal.NamedNativeQueryDefinitionImpl;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sql.spi.NamedNativeQueryMemento;

/**
 * Boot-time descriptor of a named native query, as defined in
 * annotations or xml
 *
 * @see jakarta.persistence.NamedNativeQuery
 * @see org.hibernate.annotations.NamedNativeQuery
 *
 * @author Steve Ebersole
 * @author Gavin King
 */
public interface NamedNativeQueryDefinition<E> extends NamedQueryDefinition<E> {
	String getSqlQueryString();

	String getResultSetMappingName();

	@Override
	NamedNativeQueryMemento<E> resolve(SessionFactoryImplementor factory);

	class Builder<E> extends AbstractNamedQueryBuilder<E, Builder<E>> {
		private String sqlString;

		private String resultSetMappingName;

		private Set<String> querySpaces;

		private Map<String, String> parameterTypes;
		private Integer firstResult;
		private Integer maxResults;

		public Builder(String name) {
			super( name );
		}

		public Builder<E> setSqlString(String sqlString) {
			this.sqlString = sqlString;
			return getThis();
		}

		public Builder<E> setFirstResult(Integer firstResult) {
			this.firstResult = firstResult;
			return getThis();
		}

		public Builder<E> setMaxResults(Integer maxResults) {
			this.maxResults = maxResults;
			return getThis();
		}

		public NamedNativeQueryDefinition<E> build() {
			return new NamedNativeQueryDefinitionImpl<>(
					getName(),
					getResultClass(),
					sqlString,
					resultSetMappingName,
					getQuerySpaces(),
					getCacheable(),
					getCacheRegion(),
					getCacheMode(),
					getFlushMode(),
					getReadOnly(),
					getTimeout(),
					getFetchSize(),
					getComment(),
					firstResult,
					maxResults,
					getHints()
			);
		}

		@Override
		protected Builder<E> getThis() {
			return this;
		}

		public String getSqlString() {
			return sqlString;
		}

		public Set<String> getQuerySpaces() {
			return querySpaces;
		}

		public Map<String, String> getParameterTypes() {
			return parameterTypes == null ? Collections.emptyMap() : parameterTypes;
		}

		public String getResultSetMappingName() {
			return resultSetMappingName;
		}

		public Builder<E> addSynchronizedQuerySpace(String space) {
			if ( this.querySpaces == null ) {
				this.querySpaces = new HashSet<>();
			}
			this.querySpaces.add( space );
			return getThis();
		}

		public Builder<E> setQuerySpaces(Set<String> spaces) {
			this.querySpaces = spaces;
			return this;
		}

		public Builder<E> setResultSetMappingName(String resultSetMappingName) {
			this.resultSetMappingName = resultSetMappingName;
			return this;
		}

		public void addParameterTypeHint(String name, String type) {
			if ( parameterTypes == null ) {
				parameterTypes = new HashMap<>();
			}

			parameterTypes.put( name, type );
		}
	}
}
