/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.List;
import java.util.Set;

import org.hibernate.boot.internal.RootMappingDefaults;
import org.hibernate.boot.models.spi.ConversionRegistration;
import org.hibernate.boot.models.spi.ConverterRegistration;
import org.hibernate.boot.models.spi.GlobalRegistrations;
import org.hibernate.boot.models.xml.spi.PersistenceUnitMetadata;
import org.hibernate.models.spi.ClassDetailsRegistry;

import org.jboss.jandex.IndexView;

/**
 * @author Steve Ebersole
 */
public class DomainModelSource {
	private final ClassDetailsRegistry classDetailsRegistry;
	private final IndexView jandexIndex;
	private final GlobalRegistrations globalRegistrations;
	private final RootMappingDefaults effectiveMappingDefaults;
	private final PersistenceUnitMetadata persistenceUnitMetadata;
	private final List<String> allKnownClassNames;

	public DomainModelSource(
			ClassDetailsRegistry classDetailsRegistry,
			IndexView jandexIndex,
			List<String> allKnownClassNames,
			GlobalRegistrations globalRegistrations,
			RootMappingDefaults effectiveMappingDefaults,
			PersistenceUnitMetadata persistenceUnitMetadata) {
		this.classDetailsRegistry = classDetailsRegistry;
		this.jandexIndex = jandexIndex;
		this.allKnownClassNames = allKnownClassNames;
		this.globalRegistrations = globalRegistrations;
		this.effectiveMappingDefaults = effectiveMappingDefaults;
		this.persistenceUnitMetadata = persistenceUnitMetadata;
	}

	public ClassDetailsRegistry getClassDetailsRegistry() {
		return classDetailsRegistry;
	}

	public IndexView getJandexIndex() {
		return jandexIndex;
	}

	public GlobalRegistrations getGlobalRegistrations() {
		return globalRegistrations;
	}

	public RootMappingDefaults getEffectiveMappingDefaults() {
		return effectiveMappingDefaults;
	}

	public PersistenceUnitMetadata getPersistenceUnitMetadata() {
		return persistenceUnitMetadata;
	}

	public List<ConversionRegistration> getConversionRegistrations() {
		return globalRegistrations.getConverterRegistrations();
	}

	public Set<ConverterRegistration> getConverterRegistrations() {
		return globalRegistrations.getJpaConverters();
	}

	public List<String> getManagedClassNames() {
		return allKnownClassNames;
	}
}
