/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.jpa.criteria.nulliteral;

import org.hibernate.orm.test.jpa.BaseEntityManagerFunctionalTestCase;

import org.hibernate.testing.orm.junit.Jira;
import org.junit.Test;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

/**
 * @author Andrea Boriero
 */
public class NullLiteralExpression extends BaseEntityManagerFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class[] { Person.class, Subject.class };
	}

	@Test
	@Jira( "https://hibernate.atlassian.net/browse/HHH-11159" )
	public void testNullLiteralExpressionInCriteriaUpdate() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaUpdate<Person> criteriaUpdate = builder.createCriteriaUpdate( Person.class );
			criteriaUpdate.from( Person.class );
			criteriaUpdate.set( Person_.subject, builder.nullLiteral( Subject.class ) );
			entityManager.createQuery( criteriaUpdate ).executeUpdate();
		} );
	}

	@Test
	@Jira( "https://hibernate.atlassian.net/browse/HHH-16803" )
	public void testEnumNullLiteralUpdate() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			final CriteriaUpdate<Person> criteriaUpdate = builder.createCriteriaUpdate( Person.class );
			criteriaUpdate.from( Person.class );
			criteriaUpdate.set( Person_.eyeColor, builder.nullLiteral( EyeColor.class ) );
			entityManager.createQuery( criteriaUpdate ).executeUpdate();
		} );
	}
}
