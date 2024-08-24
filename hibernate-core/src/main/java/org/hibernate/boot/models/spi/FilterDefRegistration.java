/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.hibernate.boot.model.internal.FilterDefBinder;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.resource.beans.spi.ManagedBean;
import org.hibernate.resource.beans.spi.ManagedBeanRegistry;

/**
 * Global registration of a filter definition
 *
 * @see org.hibernate.annotations.FilterDef
 * @see org.hibernate.boot.jaxb.mapping.spi.JaxbFilterDefImpl
 *
 * @author Marco Belladelli
 */
public class FilterDefRegistration {
	private final String name;
	private final String defaultCondition;
	private final boolean autoEnabled;
	private final boolean applyToLoadByKey;
	private final Map<String, ClassDetails> parameterTypes;
	private final Map<String, ClassDetails> parameterResolvers;

	public FilterDefRegistration(
			String name,
			String defaultCondition,
			boolean autoEnabled,
			boolean applyToLoadByKey,
			Map<String, ClassDetails> parameterTypes,
			Map<String, ClassDetails> parameterResolvers) {
		this.name = name;
		this.defaultCondition = defaultCondition;
		this.autoEnabled = autoEnabled;
		this.applyToLoadByKey = applyToLoadByKey;
		this.parameterTypes = parameterTypes;
		this.parameterResolvers = parameterResolvers;
	}

	public String getName() {
		return name;
	}

	public String getDefaultCondition() {
		return defaultCondition;
	}

	public boolean isAutoEnabled() {
		return autoEnabled;
	}

	public boolean isApplyToLoadByKey() {
		return applyToLoadByKey;
	}

	public Map<String, ClassDetails> getParameterTypes() {
		return parameterTypes;
	}

	public Map<String, ClassDetails> getParameterResolvers() {
		return parameterResolvers;
	}

	public FilterDefinition toFilterDefinition(MetadataBuildingContext buildingContext) {
		final ManagedBeanRegistry beanRegistry = buildingContext
				.getBootstrapContext()
				.getServiceRegistry()
				.getService( ManagedBeanRegistry.class );

		final Map<String, JdbcMapping> parameterJdbcMappings;
		if ( CollectionHelper.isEmpty( parameterTypes ) ) {
			parameterJdbcMappings = Collections.emptyMap();
		}
		else {
			parameterJdbcMappings = new HashMap<>();
			parameterTypes.forEach( (key,value) -> {
				parameterJdbcMappings.put(
						key,
						FilterDefBinder.resolveFilterParamType( value.toJavaClass(), buildingContext )
				);
			} );
		}

		final Map<String, ManagedBean<? extends Supplier<?>>> parameterResolvers;
		if ( CollectionHelper.isEmpty( this.parameterResolvers ) ) {
			parameterResolvers = Collections.emptyMap();
		}
		else {
			parameterResolvers = new HashMap<>();
			this.parameterResolvers.forEach( (key,value) -> {
				parameterResolvers.put( key, beanRegistry.getBean( value.toJavaClass() ) );
			} );
		}

		return new FilterDefinition(
				getName(),
				getDefaultCondition(),
				isAutoEnabled(),
				isApplyToLoadByKey(),
				parameterJdbcMappings,
				parameterResolvers
		);
	}
}
