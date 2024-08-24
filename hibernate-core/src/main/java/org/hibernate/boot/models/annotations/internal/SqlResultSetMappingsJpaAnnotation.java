/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.annotations.internal;

import java.lang.annotation.Annotation;

import org.hibernate.boot.models.JpaAnnotations;
import org.hibernate.boot.models.annotations.spi.RepeatableContainer;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.jboss.jandex.AnnotationInstance;

import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class SqlResultSetMappingsJpaAnnotation
		implements SqlResultSetMappings, RepeatableContainer<SqlResultSetMapping> {
	private SqlResultSetMapping[] value;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public SqlResultSetMappingsJpaAnnotation(SourceModelBuildingContext modelContext) {
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public SqlResultSetMappingsJpaAnnotation(SqlResultSetMappings annotation, SourceModelBuildingContext modelContext) {
		this.value = extractJdkValue( annotation, JpaAnnotations.SQL_RESULT_SET_MAPPINGS, "value", modelContext );
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public SqlResultSetMappingsJpaAnnotation(AnnotationInstance annotation, SourceModelBuildingContext modelContext) {
		this.value = extractJandexValue( annotation, JpaAnnotations.SQL_RESULT_SET_MAPPINGS, "value", modelContext );
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return SqlResultSetMappings.class;
	}

	@Override
	public SqlResultSetMapping[] value() {
		return value;
	}

	public void value(SqlResultSetMapping[] value) {
		this.value = value;
	}


}
