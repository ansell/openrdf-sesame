/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import info.aduna.io.IOUtil;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.http.config.HTTPRepositoryConfig;
import org.openrdf.repository.http.config.HTTPRepositoryFactory;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.repository.sparql.config.SPARQLRepositoryConfig;
import org.openrdf.repository.sparql.config.SPARQLRepositoryFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.federation.config.FederationConfig;

/**
 * Unit tests for {@link Federate}.
 * 
 * @author Dale Visser
 */
public class FederateTest {

	private RepositoryManager manager;

	private static final String FED_ID = "fedID";

	private static final String MEMBER_ID1 = "alien";

	private static final String MEMBER_ID2 = "scary";

	private static final String HTTP_MEMBER_ID = "http";

	private static final String HTTP2_MEMBER_ID = "http2";

	private static final String SPARQL_MEMBER_ID = "sparql";

	private static final String SPARQL2_MEMBER_ID = "sparql2";

	private static final String FED_DESCRIPTION = "Test Federation Title";

	private Federate federate;

	private ConsoleIO streams;

	@Rule
	public final TemporaryFolder LOCATION = new TemporaryFolder();

	private void prepareManager(boolean fakeHTTP)
		throws UnsupportedEncodingException, IOException, OpenRDFException
	{
		LocalRepositoryManager mgr = new LocalRepositoryManager(LOCATION.getRoot());
		mgr.initialize();
		addRepository(MEMBER_ID1, mgr);
		addRepository(MEMBER_ID2, mgr);
		addRepository(HTTP_MEMBER_ID, mgr);
		addRepository(HTTP2_MEMBER_ID, mgr);
		addRepository(SPARQL_MEMBER_ID, mgr);
		addRepository(SPARQL2_MEMBER_ID, mgr);
		if (fakeHTTP) {
			URL file = mgr.getLocation();
			URL http = new URL(file.toString().replaceFirst("[Ff][Ii][Ll][Ee]", "http"));
			manager = spy(mgr);
			when(manager.getLocation()).thenReturn(http);
		}
		else {
			manager = mgr;
		}
		ConsoleState state = mock(ConsoleState.class);
		when(state.getManager()).thenReturn(manager);
		streams = mock(ConsoleIO.class);
		when(streams.readln("Federation Description (optional):")).thenReturn(FED_DESCRIPTION);
		federate = new Federate(streams, state);
	}

