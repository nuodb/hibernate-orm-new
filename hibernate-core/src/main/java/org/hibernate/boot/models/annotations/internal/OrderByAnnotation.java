/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.annotations.internal;

import java.lang.annotation.Annotation;

import org.hibernate.annotations.OrderBy;
import org.hibernate.boot.models.HibernateAnnotations;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.jboss.jandex.AnnotationInstance;

import static org.hibernate.boot.models.internal.OrmAnnotationHelper.extractJandexValue;

@SuppressWarnings({ "ClassExplicitlyAnnotation", "unused" })
@jakarta.annotation.Generated("org.hibernate.orm.build.annotations.ClassGeneratorProcessor")
public class OrderByAnnotation implements OrderBy {
	private String clause;

	public OrderByAnnotation(SourceModelBuildingContext modelContext) {
		this.clause = "";
	}

	public OrderByAnnotation(OrderBy annotation, SourceModelBuildingContext modelContext) {
		clause = annotation.clause();
	}

	public OrderByAnnotation(AnnotationInstance annotation, SourceModelBuildingContext modelContext) {
		clause = extractJandexValue( annotation, HibernateAnnotations.ORDER_BY, "clause", modelContext );
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return OrderBy.class;
	}

	@Override
	public String clause() {
		return clause;
	}

	public void clause(String value) {
		this.clause = value;
	}


}
