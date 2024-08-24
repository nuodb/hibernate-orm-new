/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.internal;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.hibernate.boot.models.DialectOverrideAnnotations;
import org.hibernate.boot.models.HibernateAnnotations;
import org.hibernate.boot.models.JpaAnnotations;
import org.hibernate.boot.models.XmlAnnotations;
import org.hibernate.models.AnnotationAccessException;
import org.hibernate.models.spi.AnnotationDescriptor;
import org.hibernate.models.spi.AttributeDescriptor;
import org.hibernate.models.spi.JandexValueExtractor;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

/**
 * @author Steve Ebersole
 */
public class OrmAnnotationHelper {

	public static void forEachOrmAnnotation(Consumer<AnnotationDescriptor<?>> consumer) {
		JpaAnnotations.forEachAnnotation( consumer );
		HibernateAnnotations.forEachAnnotation( consumer );
		DialectOverrideAnnotations.forEachAnnotation( consumer );
		XmlAnnotations.forEachAnnotation( consumer );
	}

	public static void forEachOrmAnnotation(Class<?> declarer, Consumer<AnnotationDescriptor<?>> consumer) {
		for ( Field field : declarer.getFields() ) {
			if ( AnnotationDescriptor.class.isAssignableFrom( field.getType() ) ) {
				try {
					consumer.accept( (AnnotationDescriptor<?>) field.get( null ) );
				}
				catch (IllegalAccessException e) {
					throw new AnnotationAccessException(
							String.format(
									Locale.ROOT,
									"Unable to access standard annotation descriptor field - %s",
									field.getName()
							),
							e
					);
				}
			}
		}
	}

	public static <V, A extends Annotation> V extractJdkValue(A jdkAnnotation, AttributeDescriptor<V> attributeDescriptor, SourceModelBuildingContext modelContext) {
		return attributeDescriptor
				.getTypeDescriptor()
				.createJdkValueExtractor( modelContext )
				.extractValue( jdkAnnotation, attributeDescriptor, modelContext );
	}

	public static <V, A extends Annotation> V extractJdkValue(A jdkAnnotation, AnnotationDescriptor<A> annotationDescriptor, String attributeName, SourceModelBuildingContext modelContext) {
		final AttributeDescriptor<V> attributeDescriptor = annotationDescriptor.getAttribute( attributeName );
		return extractJdkValue( jdkAnnotation, attributeDescriptor, modelContext );
	}

	public static <V> V extractJandexValue(AnnotationInstance jandexAnnotation, AttributeDescriptor<V> attributeDescriptor, SourceModelBuildingContext modelContext) {
		final JandexValueExtractor<V> extractor = attributeDescriptor.getTypeDescriptor().createJandexValueExtractor( modelContext );
		return extractor.extractValue( jandexAnnotation, attributeDescriptor, modelContext );
//		final AnnotationValue value = jandexAnnotation.value( attributeDescriptor.getName() );
//		return attributeDescriptor
//				.getTypeDescriptor()
//				.createJandexValueConverter( modelContext )
//				.convert( value, modelContext );
	}

	public static <V, A extends Annotation> V extractJandexValue(AnnotationInstance jandexAnnotation, AnnotationDescriptor<A> annotationDescriptor, String attributeName, SourceModelBuildingContext modelContext) {
		final AttributeDescriptor<V> attributeDescriptor = annotationDescriptor.getAttribute( attributeName );
		return extractJandexValue( jandexAnnotation, attributeDescriptor, modelContext );
	}

	public static List<Annotation> extractAnnotationTypeAnnotations(Class<? extends Annotation> annotationType) {
		final ArrayList<Annotation> result = new ArrayList<>();
		final Annotation[] annotationTypeAnnotations = annotationType.getAnnotations();
		for ( int i = 0; i < annotationTypeAnnotations.length; i++ ) {
			final Annotation annotationTypeAnnotation = annotationTypeAnnotations[i];
			final Class<? extends Annotation> annotationTypeAnnotationType = annotationTypeAnnotation.annotationType();

			// skip a few well-know ones that are irrelevant
			if ( annotationTypeAnnotationType == Repeatable.class
					|| annotationTypeAnnotationType == Target.class
					|| annotationTypeAnnotationType == Retention.class
					|| annotationTypeAnnotationType == Documented.class ) {
				continue;
			}

			result.add( annotationTypeAnnotation );
		}
		return result;
	}
}
