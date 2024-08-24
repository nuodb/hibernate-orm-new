/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.idbag;

import java.util.List;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import org.hibernate.Hibernate;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Gavin King
 */
@DomainModel(
		xmlMappings = "org/hibernate/orm/test/idbag/UserGroup.hbm.xml"
)
@SessionFactory
public class IdBagTest {

	@Test
	public void testUpdateIdBag(SessionFactoryScope scope) {
		scope.inTransaction(
				s -> {
					User gavin = new User( "gavin" );
					Group admins = new Group( "admins" );
					Group plebs = new Group( "plebs" );
					Group moderators = new Group( "moderators" );
					Group banned = new Group( "banned" );
					gavin.getGroups().add( plebs );
					//gavin.getGroups().add(moderators);
					s.persist( gavin );
					s.persist( plebs );
					s.persist( admins );
					s.persist( moderators );
					s.persist( banned );
				}
		);

		scope.inTransaction(
				s -> {
					CriteriaBuilder criteriaBuilder = s.getCriteriaBuilder();
					CriteriaQuery<User> criteria = criteriaBuilder.createQuery( User.class );
					criteria.from( User.class );
					User gavin = s.createQuery( criteria ).uniqueResult();
//					User gavin = (User) s.createCriteria( User.class ).uniqueResult();
					Group admins = s.load( Group.class, "admins" );
					Group plebs = s.load( Group.class, "plebs" );
					Group banned = s.load( Group.class, "banned" );
					gavin.getGroups().add( admins );
					gavin.getGroups().remove( plebs );
					//gavin.getGroups().add(banned);

					s.remove( plebs );
					s.remove( banned );
					s.remove( s.load( Group.class, "moderators" ) );
					s.remove( admins );
					s.remove( gavin );
				}
		);
	}

	@Test
	public void testJoin(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					User gavin = new User( "gavin" );
					Group admins = new Group( "admins" );
					Group plebs = new Group( "plebs" );
					gavin.getGroups().add( plebs );
					gavin.getGroups().add( admins );
					session.persist( gavin );
					session.persist( plebs );
					session.persist( admins );

					List l = session.createQuery( "from User u join u.groups g" ).list();
					assertEquals( 1, l.size() );

					session.clear();

					gavin = (User) session.createQuery( "from User u join fetch u.groups" ).uniqueResult();
					assertTrue( Hibernate.isInitialized( gavin.getGroups() ) );
					assertEquals( 2, gavin.getGroups().size() );
					assertEquals( "admins", ( (Group) gavin.getGroups().get( 0 ) ).getName() );

					session.remove( gavin.getGroups().get( 0 ) );
					session.remove( gavin.getGroups().get( 1 ) );
					session.remove( gavin );
				}
		);
	}

}

