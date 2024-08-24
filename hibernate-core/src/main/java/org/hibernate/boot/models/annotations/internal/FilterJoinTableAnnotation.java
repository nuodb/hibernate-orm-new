/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.annotations.internal;

import java.lang.annotation.Annotation;

import org.hibernate.annotations.FilterJoinTable;
import org.hibernate.boot.jaxb.mapping.spi.JaxbFilterImpl;
import org.hibernate.boot.models.annotations.spi.FilterDetails;
import org.hibernate.boot.models.xml.internal.FilterProcessing;
import org.hibernate.boot.models.xml.spi.XmlDocumentContext;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.jboss.jandex.AnnotationInstance;

import static org.hibernate.boot.models.HibernateAnnotations.FILTER_JOIN_TABLE;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;
import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJdkValue;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class FilterJoinTableAnnotation implements FilterJoinTable, FilterDetails {
	private String name;
	private String condition;
	private boolean deduceAliasInjectionPoints;
	private org.hibernate.annotations.SqlFragmentAlias[] aliases;

	/**
	 * Used in creating dynamic annotation instances (e.g. from XML)
	 */
	public FilterJoinTableAnnotation(SourceModelBuildingContext modelContext) {
		this.condition = "";
		this.deduceAliasInjectionPoints = true;
		this.aliases = new org.hibernate.annotations.SqlFragmentAlias[0];
	}

	/**
	 * Used in creating annotation instances from JDK variant
	 */
	public FilterJoinTableAnnotation(FilterJoinTable annotation, SourceModelBuildingContext modelContext) {
		this.name = annotation.name();
		this.condition = annotation.condition();
		this.deduceAliasInjectionPoints = annotation.deduceAliasInjectionPoints();
		this.aliases = extractJdkValue( annotation, FILTER_JOIN_TABLE, "aliases", modelContext );
	}

	/**
	 * Used in creating annotation instances from Jandex variant
	 */
	public FilterJoinTableAnnotation(AnnotationInstance annotation, SourceModelBuildingContext modelContext) {
		this.name = extractJandexValue( annotation, FILTER_JOIN_TABLE, "name", modelContext );
		this.condition = extractJandexValue( annotation, FILTER_JOIN_TABLE, "condition", modelContext );
		this.deduceAliasInjectionPoints = extractJandexValue(
				annotation,
				FILTER_JOIN_TABLE,
				"deduceAliasInjectionPoints",
				modelContext
		);
		this.aliases = extractJandexValue( annotation, FILTER_JOIN_TABLE, "aliases", modelContext );
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return FilterJoinTable.class;
	}

	@Override
	public String name() {
		return name;
	}

	public void name(String value) {
		this.name = value;
	}


	@Override
	public String condition() {
		return condition;
	}

	public void condition(String value) {
		this.condition = value;
	}


	@Override
	public boolean deduceAliasInjectionPoints() {
		return deduceAliasInjectionPoints;
	}

	public void deduceAliasInjectionPoints(boolean value) {
		this.deduceAliasInjectionPoints = value;
	}


	@Override
	public org.hibernate.annotations.SqlFragmentAlias[] aliases() {
		return aliases;
	}

	public void aliases(org.hibernate.annotations.SqlFragmentAlias[] value) {
		this.aliases = value;
	}

	@Override
	public void apply(JaxbFilterImpl jaxbFilter, XmlDocumentContext xmlDocumentContext) {
		name( jaxbFilter.getName() );

		if ( StringHelper.isNotEmpty( jaxbFilter.getCondition() ) ) {
			condition( jaxbFilter.getCondition() );
		}

		if ( jaxbFilter.isAutoAliasInjection() != null ) {
			deduceAliasInjectionPoints( jaxbFilter.isAutoAliasInjection() );
		}

		aliases( FilterProcessing.collectSqlFragmentAliases(
				jaxbFilter.getAliases(),
				xmlDocumentContext
		) );
	}
}
