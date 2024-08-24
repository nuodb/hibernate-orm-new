/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.annotations.internal;

import java.lang.annotation.Annotation;

import org.hibernate.annotations.FetchProfileOverrides;
import org.hibernate.boot.models.DialectOverrideAnnotations;
import org.hibernate.boot.models.annotations.spi.RepeatableContainer;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.jboss.jandex.AnnotationInstance;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class FetchProfileOverridesAnnotation
		implements FetchProfileOverrides, RepeatableContainer<org.hibernate.annotations.FetchProfileOverride> {
	private org.hibernate.annotations.FetchProfileOverride[] value;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public FetchProfileOverridesAnnotation(SourceModelBuildingContext modelContext) {
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public FetchProfileOverridesAnnotation(FetchProfileOverrides annotation, SourceModelBuildingContext modelContext) {
		this.value = extractJdkValue(
				annotation,
				DialectOverrideAnnotations.FETCH_PROFILE_OVERRIDES,
				"value",
				modelContext
		);
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public FetchProfileOverridesAnnotation(AnnotationInstance annotation, SourceModelBuildingContext modelContext) {
		this.value = extractJandexValue(
				annotation,
				DialectOverrideAnnotations.FETCH_PROFILE_OVERRIDES,
				"value",
				modelContext
		);
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return FetchProfileOverrides.class;
	}

	@Override
	public org.hibernate.annotations.FetchProfileOverride[] value() {
		return value;
	}

	public void value(org.hibernate.annotations.FetchProfileOverride[] value) {
		this.value = value;
	}


}
