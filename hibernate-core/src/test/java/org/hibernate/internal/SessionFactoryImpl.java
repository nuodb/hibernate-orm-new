/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.internal;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import jakarta.persistence.Cache;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.SynchronizationType;

import org.hibernate.CustomEntityDirtinessStrategy;
import org.hibernate.EntityNameResolver;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionEventListener;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.StatelessSession;
import org.hibernate.StatelessSessionBuilder;
import org.hibernate.boot.cfgxml.spi.CfgXmlAccessService;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.boot.model.relational.internal.SqlStringGenerationContextImpl;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.cfg.internal.DomainDataRegionConfigImpl;
import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.spi.CacheImplementor;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.hibernate.context.internal.JTASessionContext;
import org.hibernate.context.internal.ManagedSessionContext;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.hibernate.context.spi.CurrentSessionContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jndi.spi.JndiService;
import org.hibernate.engine.profile.Association;
import org.hibernate.engine.profile.Fetch;
import org.hibernate.engine.profile.FetchProfile;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.SessionBuilderImplementor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.hibernate.event.spi.EventEngine;
import org.hibernate.graph.spi.RootGraphImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.integrator.spi.IntegratorService;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.jpa.internal.ExceptionMapperLegacyJpaImpl;
import org.hibernate.jpa.internal.PersistenceUnitUtilImpl;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.metamodel.internal.RuntimeMetamodelsImpl;
import org.hibernate.metamodel.model.domain.spi.JpaMetamodelImplementor;
import org.hibernate.metamodel.spi.RuntimeMetamodelsImplementor;
import org.hibernate.query.BindableType;
import org.hibernate.metamodel.model.domain.internal.MappingMetamodelImpl;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.SessionFactoryBasedWrapperOptions;
import org.hibernate.procedure.spi.ProcedureCallImplementor;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.query.QueryLogging;
import org.hibernate.query.hql.spi.SqmQueryImplementor;
import org.hibernate.query.named.NamedObjectRepository;
import org.hibernate.query.named.NamedQueryMemento;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.spi.QueryImplementor;
import org.hibernate.query.sql.spi.NativeQueryImplementor;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.spi.NamedSqmQueryMemento;
import org.hibernate.resource.jdbc.spi.PhysicalConnectionHandlingMode;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.hibernate.resource.transaction.backend.jta.internal.synchronization.ExceptionMapper;
import org.hibernate.resource.transaction.spi.TransactionCoordinatorBuilder;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.hibernate.service.spi.SessionFactoryServiceRegistryFactory;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.hibernate.tool.schema.spi.DelayedDropAction;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.spi.TypeConfiguration;

import org.jboss.logging.Logger;

import static org.hibernate.engine.internal.ManagedTypeHelper.asHibernateProxy;
import static org.hibernate.engine.internal.ManagedTypeHelper.isHibernateProxy;


/**
 * <b>NUODB OVERRIDE CLASS</b>
 * <p>
 * <b>NuoDB:</b> Allow properties used to configure the factory to be logged.
 * <p>
 * Concrete implementation of the {@code SessionFactory} interface. Has the following
 * responsibilities
 * <ul>
 * <li>caches configuration settings (immutably)
 * <li>caches "compiled" mappings ie. {@code EntityPersister}s and
 *     {@code CollectionPersister}s (immutable)
 * <li>caches "compiled" queries (memory sensitive cache)
 * <li>manages {@code PreparedStatement}s
 * <li> delegates JDBC {@code Connection} management to the {@code ConnectionProvider}
 * <li>factory for instances of {@code SessionImpl}
 * </ul>
 * This class must appear immutable to clients, even if it does all kinds of caching
 * and pooling under the covers. It is crucial that the class is not only thread
 * safe, but also highly concurrent. Synchronization must be used extremely sparingly.
 *
 * @author Gavin King
 * @author Steve Ebersole
 * @author Chris Cranford
 */
public class SessionFactoryImpl implements SessionFactoryImplementor {

	// NuoDB: 20-Jun-2023
	public static boolean showProperties = false;

	private static final CoreMessageLogger LOG = CoreLogging.messageLogger( SessionFactoryImpl.class );

	private final String name;
	private final String uuid;

	private transient volatile Status status = Status.OPEN;

	private final transient SessionFactoryObserverChain observer = new SessionFactoryObserverChain();

	private final transient SessionFactoryOptions sessionFactoryOptions;
	private final transient Map<String,Object> properties;

	private final transient SessionFactoryServiceRegistry serviceRegistry;
	private final transient EventEngine eventEngine;
	private final transient JdbcServices jdbcServices;
	private final transient SqlStringGenerationContext sqlStringGenerationContext;

	// todo : org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor too?

	private final transient RuntimeMetamodelsImplementor runtimeMetamodels;
	private final PersistenceUnitUtil jpaPersistenceUnitUtil;
	private final transient CacheImplementor cacheAccess;
	private final transient QueryEngine queryEngine;

	private final transient CurrentSessionContext currentSessionContext;

	private volatile DelayedDropAction delayedDropAction;

	// todo : move to MetamodelImpl
	private final transient Map<String,IdentifierGenerator> identifierGenerators;
	private final transient Map<String, FilterDefinition> filters;
	private final transient Map<String, FetchProfile> fetchProfiles;

	private final transient FastSessionServices fastSessionServices;
	private final transient WrapperOptions wrapperOptions;
	private final transient SessionBuilder defaultSessionOpenOptions;
	private final transient SessionBuilder temporarySessionOpenOptions;
	private final transient StatelessSessionBuilder defaultStatelessOptions;
	private final transient EntityNameResolver entityNameResolver;

