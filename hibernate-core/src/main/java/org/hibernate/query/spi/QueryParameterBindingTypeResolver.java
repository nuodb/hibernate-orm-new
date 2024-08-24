/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.spi;

import org.hibernate.Incubating;
import org.hibernate.metamodel.MappingMetamodel;
import org.hibernate.query.BindableType;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * A resolver for {@link BindableType} based on a parameter value being bound, when no explicit type information is
 * supplied.
 *
 * @apiNote This interface was originally a supertype of {@link org.hibernate.engine.spi.SessionFactoryImplementor},
 *          but this is now a deprecated relationship. Its functionality should now be accessed via its new subtype
 *          {@link org.hibernate.metamodel.spi.MappingMetamodelImplementor}.
 *
 * @author Steve Ebersole
 */
@Incubating
public interface QueryParameterBindingTypeResolver {
	<T> BindableType<? super T> resolveParameterBindType(T bindValue);
	<T> BindableType<T> resolveParameterBindType(Class<T> clazz);
	TypeConfiguration getTypeConfiguration();
	MappingMetamodel getMappingMetamodel();
}
