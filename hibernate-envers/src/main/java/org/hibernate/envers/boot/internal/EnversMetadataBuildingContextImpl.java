/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.boot.internal;

import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.boot.model.TypeDefinitionRegistry;
import org.hibernate.boot.model.naming.ObjectNameNormalizer;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.EffectiveMappingDefaults;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.envers.boot.spi.EnversMetadataBuildingContext;
import org.hibernate.envers.configuration.Configuration;
import org.hibernate.envers.configuration.internal.MappingCollector;
import org.hibernate.envers.configuration.internal.metadata.AuditEntityConfigurationRegistry;
import org.hibernate.envers.configuration.internal.metadata.AuditEntityNameRegister;
import org.hibernate.service.ServiceRegistry;

/**
 * @author Chris Cranford
 */
public class EnversMetadataBuildingContextImpl implements EnversMetadataBuildingContext {

	private static final String CONTRiBUTOR = "envers";

	private final Configuration configuration;
	private final InFlightMetadataCollector metadataCollector;
	private final EffectiveMappingDefaults effectiveMappingDefaults;
	private final MappingCollector mappingCollector;
	private final ObjectNameNormalizer objectNameNormalizer;
	private final AuditEntityNameRegister auditEntityNameRegistry;
	private final AuditEntityConfigurationRegistry auditEntityConfigurationRegistry;

	public EnversMetadataBuildingContextImpl(
			Configuration configuration,
			InFlightMetadataCollector metadataCollector,
			EffectiveMappingDefaults effectiveMappingDefaults,
			MappingCollector mappingCollector) {
		this.configuration = configuration;
		this.metadataCollector = metadataCollector;
		this.effectiveMappingDefaults = effectiveMappingDefaults;
		this.mappingCollector = mappingCollector;
		this.auditEntityNameRegistry = new AuditEntityNameRegister();
		this.auditEntityConfigurationRegistry = new AuditEntityConfigurationRegistry();

		this.objectNameNormalizer = new ObjectNameNormalizer(this);
	}

	@Override
	public BootstrapContext getBootstrapContext() {
		return metadataCollector.getBootstrapContext();
	}

	@Override
	public MetadataBuildingOptions getBuildingOptions() {
		return metadataCollector.getMetadataBuildingOptions();
	}

	@Override
	public EffectiveMappingDefaults getEffectiveDefaults() {
		return effectiveMappingDefaults;
	}

	@Override
	public InFlightMetadataCollector getMetadataCollector() {
		return metadataCollector;
	}

	@Override
	public ObjectNameNormalizer getObjectNameNormalizer() {
		return objectNameNormalizer;
	}

	@Override
	public TypeDefinitionRegistry getTypeDefinitionRegistry() {
		return metadataCollector.getTypeDefinitionRegistry();
	}

	@Override
	public String getCurrentContributorName() {
		return CONTRiBUTOR;
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public MappingCollector getMappingCollector() {
		return mappingCollector;
	}

	@Override
	public ServiceRegistry getServiceRegistry() {
		return metadataCollector.getBootstrapContext().getServiceRegistry();
	}

	@Override
	public ReflectionManager getReflectionManager() {
		return configuration.getReflectionManager();
	}

	@Override
	public AuditEntityNameRegister getAuditEntityNameRegistry() {
		return auditEntityNameRegistry;
	}

	@Override
	public AuditEntityConfigurationRegistry getAuditEntityConfigurationRegistry() {
		return auditEntityConfigurationRegistry;
	}
}