	public SessionFactoryImpl(
			final MetadataImplementor bootMetamodel,
			SessionFactoryOptions options) {
		LOG.debug( "Building session factory" );

		final TypeConfiguration typeConfiguration = bootMetamodel.getTypeConfiguration();
		final MetadataBuildingContext bootModelBuildingContext = typeConfiguration.getMetadataBuildingContext();
		final BootstrapContext bootstrapContext = bootModelBuildingContext.getBootstrapContext();

		this.sessionFactoryOptions = options;

		this.serviceRegistry = options
				.getServiceRegistry()
				.getService( SessionFactoryServiceRegistryFactory.class )
				.buildServiceRegistry( this, options );

		this.eventEngine = new EventEngine( bootMetamodel, this );

		bootMetamodel.initSessionFactory( this );

		final CfgXmlAccessService cfgXmlAccessService = serviceRegistry.getService( CfgXmlAccessService.class );

		String sfName = sessionFactoryOptions.getSessionFactoryName();
		if ( cfgXmlAccessService.getAggregatedConfig() != null ) {
			if ( sfName == null ) {
				sfName = cfgXmlAccessService.getAggregatedConfig().getSessionFactoryName();
			}
		}

		this.name = sfName;
		this.uuid = options.getUuid();

		jdbcServices = serviceRegistry.getService( JdbcServices.class );

		ConfigurationService configurationService = serviceRegistry.getService( ConfigurationService.class );

		this.properties = new TreeMap<>();
		this.properties.putAll( configurationService.getSettings() );
		// NuoDB Start: 20-Jun-2023
		if (showProperties)
			LOG.info("Properties = " + this.properties.toString().replaceAll(",", System.lineSeparator()));
		// NuoDB SEndtart: 20-Jun-2023
		if ( !properties.containsKey( AvailableSettings.JPA_VALIDATION_FACTORY )
				&& !properties.containsKey( AvailableSettings.JAKARTA_VALIDATION_FACTORY ) ) {
			if ( getSessionFactoryOptions().getValidatorFactoryReference() != null ) {
				properties.put(
						AvailableSettings.JPA_VALIDATION_FACTORY,
						getSessionFactoryOptions().getValidatorFactoryReference()
				);
				properties.put(
						AvailableSettings.JAKARTA_VALIDATION_FACTORY,
						getSessionFactoryOptions().getValidatorFactoryReference()
				);
			}
		}

		maskOutSensitiveInformation(this.properties);
		logIfEmptyCompositesEnabled( this.properties );

		sqlStringGenerationContext = SqlStringGenerationContextImpl.fromExplicit(
				jdbcServices.getJdbcEnvironment(), bootMetamodel.getDatabase(),
				options.getDefaultCatalog(), options.getDefaultSchema() );

		this.cacheAccess = this.serviceRegistry.getService( CacheImplementor.class );
		this.jpaPersistenceUnitUtil = new PersistenceUnitUtilImpl( this );

		for ( SessionFactoryObserver sessionFactoryObserver : options.getSessionFactoryObservers() ) {
			this.observer.addObserver( sessionFactoryObserver );
		}

		this.filters = new HashMap<>();
		this.filters.putAll( bootMetamodel.getFilterDefinitions() );

		LOG.debugf( "Session factory constructed with filter configurations : %s", filters );
		LOG.debugf( "Instantiating session factory with properties: %s", properties );

		class IntegratorObserver implements SessionFactoryObserver {
			private final ArrayList<Integrator> integrators = new ArrayList<>();

			@Override
			public void sessionFactoryCreated(SessionFactory factory) {
			}

			@Override
			public void sessionFactoryClosed(SessionFactory factory) {
				for ( Integrator integrator : integrators ) {
					integrator.disintegrate( SessionFactoryImpl.this, SessionFactoryImpl.this.serviceRegistry );
				}
				integrators.clear();
			}
		}
		final IntegratorObserver integratorObserver = new IntegratorObserver();
		this.observer.addObserver( integratorObserver );
		try {
			for ( Integrator integrator : serviceRegistry.getService( IntegratorService.class ).getIntegrators() ) {
				integrator.integrate( bootMetamodel, bootstrapContext, this );
				integratorObserver.integrators.add( integrator );
			}
			//Generators:
			this.identifierGenerators = new HashMap<>();
			bootMetamodel.getEntityBindings().stream().filter( model -> !model.isInherited() ).forEach( model -> {
				final IdentifierGenerator generator = model.getIdentifier().createIdentifierGenerator(
						bootstrapContext.getIdentifierGeneratorFactory(),
						jdbcServices.getJdbcEnvironment().getDialect(),
						(RootClass) model
				);
				generator.initialize( sqlStringGenerationContext );
				identifierGenerators.put( model.getEntityName(), generator );
			} );
			bootMetamodel.validate();

			LOG.debug( "Instantiated session factory" );

			primeSecondLevelCacheRegions( bootMetamodel );

			this.queryEngine = QueryEngine.from( this, bootMetamodel );

			final RuntimeMetamodelsImpl runtimeMetamodels = new RuntimeMetamodelsImpl();
			this.runtimeMetamodels = runtimeMetamodels;
			runtimeMetamodels.finishInitialization(
					bootMetamodel,
					bootstrapContext,
					this
			);

			this.queryEngine.prepare( this, bootMetamodel, bootstrapContext );

			if ( options.isNamedQueryStartupCheckingEnabled() ) {
				final Map<String, HibernateException> errors = queryEngine.getNamedObjectRepository().checkNamedQueries( queryEngine );

				if ( !errors.isEmpty() ) {
					StringBuilder failingQueries = new StringBuilder( "Errors in named queries: " );
					String sep = "";
					for ( Map.Entry<String, HibernateException> entry : errors.entrySet() ) {
						QueryLogging.QUERY_MESSAGE_LOGGER.namedQueryError( entry.getKey(), entry.getValue() );
						failingQueries.append( sep ).append( entry.getKey() );
						sep = ", ";
					}
					final HibernateException exception = new HibernateException( failingQueries.toString() );
					errors.values().forEach( exception::addSuppressed );
					throw exception;
				}
			}

			SchemaManagementToolCoordinator.process(
					bootMetamodel,
					serviceRegistry,
					properties,
					action -> SessionFactoryImpl.this.delayedDropAction = action
			);

			currentSessionContext = buildCurrentSessionContext();

			// this needs to happen after persisters are all ready to go...
			this.fetchProfiles = new HashMap<>();
			for ( org.hibernate.mapping.FetchProfile mappingProfile : bootMetamodel.getFetchProfiles() ) {
				final FetchProfile fetchProfile = new FetchProfile( mappingProfile.getName() );
				for ( org.hibernate.mapping.FetchProfile.Fetch mappingFetch : mappingProfile.getFetches() ) {
					// resolve the persister owning the fetch
					final String entityName = this.runtimeMetamodels.getImportedName( mappingFetch.getEntity() );
					final EntityPersister owner = entityName == null
							? null
							: this.runtimeMetamodels.getMappingMetamodel().getEntityDescriptor( entityName );
					if ( owner == null ) {
						throw new HibernateException(
								"Unable to resolve entity reference [" + mappingFetch.getEntity()
										+ "] in fetch profile [" + fetchProfile.getName() + "]"
						);
					}

					// validate the specified association fetch
					Type associationType = owner.getPropertyType( mappingFetch.getAssociation() );
					if ( associationType == null || !associationType.isAssociationType() ) {
						throw new HibernateException( "Fetch profile [" + fetchProfile.getName() + "] specified an invalid association" );
					}

					// resolve the style
					final Fetch.Style fetchStyle = Fetch.Style.parse( mappingFetch.getStyle() );

					// then construct the fetch instance...
					fetchProfile.addFetch( new Association( owner, mappingFetch.getAssociation() ), fetchStyle );
					((Loadable) owner).registerAffectingFetchProfile( fetchProfile.getName() );
				}
				fetchProfiles.put( fetchProfile.getName(), fetchProfile );
			}

			this.defaultSessionOpenOptions = createDefaultSessionOpenOptionsIfPossible();
			this.temporarySessionOpenOptions = this.defaultSessionOpenOptions == null ? null : buildTemporarySessionOpenOptions();
			this.defaultStatelessOptions = this.defaultSessionOpenOptions == null ? null : withStatelessOptions();
			this.fastSessionServices = new FastSessionServices( this );
			this.wrapperOptions = new SessionFactoryBasedWrapperOptions( this );

			this.observer.sessionFactoryCreated( this );

			SessionFactoryRegistry.INSTANCE.addSessionFactory(
					getUuid(),
					name,
					sessionFactoryOptions.isSessionFactoryNameAlsoJndiName(),
					this,
					serviceRegistry.getService( JndiService.class )
			);

			//As last operation, delete all caches from ReflectionManager
			//(not modelled as a listener as we want this to be last)
			bootstrapContext.getReflectionManager().reset();

			this.entityNameResolver = new CoordinatingEntityNameResolver( this, getInterceptor() );
		}
		catch (Exception e) {
			for ( Integrator integrator : serviceRegistry.getService( IntegratorService.class ).getIntegrators() ) {
				integrator.disintegrate( this, serviceRegistry );
				integratorObserver.integrators.remove( integrator );
				serviceRegistry.close();
			}

			try {
				close();
			}
			catch (Exception closeException) {
				LOG.debugf( "Eating error closing SF on failed attempt to start it" );
			}
			throw e;
		}
	}

