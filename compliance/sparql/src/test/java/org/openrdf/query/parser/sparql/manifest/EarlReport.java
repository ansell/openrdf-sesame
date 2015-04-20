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
package org.openrdf.query.parser.sparql.manifest;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.DOAP;
import org.openrdf.model.vocabulary.EARL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.parser.sparql.manifest.SPARQL11SyntaxTest;
import org.openrdf.query.parser.sparql.manifest.SPARQLQueryTest;
import org.openrdf.query.parser.sparql.manifest.SPARQLUpdateConformanceTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author Arjohn Kampman
 */
public class EarlReport {

	protected static Repository earlRepository;

	protected static ValueFactory vf;

	protected static RepositoryConnection con;

	protected static Resource projectNode;

	protected static Resource asserterNode;

	private static Logger logger = LoggerFactory.getLogger(EarlReport.class);
	
	public static void main(String[] args)
		throws Exception
	{
		earlRepository = new SailRepository(new MemoryStore());
		earlRepository.initialize();
		vf = earlRepository.getValueFactory();
		con = earlRepository.getConnection();
		con.begin();

		con.setNamespace("rdf", RDF.NAMESPACE);
		con.setNamespace("xsd", XMLSchema.NAMESPACE);
		con.setNamespace("doap", DOAP.NAMESPACE);
		con.setNamespace("earl", EARL.NAMESPACE);
		con.setNamespace("dcterms", DCTERMS.NAMESPACE);

		projectNode = vf.createBNode();
		BNode releaseNode = vf.createBNode();
		con.add(projectNode, RDF.TYPE, DOAP.PROJECT);
		con.add(projectNode, DOAP.NAME, vf.createLiteral("OpenRDF Sesame"));
		con.add(projectNode, DOAP.RELEASE, releaseNode);
		con.add(projectNode, DOAP.HOMEPAGE, vf.createIRI("http://www.openrdf.org/"));
		con.add(releaseNode, RDF.TYPE, DOAP.VERSION);
		con.add(releaseNode, DOAP.NAME, vf.createLiteral("Sesame 2.7.0"));
		SimpleDateFormat xsdDataFormat = new SimpleDateFormat("yyyy-MM-dd");
		String currentDate = xsdDataFormat.format(new Date());
		con.add(releaseNode, DOAP.CREATED, vf.createLiteral(currentDate, XMLSchema.DATE));

		asserterNode = vf.createBNode();
		con.add(asserterNode, RDF.TYPE, EARL.SOFTWARE);
		con.add(asserterNode, DC.TITLE, vf.createLiteral("OpenRDF SPARQL 1.1 compliance tests"));

		TestResult testResult = new TestResult();
		EarlTestListener listener = new EarlTestListener();
		testResult.addListener(listener);

		logger.info("running query evaluation tests..");
		W3CApprovedSPARQL11QueryTest.suite().run(testResult);
		
		logger.info("running syntax tests...");
		W3CApprovedSPARQL11SyntaxTest.suite().run(testResult);
		
		logger.info("running update tests...");
		W3CApprovedSPARQL11UpdateTest.suite().run(testResult);
		logger.info("tests complete, generating EARL report...");
		
		con.commit();

		RDFWriterFactory factory = RDFWriterRegistry.getInstance().get(RDFFormat.TURTLE);
		File outFile = File.createTempFile("sesame-sparql-compliance", "."
				+ RDFFormat.TURTLE.getDefaultFileExtension());
		FileOutputStream out = new FileOutputStream(outFile);
		try {
			con.export(factory.getWriter(out));
		}
		finally {
			out.close();
		}

		con.close();
		earlRepository.shutDown();

		logger.info("EARL output written to " + outFile);
	}

	protected static class EarlTestListener implements TestListener {

		private int errorCount;

		private int failureCount;

		public void startTest(Test test) {
			errorCount = failureCount = 0;
		}

		public void endTest(Test test) {
			String testURI = null;
			;
			if (test instanceof SPARQLQueryTest) {
				testURI = ((SPARQLQueryTest)test).testURI;
			}
			else if (test instanceof SPARQL11SyntaxTest) {
				testURI = ((SPARQL11SyntaxTest)test).testURI;
			}
			else if (test instanceof SPARQLUpdateConformanceTest) {
				testURI = ((SPARQLUpdateConformanceTest)test).testURI;
			}
			else {
				throw new RuntimeException("Unexpected test type: " + test.getClass());
			}

			try {
				BNode testNode = vf.createBNode();
				BNode resultNode = vf.createBNode();
				con.add(testNode, RDF.TYPE, EARL.ASSERTION);
				con.add(testNode, EARL.ASSERTEDBY, asserterNode);
				con.add(testNode, EARL.MODE, EARL.AUTOMATIC);
				con.add(testNode, EARL.SUBJECT, projectNode);
				con.add(testNode, EARL.TEST, vf.createIRI(testURI));
				con.add(testNode, EARL.RESULT, resultNode);
				con.add(resultNode, RDF.TYPE, EARL.TESTRESULT);

				if (errorCount > 0) {
					con.add(resultNode, EARL.OUTCOME, EARL.FAIL);
				}
				else if (failureCount > 0) {
					con.add(resultNode, EARL.OUTCOME, EARL.FAIL);
				}
				else {
					con.add(resultNode, EARL.OUTCOME, EARL.PASS);
				}
			}
			catch (RepositoryException e) {
				throw new RuntimeException(e);
			}
		}

		public void addError(Test test, Throwable t) {
			errorCount++;
		}

		public void addFailure(Test test, AssertionFailedError error) {
			failureCount++;
		}
	}
}
