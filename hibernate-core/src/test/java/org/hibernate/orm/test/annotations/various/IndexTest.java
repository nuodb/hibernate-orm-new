/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.annotations.various;

import java.util.Date;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * @author Emmanuel Bernard
 */
@DomainModel(
		annotatedClasses = {
				Conductor.class,
				Vehicule.class,
				ProfessionalAgreement.class,
				Truck.class
		}
)
@SessionFactory
public class IndexTest {

	@AfterEach
	public void tearDown(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					session.createQuery( "delete from Conductor" ).executeUpdate();
					session.createQuery( "delete from Vehicule" ).executeUpdate();
					session.createQuery( "delete from ProfessionalAgreement" ).executeUpdate();
					session.createQuery( "delete from Truck" ).executeUpdate();
				}
		);
	}

	@Test
	public void testIndexManyToOne(SessionFactoryScope scope) {
		//TODO find a way to test indexes???
		scope.inTransaction(
				session -> {
					Conductor emmanuel = new Conductor();
					emmanuel.setName( "Emmanuel" );
					session.persist( emmanuel );
					Vehicule tank = new Vehicule();
					tank.setCurrentConductor( emmanuel );
					tank.setRegistrationNumber( "324VX43" );
					session.persist( tank );
					session.flush();
					session.remove( tank );
					session.remove( emmanuel );
				}
		);
	}

	@Test
	public void testIndexAndJoined(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					Conductor cond = new Conductor();
					cond.setName( "Bob" );
					session.persist( cond );
					ProfessionalAgreement agreement = new ProfessionalAgreement();
					agreement.setExpirationDate( new Date() );
					session.persist( agreement );
					Truck truck = new Truck();
					truck.setAgreement( agreement );
					truck.setWeight( 20 );
					truck.setRegistrationNumber( "2003424" );
					truck.setYear( 2005 );
					truck.setCurrentConductor( cond );
					session.persist( truck );
					session.flush();
					session.remove( truck );
					session.remove( agreement );
					session.remove( cond );
				}
		);
	}

}