	private final void addRepository(String identity, RepositoryManager mgr)
		throws UnsupportedEncodingException, IOException, OpenRDFException
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		addRepository(classLoader.getResourceAsStream("federate/" + identity + "-config.ttl"),
				classLoader.getResource("federate/" + identity + ".ttl"), mgr);
	}

	private void addRepository(InputStream configStream, URL data, RepositoryManager mgr)
		throws UnsupportedEncodingException, IOException, OpenRDFException
	{
		String configString = IOUtil.readString(new InputStreamReader(configStream, "UTF-8"));
		configStream.close();
		Repository systemRepo = mgr.getSystemRepository();
		ValueFactory factory = systemRepo.getValueFactory();
		Graph graph = new GraphImpl(factory);
		RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, factory);
		rdfParser.setRDFHandler(new StatementCollector(graph));
		rdfParser.parse(new StringReader(configString), RepositoryConfigSchema.NAMESPACE);
		Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
		RepositoryConfig repoConfig = RepositoryConfig.create(graph, repositoryNode);
		repoConfig.validate();
		RepositoryConfigUtil.updateRepositoryConfigs(systemRepo, repoConfig);
		String repoID = GraphUtil.getUniqueObjectLiteral(graph, repositoryNode,
				RepositoryConfigSchema.REPOSITORYID).stringValue();
		if (null != data) { // null if we didn't provide a data file
			RepositoryConnection connection = mgr.getRepository(repoID).getConnection();
			try {
				connection.add(data, null, RDFFormat.TURTLE);
			}
			finally {
				connection.close();
			}
		}
	}

	@After
	public void tearDown()
		throws OpenRDFException
	{
		manager.shutDown();
	}

	private void execute(String... args)
		throws IOException
	{
		List<String> execArgs = new ArrayList<String>(args.length + 1);
		execArgs.add("federate");
		Collections.addAll(execArgs, args);
		federate.execute(execArgs.toArray(new String[execArgs.size()]));
	}

	@Test
	public void noArgumentsPrintsHelp()
		throws IOException, OpenRDFException
	{
		prepareManager(true);
		execute();
		verify(streams).writeln(PrintHelp.FEDERATE);
	}

	@Test
	public void oneArgumentPrintsHelp()
		throws IOException, OpenRDFException
	{
		prepareManager(true);
		execute(FED_ID);
		verify(streams).writeln(PrintHelp.FEDERATE);
	}

	@Test
	public void twoArgumentsPrintsHelp()
		throws IOException, OpenRDFException
	{
		prepareManager(true);
		execute(FED_ID, MEMBER_ID1);
		verify(streams).writeln(PrintHelp.FEDERATE);
	}

	@Test
	public void invalidMemberTypePrintsError()
		throws IOException, OpenRDFException
	{
		prepareManager(true);
		execute("type=memory", FED_ID, MEMBER_ID1, MEMBER_ID2);
		verifyFailure();
	}

	@Test
	public void duplicateMembersPrintsError()
		throws IOException, OpenRDFException
	{
		prepareManager(true);
		execute(FED_ID, MEMBER_ID1, MEMBER_ID1);
		verifyFailure();
	}

	@Test
	public void fedSameAsMemberPrintsError()
		throws IOException, OpenRDFException
	{
		prepareManager(true);
		execute(FED_ID, MEMBER_ID1, FED_ID, MEMBER_ID1);
		verifyFailure();
	}

	@Test
	public void sparqlAndNotReadOnlyPrintsError()
		throws IOException, OpenRDFException
	{
		prepareManager(true);
		execute("readonly=false", "type=sparql", FED_ID, MEMBER_ID1, MEMBER_ID2);
		verifyFailure();
	}

	@Test
	public void fedAlreadyExistsPrintsSpecificError()
		throws IOException, OpenRDFException
	{
		prepareManager(true);
		execute(MEMBER_ID1, FED_ID, MEMBER_ID2);
		verifyFailure(MEMBER_ID1 + " already exists.");
	}

	@Test
	public void nonexistentMemberPrintsSpecificError()
		throws IOException, OpenRDFException
	{
		prepareManager(true);
		execute(FED_ID, MEMBER_ID1, "FreeLunch");
		verifyFailure("FreeLunch does not exist.");
	}

	@Test
	public void federateOnLocalManagerFailsWithNonHttpMembers()
		throws UnsupportedEncodingException, IOException, OpenRDFException
	{
		prepareManager(false);
		execute(FED_ID, MEMBER_ID1, MEMBER_ID2);
		verifyFailure("Connection is local, and alien isn't a openrdf:HTTPRepository or openrdf:SPARQLRepository");
	}

	@Test
	public void federateFailsWithHTTPtypeAndSPARQLmembers()
		throws UnsupportedEncodingException, IOException, OpenRDFException
	{
		prepareManager(false);
		execute(FED_ID, HTTP_MEMBER_ID, SPARQL_MEMBER_ID);
		verifyFailure(SPARQL_MEMBER_ID + " is " + SPARQLRepositoryFactory.REPOSITORY_TYPE
				+ ", and can't be federated as " + HTTPRepositoryFactory.REPOSITORY_TYPE);
	}

	@Test
	public void federateSailsOnRemoteSucceeds()
		throws IOException, OpenRDFException
	{
		prepareManager(true);
		execute(FED_ID, MEMBER_ID1, MEMBER_ID2);
		verifySuccess(HTTPRepositoryFactory.REPOSITORY_TYPE);
	}

	@Test
	public void federateHTTPonLocalWithHTTPtypeSucceeds()
		throws IOException, OpenRDFException
	{
		prepareManager(false);
		execute(FED_ID, HTTP_MEMBER_ID, HTTP2_MEMBER_ID);
		verifySuccess(HTTPRepositoryFactory.REPOSITORY_TYPE);
	}

	@Test
	public void federateSPARQLonLocalWithSPARQLtypeSucceeds()
		throws IOException, OpenRDFException
	{
		prepareManager(false);
		execute(FED_ID, "type=sparql", SPARQL_MEMBER_ID, SPARQL2_MEMBER_ID);
		verifySuccess(SPARQLRepositoryFactory.REPOSITORY_TYPE);
	}

	private void verifySuccess(String memberType)
		throws RepositoryException, RepositoryConfigException, IOException
	{
		assertTrue("Expected " + FED_ID + " to exist.", manager.hasRepositoryConfig(FED_ID));
		verify(streams, times(1)).writeln("Federation created.");
		verify(streams, never()).writeError(anyString());
		verify(streams, times(1)).readln("Federation Description (optional):");
		assertEquals("Description of created federation repository not what expected.", FED_DESCRIPTION,
				manager.getRepositoryInfo(FED_ID).getDescription());
		SailRepositoryConfig sailRepoConfig = (SailRepositoryConfig)manager.getRepositoryConfig(FED_ID).getRepositoryImplConfig();
		FederationConfig fedSailConfig = (FederationConfig)sailRepoConfig.getSailImplConfig();
		for (RepositoryImplConfig ric : fedSailConfig.getMembers()) {
			assertEquals("Expected a certain member type.", memberType, ric.getType());
			assertThat(
					ric,
					instanceOf(HTTPRepositoryFactory.REPOSITORY_TYPE.equals(memberType) ? HTTPRepositoryConfig.class
							: SPARQLRepositoryConfig.class));
		}
	}

	private void verifyFailure(String... error)
		throws RepositoryException, RepositoryConfigException
	{
		if (error.length > 0) {
			verify(streams).writeError(error[0]);
		}
		else {
			verify(streams).writeError(anyString());
		}
		assertFalse("Expected " + FED_ID + " to not exist.", manager.hasRepositoryConfig(FED_ID));
	}
}