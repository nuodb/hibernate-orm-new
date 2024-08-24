/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import org.hibernate.metamodel.model.domain.EntityDomainType;
import org.hibernate.metamodel.model.domain.spi.JpaMetamodelImplementor;
import org.hibernate.query.hql.spi.SqmCreationState;
import org.hibernate.query.sqm.SqmJoinable;
import org.hibernate.query.sqm.SqmPathSource;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.domain.SqmEntityValuedSimplePath;
import org.hibernate.query.sqm.tree.domain.SqmPath;
import org.hibernate.query.sqm.tree.domain.SqmPluralPartJoin;
import org.hibernate.query.sqm.tree.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class EntitySqmPathSource<J> extends AbstractSqmPathSource<J> implements SqmJoinable<Object, J> {
	private final boolean isGeneric;

	public EntitySqmPathSource(
			String localPathName,
			SqmPathSource<J> pathModel,
			EntityDomainType<J> domainType,
			BindableType jpaBindableType,
			boolean isGeneric) {
		super( localPathName, pathModel, domainType, jpaBindableType );
		this.isGeneric = isGeneric;
	}

	@Override
	public EntityDomainType<J> getSqmPathType() {
		return (EntityDomainType<J>) super.getSqmPathType();
	}

	@Override
	public SqmPathSource<?> findSubPathSource(String name) {
		return getSqmPathType().findSubPathSource( name );
	}

	@Override
	public SqmPathSource<?> findSubPathSource(String name, JpaMetamodelImplementor metamodel) {
		return getSqmPathType().findSubPathSource( name, metamodel );
	}

	@Override
	public boolean isGeneric() {
		return isGeneric;
	}

	@Override
	public SqmPath<J> createSqmPath(SqmPath<?> lhs, SqmPathSource<?> intermediatePathSource) {
		return new SqmEntityValuedSimplePath<>(
				PathHelper.append( lhs, this, intermediatePathSource ),
				pathModel,
				lhs,
				lhs.nodeBuilder()
		);
	}

	@Override
	public SqmPluralPartJoin<Object, J> createSqmJoin(
			SqmFrom<?, Object> lhs,
			SqmJoinType joinType,
			String alias,
			boolean fetched,
			SqmCreationState creationState) {
		return new SqmPluralPartJoin<>(
				lhs,
				pathModel,
				alias,
				joinType,
				creationState.getCreationContext().getNodeBuilder()
		);
	}

	@Override
	public String getName() {
		return getPathName();
	}
}
