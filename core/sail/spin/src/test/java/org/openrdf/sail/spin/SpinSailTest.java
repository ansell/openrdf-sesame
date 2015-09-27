/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.spin;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.inferencer.fc.DedupingInferencer;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

public class SpinSailTest {
	private static final String BASE_DIR = "/testcases/";

	@Rule
	public ExpectedException constraintException = ExpectedException.none();

	private Repository repo;
	private RepositoryConnection conn;

	@Before
	public void setup() throws RepositoryException {
		NotifyingSail baseSail = new MemoryStore();
		DedupingInferencer deduper = new DedupingInferencer(baseSail);
		ForwardChainingRDFSInferencer rdfsInferencer = new ForwardChainingRDFSInferencer(deduper);
		SpinSail spinSail = new SpinSail(rdfsInferencer);
		repo = new SailRepository(spinSail);
		repo.initialize();
		conn = repo.getConnection();
	}

	@After
	public void tearDown() throws RepositoryException {
		if(conn != null) {
			conn.close();
		}
		if(repo != null) {
			repo.shutDown();
		}
	}

	@Test
	public void testAskConstraint() throws Exception {
		constraintException.expectCause(isA(ConstraintViolationException.class));
		constraintException.expectMessage("Test constraint");
		loadStatements("testAskConstraint.ttl");
	}

	@Test
	public void testTemplateConstraint() throws Exception {
		constraintException.expectCause(isA(ConstraintViolationException.class));
		constraintException.expectMessage("Invalid number of values: 0");
		loadStatements("testTemplateConstraint.ttl");
	}

	@Test
	public void testConstructRule() throws Exception {
		loadStatements("testConstructRule.ttl");
		assertStatements("testConstructRule-expected.ttl");
	}

	@Test
	public void testUpdateTemplateRule() throws Exception {
		loadStatements("testUpdateTemplateRule.ttl");
		assertStatements("testUpdateTemplateRule-expected.ttl");
	}

	@Test
	public void testEvalFunctionConstraint() throws Exception {
		loadStatements("testEvalFunctionConstraint.ttl");
	}

	@Test
	public void testConstructProperty() throws Exception {
		loadStatements("testConstructProperty.ttl");
		assertStatements("testConstructProperty-expected.ttl");
	}

	@Test
	public void testSelectProperty() throws Exception {
		loadStatements("testSelectProperty.ttl");
		assertStatements("testSelectProperty-expected.ttl");
	}

	@Test
	public void testMagicProperty() throws Exception {
		loadStatements("testMagicProperty.ttl");
		assertStatements("testMagicProperty-expected.ttl");
	}

	@Test
	public void testMagicPropertyFunction() throws Exception {
		loadStatements("testMagicPropertyFunction.ttl");
		assertStatements("testMagicPropertyFunction-expected.ttl");
	}

	@Test
	public void testSpinxRule() throws Exception {
		loadStatements("testSpinxRule.ttl");
		assertStatements("testSpinxRule-expected.ttl");
	}

	private void loadStatements(String ttl) throws RepositoryException, RDFParseException, IOException {
		URL url = getClass().getResource(BASE_DIR+ttl);
		InputStream in = url.openStream();
		try {
			conn.add(in, url.toString(), RDFFormat.TURTLE);
		}
		finally {
			in.close();
		}
	}

	private void assertStatements(String ttl) throws RDFParseException, RDFHandlerException, IOException, RepositoryException {
		StatementCollector expected = new StatementCollector();
		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		parser.setRDFHandler(expected);
		URL url = getClass().getResource(BASE_DIR+ttl);
		InputStream rdfStream = url.openStream();
		parser.parse(rdfStream, url.toString());
		rdfStream.close();

		for(Statement stmt : expected.getStatements()) {
			assertTrue("Expected statement: "+stmt, conn.hasStatement(stmt, true));
		}
	}
}
