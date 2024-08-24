/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.categorize.spi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Internal;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.internal.RootMappingDefaults;
import org.hibernate.boot.model.process.spi.ManagedResources;
import org.hibernate.boot.models.categorize.ModelCategorizationLogging;
import org.hibernate.boot.models.categorize.internal.ClassLoaderServiceLoading;
import org.hibernate.boot.models.internal.DomainModelCategorizationCollector;
import org.hibernate.boot.models.internal.GlobalRegistrationsImpl;
import org.hibernate.boot.models.categorize.internal.ModelCategorizationContextImpl;
import org.hibernate.boot.models.internal.ModelsHelper;
import org.hibernate.boot.models.xml.internal.PersistenceUnitMetadataImpl;
import org.hibernate.boot.models.xml.spi.XmlPreProcessingResult;
import org.hibernate.boot.models.xml.spi.XmlPreProcessor;
import org.hibernate.boot.models.xml.spi.XmlProcessingResult;
import org.hibernate.boot.models.xml.spi.XmlProcessor;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.models.internal.SourceModelBuildingContextImpl;
import org.hibernate.models.internal.jandex.JandexIndexerHelper;
import org.hibernate.models.spi.AnnotationDescriptorRegistry;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.ClassDetailsRegistry;
import org.hibernate.models.spi.ClassLoading;

import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;

import static org.hibernate.boot.models.categorize.internal.EntityHierarchyBuilder.createEntityHierarchies;
import static org.hibernate.internal.util.collections.CollectionHelper.mutableJoin;

/**
 * Processes a {@linkplain ManagedResources} (classes, mapping, etc.) and
 * produces a {@linkplain CategorizedDomainModel categorized domain model}
 *
 * @author Steve Ebersole
 */
public class ManagedResourcesProcessor {
	public static CategorizedDomainModel processManagedResources(
			ManagedResources managedResources,
			MetadataBuildingOptions metadataBuildingOptions,
			BootstrapContext bootstrapContext) {

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// 	- pre-process the XML
		// 	- collect all known classes
		// 	- resolve (possibly building) Jandex index
		// 	- build the SourceModelBuildingContext
		//
		// INPUTS:
		//		- serviceRegistry
		//		- managedResources
		//		- bootstrapContext (supplied Jandex index, if one)
		//
		// OUTPUTS:
		//		- xmlPreProcessingResult
		//		- allKnownClassNames (technically could be included in xmlPreProcessingResult)
		//		- sourceModelBuildingContext

		final ClassLoaderService classLoaderService = bootstrapContext.getServiceRegistry().getService( ClassLoaderService.class );
		final ClassLoaderServiceLoading classLoading = new ClassLoaderServiceLoading( classLoaderService );

		final PersistenceUnitMetadataImpl persistenceUnitMetadata = new PersistenceUnitMetadataImpl();

		final XmlPreProcessingResult xmlPreProcessingResult = XmlPreProcessor.preProcessXmlResources(
				managedResources,
				persistenceUnitMetadata
		);

		//noinspection unchecked
		final List<String> allKnownClassNames = mutableJoin(
				managedResources.getAnnotatedClassReferences().stream().map( Class::getName ).collect( Collectors.toList() ),
				managedResources.getAnnotatedClassNames(),
				xmlPreProcessingResult.getMappedClasses()
		);
		managedResources.getAnnotatedPackageNames().forEach( (packageName) -> {
			try {
				final Class<?> packageInfoClass = classLoading.classForName( packageName + ".package-info" );
				allKnownClassNames.add( packageInfoClass.getName() );
			}
			catch (ClassLoadingException classLoadingException) {
				// no package-info, so there can be no annotations... just skip it
			}
		} );
		managedResources.getAnnotatedClassReferences().forEach( (clazz) -> allKnownClassNames.add( clazz.getName() ) );

		// At this point we know all managed class names across all sources.
		// Resolve the Jandex Index and build the SourceModelBuildingContext.
		final IndexView jandexIndex = resolveJandexIndex( allKnownClassNames, bootstrapContext.getJandexView(), classLoading );
		final SourceModelBuildingContextImpl sourceModelBuildingContext = new SourceModelBuildingContextImpl(
				classLoading,
				jandexIndex,
				ModelsHelper::preFillRegistries
		);


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// 	- process metadata-complete XML
		//	- collect overlay XML
		//	- process annotations (including those from metadata-complete XML)
		//	- apply overlay XML
		//
		// INPUTS:
		//		- "options" (areIdGeneratorsGlobal, etc)
		//		- xmlPreProcessingResult
		//		- sourceModelBuildingContext
		//
		// OUTPUTS
		//		- rootEntities
		//		- mappedSuperClasses
		//  	- embeddables

		// JPA id generator global-ity thing
		final boolean areIdGeneratorsGlobal = true;
		final ClassDetailsRegistry classDetailsRegistry = sourceModelBuildingContext.getClassDetailsRegistry();
		final AnnotationDescriptorRegistry descriptorRegistry = sourceModelBuildingContext.getAnnotationDescriptorRegistry();
		final GlobalRegistrationsImpl globalRegistrations = new GlobalRegistrationsImpl( sourceModelBuildingContext, bootstrapContext );
		final DomainModelCategorizationCollector modelCategorizationCollector = new DomainModelCategorizationCollector(
				areIdGeneratorsGlobal,
				globalRegistrations,
				jandexIndex,
				sourceModelBuildingContext
		);

		final RootMappingDefaults rootMappingDefaults = new RootMappingDefaults(
				metadataBuildingOptions.getMappingDefaults(),
				persistenceUnitMetadata
		);
		final XmlProcessingResult xmlProcessingResult = XmlProcessor.processXml(
				xmlPreProcessingResult,
				modelCategorizationCollector,
				sourceModelBuildingContext,
				bootstrapContext,
				rootMappingDefaults
		);

		allKnownClassNames.forEach( (className) -> {
			final ClassDetails classDetails = classDetailsRegistry.resolveClassDetails( className );
			modelCategorizationCollector.apply( classDetails );
		} );
		xmlPreProcessingResult.getMappedNames().forEach( (className) -> {
			final ClassDetails classDetails = classDetailsRegistry.resolveClassDetails( className );
			modelCategorizationCollector.apply( classDetails );
		} );

		xmlProcessingResult.apply( xmlPreProcessingResult.getPersistenceUnitMetadata() );


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//	- create entity-hierarchies
		//	- create the CategorizedDomainModel
		//
		// INPUTS:
		//		- rootEntities
		//		- mappedSuperClasses
		//  	- embeddables
		//
		// OUTPUTS:
		//		- CategorizedDomainModel

		final ClassDetailsRegistry classDetailsRegistryImmutable = classDetailsRegistry
				.makeImmutableCopy();

		final AnnotationDescriptorRegistry annotationDescriptorRegistryImmutable = descriptorRegistry
				.makeImmutableCopy();

		// Collect the entity hierarchies based on the set of `rootEntities`
		final ModelCategorizationContextImpl mappingBuildingContext = new ModelCategorizationContextImpl(
				classDetailsRegistryImmutable,
				annotationDescriptorRegistryImmutable,
				globalRegistrations
		);

		final Set<EntityHierarchy> entityHierarchies;
		if ( ModelCategorizationLogging.MODEL_CATEGORIZATION_LOGGER.isDebugEnabled() ) {
			final Map<String,ClassDetails> unusedMappedSuperClasses = new HashMap<>( modelCategorizationCollector.getMappedSuperclasses() );
			entityHierarchies = createEntityHierarchies(
					modelCategorizationCollector.getRootEntities(),
					(identifiableType) -> {
						if ( identifiableType instanceof MappedSuperclassTypeMetadata ) {
							unusedMappedSuperClasses.remove( identifiableType.getClassDetails().getClassName() );
						}
					},
					mappingBuildingContext
			);
			warnAboutUnusedMappedSuperclasses( unusedMappedSuperClasses );
		}
		else {
			entityHierarchies = createEntityHierarchies(
					modelCategorizationCollector.getRootEntities(),
					ManagedResourcesProcessor::ignore,
					mappingBuildingContext
			);
		}

		return modelCategorizationCollector.createResult(
				entityHierarchies,
				xmlPreProcessingResult.getPersistenceUnitMetadata(),
				classDetailsRegistryImmutable,
				annotationDescriptorRegistryImmutable
		);
	}

