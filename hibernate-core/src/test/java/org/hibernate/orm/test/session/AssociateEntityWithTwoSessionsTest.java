/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.session;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.proxy.AbstractLazyInitializer;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.logger.LoggerInspectionRule;
import org.hibernate.testing.logger.Triggerable;
import org.hibernate.testing.orm.junit.EntityManagerFactoryScope;
import org.hibernate.testing.orm.junit.Jpa;

import org.junit.Rule;
import org.junit.jupiter.api.Test;

import org.jboss.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Vlad Mihalcea
 */
@Jpa(annotatedClasses = {
		AssociateEntityWithTwoSessionsTest.Location.class,
		AssociateEntityWithTwoSessionsTest.Event.class
})
public class AssociateEntityWithTwoSessionsTest {

	@Rule
	public LoggerInspectionRule logInspection = new LoggerInspectionRule(
			Logger.getMessageLogger( CoreMessageLogger.class, AbstractLazyInitializer.class.getName() ) );

	@Test
	@TestForIssue( jiraKey = "HHH-12216" )
	public void test(EntityManagerFactoryScope scope) {

		final Location location = new Location();
		location.setCity( "Cluj" );

		final Event event = new Event();
		event.setLocation( location );

		scope.inTransaction( entityManager -> {
			entityManager.persist( location );
			entityManager.persist( event );
		} );

		final Triggerable triggerable = logInspection.watchForLogMessages( "HHH000485" );
		triggerable.reset();

		scope.inTransaction( entityManager -> {
			Event event1 = entityManager.find( Event.class, event.id );
			Location location1 = event1.getLocation();

			try {
				scope.inTransaction( _entityManager -> {
					_entityManager.unwrap( Session.class ).lock( location1, LockMode.NONE );
				} );

				fail("Should have thrown a HibernateException");
			}
			catch (Exception expected) {
			}
		} );

		assertEquals(
			"HHH000485: Illegally attempted to associate a proxy for entity [org.hibernate.orm.test.session.AssociateEntityWithTwoSessionsTest$Location] with id [1] with two open sessions.",
			triggerable.triggerMessage()
		);

	}

	@Entity(name = "Location")
	public static class Location {

		@Id
		@GeneratedValue
		public Long id;

		public String city;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}
	}

	@Entity(name = "Event")
	public static class Event {

		@Id
		@GeneratedValue
		public Long id;

		@ManyToOne(fetch = FetchType.LAZY)
		private Location location;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Location getLocation() {
			return location;
		}

		public void setLocation(Location location) {
			this.location = location;
		}
	}
}
