/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.annotations.uniqueconstraint;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.AnnotationException;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseUnitTestCase;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.Test;

/**
 * @author Nikolay Shestakov
 *
 */
public class UniqueConstraintValidationTest extends BaseUnitTestCase {

	@Test(expected = AnnotationException.class)
	@TestForIssue(jiraKey = "HHH-4084")
	public void testUniqueConstraintWithEmptyColumnName() {
		buildSessionFactory(EmptyColumnNameEntity.class);
	}

	@Test(expected = AnnotationException.class)
	public void testUniqueConstraintWithEmptyColumnNameList() {
		buildSessionFactory(EmptyColumnNameListEntity.class);
	}

	@Test
	public void testUniqueConstraintWithNotExistsColumnName() {
		buildSessionFactory(NotExistsColumnEntity.class);
	}

	private void buildSessionFactory(Class<?> entity) {
		StandardServiceRegistry serviceRegistry = ServiceRegistryUtil.serviceRegistry();

		try {
			new MetadataSources( serviceRegistry )
					.addAnnotatedClass( entity )
					.buildMetadata()
					.buildSessionFactory()
					.close();
		}
		finally {
			StandardServiceRegistryBuilder.destroy( serviceRegistry );
		}
	}

	@Entity
	@Table(name = "tbl_emptycolumnnameentity", uniqueConstraints = @UniqueConstraint(columnNames = ""))
	public static class EmptyColumnNameEntity implements Serializable {
		@Id
		protected Long id;
	}

	@Entity
	@Table(name = "tbl_emptycolumnnamelistentity", uniqueConstraints = @UniqueConstraint(columnNames = {}))
	public static class EmptyColumnNameListEntity implements Serializable {
		@Id
		protected Long id;
	}

	@Entity
	@Table(name = "tbl_notexistscolumnentity", uniqueConstraints = @UniqueConstraint(columnNames = "notExists"))
	public static class NotExistsColumnEntity implements Serializable {
		@Id
		protected Long id;
	}
}
