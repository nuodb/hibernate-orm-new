/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.domain;

import jakarta.persistence.criteria.PluralJoin;

import org.hibernate.metamodel.model.domain.PluralPersistentAttribute;
import org.hibernate.spi.NavigablePath;
import org.hibernate.query.criteria.JpaJoin;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.from.SqmFrom;

/**
 * Base support for joins to plural attributes
 *
 * @param <L> The left-hand side of the join
 * @param <C> The collection type
 * @param <E> The collection's element type
 *
 * @author Steve Ebersole
 */
public abstract class AbstractSqmPluralJoin<L,C,E>
		extends AbstractSqmAttributeJoin<L,E>
		implements JpaJoin<L,E>, PluralJoin<L,C,E> {

	public AbstractSqmPluralJoin(
			SqmFrom<?, L> lhs,
			PluralPersistentAttribute<L,C,E> joinedNavigable,
			String alias,
			SqmJoinType joinType,
			boolean fetched,
			NodeBuilder nodeBuilder) {
		super( lhs, joinedNavigable, alias, joinType, fetched, nodeBuilder );
	}

	protected AbstractSqmPluralJoin(
			SqmFrom<?, L> lhs,
			NavigablePath navigablePath,
			PluralPersistentAttribute<L,C,E> joinedNavigable,
			String alias,
			SqmJoinType joinType,
			boolean fetched,
			NodeBuilder nodeBuilder) {
		super( lhs, navigablePath, joinedNavigable, alias, joinType, fetched, nodeBuilder );
	}

	@Override
	public PluralPersistentAttribute<L, C, E> getModel() {
		return (PluralPersistentAttribute<L, C, E>) super.getNodeType();
	}
}
