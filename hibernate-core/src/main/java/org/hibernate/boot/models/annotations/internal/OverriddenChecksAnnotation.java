/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.annotations.internal;

import java.lang.annotation.Annotation;

import org.hibernate.annotations.DialectOverride;
import org.hibernate.boot.models.DialectOverrideAnnotations;
import org.hibernate.boot.models.annotations.spi.RepeatableContainer;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.jboss.jandex.AnnotationInstance;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;

/**
 * @author Steve Ebersole
 */
@SuppressWarnings("ClassExplicitlyAnnotation")
public class OverriddenChecksAnnotation
		implements DialectOverride.Checks, RepeatableContainer<DialectOverride.Check> {
	private DialectOverride.Check[] value;

	public OverriddenChecksAnnotation(SourceModelBuildingContext modelContext) {
	}

	public OverriddenChecksAnnotation(DialectOverride.Checks source, SourceModelBuildingContext modelContext) {
		value( extractJdkValue( source, DialectOverrideAnnotations.DIALECT_OVERRIDE_CHECKS, "value", modelContext ) );
	}

	public OverriddenChecksAnnotation(AnnotationInstance source, SourceModelBuildingContext modelContext) {
		value( extractJandexValue(
				source,
				DialectOverrideAnnotations.DIALECT_OVERRIDE_CHECKS,
				"value",
				modelContext
		) );
	}

	@Override
	public DialectOverride.Check[] value() {
		return value;
	}

	@Override
	public void value(DialectOverride.Check[] value) {
		this.value = value;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return DialectOverride.Checks.class;
	}
}
