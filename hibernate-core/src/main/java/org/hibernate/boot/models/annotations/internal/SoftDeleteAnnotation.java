/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.annotations.internal;

import java.lang.annotation.Annotation;

import org.hibernate.annotations.SoftDelete;
import org.hibernate.boot.models.HibernateAnnotations;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.jboss.jandex.AnnotationInstance;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class SoftDeleteAnnotation implements SoftDelete {
	private String columnName;
	private String options;
	private String comment;
	private org.hibernate.annotations.SoftDeleteType strategy;
	private java.lang.Class<? extends jakarta.persistence.AttributeConverter<java.lang.Boolean, ?>> converter;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public SoftDeleteAnnotation(SourceModelBuildingContext modelContext) {
		this.columnName = "";
		this.strategy = org.hibernate.annotations.SoftDeleteType.DELETED;
		this.converter = org.hibernate.annotations.SoftDelete.UnspecifiedConversion.class;
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public SoftDeleteAnnotation(SoftDelete annotation, SourceModelBuildingContext modelContext) {
		this.columnName = annotation.columnName();
		this.strategy = annotation.strategy();
		this.options = annotation.options();
		this.comment = annotation.comment();
		this.converter = annotation.converter();
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public SoftDeleteAnnotation(AnnotationInstance annotation, SourceModelBuildingContext modelContext) {
		this.columnName = extractJandexValue(
				annotation,
				HibernateAnnotations.SOFT_DELETE,
				"columnName",
				modelContext
		);
		this.strategy = extractJandexValue( annotation, HibernateAnnotations.SOFT_DELETE, "strategy", modelContext );
		this.options = extractJandexValue( annotation, HibernateAnnotations.SOFT_DELETE, "options", modelContext );
		this.comment = extractJandexValue( annotation, HibernateAnnotations.SOFT_DELETE, "comment", modelContext );
		this.converter = extractJandexValue( annotation, HibernateAnnotations.SOFT_DELETE, "converter", modelContext );
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return SoftDelete.class;
	}

	@Override
	public String columnName() {
		return columnName;
	}

	public void columnName(String value) {
		this.columnName = value;
	}

	@Override
	public String options() {
		return options;
	}

	public void options(String options) {
		this.options = options;
	}

	@Override
	public String comment() {
		return comment;
	}

	public void comment(String comment) {
		this.comment = comment;
	}

	@Override
	public org.hibernate.annotations.SoftDeleteType strategy() {
		return strategy;
	}

	public void strategy(org.hibernate.annotations.SoftDeleteType value) {
		this.strategy = value;
	}


	@Override
	public java.lang.Class<? extends jakarta.persistence.AttributeConverter<java.lang.Boolean, ?>> converter() {
		return converter;
	}

	public void converter(java.lang.Class<? extends jakarta.persistence.AttributeConverter<java.lang.Boolean, ?>> value) {
		this.converter = value;
	}


}
