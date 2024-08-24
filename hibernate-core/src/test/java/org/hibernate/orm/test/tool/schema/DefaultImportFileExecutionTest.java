package org.hibernate.orm.test.tool.schema;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.tool.schema.SourceType;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.internal.SchemaCreatorImpl;
import org.hibernate.tool.schema.spi.CommandAcceptanceException;
import org.hibernate.tool.schema.spi.ContributableMatcher;
import org.hibernate.tool.schema.spi.ExceptionHandler;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.ScriptSourceInput;
import org.hibernate.tool.schema.spi.ScriptTargetOutput;
import org.hibernate.tool.schema.spi.SourceDescriptor;
import org.hibernate.tool.schema.spi.TargetDescriptor;

import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hibernate.tool.schema.internal.SchemaCreatorImpl.DEFAULT_IMPORT_FILE;

public class DefaultImportFileExecutionTest {

	private File defaultImportFile;
	private StandardServiceRegistry serviceRegistry;
	private static final String COMMAND = "INSERT INTO TEST_ENTITY (id, name) values (1,'name')";


	@BeforeEach
	public void setUp() throws Exception {
		defaultImportFile = createDefaultImportFile( "import.sql" );
		serviceRegistry = ServiceRegistryUtil.serviceRegistry();
	}

	@AfterEach
	public void tearDown() {
		serviceRegistry.close();
		if ( defaultImportFile.exists() ) {
			try {
				Files.delete( defaultImportFile.toPath() );
			}
			catch (IOException e) {
				throw new RuntimeException( e );
			}
		}
	}

	@Test
	public void testImportScriptIsExecutedOnce() {
		assertThat( serviceRegistry.getService( ClassLoaderService.class )
							.locateResource( DEFAULT_IMPORT_FILE ) ).isNotNull();

		TargetDescriptorImpl targetDescriptor = TargetDescriptorImpl.INSTANCE;

		createSchema( targetDescriptor );

		TestScriptTargetOutput targetOutput = (TestScriptTargetOutput) targetDescriptor.getScriptTargetOutput();

		assertThat( targetOutput.getInsertCommands().size() ).isEqualTo( 1 );
	}

	private void createSchema(TargetDescriptorImpl targetDescriptor) {

		final Metadata mappings = buildMappings( serviceRegistry );

		final SchemaCreatorImpl schemaCreator = new SchemaCreatorImpl( serviceRegistry );

		final Map<String, Object> options = new HashMap<>();
		options.put( AvailableSettings.JAKARTA_HBM2DDL_LOAD_SCRIPT_SOURCE, "import.sql" );
		schemaCreator.doCreation(
				mappings,
				new ExecutionOptionsTestImpl( options ),
				ContributableMatcher.ALL,
				SourceDescriptorImpl.INSTANCE,
				targetDescriptor
		);
	}

	private static File createDefaultImportFile(String fileName) throws Exception {
		URL myUrl = Thread.currentThread().getContextClassLoader().getResource( "hibernate.properties" );
		String path = myUrl.getPath().replace( "hibernate.properties", fileName );
		final File file = new File( path );
		file.createNewFile();

		try (final FileWriter myWriter = new FileWriter( file )) {
			myWriter.write( COMMAND );
		}

		return file;
	}

	private Metadata buildMappings(StandardServiceRegistry registry) {
		return new MetadataSources( registry )
				.addAnnotatedClass( TestEntity.class )
				.buildMetadata();
	}

	@Entity(name = "TestEntity")
	@Table(name = "TEST_ENTITY")
	public static class TestEntity {
		@Id
		private long id;

		private String name;
	}

	public class ExecutionOptionsTestImpl implements ExecutionOptions, ExceptionHandler {
		Map<String, Object> configValues;

		public ExecutionOptionsTestImpl(Map<String, Object> configValues) {
			this.configValues = configValues;
		}

		@Override
		public Map getConfigurationValues() {
			return configValues;
		}

		@Override
		public boolean shouldManageNamespaces() {
			return true;
		}

		@Override
		public ExceptionHandler getExceptionHandler() {
			return this;
		}

		@Override
		public SchemaFilter getSchemaFilter() {
			return SchemaFilter.ALL;
		}

		@Override
		public void handleException(CommandAcceptanceException exception) {
			throw exception;
		}
	}

	private static class SourceDescriptorImpl implements SourceDescriptor {
		/**
		 * Singleton access
		 */
		public static final SourceDescriptorImpl INSTANCE = new SourceDescriptorImpl();

		@Override
		public SourceType getSourceType() {
			return SourceType.METADATA;
		}

		@Override
		public ScriptSourceInput getScriptSourceInput() {
			return null;
		}
	}

	private static class TargetDescriptorImpl implements TargetDescriptor {
		/**
		 * Singleton access
		 */
		public static final TargetDescriptorImpl INSTANCE = new TargetDescriptorImpl();

		@Override
		public EnumSet<TargetType> getTargetTypes() {
			return EnumSet.of( TargetType.SCRIPT );
		}

		@Override
		public ScriptTargetOutput getScriptTargetOutput() {
			return TestScriptTargetOutput.INSTANCE;
		}
	}

	private static class TestScriptTargetOutput implements ScriptTargetOutput {

		public static final TestScriptTargetOutput INSTANCE = new TestScriptTargetOutput();

		List<String> insertCommands = new ArrayList<>();

		@Override
		public void prepare() {

		}

		@Override
		public void accept(String command) {
			if ( command.toLowerCase().startsWith( "insert" ) ) {
				insertCommands.add( command );
			}
		}

		@Override
		public void release() {

		}

		public List<String> getInsertCommands() {
			return insertCommands;
		}
	}
}
