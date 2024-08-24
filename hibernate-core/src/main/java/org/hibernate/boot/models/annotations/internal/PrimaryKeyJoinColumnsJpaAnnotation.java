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

import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.PrimaryKeyJoinColumns;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class PrimaryKeyJoinColumnsJpaAnnotation
		implements PrimaryKeyJoinColumns, RepeatableContainer<PrimaryKeyJoinColumn> {
	private jakarta.persistence.PrimaryKeyJoinColumn[] value;
	private jakarta.persistence.ForeignKey foreignKey;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public PrimaryKeyJoinColumnsJpaAnnotation(SourceModelBuildingContext modelContext) {
		this.foreignKey = modelContext.getAnnotationDescriptorRegistry()
				.getDescriptor( jakarta.persistence.ForeignKey.class )
				.createUsage( modelContext );
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public PrimaryKeyJoinColumnsJpaAnnotation(
			PrimaryKeyJoinColumns annotation,
			SourceModelBuildingContext modelContext) {
		this.value = extractJdkValue( annotation, JpaAnnotations.PRIMARY_KEY_JOIN_COLUMNS, "value", modelContext );
		this.foreignKey = extractJdkValue(
				annotation,
				JpaAnnotations.PRIMARY_KEY_JOIN_COLUMNS,
				"foreignKey",
				modelContext
		);
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public PrimaryKeyJoinColumnsJpaAnnotation(AnnotationInstance annotation, SourceModelBuildingContext modelContext) {
		this.value = extractJandexValue( annotation, JpaAnnotations.PRIMARY_KEY_JOIN_COLUMNS, "value", modelContext );
		this.foreignKey = extractJandexValue(
				annotation,
				JpaAnnotations.PRIMARY_KEY_JOIN_COLUMNS,
				"foreignKey",
				modelContext
		);
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return PrimaryKeyJoinColumns.class;
	}

	@Override
	public jakarta.persistence.PrimaryKeyJoinColumn[] value() {
		return value;
	}

	public void value(jakarta.persistence.PrimaryKeyJoinColumn[] value) {
		this.value = value;
	}


	@Override
	public jakarta.persistence.ForeignKey foreignKey() {
		return foreignKey;
	}

	public void foreignKey(jakarta.persistence.ForeignKey value) {
		this.foreignKey = value;
	}


}
