/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.categorize.internal;

import java.util.Map;
import java.util.Set;

import org.hibernate.boot.models.categorize.spi.CategorizedDomainModel;
import org.hibernate.boot.models.categorize.spi.EntityHierarchy;
import org.hibernate.boot.models.spi.GlobalRegistrations;
import org.hibernate.boot.models.xml.spi.PersistenceUnitMetadata;
import org.hibernate.models.spi.AnnotationDescriptorRegistry;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.ClassDetailsRegistry;

import org.jboss.jandex.IndexView;

/**
 * @author Steve Ebersole
 */
public class CategorizedDomainModelImpl implements CategorizedDomainModel {
	private final Set<EntityHierarchy> entityHierarchies;
	private final Map<String, ClassDetails> mappedSuperclasses;
	private final Map<String, ClassDetails> embeddables;
	private final GlobalRegistrations globalRegistrations;

	private final ClassDetailsRegistry classDetailsRegistry;
	private final AnnotationDescriptorRegistry annotationDescriptorRegistry;
	private final IndexView jandexIndex;
	private final PersistenceUnitMetadata persistenceUnitMetadata;

	public CategorizedDomainModelImpl(
			ClassDetailsRegistry classDetailsRegistry,
			AnnotationDescriptorRegistry annotationDescriptorRegistry,
			IndexView jandexIndex,
			PersistenceUnitMetadata persistenceUnitMetadata,
			Set<EntityHierarchy> entityHierarchies,
			Map<String, ClassDetails> mappedSuperclasses,
			Map<String, ClassDetails> embeddables,
			GlobalRegistrations globalRegistrations) {
		this.classDetailsRegistry = classDetailsRegistry;
		this.annotationDescriptorRegistry = annotationDescriptorRegistry;
		this.persistenceUnitMetadata = persistenceUnitMetadata;
		this.entityHierarchies = entityHierarchies;
		this.mappedSuperclasses = mappedSuperclasses;
		this.embeddables = embeddables;
		this.globalRegistrations = globalRegistrations;
		this.jandexIndex = jandexIndex;
	}

	@Override
	public ClassDetailsRegistry getClassDetailsRegistry() {
		return classDetailsRegistry;
	}

	@Override
	public AnnotationDescriptorRegistry getAnnotationDescriptorRegistry() {
		return annotationDescriptorRegistry;
	}

	@Override
	public IndexView getJandexIndex() {
		return jandexIndex;
	}

	@Override
	public PersistenceUnitMetadata getPersistenceUnitMetadata() {
		return persistenceUnitMetadata;
	}

	@Override
	public GlobalRegistrations getGlobalRegistrations() {
		return globalRegistrations;
	}

	@Override
	public Set<EntityHierarchy> getEntityHierarchies() {
		return entityHierarchies;
	}

	public Map<String, ClassDetails> getMappedSuperclasses() {
		return mappedSuperclasses;
	}

	@Override
	public Map<String, ClassDetails> getEmbeddables() {
		return embeddables;
	}
}
