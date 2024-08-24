/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.annotations.internal;

import java.lang.annotation.Annotation;

import org.hibernate.boot.models.JpaAnnotations;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.jboss.jandex.AnnotationInstance;

import jakarta.persistence.SqlResultSetMapping;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class SqlResultSetMappingJpaAnnotation implements SqlResultSetMapping {
	private String name;
	private jakarta.persistence.EntityResult[] entities;
	private jakarta.persistence.ConstructorResult[] classes;
	private jakarta.persistence.ColumnResult[] columns;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public SqlResultSetMappingJpaAnnotation(SourceModelBuildingContext modelContext) {
		this.entities = new jakarta.persistence.EntityResult[0];
		this.classes = new jakarta.persistence.ConstructorResult[0];
		this.columns = new jakarta.persistence.ColumnResult[0];
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public SqlResultSetMappingJpaAnnotation(SqlResultSetMapping annotation, SourceModelBuildingContext modelContext) {
		this.name = annotation.name();
		this.entities = extractJdkValue( annotation, JpaAnnotations.SQL_RESULT_SET_MAPPING, "entities", modelContext );
		this.classes = extractJdkValue( annotation, JpaAnnotations.SQL_RESULT_SET_MAPPING, "classes", modelContext );
		this.columns = extractJdkValue( annotation, JpaAnnotations.SQL_RESULT_SET_MAPPING, "columns", modelContext );
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public SqlResultSetMappingJpaAnnotation(AnnotationInstance annotation, SourceModelBuildingContext modelContext) {
		this.name = extractJandexValue( annotation, JpaAnnotations.SQL_RESULT_SET_MAPPING, "name", modelContext );
		this.entities = extractJandexValue(
				annotation,
				JpaAnnotations.SQL_RESULT_SET_MAPPING,
				"entities",
				modelContext
		);
		this.classes = extractJandexValue( annotation, JpaAnnotations.SQL_RESULT_SET_MAPPING, "classes", modelContext );
		this.columns = extractJandexValue( annotation, JpaAnnotations.SQL_RESULT_SET_MAPPING, "columns", modelContext );
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return SqlResultSetMapping.class;
	}

	@Override
	public String name() {
		return name;
	}

	public void name(String value) {
		this.name = value;
	}


	@Override
	public jakarta.persistence.EntityResult[] entities() {
		return entities;
	}

	public void entities(jakarta.persistence.EntityResult[] value) {
		this.entities = value;
	}


	@Override
	public jakarta.persistence.ConstructorResult[] classes() {
		return classes;
	}

	public void classes(jakarta.persistence.ConstructorResult[] value) {
		this.classes = value;
	}


	@Override
	public jakarta.persistence.ColumnResult[] columns() {
		return columns;
	}

	public void columns(jakarta.persistence.ColumnResult[] value) {
		this.columns = value;
	}


}
