/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.annotations.internal;

import java.lang.annotation.Annotation;

import org.hibernate.annotations.DialectOverride;
import org.hibernate.boot.models.annotations.spi.RepeatableContainer;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.jboss.jandex.AnnotationInstance;

import static org.hibernate.boot.models.DialectOverrideAnnotations.DIALECT_OVERRIDE_SQL_UPDATES;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;

/**
 * @author Steve Ebersole
 */
@SuppressWarnings("ClassExplicitlyAnnotation")
public class OverriddenSQLUpdatesAnnotation
		implements DialectOverride.SQLUpdates, RepeatableContainer<DialectOverride.SQLUpdate> {
	private DialectOverride.SQLUpdate[] value;

	public OverriddenSQLUpdatesAnnotation(SourceModelBuildingContext sourceModelBuildingContext) {
	}

	public OverriddenSQLUpdatesAnnotation(
			DialectOverride.SQLUpdates annotation,
			SourceModelBuildingContext sourceModelContext) {
		this.value = extractJdkValue( annotation, DIALECT_OVERRIDE_SQL_UPDATES, "value", sourceModelContext );
	}

	public OverriddenSQLUpdatesAnnotation(
			AnnotationInstance annotation,
			SourceModelBuildingContext sourceModelContext) {
		this.value = extractJandexValue( annotation, DIALECT_OVERRIDE_SQL_UPDATES, "value", sourceModelContext );
	}

	@Override
	public DialectOverride.SQLUpdate[] value() {
		return value;
	}

	@Override
	public void value(DialectOverride.SQLUpdate[] value) {
		this.value = value;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return DialectOverride.SQLUpdates.class;
	}
}
