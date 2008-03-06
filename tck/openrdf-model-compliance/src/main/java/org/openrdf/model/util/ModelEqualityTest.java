/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.util;

import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.openrdf.model.Statement;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author Arjohn Kampman
 */
public class ModelEqualityTest extends TestCase {

	public static final String TESTCASES_DIR = "/testcases/model/equality/";

	public void testTest001()
		throws Exception
	{
		testFilesEqual("test001a.ttl", "test001b.ttl");
	}

	public void testFoafExampleAdvanced()
		throws Exception
	{
		testFilesEqual("foaf-example-advanced.rdf", "foaf-example-advanced.rdf");
	}

	public void testSparqlGraph11()
		throws Exception
	{
		testFilesEqual("sparql-graph-11.ttl", "sparql-graph-11.ttl");
	}

	// public void testSparqlGraph11Shuffled()
	// throws Exception
	// {
	// testFilesEqual("sparql-graph-11.ttl", "sparql-graph-11-shuffled.ttl");
	// }

	// public void testSparqlGraph11Shuffled2()
	// throws Exception
	// {
	// testFilesEqual("sparql-graph-11-shuffled.ttl", "sparql-graph-11.ttl");
	// }

	// public void testPhotoData()
	// throws Exception
	// {
	// testFilesEqual("photo-data.rdf", "photo-data.rdf");
	// }

	private void testFilesEqual(String file1, String file2)
		throws Exception
	{
		Set<Statement> model1 = loadModel(file1);
		Set<Statement> model2 = loadModel(file2);

		// long startTime = System.currentTimeMillis();
		boolean modelsEqual = ModelUtil.equals(model1, model2);
		// long endTime = System.currentTimeMillis();
		// System.out.println("Model equality checked in " + (endTime - startTime)
		// + "ms (" + file1 + ", " + file2
		// + ")");

		assertTrue(modelsEqual);
	}

	private Set<Statement> loadModel(String fileName)
		throws Exception
	{
		URL modelURL = this.getClass().getResource(TESTCASES_DIR + fileName);
		assertNotNull("Test file not found: " + fileName, modelURL);

		Set<Statement> model = new LinkedHashSet<Statement>();

		RDFFormat rdfFormat = Rio.getParserFormatForFileName(fileName);
		assertNotNull("Unable to determine RDF format for file: " + fileName, rdfFormat);

		RDFParser parser = Rio.createParser(rdfFormat);
		parser.setDatatypeHandling(DatatypeHandling.IGNORE);
		parser.setPreserveBNodeIDs(true);
		parser.setRDFHandler(new StatementCollector(model));

		InputStream in = modelURL.openStream();
		try {
			parser.parse(in, modelURL.toString());
			return model;
		}
		finally {
			in.close();
		}
	}
}
