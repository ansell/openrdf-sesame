/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import info.aduna.iteration.Iterations;

import org.openrdf.model.Statement;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.RepositoryUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;

public class InferencingTest extends TestCase {

	/*-----------*
	 * Constants *
	 *-----------*/

	public static final String TEST_DIR_PREFIX = "/testcases/rdf-mt-inferencing";

	/*-----------*
	 * Variables *
	 *-----------*/

	protected Sail sailStack;

	protected String inputData;

	protected String outputData;

	protected boolean isPositiveTest;

	protected String name;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new inferencing test. This test can either be positive or
	 * negative. For positive tests, all triples from <tt>outputData</tt>
	 * should be present in the triples returned by the supplied
	 * RdfSchemaRepository after the triples from <tt>intputData</tt> have been
	 * added to it. For negative tests, none of the triples from
	 * <tt>outputData</tt> should be present in the returned triples.
	 * 
	 * @param name
	 *        The name of the test.
	 * @param rss
	 *        The RdfSchemaRepository to test.
	 * @param inputData
	 *        The URL of the (N-Triples) data containing the triples that should
	 *        be added to the RdfSchemaRepository.
	 * @param outputData
	 *        The URL of the (N-Triples) data containing the triples that should
	 *        or should not (depending on the value of <tt>isPositiveTest</tt>
	 *        be present in the statements returned by the RdfSchemaRepository.
	 * @param isPositiveTest
	 *        Flag indicating whether this is a positive or a negative
	 *        inferencing test; <tt>true</tt> for a positive test,
	 *        <tt>false</tt> for a negative test.
	 */
	public InferencingTest(String name, Sail sailStack, String inputData, String outputData,
			boolean isPositiveTest)
	{
		super(name);
		int slashLoc = name.lastIndexOf('/');
		this.name = name.substring(0, slashLoc) + "-" + name.substring(slashLoc + 1);

		this.sailStack = sailStack;
		this.inputData = inputData;
		this.outputData = outputData;
		this.isPositiveTest = isPositiveTest;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void runTest()
		throws Exception
	{
		Collection<? extends Statement> entailedStatements = null;
		Collection<? extends Statement> expectedStatements = null;

		Repository repository = new SailRepository(sailStack);
		repository.initialize();

		RepositoryConnection con = repository.getConnection();
		con.setAutoCommit(false);

		// clear the input store
		con.clear();
		con.commit();

		// Upload input data
		InputStream stream = getClass().getResourceAsStream(inputData);
		try {
			con.add(stream, inputData, RDFFormat.NTRIPLES);
			con.commit();

			entailedStatements = Iterations.addAll(con.getStatements(null, null, null, true),
					new HashSet<Statement>());
		}
		finally {
			stream.close();
			con.close();
		}

		// Upload output data
		Repository outputRepository = new SailRepository(new MemoryStore());
		outputRepository.initialize();
		con = outputRepository.getConnection();
		con.setAutoCommit(false);

		stream = getClass().getResourceAsStream(outputData);
		try {
			con.add(stream, outputData, RDFFormat.NTRIPLES);
			con.commit();

			expectedStatements = Iterations.addAll(con.getStatements(null, null, null, false),
					new HashSet<Statement>());
		}
		finally {
			stream.close();
			con.close();
			outputRepository.shutDown();
			repository.shutDown();
		}

		// Check whether all expected statements are present in the entailment
		// closure set.
		boolean outputEntailed = ModelUtil.isSubset(expectedStatements, entailedStatements);

		if (isPositiveTest && !outputEntailed) {
			File dumpFile = dumpStatements(RepositoryUtil.difference(expectedStatements, entailedStatements));

			fail("Incomplete entailment, difference between expected and entailed dumped to file " + dumpFile);
		}
		else if (!isPositiveTest && outputEntailed) {
			File dumpFile = dumpStatements(expectedStatements);
			fail("Erroneous entailment, unexpected statements dumped to file " + dumpFile);
		}
	}

	private File dumpStatements(Collection<? extends Statement> statements)
		throws Exception
	{
		// Dump results to tmp file for debugging
		String tmpDir = System.getProperty("java.io.tmpdir");
		File tmpFile = new File(tmpDir, "junit-" + name + ".nt");

		OutputStream export = new FileOutputStream(tmpFile);
		try {
			RDFWriter writer = Rio.createWriter(RDFFormat.NTRIPLES, export);

			writer.startRDF();
			for (Statement st : statements) {
				writer.handleStatement(st);
			}
			writer.endRDF();
		}
		finally {
			export.close();
		}

		return tmpFile;
	}

	/*----------------*
	 * Static methods *
	 *----------------*/

	public static Test suite(Sail sailStack, String name) {
		TestSuite suite = new TestSuite(name);

		suite.addTest(createTestCase(sailStack, "subclassof", "test001", true));
		suite.addTest(createTestCase(sailStack, "subclassof", "test002", true));
		suite.addTest(createTestCase(sailStack, "subclassof", "test003", true));
		suite.addTest(createTestCase(sailStack, "subclassof", "error001", false));
		suite.addTest(createTestCase(sailStack, "subpropertyof", "test001", true));
		suite.addTest(createTestCase(sailStack, "subpropertyof", "test002", true));
		suite.addTest(createTestCase(sailStack, "subpropertyof", "test003", true));
		suite.addTest(createTestCase(sailStack, "subpropertyof", "error001", false));
		suite.addTest(createTestCase(sailStack, "domain", "test001", true));
		suite.addTest(createTestCase(sailStack, "domain", "error001", false));
		suite.addTest(createTestCase(sailStack, "range", "test001", true));
		suite.addTest(createTestCase(sailStack, "range", "error001", false));
		suite.addTest(createTestCase(sailStack, "type", "test001", true));
		suite.addTest(createTestCase(sailStack, "type", "test002", true));
		suite.addTest(createTestCase(sailStack, "type", "test003", true));
		suite.addTest(createTestCase(sailStack, "type", "test004", true));
		suite.addTest(createTestCase(sailStack, "type", "test005", true));
		suite.addTest(createTestCase(sailStack, "type", "error001", false));
		suite.addTest(createTestCase(sailStack, "type", "error002", false));

		return suite;
	}

	private static TestCase createTestCase(Sail sailStack, String subdir, String testName,
			boolean isPositiveTest)
	{
		return new InferencingTest(subdir + "/" + testName, sailStack, TEST_DIR_PREFIX + "/" + subdir + "/"
				+ testName + "-in.nt", TEST_DIR_PREFIX + "/" + subdir + "/" + testName + "-out.nt",
				isPositiveTest);
	}
}
