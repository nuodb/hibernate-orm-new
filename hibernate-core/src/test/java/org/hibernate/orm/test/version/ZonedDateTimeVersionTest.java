/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.version;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.Session;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
@TestForIssue( jiraKey = "HHH-10026" )
public class ZonedDateTimeVersionTest extends BaseNonConfigCoreFunctionalTestCase {

    @Override
    protected Class[] getAnnotatedClasses() {
        return new Class[] { TheEntity.class };
    }

    @Test
    public void testInstantUsageAsVersion() {
        Session session = openSession();
        session.getTransaction().begin();
        TheEntity e = new TheEntity( 1 );
        log.info("New instance: e timestamp = " + e.getTs());
        session.save( e );
        session.getTransaction().commit();
        session.close();
        log.info("After commit: e timestamp = " + e.getTs());

        session = openSession();
        session.getTransaction().begin();
        Object ts = session.createNativeQuery("select ts from the_entity where id = 1", String.class).getResultList().get(0);
        log.info("SQL by id: ts is a " + ts.getClass() + ", ts=" + ts);
        e = session.byId( TheEntity.class ).load( 1 );
        log.info("Load by id: e timestamp = " + e.getTs());
        assertThat( e.getTs(), notNullValue() );
        session.getTransaction().commit();
        session.close();

        session = openSession();
        session.getTransaction().begin();
        e = session.byId( TheEntity.class ).load( 1 );
        log.info("Delete: e timestamp = " + e.getTs());
        session.delete( e );
        session.getTransaction().commit();
        session.close();
    }


    @Entity(name = "TheEntity")
    @Table(name="the_entity")
    public static class TheEntity {
        private Integer id;
        private ZonedDateTime ts;

        public TheEntity() {
        }

        public TheEntity(Integer id) {
            this.id = id;
        }

        @Id
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        @Version
        public ZonedDateTime getTs() {
            return ts;
        }

        public void setTs(ZonedDateTime ts) {
            this.ts = ts;
        }
    }
}