	private SessionBuilder createDefaultSessionOpenOptionsIfPossible() {
		final CurrentTenantIdentifierResolver currentTenantIdentifierResolver = getCurrentTenantIdentifierResolver();
		if ( currentTenantIdentifierResolver == null ) {
			return withOptions();
		}
		else {
			//Don't store a default SessionBuilder when a CurrentTenantIdentifierResolver is provided
			return null;
		}
	}

	private SessionBuilder buildTemporarySessionOpenOptions() {
		return withOptions()
				.autoClose( false )
				.flushMode( FlushMode.MANUAL )
				.connectionHandlingMode( PhysicalConnectionHandlingMode.DELAYED_ACQUISITION_AND_RELEASE_AFTER_STATEMENT );
	}

	private void primeSecondLevelCacheRegions(MetadataImplementor mappingMetadata) {
		final Map<String, DomainDataRegionConfigImpl.Builder> regionConfigBuilders = new ConcurrentHashMap<>();

		// todo : ultimately this code can be made more efficient when we have a better intrinsic understanding of the hierarchy as a whole

		for ( PersistentClass bootEntityDescriptor : mappingMetadata.getEntityBindings() ) {
			final AccessType accessType = AccessType.fromExternalName( bootEntityDescriptor.getCacheConcurrencyStrategy() );

			if ( accessType != null ) {
				if ( bootEntityDescriptor.isCached() ) {
					regionConfigBuilders.computeIfAbsent(
							bootEntityDescriptor.getRootClass().getCacheRegionName(),
							DomainDataRegionConfigImpl.Builder::new
					)
							.addEntityConfig( bootEntityDescriptor, accessType );
				}

				if ( bootEntityDescriptor instanceof RootClass
						&& bootEntityDescriptor.hasNaturalId()
						&& bootEntityDescriptor.getNaturalIdCacheRegionName() != null ) {
					regionConfigBuilders.computeIfAbsent(
							bootEntityDescriptor.getNaturalIdCacheRegionName(),
							DomainDataRegionConfigImpl.Builder::new
					)
							.addNaturalIdConfig( (RootClass) bootEntityDescriptor, accessType );
				}
			}
		}

		for ( Collection collection : mappingMetadata.getCollectionBindings() ) {
			final AccessType accessType = AccessType.fromExternalName( collection.getCacheConcurrencyStrategy() );
			if ( accessType != null ) {
				regionConfigBuilders.computeIfAbsent(
						collection.getCacheRegionName(),
						DomainDataRegionConfigImpl.Builder::new
				)
						.addCollectionConfig( collection, accessType );
			}
		}

		final Set<DomainDataRegionConfig> regionConfigs;
		if ( regionConfigBuilders.isEmpty() ) {
			regionConfigs = Collections.emptySet();
		}
		else {
			regionConfigs = new HashSet<>();
			for ( DomainDataRegionConfigImpl.Builder builder : regionConfigBuilders.values() ) {
				regionConfigs.add( builder.build() );
			}
		}

		getCache().prime( regionConfigs );
	}

	public Session openSession() throws HibernateException {
		//The defaultSessionOpenOptions can't be used in some cases; for example when using a TenantIdentifierResolver.
		if ( this.defaultSessionOpenOptions != null ) {
			return this.defaultSessionOpenOptions.openSession();
		}
		else {
			return this.withOptions().openSession();
		}
	}

	public Session openTemporarySession() throws HibernateException {
		//The temporarySessionOpenOptions can't be used in some cases; for example when using a TenantIdentifierResolver.
		if ( this.temporarySessionOpenOptions != null ) {
			return this.temporarySessionOpenOptions.openSession();
		}
		else {
			return buildTemporarySessionOpenOptions()
					.openSession();
		}
	}

	public Session getCurrentSession() throws HibernateException {
		if ( currentSessionContext == null ) {
			throw new HibernateException( "No CurrentSessionContext configured" );
		}
		return currentSessionContext.currentSession();
	}

	@Override
	public SessionBuilderImplementor withOptions() {
		return new SessionBuilderImpl<>( this );
	}

	@Override
	public StatelessSessionBuilder withStatelessOptions() {
		return new StatelessSessionBuilderImpl( this );
	}

