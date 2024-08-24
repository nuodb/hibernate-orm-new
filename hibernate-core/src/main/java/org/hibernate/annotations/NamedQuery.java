/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.EntityManager;

import org.hibernate.CacheMode;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Declares a named query written in HQL or JPQL.
 * <p>
 * Whereas {@link jakarta.persistence.NamedQuery} allows settings to be specified
 * using stringly-typed {@link jakarta.persistence.QueryHint}s, this annotation
 * is typesafe.
 * <p>
 * Note that the members of this annotation correspond to hints enumerated by
 * {@link org.hibernate.jpa.AvailableHints}.
 *
 * @author Carlos Gonzalez-Cadenas
 *
 * @see org.hibernate.query.Query
 */
@Target({TYPE, PACKAGE})
@Retention(RUNTIME)
@Repeatable(NamedQueries.class)
public @interface NamedQuery {

	/**
	 * The name of this query. Must be unique within a persistence unit.
	 */
	String name();

	/**
	 * The text of the HQL query.
	 */
	String query();

	/**
	 * Optional query result class that is used by default when creating the query.
	 * May be overridden by explicitly passing a class object to
	 * {@link EntityManager#createNamedQuery(String, Class)}.
	 *
	 * @see jakarta.persistence.NamedQuery#resultClass()
	 */
	Class<?> resultClass() default void.class;

	/**
	 * The flush mode for this query.
	 *
	 * @see org.hibernate.query.CommonQueryContract#setFlushMode(jakarta.persistence.FlushModeType)
	 * @see org.hibernate.jpa.HibernateHints#HINT_FLUSH_MODE
	 */
	FlushModeType flushMode() default FlushModeType.PERSISTENCE_CONTEXT;

	/**
	 * Whether the query results are cacheable.
	 * Default is {@code false}, that is, not cacheable.
	 *
	 * @see org.hibernate.query.SelectionQuery#setCacheable(boolean)
	 * @see org.hibernate.jpa.HibernateHints#HINT_CACHEABLE
	 */
	boolean cacheable() default false;

	/**
	 * If the query results are cacheable, the name of the query cache region.
	 *
	 * @see org.hibernate.query.SelectionQuery#setCacheRegion(String)
	 * @see org.hibernate.jpa.HibernateHints#HINT_CACHE_REGION
	 */
	String cacheRegion() default "";

	/**
	 * The number of rows fetched by the JDBC driver per trip.
	 *
	 * @see org.hibernate.query.SelectionQuery#setFetchSize(int)
	 * @see org.hibernate.jpa.HibernateHints#HINT_FETCH_SIZE
	 */
	int fetchSize() default -1;

	/**
	 * The query timeout in seconds.
	 * Default is no timeout.
	 *
	 * @see org.hibernate.query.CommonQueryContract#setTimeout(int)
	 * @see org.hibernate.jpa.HibernateHints#HINT_TIMEOUT
	 * @see org.hibernate.jpa.SpecHints#HINT_SPEC_QUERY_TIMEOUT
	 */
	int timeout() default -1;

	/**
	 * A comment added to the generated SQL query.
	 * Useful when engaging with DBA.
	 *
	 * @see org.hibernate.query.CommonQueryContract#setComment(String)
	 * @see org.hibernate.jpa.HibernateHints#HINT_COMMENT
	 */
	String comment() default "";

	/**
	 * The cache storage mode for objects returned by this query.
	 *
	 * @see org.hibernate.query.Query#setCacheStoreMode(CacheStoreMode)
	 * @see org.hibernate.jpa.SpecHints#HINT_SPEC_CACHE_STORE_MODE
	 */
	CacheStoreMode cacheStoreMode() default CacheStoreMode.USE;

	/**
	 * The cache retrieval mode for objects returned by this query.
	 *
	 * @see org.hibernate.query.Query#setCacheRetrieveMode(CacheRetrieveMode)
	 * @see org.hibernate.jpa.SpecHints#HINT_SPEC_CACHE_RETRIEVE_MODE
	 */
	CacheRetrieveMode cacheRetrieveMode() default CacheRetrieveMode.USE;

	/**
	 * The cache interaction mode for this query.
	 *
	 * @see org.hibernate.query.SelectionQuery#setCacheMode(CacheMode)
	 * @see org.hibernate.jpa.HibernateHints#HINT_CACHE_MODE
	 */
	CacheMode cacheMode() default CacheMode.NORMAL;

	/**
	 * Whether the results should be loaded in read-only mode.
	 * Default is {@code false}.
	 *
	 * @see org.hibernate.query.SelectionQuery#setReadOnly(boolean)
	 * @see org.hibernate.jpa.HibernateHints#HINT_READ_ONLY
	 */
	boolean readOnly() default false;
}
