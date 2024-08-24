/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.internal;

import org.hibernate.annotations.TenantId;
import org.hibernate.models.internal.jandex.JandexClassDetails;
import org.hibernate.models.internal.jdk.JdkBuilders;
import org.hibernate.models.spi.AnnotationDescriptorRegistry;
import org.hibernate.models.spi.ClassDetailsRegistry;
import org.hibernate.models.spi.RegistryPrimer;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.IndexView;

/**
 * @author Steve Ebersole
 */
public class ModelsHelper {
	public static void preFillRegistries(RegistryPrimer.Contributions contributions, SourceModelBuildingContext buildingContext) {
		OrmAnnotationHelper.forEachOrmAnnotation( contributions::registerAnnotation );

		buildingContext.getAnnotationDescriptorRegistry().getDescriptor( TenantId.class );

		final IndexView jandexIndex = buildingContext.getJandexIndex();
		if ( jandexIndex == null ) {
			return;
		}

		final ClassDetailsRegistry classDetailsRegistry = buildingContext.getClassDetailsRegistry();
		final AnnotationDescriptorRegistry annotationDescriptorRegistry = buildingContext.getAnnotationDescriptorRegistry();

		for ( ClassInfo knownClass : jandexIndex.getKnownClasses() ) {
			final String className = knownClass.name().toString();

			if ( knownClass.isAnnotation() ) {
				// it is always safe to load the annotation classes - we will never be enhancing them
				//noinspection rawtypes
				final Class annotationClass = buildingContext
						.getClassLoading()
						.classForName( className );
				//noinspection unchecked
				annotationDescriptorRegistry.resolveDescriptor(
						annotationClass,
						(t) -> JdkBuilders.buildAnnotationDescriptor( annotationClass, buildingContext )
				);
			}

			classDetailsRegistry.resolveClassDetails(
					className,
					(name) -> new JandexClassDetails( knownClass, buildingContext )
			);
		}
	}
}
