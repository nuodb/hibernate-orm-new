/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.jpa.criteria;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Parameter;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.hibernate.orm.test.jpa.BaseEntityManagerFunctionalTestCase;
import org.hibernate.query.SemanticException;
import org.hibernate.query.spi.QueryImplementor;
import org.hibernate.testing.TestForIssue;
import org.junit.Test;

public class CriteriaQueryTypeQueryAdapterTest extends BaseEntityManagerFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class[] {
				Bid.class,
				Item.class
		};
	}

	@Test
	@TestForIssue(jiraKey = "HHH-12685")
	public void testCriteriaQueryParameterIsBoundCheckNotFails() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Item> query = builder.createQuery( Item.class );
			Root<Item> root = query.from( Item.class );
			ParameterExpression<String> parameter = builder.parameter( String.class, "name" );
			Predicate predicate = builder.equal( root.get( "name" ), parameter );
			query.where( predicate );
			TypedQuery<Item> criteriaQuery = entityManager.createQuery( query );
			Parameter<?> dynamicParameter = criteriaQuery.getParameter( "name" );
			boolean bound = criteriaQuery.isBound( dynamicParameter );
			assertFalse( bound );
		} );
	}

	@Test
	@TestForIssue(jiraKey = "HHH-12685")
	public void testCriteriaQueryGetParameters() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Item> query = builder.createQuery( Item.class );
			Root<Item> root = query.from( Item.class );
			ParameterExpression<String> parameter = builder.parameter( String.class, "name" );
			Predicate predicate = builder.equal( root.get( "name" ), parameter );
			query.where( predicate );

			TypedQuery<Item> criteriaQuery = entityManager.createQuery( query );

			Set<Parameter<?>> parameters = criteriaQuery.getParameters();
			assertEquals( 1, parameters.size() );

			Parameter<?> dynamicParameter = parameters.iterator().next();
			assertEquals( "name", dynamicParameter.getName() );
		} );
	}

	@TestForIssue(jiraKey = "HHH-12685")
	@Test(expected = IllegalArgumentException.class)
	public void testCriteriaQueryGetParameterOfWrongType() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Item> query = builder.createQuery( Item.class );
			Root<Item> root = query.from( Item.class );
			ParameterExpression<String> parameter = builder.parameter( String.class, "name" );
			Predicate predicate = builder.equal( root.get( "name" ), parameter );
			query.where( predicate );
			TypedQuery<Item> criteriaQuery = entityManager.createQuery( query );
			criteriaQuery.getParameter( "name", Integer.class );
		} );
	}

	@TestForIssue(jiraKey = "HHH-12685")
	@Test(expected = IllegalArgumentException.class)
	public void testCriteriaQueryGetNonExistingParameter() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Item> query = builder.createQuery( Item.class );
			Root<Item> root = query.from( Item.class );
			ParameterExpression<String> parameter = builder.parameter( String.class, "name" );
			Predicate predicate = builder.equal( root.get( "name" ), parameter );
			query.where( predicate );
			TypedQuery<Item> criteriaQuery = entityManager.createQuery( query );
			criteriaQuery.getParameter( "placedAt" );
		} );
	}

	@Test
	public void testSetParameterPassingTypeNotFails() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Item> query = builder.createQuery( Item.class );

			Predicate predicate = builder.equal(
					query.from( Item.class ).get( "name" ),
					builder.parameter( String.class, "name" )
			);
			query.where( predicate );

			QueryImplementor<?> criteriaQuery = (QueryImplementor<?>) entityManager.createQuery( query );

			criteriaQuery.setParameter( "name", "2" ).list();
		} );
	}

	@Test
	public void testSetParameterTypeInstantNotFails() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Bid> query = builder.createQuery( Bid.class );

			Predicate predicate = builder.equal(
					query.from( Bid.class ).get( "placedAt" ),
					builder.parameter( Instant.class, "placedAt" )
			);
			query.where( predicate );

			QueryImplementor<?> criteriaQuery = (QueryImplementor<?>) entityManager.createQuery( query );

			criteriaQuery.setParameter( "placedAt", Instant.now() ).list();
		} );
	}

	@Test(expected = SemanticException.class)
	public void testSetParameterOfTypeInstantToAFloatParameterType() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Bid> query = builder.createQuery( Bid.class );

			Predicate predicate = builder.equal(
					query.from( Bid.class ).get( "amount" ),
					builder.parameter( Instant.class, "placedAt" )
			);
			query.where( predicate );

			QueryImplementor<?> criteriaQuery = (QueryImplementor<?>) entityManager.createQuery( query );

			criteriaQuery.setParameter( "placedAt", Instant.now() ).list();
		} );
	}


	@Test(expected = SemanticException.class)
	public void testSetParameterOfTypeDateToAFloatParameterType() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Bid> query = builder.createQuery( Bid.class );

			Predicate predicate = builder.equal(
					query.from( Bid.class ).get( "amount" ),
					builder.parameter( Date.class, "placedAt" )
			);
			query.where( predicate );

			QueryImplementor<?> criteriaQuery = (QueryImplementor<?>) entityManager.createQuery( query );

			criteriaQuery.setParameter( "placedAt", Date.from(Instant.now()), TemporalType.DATE ).list();
		} );
	}

	@Entity(name = "Bid")
	public static class Bid implements Serializable {
		@Id
		Long id;

		float amount;

		Instant placedAt;

		@Id
		@ManyToOne
		Item item;
	}

	@Entity(name = "Item")
	public static class Item implements Serializable {
		@Id
		Long id;

		String name;

		@OneToMany(mappedBy = "item")
		Set<Bid> bids = new HashSet<>();
	}
}