	public StatelessSession openStatelessSession() {
		if ( this.defaultStatelessOptions != null ) {
			return this.defaultStatelessOptions.openStatelessSession();
		}
		else {
			return withStatelessOptions().openStatelessSession();
		}
	}

	public StatelessSession openStatelessSession(Connection connection) {
		return withStatelessOptions().connection( connection ).openStatelessSession();
	}

	@Override
	public void addObserver(SessionFactoryObserver observer) {
		this.observer.addObserver( observer );
	}

	@Override
	public Map<String, Object> getProperties() {
		validateNotClosed();
		return properties;
	}

	protected void validateNotClosed() {
		if ( status == Status.CLOSED ) {
			throw new IllegalStateException( "EntityManagerFactory is closed" );
		}
	}

	@Override
	public String getUuid() {
		return uuid;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TypeConfiguration getTypeConfiguration() {
		return runtimeMetamodels.getMappingMetamodel().getTypeConfiguration();
	}

	@Override
	public QueryEngine getQueryEngine() {
		return queryEngine;
	}

	@Override
	public EventEngine getEventEngine() {
		return eventEngine;
	}

	@Override
	public JdbcServices getJdbcServices() {
		return jdbcServices;
	}

	@Override
	public SqlStringGenerationContext getSqlStringGenerationContext() {
		return sqlStringGenerationContext;
	}

	public IdentifierGeneratorFactory getIdentifierGeneratorFactory() {
		return null;
	}

	@Override
	public DeserializationResolver getDeserializationResolver() {
		return () -> (SessionFactoryImplementor) SessionFactoryRegistry.INSTANCE.findSessionFactory(
				uuid,
				name
		);
	}

	@Override
	public <T> List<EntityGraph<? super T>> findEntityGraphsByType(Class<T> entityClass) {
		return (List) getJpaMetamodel().findEntityGraphsByJavaType( entityClass );
	}

	// todo : (5.2) review synchronizationType, persistenceContextType, transactionType usage

	@Override
	public Session createEntityManager() {
		validateNotClosed();
		return buildEntityManager( SynchronizationType.SYNCHRONIZED, null );
	}

	private <K,V> Session buildEntityManager(final SynchronizationType synchronizationType, final Map<K,V> map) {
		assert status != Status.CLOSED;

		SessionBuilderImplementor builder = withOptions();
		builder.autoJoinTransactions( synchronizationType == SynchronizationType.SYNCHRONIZED );

		final Session session = builder.openSession();
		if ( map != null ) {
			for ( Map.Entry<K, V> o : map.entrySet() ) {
				final K key = o.getKey();
				if ( key instanceof String ) {
					final String sKey = (String) key;
					session.setProperty( sKey, o.getValue() );
				}
			}
		}
		return session;
	}

	@Override
	public Session createEntityManager(Map map) {
		validateNotClosed();
		return buildEntityManager( SynchronizationType.SYNCHRONIZED, map );
	}

	@Override
	public Session createEntityManager(SynchronizationType synchronizationType) {
		validateNotClosed();
		errorIfResourceLocalDueToExplicitSynchronizationType();
		return buildEntityManager( synchronizationType, null );
	}

	private void errorIfResourceLocalDueToExplicitSynchronizationType() {
		// JPA requires that we throw IllegalStateException in cases where:
		//		1) the PersistenceUnitTransactionType (TransactionCoordinator) is non-JTA
		//		2) an explicit SynchronizationType is specified
		if ( !getServiceRegistry().getService( TransactionCoordinatorBuilder.class ).isJta() ) {
			throw new IllegalStateException(
					"Illegal attempt to specify a SynchronizationType when building an EntityManager from an " +
							"EntityManagerFactory defined as RESOURCE_LOCAL (as opposed to JTA)"
			);
		}
	}

	@Override
	public Session createEntityManager(SynchronizationType synchronizationType, Map map) {
		validateNotClosed();
		errorIfResourceLocalDueToExplicitSynchronizationType();
		return buildEntityManager( synchronizationType, map );
	}

	@Override
	public NodeBuilder getCriteriaBuilder() {
		validateNotClosed();
		return queryEngine.getCriteriaBuilder();
	}

	@Override
	public MetamodelImplementor getMetamodel() {
		validateNotClosed();
		return (MetamodelImplementor) runtimeMetamodels.getMappingMetamodel();
	}

	@Override
	public boolean isOpen() {
		return status != Status.CLOSED;
	}

	@Override
	public RootGraphImplementor<?> findEntityGraphByName(String name) {
		return getJpaMetamodel().findEntityGraphByName( name );
	}

	@Override
	public String bestGuessEntityName(Object object) {
		final LazyInitializer initializer = HibernateProxy.extractLazyInitializer( object );
		if ( initializer != null ) {
			// it is possible for this method to be called during flush processing,
			// so make certain that we do not accidentally initialize an uninitialized proxy
			if ( initializer.isUninitialized() ) {
				return initializer.getEntityName();
			}
			object = initializer.getImplementation();
		}
		return entityNameResolver.resolveEntityName( object );
	}

	@Override
	public SessionFactoryOptions getSessionFactoryOptions() {
		return sessionFactoryOptions;
	}

	public Interceptor getInterceptor() {
		return sessionFactoryOptions.getInterceptor();
	}

	@Override
	public Reference getReference() {
		// from javax.naming.Referenceable
		LOG.debug( "Returning a Reference to the SessionFactory" );
		return new Reference(
				SessionFactoryImpl.class.getName(),
				new StringRefAddr("uuid", getUuid()),
				SessionFactoryRegistry.ObjectFactoryImpl.class.getName(),
				null
		);
	}

	public Type getIdentifierType(String className) throws MappingException {
		return runtimeMetamodels.getMappingMetamodel().getEntityDescriptor( className ).getIdentifierType();
	}
	public String getIdentifierPropertyName(String className) throws MappingException {
		return runtimeMetamodels.getMappingMetamodel().getEntityDescriptor( className ).getIdentifierPropertyName();
	}

	public CollectionMetadata getCollectionMetadata(String roleName) throws HibernateException {
		return (CollectionMetadata) runtimeMetamodels.getMappingMetamodel().getCollectionDescriptor( roleName );
	}

	public Type getReferencedPropertyType(String className, String propertyName) throws MappingException {
		return runtimeMetamodels.getMappingMetamodel().getEntityDescriptor( className ).getPropertyType( propertyName );
	}

	/**
	 * Closes the session factory, releasing all held resources.
	 *
	 * <ol>
	 * <li>cleans up used cache regions and "stops" the cache provider.
	 * <li>close the JDBC connection
	 * <li>remove the JNDI binding
	 * </ol>
	 *
	 * Note: Be aware that the sessionFactory instance still can
	 * be a "heavy" object memory wise after close() has been called.  Thus
	 * it is important to not keep referencing the instance to let the garbage
	 * collector release the memory.
	 */
	@Override
	public void close() throws HibernateException {
		synchronized (this) {
			if ( status != Status.OPEN ) {
				if ( getSessionFactoryOptions().getJpaCompliance().isJpaClosedComplianceEnabled() ) {
					throw new IllegalStateException( "EntityManagerFactory is already closed" );
				}

				LOG.trace( "Already closed" );
				return;
			}

			status = Status.CLOSING;
		}

		try {
			LOG.closing();
			observer.sessionFactoryClosing( this );

		// NOTE : the null checks below handle cases where close is called from
		//		a failed attempt to create the SessionFactory

			if ( cacheAccess != null ) {
				cacheAccess.close();
			}

			if ( runtimeMetamodels != null && runtimeMetamodels.getMappingMetamodel() != null ) {
				final JdbcConnectionAccess jdbcConnectionAccess = jdbcServices.getBootstrapJdbcConnectionAccess();
				runtimeMetamodels.getMappingMetamodel().forEachEntityDescriptor(
						entityPersister -> {
							if ( entityPersister.getSqmMultiTableMutationStrategy() != null ) {
								entityPersister.getSqmMultiTableMutationStrategy().release(
										this,
										jdbcConnectionAccess
								);
							}
							if ( entityPersister.getSqmMultiTableInsertStrategy() != null ) {
								entityPersister.getSqmMultiTableInsertStrategy().release(
										this,
										jdbcConnectionAccess
								);
							}
						}
				);
				( (MappingMetamodelImpl) runtimeMetamodels.getMappingMetamodel() ).close();
			}

			if ( queryEngine != null ) {
				queryEngine.close();
			}

			if ( delayedDropAction != null ) {
				delayedDropAction.perform( serviceRegistry );
			}

			SessionFactoryRegistry.INSTANCE.removeSessionFactory(
					getUuid(),
					name,
					sessionFactoryOptions.isSessionFactoryNameAlsoJndiName(),
					serviceRegistry.getService( JndiService.class )
			);
		}
		finally {
			status = Status.CLOSED;
		}

		observer.sessionFactoryClosed( this );
		serviceRegistry.destroy();
	}

	public CacheImplementor getCache() {
		validateNotClosed();
		return cacheAccess;
	}

	@Override
	public PersistenceUnitUtil getPersistenceUnitUtil() {
		validateNotClosed();
		return jpaPersistenceUnitUtil;
	}

	@Override
	public void addNamedQuery(String name, Query query) {
		validateNotClosed();

		// NOTE : we use Query#unwrap here (rather than direct type checking) to account for possibly wrapped
		// query implementations

		// first, handle StoredProcedureQuery
		final NamedObjectRepository namedObjectRepository = getQueryEngine().getNamedObjectRepository();
		try {
			final ProcedureCallImplementor<?> unwrapped = query.unwrap( ProcedureCallImplementor.class );
			if ( unwrapped != null ) {
				namedObjectRepository.registerCallableQueryMemento(
						name,
						unwrapped.toMemento( name )
				);
				return;
			}
		}
		catch ( PersistenceException ignore ) {
			// this means 'query' is not a ProcedureCallImplementor
		}

		// then try as a native-SQL or JPQL query
		try {
			QueryImplementor<?> hibernateQuery = query.unwrap( QueryImplementor.class );
			if ( hibernateQuery != null ) {
				// create and register the proper NamedQueryDefinition...
				if ( hibernateQuery instanceof NativeQueryImplementor ) {
					namedObjectRepository.registerNativeQueryMemento(
							name,
							( (NativeQueryImplementor<?>) hibernateQuery ).toMemento( name )
					);

				}
				else {
					final NamedQueryMemento namedQueryMemento = ( (SqmQueryImplementor<?>) hibernateQuery ).toMemento(
							name );
					namedObjectRepository.registerSqmQueryMemento(
							name,
							(NamedSqmQueryMemento) namedQueryMemento
					);
				}
				return;
			}
		}
		catch ( PersistenceException ignore ) {
			// this means 'query' is not a native-SQL or JPQL query
		}

		// if we get here, we are unsure how to properly unwrap the incoming query to extract the needed information
		throw new PersistenceException(
				String.format(
						"Unsure how to properly unwrap given Query [%s] as basis for named query",
						query
				)
		);
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		if ( type.isAssignableFrom( SessionFactory.class ) ) {
			return type.cast( this );
		}

		if ( type.isAssignableFrom( SessionFactoryImplementor.class ) ) {
			return type.cast( this );
		}

		if ( type.isAssignableFrom( SessionFactoryImpl.class ) ) {
			return type.cast( this );
		}

		if ( type.isAssignableFrom( EntityManagerFactory.class ) ) {
			return type.cast( this );
		}

		if ( type.isAssignableFrom( SessionFactoryServiceRegistry.class ) ) {
			return type.cast( serviceRegistry );
		}

		if ( type.isAssignableFrom( JdbcServices.class ) ) {
			return type.cast( jdbcServices );
		}

		if ( type.isAssignableFrom( Cache.class ) || type.isAssignableFrom( org.hibernate.Cache.class ) ) {
			return type.cast( cacheAccess );
		}

		if ( type.isInstance( runtimeMetamodels ) ) {
			return type.cast( runtimeMetamodels );
		}

		if ( type.isInstance( runtimeMetamodels.getJpaMetamodel() ) ) {
			return type.cast( runtimeMetamodels.getJpaMetamodel() );
		}

		if ( type.isAssignableFrom( MetamodelImplementor.class ) || type.isAssignableFrom( MetadataImplementor.class ) ) {
			return type.cast( runtimeMetamodels.getMappingMetamodel() );
		}

		if ( type.isAssignableFrom( QueryEngine.class ) ) {
			return type.cast( queryEngine );
		}

		throw new PersistenceException( "Hibernate cannot unwrap EntityManagerFactory as '" + type.getName() + "'" );
	}

	@Override
	public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
		getMappingMetamodel().addNamedEntityGraph( graphName, (RootGraphImplementor<T>) entityGraph );
	}

