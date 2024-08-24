/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.annotations.internal;

import java.lang.annotation.Annotation;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.Checks;
import org.hibernate.boot.models.HibernateAnnotations;
import org.hibernate.boot.models.annotations.spi.RepeatableContainer;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.jboss.jandex.AnnotationInstance;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class ChecksAnnotation implements Checks, RepeatableContainer<Check> {
	private org.hibernate.annotations.Check[] value;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public ChecksAnnotation(SourceModelBuildingContext modelContext) {
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public ChecksAnnotation(Checks annotation, SourceModelBuildingContext modelContext) {
		this.value = extractJdkValue( annotation, HibernateAnnotations.CHECKS, "value", modelContext );
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public ChecksAnnotation(AnnotationInstance annotation, SourceModelBuildingContext modelContext) {
		this.value = extractJandexValue( annotation, HibernateAnnotations.CHECKS, "value", modelContext );
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return Checks.class;
	}

	@Override
	public org.hibernate.annotations.Check[] value() {
		return value;
	}

	public void value(org.hibernate.annotations.Check[] value) {
		this.value = value;
	}


}
