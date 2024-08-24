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

import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class NamedEntityGraphsJpaAnnotation implements NamedEntityGraphs, RepeatableContainer<NamedEntityGraph> {
	private jakarta.persistence.NamedEntityGraph[] value;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public NamedEntityGraphsJpaAnnotation(SourceModelBuildingContext modelContext) {
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public NamedEntityGraphsJpaAnnotation(NamedEntityGraphs annotation, SourceModelBuildingContext modelContext) {
		this.value = extractJdkValue( annotation, JpaAnnotations.NAMED_ENTITY_GRAPHS, "value", modelContext );
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public NamedEntityGraphsJpaAnnotation(AnnotationInstance annotation, SourceModelBuildingContext modelContext) {
		this.value = extractJandexValue( annotation, JpaAnnotations.NAMED_ENTITY_GRAPHS, "value", modelContext );
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return NamedEntityGraphs.class;
	}

	@Override
	public jakarta.persistence.NamedEntityGraph[] value() {
		return value;
	}

	public void value(jakarta.persistence.NamedEntityGraph[] value) {
		this.value = value;
	}


}
