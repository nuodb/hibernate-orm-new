/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.domain;

import org.hibernate.metamodel.mapping.CollectionPart;
import org.hibernate.metamodel.model.domain.ListPersistentAttribute;
import org.hibernate.metamodel.model.domain.TreatableDomainType;
import org.hibernate.query.criteria.JpaExpression;
import org.hibernate.query.criteria.JpaPredicate;
import org.hibernate.query.hql.spi.SqmCreationProcessingState;
import org.hibernate.query.hql.spi.SqmCreationState;
import org.hibernate.query.sqm.SqmPathSource;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.from.SqmAttributeJoin;
import org.hibernate.query.sqm.tree.from.SqmTreatedAttributeJoin;
import org.hibernate.spi.NavigablePath;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

/**
 * @author Steve Ebersole
 */
public class SqmTreatedListJoin<O,T, S extends T> extends SqmListJoin<O,S> implements SqmTreatedAttributeJoin<O,T,S> {
	private final SqmListJoin<O,T> wrappedPath;
	private final TreatableDomainType<S> treatTarget;

	public SqmTreatedListJoin(
			SqmListJoin<O, T> wrappedPath,
			TreatableDomainType<S> treatTarget,
			String alias) {
		this( wrappedPath, treatTarget, alias, false );
	}

	public SqmTreatedListJoin(
			SqmListJoin<O, T> wrappedPath,
			TreatableDomainType<S> treatTarget,
			String alias,
			boolean fetched) {
		//noinspection unchecked
		super(
				wrappedPath.getLhs(),
				wrappedPath.getNavigablePath()
						.append( CollectionPart.Nature.ELEMENT.getName() )
						.treatAs( treatTarget.getTypeName(), alias ),
				(ListPersistentAttribute<O, S>) wrappedPath.getAttribute(),
				alias,
				wrappedPath.getSqmJoinType(),
				fetched,
				wrappedPath.nodeBuilder()
		);
		this.treatTarget = treatTarget;
		this.wrappedPath = wrappedPath;
	}

	private SqmTreatedListJoin(
			NavigablePath navigablePath,
			SqmListJoin<O, T> wrappedPath,
			TreatableDomainType<S> treatTarget,
			String alias,
			boolean fetched) {
		//noinspection unchecked
		super(
				wrappedPath.getLhs(),
				navigablePath,
				(ListPersistentAttribute<O, S>) wrappedPath.getAttribute(),
				alias,
				wrappedPath.getSqmJoinType(),
				fetched,
				wrappedPath.nodeBuilder()
		);
		this.treatTarget = treatTarget;
		this.wrappedPath = wrappedPath;
	}

	@Override
	public SqmTreatedListJoin<O, T, S> copy(SqmCopyContext context) {
		final SqmTreatedListJoin<O, T, S> existing = context.getCopy( this );
		if ( existing != null ) {
			return existing;
		}
		final SqmTreatedListJoin<O, T, S> path = context.registerCopy(
				this,
				new SqmTreatedListJoin<>(
						getNavigablePath(),
						wrappedPath.copy( context ),
						treatTarget,
						getExplicitAlias(),
						isFetched()
				)
		);
		copyTo( path, context );
		return path;
	}

	@Override
	public SqmListJoin<O,T> getWrappedPath() {
		return wrappedPath;
	}

	@Override
	public TreatableDomainType<S> getTreatTarget() {
		return treatTarget;
	}

	@Override
	public SqmPathSource<S> getNodeType() {
		return treatTarget;
	}

	@Override
	public TreatableDomainType<S> getReferencedPathSource() {
		return treatTarget;
	}

	@Override
	public SqmPathSource<?> getResolvedModel() {
		return treatTarget;
	}

	@Override
	public SqmPath<?> resolveIndexedAccess(
			SqmExpression<?> selector,
			boolean isTerminal,
			SqmCreationState creationState) {
		return getWrappedPath().resolveIndexedAccess( selector, isTerminal, creationState );
	}

	@Override
	public SqmTreatedListJoin<O,T,S> on(JpaExpression<Boolean> restriction) {
		return (SqmTreatedListJoin<O,T,S>) super.on( restriction );
	}

	@Override
	public SqmTreatedListJoin<O,T,S> on(Expression<Boolean> restriction) {
		return (SqmTreatedListJoin<O, T, S>) super.on( restriction );
	}

	@Override
	public SqmTreatedListJoin<O,T,S> on(JpaPredicate... restrictions) {
		return (SqmTreatedListJoin<O, T, S>) super.on( restrictions );
	}

	@Override
	public SqmTreatedListJoin<O,T,S> on(Predicate... restrictions) {
		return (SqmTreatedListJoin<O, T, S>) super.on( restrictions );
	}




	@Override
	public SqmAttributeJoin<O, S> makeCopy(SqmCreationProcessingState creationProcessingState) {
		return new SqmTreatedListJoin<>( wrappedPath, treatTarget, getAlias() );
	}

	@Override
	public void appendHqlString(StringBuilder sb) {
		sb.append( "treat(" );
		wrappedPath.appendHqlString( sb );
		sb.append( " as " );
		sb.append( treatTarget.getTypeName() );
		sb.append( ')' );
	}
}
