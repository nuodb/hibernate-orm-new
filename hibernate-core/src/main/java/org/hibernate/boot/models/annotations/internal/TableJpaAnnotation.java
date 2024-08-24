/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.annotations.internal;

import java.lang.annotation.Annotation;

import org.hibernate.boot.jaxb.mapping.spi.JaxbTableImpl;
import org.hibernate.boot.models.JpaAnnotations;
import org.hibernate.boot.models.annotations.spi.CommonTableDetails;
import org.hibernate.boot.models.xml.spi.XmlDocumentContext;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.jboss.jandex.AnnotationInstance;

import jakarta.persistence.Table;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;
import static org.hibernate.boot.models.xml.internal.XmlAnnotationHelper.applyCatalog;
import static org.hibernate.boot.models.xml.internal.XmlAnnotationHelper.applyOptionalString;
import static org.hibernate.boot.models.xml.internal.XmlAnnotationHelper.applySchema;
import static org.hibernate.boot.models.xml.internal.XmlAnnotationHelper.collectCheckConstraints;
import static org.hibernate.boot.models.xml.internal.XmlAnnotationHelper.collectIndexes;
import static org.hibernate.boot.models.xml.internal.XmlAnnotationHelper.collectUniqueConstraints;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class TableJpaAnnotation implements Table, CommonTableDetails {
	private String name;
	private String catalog;
	private String schema;
	private jakarta.persistence.UniqueConstraint[] uniqueConstraints;
	private jakarta.persistence.Index[] indexes;
	private jakarta.persistence.CheckConstraint[] check;
	private String comment;
	private String options;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public TableJpaAnnotation(SourceModelBuildingContext modelContext) {
		this.name = "";
		this.catalog = "";
		this.schema = "";
		this.uniqueConstraints = new jakarta.persistence.UniqueConstraint[0];
		this.indexes = new jakarta.persistence.Index[0];
		this.check = new jakarta.persistence.CheckConstraint[0];
		this.comment = "";
		this.options = "";
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public TableJpaAnnotation(Table annotation, SourceModelBuildingContext modelContext) {
		this.name = annotation.name();
		this.catalog = annotation.catalog();
		this.schema = annotation.schema();
		this.uniqueConstraints = extractJdkValue( annotation, JpaAnnotations.TABLE, "uniqueConstraints", modelContext );
		this.indexes = extractJdkValue( annotation, JpaAnnotations.TABLE, "indexes", modelContext );
		this.check = extractJdkValue( annotation, JpaAnnotations.TABLE, "check", modelContext );
		this.comment = annotation.comment();
		this.options = annotation.options();
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public TableJpaAnnotation(AnnotationInstance annotation, SourceModelBuildingContext modelContext) {
		this.name = extractJandexValue( annotation, JpaAnnotations.TABLE, "name", modelContext );
		this.catalog = extractJandexValue( annotation, JpaAnnotations.TABLE, "catalog", modelContext );
		this.schema = extractJandexValue( annotation, JpaAnnotations.TABLE, "schema", modelContext );
		this.uniqueConstraints = extractJandexValue(
				annotation,
				JpaAnnotations.TABLE,
				"uniqueConstraints",
				modelContext
		);
		this.indexes = extractJandexValue( annotation, JpaAnnotations.TABLE, "indexes", modelContext );
		this.check = extractJandexValue( annotation, JpaAnnotations.TABLE, "check", modelContext );
		this.comment = extractJandexValue( annotation, JpaAnnotations.TABLE, "comment", modelContext );
		this.options = extractJandexValue( annotation, JpaAnnotations.TABLE, "options", modelContext );
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return Table.class;
	}

	@Override
	public String name() {
		return name;
	}

	public void name(String value) {
		this.name = value;
	}


	@Override
	public String catalog() {
		return catalog;
	}

	public void catalog(String value) {
		this.catalog = value;
	}


	@Override
	public String schema() {
		return schema;
	}

	public void schema(String value) {
		this.schema = value;
	}


	@Override
	public jakarta.persistence.UniqueConstraint[] uniqueConstraints() {
		return uniqueConstraints;
	}

	public void uniqueConstraints(jakarta.persistence.UniqueConstraint[] value) {
		this.uniqueConstraints = value;
	}


	@Override
	public jakarta.persistence.Index[] indexes() {
		return indexes;
	}

	public void indexes(jakarta.persistence.Index[] value) {
		this.indexes = value;
	}


	@Override
	public jakarta.persistence.CheckConstraint[] check() {
		return check;
	}

	public void check(jakarta.persistence.CheckConstraint[] value) {
		this.check = value;
	}


	@Override
	public String comment() {
		return comment;
	}

	public void comment(String value) {
		this.comment = value;
	}


	@Override
	public String options() {
		return options;
	}

	public void options(String value) {
		this.options = value;
	}


	public void apply(JaxbTableImpl jaxbTable, XmlDocumentContext xmlDocumentContext) {
		applyOptionalString( jaxbTable.getName(), this::name );
		applyCatalog( jaxbTable, this, xmlDocumentContext );
		applySchema( jaxbTable, this, xmlDocumentContext );
		applyOptionalString( jaxbTable.getComment(), this::comment );
		applyOptionalString( jaxbTable.getOptions(), this::options );

		check( collectCheckConstraints( jaxbTable.getCheckConstraints(), xmlDocumentContext ) );
		indexes( collectIndexes( jaxbTable.getIndexes(), xmlDocumentContext ) );
		uniqueConstraints( collectUniqueConstraints( jaxbTable.getUniqueConstraints(), xmlDocumentContext ) );
	}
}
