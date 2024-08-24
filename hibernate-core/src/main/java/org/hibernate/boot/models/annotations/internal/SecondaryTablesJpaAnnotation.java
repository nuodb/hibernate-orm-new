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

import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SecondaryTables;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class SecondaryTablesJpaAnnotation implements SecondaryTables, RepeatableContainer<SecondaryTable> {
	private jakarta.persistence.SecondaryTable[] value;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public SecondaryTablesJpaAnnotation(SourceModelBuildingContext modelContext) {
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public SecondaryTablesJpaAnnotation(SecondaryTables annotation, SourceModelBuildingContext modelContext) {
		this.value = extractJdkValue( annotation, JpaAnnotations.SECONDARY_TABLES, "value", modelContext );
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public SecondaryTablesJpaAnnotation(AnnotationInstance annotation, SourceModelBuildingContext modelContext) {
		this.value = extractJandexValue( annotation, JpaAnnotations.SECONDARY_TABLES, "value", modelContext );
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return SecondaryTables.class;
	}

	@Override
	public jakarta.persistence.SecondaryTable[] value() {
		return value;
	}

	public void value(jakarta.persistence.SecondaryTable[] value) {
		this.value = value;
	}


}
