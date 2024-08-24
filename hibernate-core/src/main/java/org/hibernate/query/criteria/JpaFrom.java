/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.criteria;

import org.hibernate.Incubating;
import org.hibernate.metamodel.model.domain.EntityDomainType;
import org.hibernate.query.sqm.tree.SqmJoinType;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.CollectionAttribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.MapAttribute;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * API extension to the JPA {@link From} contract
 *
 * @author Steve Ebersole
 */
public interface JpaFrom<O,T> extends JpaPath<T>, JpaFetchParent<O,T>, From<O,T> {
	@Override
	JpaFrom<O,T> getCorrelationParent();

	@Override
	<Y> JpaEntityJoin<T, Y> join(Class<Y> entityClass);

	@Override
	<Y> JpaEntityJoin<T, Y> join(Class<Y> entityClass, JoinType joinType);

	default <X> JpaEntityJoin<T, X> join(Class<X> entityJavaType, SqmJoinType joinType) {
		return join( entityJavaType, joinType.getCorrespondingJpaJoinType() );
	}

	@Override
	<Y> JpaJoin<T, Y> join(EntityType<Y> entity);

	@Override
	<Y> JpaJoin<T, Y> join(EntityType<Y> entity, JoinType joinType);

	<X> JpaEntityJoin<T,X> join(EntityDomainType<X> entity);

	<X> JpaEntityJoin<T,X> join(EntityDomainType<X> entity, SqmJoinType joinType);

	@Incubating
	<X> JpaDerivedJoin<X> join(Subquery<X> subquery);

	@Incubating
	<X> JpaDerivedJoin<X> join(Subquery<X> subquery, SqmJoinType joinType);

	@Incubating
	<X> JpaDerivedJoin<X> joinLateral(Subquery<X> subquery);

	@Incubating
	<X> JpaDerivedJoin<X> joinLateral(Subquery<X> subquery, SqmJoinType joinType);

	@Incubating
	<X> JpaDerivedJoin<X> join(Subquery<X> subquery, SqmJoinType joinType, boolean lateral);

	@Incubating
	<X> JpaJoin<?, X> join(JpaCteCriteria<X> cte);

	@Incubating
	<X> JpaJoin<?, X> join(JpaCteCriteria<X> cte, SqmJoinType joinType);

	@Incubating
	<X> JpaCrossJoin<X> crossJoin(Class<X> entityJavaType);

	@Incubating
	<X> JpaCrossJoin<X> crossJoin(EntityDomainType<X> entity);

	// Covariant overrides

	@Override
	<Y> JpaJoin<T, Y> join(SingularAttribute<? super T, Y> attribute);

	@Override
	<Y> JpaJoin<T, Y> join(SingularAttribute<? super T, Y> attribute, JoinType jt);

	@Override
	<Y> JpaCollectionJoin<T, Y> join(CollectionAttribute<? super T, Y> collection);

	@Override
	<Y> JpaSetJoin<T, Y> join(SetAttribute<? super T, Y> set);

	@Override
	<Y> JpaListJoin<T, Y> join(ListAttribute<? super T, Y> list);

	@Override
	<K, V> JpaMapJoin<T, K, V> join(MapAttribute<? super T, K, V> map);

	@Override
	<Y> JpaCollectionJoin<T, Y> join(CollectionAttribute<? super T, Y> collection, JoinType jt);

	@Override
	<Y> JpaSetJoin<T, Y> join(SetAttribute<? super T, Y> set, JoinType jt);

	@Override
	<Y> JpaListJoin<T, Y> join(ListAttribute<? super T, Y> list, JoinType jt);

	@Override
	<K, V> JpaMapJoin<T, K, V> join(MapAttribute<? super T, K, V> map, JoinType jt);

	@Override
	<X, Y> JpaJoin<X, Y> join(String attributeName);

	@Override
	<X, Y> JpaCollectionJoin<X, Y> joinCollection(String attributeName);

	@Override
	<X, Y> JpaSetJoin<X, Y> joinSet(String attributeName);

	@Override
	<X, Y> JpaListJoin<X, Y> joinList(String attributeName);

	@Override
	<X, K, V> JpaMapJoin<X, K, V> joinMap(String attributeName);

	@Override
	<X, Y> JpaJoin<X, Y> join(String attributeName, JoinType jt);

	@Override
	<X, Y> JpaCollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt);

	@Override
	<X, Y> JpaSetJoin<X, Y> joinSet(String attributeName, JoinType jt);

	@Override
	<X, Y> JpaListJoin<X, Y> joinList(String attributeName, JoinType jt);

	@Override
	<X, K, V> JpaMapJoin<X, K, V> joinMap(String attributeName, JoinType jt);

	@Override
	<S extends T> JpaTreatedFrom<O,T,S> treatAs(Class<S> treatJavaType);

	@Override
	<S extends T> JpaTreatedFrom<O,T,S> treatAs(EntityDomainType<S> treatJavaType);
}
