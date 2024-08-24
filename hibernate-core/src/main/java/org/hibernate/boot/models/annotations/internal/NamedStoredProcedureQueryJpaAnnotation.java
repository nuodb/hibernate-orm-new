/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.annotations.internal;

import java.lang.annotation.Annotation;

import org.hibernate.boot.jaxb.mapping.spi.JaxbNamedStoredProcedureQueryImpl;
import org.hibernate.boot.models.JpaAnnotations;
import org.hibernate.boot.models.xml.internal.QueryProcessing;
import org.hibernate.boot.models.xml.spi.XmlDocumentContext;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.jboss.jandex.AnnotationInstance;

import jakarta.persistence.NamedStoredProcedureQuery;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class NamedStoredProcedureQueryJpaAnnotation implements NamedStoredProcedureQuery {
	private String name;
	private String procedureName;
	private jakarta.persistence.StoredProcedureParameter[] parameters;
	private java.lang.Class[] resultClasses;
	private String[] resultSetMappings;
	private jakarta.persistence.QueryHint[] hints;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public NamedStoredProcedureQueryJpaAnnotation(SourceModelBuildingContext modelContext) {
		this.parameters = new jakarta.persistence.StoredProcedureParameter[0];
		this.resultClasses = new java.lang.Class[0];
		this.resultSetMappings = new String[0];
		this.hints = new jakarta.persistence.QueryHint[0];
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public NamedStoredProcedureQueryJpaAnnotation(
			NamedStoredProcedureQuery annotation,
			SourceModelBuildingContext modelContext) {
		this.name = annotation.name();
		this.procedureName = annotation.procedureName();
		this.parameters = extractJdkValue(
				annotation,
				JpaAnnotations.NAMED_STORED_PROCEDURE_QUERY,
				"parameters",
				modelContext
		);
		this.resultClasses = annotation.resultClasses();
		this.resultSetMappings = annotation.resultSetMappings();
		this.hints = extractJdkValue( annotation, JpaAnnotations.NAMED_STORED_PROCEDURE_QUERY, "hints", modelContext );
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public NamedStoredProcedureQueryJpaAnnotation(
			AnnotationInstance annotation,
			SourceModelBuildingContext modelContext) {
		this.name = extractJandexValue( annotation, JpaAnnotations.NAMED_STORED_PROCEDURE_QUERY, "name", modelContext );
		this.procedureName = extractJandexValue(
				annotation,
				JpaAnnotations.NAMED_STORED_PROCEDURE_QUERY,
				"procedureName",
				modelContext
		);
		this.parameters = extractJandexValue(
				annotation,
				JpaAnnotations.NAMED_STORED_PROCEDURE_QUERY,
				"parameters",
				modelContext
		);
		this.resultClasses = extractJandexValue(
				annotation,
				JpaAnnotations.NAMED_STORED_PROCEDURE_QUERY,
				"resultClasses",
				modelContext
		);
		this.resultSetMappings = extractJandexValue(
				annotation,
				JpaAnnotations.NAMED_STORED_PROCEDURE_QUERY,
				"resultSetMappings",
				modelContext
		);
		this.hints = extractJandexValue(
				annotation,
				JpaAnnotations.NAMED_STORED_PROCEDURE_QUERY,
				"hints",
				modelContext
		);
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return NamedStoredProcedureQuery.class;
	}

	@Override
	public String name() {
		return name;
	}

	public void name(String value) {
		this.name = value;
	}


	@Override
	public String procedureName() {
		return procedureName;
	}

	public void procedureName(String value) {
		this.procedureName = value;
	}


	@Override
	public jakarta.persistence.StoredProcedureParameter[] parameters() {
		return parameters;
	}

	public void parameters(jakarta.persistence.StoredProcedureParameter[] value) {
		this.parameters = value;
	}


	@Override
	public java.lang.Class[] resultClasses() {
		return resultClasses;
	}

	public void resultClasses(java.lang.Class[] value) {
		this.resultClasses = value;
	}


	@Override
	public String[] resultSetMappings() {
		return resultSetMappings;
	}

	public void resultSetMappings(String[] value) {
		this.resultSetMappings = value;
	}


	@Override
	public jakarta.persistence.QueryHint[] hints() {
		return hints;
	}

	public void hints(jakarta.persistence.QueryHint[] value) {
		this.hints = value;
	}


	public void apply(JaxbNamedStoredProcedureQueryImpl jaxbQuery, XmlDocumentContext xmlDocumentContext) {
		name( jaxbQuery.getName() );
		procedureName( jaxbQuery.getProcedureName() );

		hints( QueryProcessing.collectQueryHints( jaxbQuery.getHints(), xmlDocumentContext ) );
		parameters( QueryProcessing.collectParameters( jaxbQuery.getProcedureParameters(), xmlDocumentContext ) );

		resultClasses( QueryProcessing.collectResultClasses( jaxbQuery.getResultClasses(), xmlDocumentContext ) );
		resultSetMappings( QueryProcessing.collectResultMappings( jaxbQuery.getResultClasses(), xmlDocumentContext ) );
	}
}
