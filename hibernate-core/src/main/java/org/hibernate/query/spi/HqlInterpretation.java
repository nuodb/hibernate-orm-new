/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.spi;

import org.hibernate.query.sqm.internal.DomainParameterXref;
import org.hibernate.query.sqm.tree.SqmStatement;

/**
 * @author Steve Ebersole
 *
 * @param <R> the query result type
 */
public interface HqlInterpretation<R> {
	SqmStatement<R> getSqmStatement();

	ParameterMetadataImplementor getParameterMetadata();

	DomainParameterXref getDomainParameterXref();

	void validateResultType(Class<?> resultType);

}
