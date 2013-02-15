package org.openrdf.repository.manager;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

public class TestProxyRepositoryFactory {

	private final ProxyRepositoryFactory factory = new ProxyRepositoryFactory();

	@Test
	public final void testGetRepositoryType() {
		assertThat(factory.getRepositoryType(), is("openrdf:ProxyRepository"));
	}

	@Test(expected = RepositoryConfigException.class)
	public final void testGetConfig()
		throws RepositoryConfigException
	{
		RepositoryImplConfig factoryConfig = factory.getConfig();
		assertThat(factoryConfig, instanceOf(ProxyRepositoryConfig.class));
		factoryConfig.validate();
	}

	@Test
	public final void testGetRepository()
		throws OpenRDFException, IOException
	{
		RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, ValueFactoryImpl.getInstance());
		Graph graph = new TreeModel();
		rdfParser.setRDFHandler(new StatementCollector(graph));
		rdfParser.parse(new BufferedReader(new InputStreamReader(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("proxy.ttl"))),
				RepositoryConfigSchema.NAMESPACE);
		RepositoryConfig config = RepositoryConfig.create(graph,
				GraphUtil.getUniqueSubject(graph, RDF.TYPE, RepositoryConfigSchema.REPOSITORY));
		config.validate();
		assertThat(config.getID(), is("proxy"));
		assertThat(config.getTitle(), is("Test Proxy for 'memory'"));
		RepositoryImplConfig implConfig = config.getRepositoryImplConfig();
		assertThat(implConfig.getType(), is("openrdf:ProxyRepository"));
		assertThat(implConfig, instanceOf(ProxyRepositoryConfig.class));
		assertThat(((ProxyRepositoryConfig)implConfig).getProxiedRepositoryID(), is("memory"));

		// Factory just needs a manager instance to proceed with construction.
		// It doesn't actually invoke the manager until the repository is
		// accessed. Normally LocalRepositoryManager is the caller of
		// getRepository(), and will have called this setter itself.
		factory.setRepositoryManager(new LocalRepositoryManager(null));
		Repository repository = factory.getRepository(implConfig);
		assertThat(repository, instanceOf(ProxyRepository.class));
	}
}
