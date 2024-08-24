/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.spi;

import java.util.function.Consumer;
import java.util.function.Predicate;
import jakarta.persistence.Parameter;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.ParameterMetadata;
import org.hibernate.query.QueryParameter;

/**
 * @author Steve Ebersole
 */
public interface ParameterMetadataImplementor extends ParameterMetadata {
	void visitParameters(Consumer<QueryParameterImplementor<?>> consumer);

	default void collectAllParameters(Consumer<QueryParameterImplementor<?>> collector) {
		visitParameters( collector );
	}

	@Override
	default void visitRegistrations(Consumer<? extends QueryParameter<?>> action) {
		//noinspection unchecked
		visitParameters( (Consumer) action );
	}

	boolean hasAnyMatching(Predicate<QueryParameterImplementor<?>> filter);

	@Override
	QueryParameterImplementor<?> findQueryParameter(String name);

	@Override
	QueryParameterImplementor<?> getQueryParameter(String name);

	@Override
	QueryParameterImplementor<?> findQueryParameter(int positionLabel);

	@Override
	QueryParameterImplementor<?> getQueryParameter(int positionLabel);

	@Override
	<P> QueryParameterImplementor<P> resolve(Parameter<P> param);

	QueryParameterBindings createBindings(SessionFactoryImplementor sessionFactory);
}
