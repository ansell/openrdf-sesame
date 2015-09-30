/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.Set;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.RDFParser.DatatypeHandling;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.junit.Test;

/**
 * @author Arjohn Kampman
 */
public abstract class ModelEqualityTest {

	public static final String TESTCASES_DIR = "/testcases/model/equality/";

	@Test
	public void testTest001()
		throws Exception
	{
		testFilesEqual("test001a.ttl", "test001b.ttl");
	}

	@Test
	public void testFoafExampleAdvanced()
		throws Exception
	{
		testFilesEqual("foaf-example-advanced.rdf", "foaf-example-advanced.rdf");
	}

	@Test
	public void testSparqlGraph11()
		throws Exception
	{
		testFilesEqual("sparql-graph-11.ttl", "sparql-graph-11.ttl");
	}

	@Test
	public void testBlankNodeGraphs()
		throws Exception
	{
		testFilesEqual("toRdf-0061-out.nq", "toRdf-0061-out.nq");
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
		boolean modelsEqual = Models.isomorphic(model1, model2);
		// long endTime = System.currentTimeMillis();
		// System.out.println("Model equality checked in " + (endTime - startTime)
		// + "ms (" + file1 + ", " + file2
		// + ")");

		assertTrue(modelsEqual);
	}

	private Model loadModel(String fileName)
		throws Exception
	{
		URL modelURL = this.getClass().getResource(TESTCASES_DIR + fileName);
		assertNotNull("Test file not found: " + fileName, modelURL);

		Model model = createEmptyModel();
		
		Optional<RDFFormat> rdfFormat = Rio.getParserFormatForFileName(fileName);
		assertTrue("Unable to determine RDF format for file: " + fileName, rdfFormat.isPresent());

		RDFParser parser = Rio.createParser(rdfFormat.get());
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

	protected abstract Model createEmptyModel();
}
