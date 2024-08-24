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

import jakarta.persistence.NamedStoredProcedureQueries;
import jakarta.persistence.NamedStoredProcedureQuery;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class NamedStoredProcedureQueriesJpaAnnotation
		implements NamedStoredProcedureQueries, RepeatableContainer<NamedStoredProcedureQuery> {
	private jakarta.persistence.NamedStoredProcedureQuery[] value;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public NamedStoredProcedureQueriesJpaAnnotation(SourceModelBuildingContext modelContext) {
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public NamedStoredProcedureQueriesJpaAnnotation(
			NamedStoredProcedureQueries annotation,
			SourceModelBuildingContext modelContext) {
		this.value = extractJdkValue(
				annotation,
				JpaAnnotations.NAMED_STORED_PROCEDURE_QUERIES,
				"value",
				modelContext
		);
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public NamedStoredProcedureQueriesJpaAnnotation(
			AnnotationInstance annotation,
			SourceModelBuildingContext modelContext) {
		this.value = extractJandexValue(
				annotation,
				JpaAnnotations.NAMED_STORED_PROCEDURE_QUERIES,
				"value",
				modelContext
		);
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return NamedStoredProcedureQueries.class;
	}

	@Override
	public jakarta.persistence.NamedStoredProcedureQuery[] value() {
		return value;
	}

	public void value(jakarta.persistence.NamedStoredProcedureQuery[] value) {
		this.value = value;
	}


}