	private static void ignore(IdentifiableTypeMetadata identifiableTypeMetadata) {
	}

	private static void warnAboutUnusedMappedSuperclasses(Map<String, ClassDetails> mappedSuperClasses) {
		assert ModelCategorizationLogging.MODEL_CATEGORIZATION_LOGGER.isDebugEnabled();
		for ( Map.Entry<String, ClassDetails> entry : mappedSuperClasses.entrySet() ) {
			ModelCategorizationLogging.MODEL_CATEGORIZATION_LOGGER.debugf(
					"Encountered MappedSuperclass [%s] which was unused in any entity hierarchies",
					entry.getKey()
			);
		}
	}

	public static IndexView resolveJandexIndex(
			List<String> allKnownClassNames,
			IndexView suppliedJandexIndex,
			ClassLoading classLoading) {
		// todo : we could build a new Jandex (Composite)Index that includes the `managedResources#getAnnotatedClassNames`
		// 		and all classes from `managedResources#getXmlMappingBindings`.  Only really worth it in the case
		//		of runtime enhancement.  This would definitely need to be toggle-able.
		//		+
		//		For now, let's not as it does not matter for this PoC
		if ( 1 == 1 ) {
			return suppliedJandexIndex;
		}

		final Indexer jandexIndexer = new Indexer();
		for ( String knownClassName : allKnownClassNames ) {
			JandexIndexerHelper.apply( knownClassName, jandexIndexer, classLoading );
		}

		if ( suppliedJandexIndex == null ) {
			return jandexIndexer.complete();
		}

		return CompositeIndex.create( suppliedJandexIndex, jandexIndexer.complete() );
	}

	/**
	 * For testing use only
	 */
	@Internal
	public static CategorizedDomainModel processManagedResources(
			ManagedResources managedResources,
			BootstrapContext bootstrapContext) {
		return processManagedResources(
				managedResources,
				new MetadataBuilderImpl.MetadataBuildingOptionsImpl( bootstrapContext.getServiceRegistry() ),
				bootstrapContext
		);
	}
}
