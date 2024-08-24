/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.entitymode.map.compositeId;

import java.util.HashMap;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DomainModel(
		xmlMappings = "org/hibernate/orm/test/entitymode/map/compositeId/CompId.hbm.xml"
)
@SessionFactory
@TestForIssue(jiraKey = "HHH-15201")
public class CompositeIdTest {

	@Test
	public void testImplicitCompositeIdInDynamicMapMode(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					HashMap<Object, Object> id = new HashMap<>();
					id.put( "id1", "1" );
					id.put( "id2", "2" );
					id.put( "name", "Fab" );

					session.persist( "CompId", id );
				}
		);

		scope.inTransaction(
				session -> {
					HashMap<Object, Object> id = new HashMap<>();
					id.put( "id1", "1" );
					id.put( "id2", "2" );
					HashMap<Object, Object> compId = (HashMap<Object, Object>) session.get( "CompId", id );
					assertNotNull( compId );
					assertThat( compId.get( "id1" ), is( "1" ) );
					assertThat( compId.get( "id2" ), is( "2" ) );
					assertThat( compId.get( "name" ), is( "Fab" ) );
				}
		);
	}
}
