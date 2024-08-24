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

import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class NamedNativeQueriesJpaAnnotation implements NamedNativeQueries, RepeatableContainer<NamedNativeQuery> {
	private jakarta.persistence.NamedNativeQuery[] value;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public NamedNativeQueriesJpaAnnotation(SourceModelBuildingContext modelContext) {
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public NamedNativeQueriesJpaAnnotation(NamedNativeQueries annotation, SourceModelBuildingContext modelContext) {
		this.value = extractJdkValue( annotation, JpaAnnotations.NAMED_NATIVE_QUERIES, "value", modelContext );
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public NamedNativeQueriesJpaAnnotation(AnnotationInstance annotation, SourceModelBuildingContext modelContext) {
		this.value = extractJandexValue( annotation, JpaAnnotations.NAMED_NATIVE_QUERIES, "value", modelContext );
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return NamedNativeQueries.class;
	}

	@Override
	public jakarta.persistence.NamedNativeQuery[] value() {
		return value;
	}

	public void value(jakarta.persistence.NamedNativeQuery[] value) {
		this.value = value;
	}


}