	@Override
	public boolean isClosed() {
		return status == Status.CLOSED;
	}

	private transient StatisticsImplementor statistics;

	public StatisticsImplementor getStatistics() {
		if ( statistics == null ) {
			statistics = serviceRegistry.getService( StatisticsImplementor.class );
		}
		return statistics;
	}

	public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
		FilterDefinition def = filters.get( filterName );
		if ( def == null ) {
			throw new HibernateException( "No such filter configured [" + filterName + "]" );
		}
		return def;
	}

	public boolean containsFetchProfileDefinition(String name) {
		return fetchProfiles.containsKey( name );
	}

	public Set<String> getDefinedFilterNames() {
		return filters.keySet();
	}

	public IdentifierGenerator getIdentifierGenerator(String rootEntityName) {
		return identifierGenerators.get(rootEntityName);
	}

	private boolean canAccessTransactionManager() {
		try {
			return serviceRegistry.getService( JtaPlatform.class ).retrieveTransactionManager() != null;
		}
		catch (Exception e) {
			return false;
		}
	}

	private CurrentSessionContext buildCurrentSessionContext() {
		String impl = (String) properties.get( Environment.CURRENT_SESSION_CONTEXT_CLASS );
		// for backward-compatibility
		if ( impl == null ) {
			if ( canAccessTransactionManager() ) {
				impl = "jta";
			}
			else {
				return null;
			}
		}

		switch (impl) {
			case "jta":
//			if ( ! transactionFactory().compatibleWithJtaSynchronization() ) {
//				LOG.autoFlushWillNotWork();
//			}
				return new JTASessionContext(this);
			case "thread":
				return new ThreadLocalSessionContext(this);
			case "managed":
				return new ManagedSessionContext(this);
			default:
				try {
					Class<?> implClass = serviceRegistry.getService(ClassLoaderService.class).classForName(impl);
					return (CurrentSessionContext)
							implClass.getConstructor( new Class[]{SessionFactoryImplementor.class} )
									.newInstance(this);
				}
				catch (Throwable t) {
					LOG.unableToConstructCurrentSessionContext(impl, t);
					return null;
				}
		}
	}

	@Override
	public RuntimeMetamodelsImplementor getRuntimeMetamodels() {
		return runtimeMetamodels;

	}

	@Override
	public JpaMetamodelImplementor getJpaMetamodel() {
		return runtimeMetamodels.getJpaMetamodel();
	}

	@Override
	public Integer getMaximumFetchDepth() {
		return getSessionFactoryOptions().getMaximumFetchDepth();
	}

	@Override
	public ServiceRegistryImplementor getServiceRegistry() {
		return serviceRegistry;
	}

	@Override
	public EntityNotFoundDelegate getEntityNotFoundDelegate() {
		return sessionFactoryOptions.getEntityNotFoundDelegate();
	}

	public FetchProfile getFetchProfile(String name) {
		return fetchProfiles.get( name );
	}

	@Override
	public <T> BindableType<? extends T> resolveParameterBindType(T bindValue) {
		if ( bindValue == null ) {
			// we can't guess
			return null;
		}

		Class<?> clazz;
		final LazyInitializer lazyInitializer = HibernateProxy.extractLazyInitializer( bindValue );
		if ( lazyInitializer != null ) {
			clazz = lazyInitializer.getPersistentClass();
		}
		else {
			clazz = bindValue.getClass();
		}

		// Resolve superclass bindable type if necessary, as we don't register types for e.g. Inet4Address
		Class<?> c = clazz;
		do {
			BindableType<?> type = resolveParameterBindType( c );
			if ( type != null ) {
				//noinspection unchecked
				return (BindableType<? extends T>) type;
			}
			c = c.getSuperclass();
		} while ( c != Object.class );
		if ( !clazz.isEnum() && Serializable.class.isAssignableFrom( clazz ) ) {
			//noinspection unchecked
			return (BindableType<? extends T>) resolveParameterBindType( Serializable.class );
		}
		return null;
	}

	@Override
	public <T> BindableType<T> resolveParameterBindType(Class<T> javaType) {
		return getRuntimeMetamodels().getMappingMetamodel().resolveQueryParameterType( javaType );
	}

	/**
	 * @deprecated use {@link #configuredInterceptor(Interceptor, boolean, SessionFactoryOptions)}
	 */
	@Deprecated
	public static Interceptor configuredInterceptor(Interceptor interceptor, SessionFactoryOptions options) {
		return configuredInterceptor( interceptor, false, options );
	}

	public static Interceptor configuredInterceptor(Interceptor interceptor, boolean explicitNoInterceptor, SessionFactoryOptions options) {
		// NOTE : DO NOT return EmptyInterceptor.INSTANCE from here as a "default for the Session"
		// 		we "filter" that one out here.  The return from here should represent the
		//		explicitly configured Interceptor (if one).  Return null from here instead; Session
		//		will handle it

		if ( interceptor != null && interceptor != EmptyInterceptor.INSTANCE ) {
			return interceptor;
		}

		// prefer the SessionFactory-scoped interceptor, prefer that to any Session-scoped interceptor prototype
		final Interceptor optionsInterceptor = options.getInterceptor();
		if ( optionsInterceptor != null && optionsInterceptor != EmptyInterceptor.INSTANCE ) {
			return optionsInterceptor;
		}

		// If explicitly asking for no interceptor and there is no SessionFactory-scoped interceptors, then
		// no need to inherit from the configured stateless session ones.
		if ( explicitNoInterceptor ) {
			return null;
		}

		// then check the Session-scoped interceptor prototype
		final Supplier<? extends Interceptor> statelessInterceptorImplementorSupplier = options.getStatelessInterceptorImplementorSupplier();
		if ( statelessInterceptorImplementorSupplier != null ) {
			return statelessInterceptorImplementorSupplier.get();
		}

		return null;
	}

	public static class SessionBuilderImpl<T extends SessionBuilder> implements SessionBuilderImplementor<T>, SessionCreationOptions {
		private static final Logger log = CoreLogging.logger( SessionBuilderImpl.class );

		private final SessionFactoryImpl sessionFactory;
		private Interceptor interceptor;
		private StatementInspector statementInspector;
		private Connection connection;
		private PhysicalConnectionHandlingMode connectionHandlingMode;
		private boolean autoJoinTransactions = true;
		private FlushMode flushMode;
		private boolean autoClose;
		private boolean autoClear;
		private String tenantIdentifier;
		private TimeZone jdbcTimeZone;
		private boolean explicitNoInterceptor;

		// Lazy: defaults can be built by invoking the builder in fastSessionServices.defaultSessionEventListeners
		// (Need a fresh build for each Session as the listener instances can't be reused across sessions)
		// Only initialize of the builder is overriding the default.
		private List<SessionEventListener> listeners;

		//todo : expose setting
		private SessionOwnerBehavior sessionOwnerBehavior = SessionOwnerBehavior.LEGACY_NATIVE;

		public SessionBuilderImpl(SessionFactoryImpl sessionFactory) {
			this.sessionFactory = sessionFactory;

			// set up default builder values...
			final SessionFactoryOptions sessionFactoryOptions = sessionFactory.getSessionFactoryOptions();
			this.statementInspector = sessionFactoryOptions.getStatementInspector();
			this.connectionHandlingMode = sessionFactoryOptions.getPhysicalConnectionHandlingMode();
			this.autoClose = sessionFactoryOptions.isAutoCloseSessionEnabled();

			final CurrentTenantIdentifierResolver currentTenantIdentifierResolver = sessionFactory.getCurrentTenantIdentifierResolver();
			if ( currentTenantIdentifierResolver != null ) {
				tenantIdentifier = currentTenantIdentifierResolver.resolveCurrentTenantIdentifier();
			}
			this.jdbcTimeZone = sessionFactoryOptions.getJdbcTimeZone();
		}


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SessionCreationOptions

		@Override
		public ExceptionMapper getExceptionMapper() {
			return sessionOwnerBehavior == SessionOwnerBehavior.LEGACY_JPA
					? ExceptionMapperLegacyJpaImpl.INSTANCE
					: null;
		}

		@Override
		public boolean shouldAutoJoinTransactions() {
			return autoJoinTransactions;
		}

		@Override
		public FlushMode getInitialSessionFlushMode() {
			return flushMode;
		}

		@Override
		public boolean shouldAutoClose() {
			return autoClose;
		}

		@Override
		public boolean shouldAutoClear() {
			return autoClear;
		}

		@Override
		public Connection getConnection() {
			return connection;
		}

		@Override
		public Interceptor getInterceptor() {
			return configuredInterceptor( interceptor, explicitNoInterceptor, sessionFactory.getSessionFactoryOptions() );
		}

		@Override
		public StatementInspector getStatementInspector() {
			return statementInspector;
		}

		@Override
		public PhysicalConnectionHandlingMode getPhysicalConnectionHandlingMode() {
			return connectionHandlingMode;
		}

		@Override
		public String getTenantIdentifier() {
			return tenantIdentifier;
		}

		@Override
		public TimeZone getJdbcTimeZone() {
			return jdbcTimeZone;
		}

		@Override
		public List<SessionEventListener> getCustomSessionEventListener() {
			return listeners;
		}

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SessionBuilder

		@Override
		public Session openSession() {
			log.tracef( "Opening Hibernate Session.  tenant=%s", tenantIdentifier );
			return new SessionImpl( sessionFactory, this );
		}

		@SuppressWarnings("unchecked")
		private T getThis() {
			return (T) this;
		}

		@Override
		public T interceptor(Interceptor interceptor) {
			this.interceptor = interceptor;
			this.explicitNoInterceptor = false;
			return getThis();
		}

		@Override
		public T noInterceptor() {
			this.interceptor = EmptyInterceptor.INSTANCE;
			this.explicitNoInterceptor = true;
			return getThis();
		}

		@Override
		public T statementInspector(StatementInspector statementInspector) {
			this.statementInspector = statementInspector;
			return getThis();
		}

		@Override
		public T connection(Connection connection) {
			this.connection = connection;
			return getThis();
		}

		@Override
		public T connectionHandlingMode(PhysicalConnectionHandlingMode connectionHandlingMode) {
			this.connectionHandlingMode = connectionHandlingMode;
			return getThis();
		}

		@Override
		public T autoJoinTransactions(boolean autoJoinTransactions) {
			this.autoJoinTransactions = autoJoinTransactions;
			return getThis();
		}

		@Override
		public T autoClose(boolean autoClose) {
			this.autoClose = autoClose;
			return getThis();
		}

		@Override
		public T autoClear(boolean autoClear) {
			this.autoClear = autoClear;
			return getThis();
		}

		@Override
		public T flushMode(FlushMode flushMode) {
			this.flushMode = flushMode;
			return getThis();
		}

		@Override
		public T tenantIdentifier(String tenantIdentifier) {
			this.tenantIdentifier = tenantIdentifier;
			return getThis();
		}

		@Override
		public T eventListeners(SessionEventListener... listeners) {
			if ( this.listeners == null ) {
				this.listeners = sessionFactory.getSessionFactoryOptions()
						.getBaselineSessionEventsListenerBuilder()
						.buildBaselineList();
			}
			Collections.addAll( this.listeners, listeners );
			return getThis();
		}

		@Override
		public T clearEventListeners() {
			if ( listeners == null ) {
				//Needs to initialize explicitly to an empty list as otherwise "null" implies the default listeners will be applied
				this.listeners = new ArrayList<>( 3 );
			}
			else {
				listeners.clear();
			}
			return getThis();
		}

		@Override
		public T jdbcTimeZone(TimeZone timeZone) {
			jdbcTimeZone = timeZone;
			return getThis();
		}
	}

	public static class StatelessSessionBuilderImpl implements StatelessSessionBuilder, SessionCreationOptions {
		private final SessionFactoryImpl sessionFactory;
		private Connection connection;
		private String tenantIdentifier;

		public StatelessSessionBuilderImpl(SessionFactoryImpl sessionFactory) {
			this.sessionFactory = sessionFactory;

			CurrentTenantIdentifierResolver tenantIdentifierResolver = sessionFactory.getCurrentTenantIdentifierResolver();
			if ( tenantIdentifierResolver != null ) {
				tenantIdentifier = tenantIdentifierResolver.resolveCurrentTenantIdentifier();
			}
		}

		@Override
		public StatelessSession openStatelessSession() {
			return new StatelessSessionImpl( sessionFactory, this );
		}

		@Override
		public StatelessSessionBuilder connection(Connection connection) {
			this.connection = connection;
			return this;
		}

		@Override
		public StatelessSessionBuilder tenantIdentifier(String tenantIdentifier) {
			this.tenantIdentifier = tenantIdentifier;
			return this;
		}

		@Override
		public boolean shouldAutoJoinTransactions() {
			return true;
		}

		@Override
		public FlushMode getInitialSessionFlushMode() {
			return FlushMode.ALWAYS;
		}

		@Override
		public boolean shouldAutoClose() {
			return false;
		}

		@Override
		public boolean shouldAutoClear() {
			return false;
		}

		@Override
		public Connection getConnection() {
			return connection;
		}

		@Override
		public Interceptor getInterceptor() {
			return configuredInterceptor( EmptyInterceptor.INSTANCE, false, sessionFactory.getSessionFactoryOptions() );

		}

		@Override
		public StatementInspector getStatementInspector() {
			return null;
		}

		@Override
		public PhysicalConnectionHandlingMode getPhysicalConnectionHandlingMode() {
			return sessionFactory.getSessionFactoryOptions().getPhysicalConnectionHandlingMode();
		}

		@Override
		public String getTenantIdentifier() {
			return tenantIdentifier;
		}

		@Override
		public TimeZone getJdbcTimeZone() {
			return sessionFactory.getSessionFactoryOptions().getJdbcTimeZone();
		}

		@Override
		public List<SessionEventListener> getCustomSessionEventListener() {
			return null;
		}

		@Override
		public ExceptionMapper getExceptionMapper() {
			return null;
		}
	}

	@Override
	public CustomEntityDirtinessStrategy getCustomEntityDirtinessStrategy() {
		return getSessionFactoryOptions().getCustomEntityDirtinessStrategy();
	}

	@Override
	public CurrentTenantIdentifierResolver getCurrentTenantIdentifierResolver() {
		return getSessionFactoryOptions().getCurrentTenantIdentifierResolver();
	}


	// Serialization handling ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Custom serialization hook defined by Java spec.  Used when the factory is directly serialized
	 *
	 * @param out The stream into which the object is being serialized.
	 *
	 * @throws IOException Can be thrown by the stream
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		if ( LOG.isDebugEnabled() ) {
			LOG.debugf( "Serializing: %s", getUuid() );
		}
		out.defaultWriteObject();
		LOG.trace( "Serialized" );
	}

	/**
	 * Custom serialization hook defined by Java spec.  Used when the factory is directly deserialized
	 *
	 * @param in The stream from which the object is being deserialized.
	 *
	 * @throws IOException Can be thrown by the stream
	 * @throws ClassNotFoundException Again, can be thrown by the stream
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		LOG.trace( "Deserializing" );
		in.defaultReadObject();
		if ( LOG.isDebugEnabled() ) {
			LOG.debugf( "Deserialized: %s", getUuid() );
		}
	}

	/**
	 * Custom serialization hook defined by Java spec.  Used when the factory is directly deserialized.
	 * Here we resolve the uuid/name read from the stream previously to resolve the SessionFactory
	 * instance to use based on the registrations with the {@link SessionFactoryRegistry}
	 *
	 * @return The resolved factory to use.
	 *
	 * @throws InvalidObjectException Thrown if we could not resolve the factory by uuid/name.
	 */
	private Object readResolve() throws InvalidObjectException {
		LOG.trace( "Resolving serialized SessionFactory" );
		return locateSessionFactoryOnDeserialization( getUuid(), name );
	}

	private static SessionFactory locateSessionFactoryOnDeserialization(String uuid, String name) throws InvalidObjectException{
		final SessionFactory uuidResult = SessionFactoryRegistry.INSTANCE.getSessionFactory( uuid );
		if ( uuidResult != null ) {
			LOG.debugf( "Resolved SessionFactory by UUID [%s]", uuid );
			return uuidResult;
		}

		// in case we were deserialized in a different JVM, look for an instance with the same name
		// (provided we were given a name)
		if ( name != null ) {
			final SessionFactory namedResult = SessionFactoryRegistry.INSTANCE.getNamedSessionFactory( name );
			if ( namedResult != null ) {
				LOG.debugf( "Resolved SessionFactory by name [%s]", name );
				return namedResult;
			}
		}

		throw new InvalidObjectException( "Could not find a SessionFactory [uuid=" + uuid + ",name=" + name + "]" );
	}

	/**
	 * Custom serialization hook used during Session serialization.
	 *
	 * @param oos The stream to which to write the factory
	 * @throws IOException Indicates problems writing out the serial data stream
	 */
	void serialize(ObjectOutputStream oos) throws IOException {
		oos.writeUTF( getUuid() );
		oos.writeBoolean( name != null );
		if ( name != null ) {
			oos.writeUTF( name );
		}
	}

	/**
	 * Custom deserialization hook used during Session deserialization.
	 *
	 * @param ois The stream from which to "read" the factory
	 * @return The deserialized factory
	 * @throws IOException indicates problems reading back serial data stream
	 */
	static SessionFactoryImpl deserialize(ObjectInputStream ois) throws IOException {
		LOG.trace( "Deserializing SessionFactory from Session" );
		final String uuid = ois.readUTF();
		boolean isNamed = ois.readBoolean();
		final String name = isNamed ? ois.readUTF() : null;
		return (SessionFactoryImpl) locateSessionFactoryOnDeserialization( uuid, name );
	}

	private void maskOutSensitiveInformation(Map<String, Object> props) {
		maskOutIfSet( props, AvailableSettings.JPA_JDBC_USER );
		maskOutIfSet( props, AvailableSettings.JPA_JDBC_PASSWORD );
		maskOutIfSet( props, AvailableSettings.JAKARTA_JDBC_USER );
		maskOutIfSet( props, AvailableSettings.JAKARTA_JDBC_PASSWORD );
		maskOutIfSet( props, AvailableSettings.USER );
		maskOutIfSet( props, AvailableSettings.PASS );
	}

	private void maskOutIfSet(Map<String, Object> props, String setting) {
		if ( props.containsKey( setting ) ) {
			props.put( setting, "****" );
		}
	}

	private void logIfEmptyCompositesEnabled(Map<String, Object> props ) {
		final boolean isEmptyCompositesEnabled = ConfigurationHelper.getBoolean(
				AvailableSettings.CREATE_EMPTY_COMPOSITES_ENABLED,
				props,
				false
		);

		if ( isEmptyCompositesEnabled ) {
			LOG.emptyCompositesEnabled();
		}
	}

	/**
	 * @return the FastSessionServices for this SessionFactory.
	 */
	@Override
	public FastSessionServices getFastSessionServices() {
		return this.fastSessionServices;
	}

	@Override
	public WrapperOptions getWrapperOptions() {
		return wrapperOptions;
	}

	private enum Status {
		OPEN,
		CLOSING,
		CLOSED
	}
}
