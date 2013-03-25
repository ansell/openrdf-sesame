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
package org.openrdf.rio.turtle;

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
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.DOAP;
import org.openrdf.model.vocabulary.EARL;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
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
 * @author Peter Ansell
 */
public class TurtleEarlReport {

	protected static Repository earlRepository;

	protected static ValueFactory vf;

	protected static RepositoryConnection con;

	protected static Resource projectNode;

	protected static Resource asserterNode;

	private static Logger logger = LoggerFactory.getLogger(TurtleEarlReport.class);

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

		projectNode = vf.createURI("http://www.openrdf.org/#sesame");
		BNode releaseNode = vf.createBNode();
		con.add(projectNode, RDF.TYPE, DOAP.PROJECT);
		con.add(projectNode, RDF.TYPE, EARL.TEST_SUBJECT);
		con.add(projectNode, RDF.TYPE, EARL.SOFTWARE);
		con.add(projectNode, DOAP.NAME, vf.createLiteral("OpenRDF Sesame"));
		con.add(projectNode, DCTERMS.TITLE, vf.createLiteral("OpenRDF Sesame"));
		con.add(projectNode, DOAP.HOMEPAGE, vf.createURI("http://www.openrdf.org/#sesame"));
		con.add(projectNode, DOAP.LICENSE,
				vf.createURI("https://bitbucket.org/openrdf/sesame/src/master/core/LICENSE.txt"));
		con.add(
				projectNode,
				DOAP.DESCRIPTION,
				vf.createLiteral("Sesame is an extensible Java framework for storing, querying and inferencing for RDF."));
		// Release date of Sesame-1.0
		con.add(projectNode, DOAP.CREATED, vf.createLiteral("2004-03-25", XMLSchema.DATE));
		con.add(projectNode, DOAP.PROGRAMMING_LANGUAGE, vf.createLiteral("Java"));
		con.add(projectNode, DOAP.IMPLEMENTS, vf.createURI("http://www.w3.org/TR/turtle/"));
		con.add(projectNode, DOAP.DOWNLOAD_PAGE, vf.createURI("http://sourceforge.net/projects/sesame/files/"));
		con.add(projectNode, DOAP.MAILING_LIST,
				vf.createURI("http://lists.sourceforge.net/lists/listinfo/sesame-general"));
		con.add(projectNode, DOAP.BUG_DATABASE, vf.createURI("https://openrdf.atlassian.net/browse/SES"));
		con.add(projectNode, DOAP.BLOG, vf.createURI("http://www.openrdf.org/news.jsp"));

		// TODO: Add other developers here
		URI ansellUri = vf.createURI("https://github.com/ansell");
		URI broekstraUri = vf.createURI("https://bitbucket.org/jeenbroekstra");

		con.add(projectNode, DOAP.DEVELOPER, ansellUri);
		con.add(projectNode, DOAP.DEVELOPER, broekstraUri);

		con.add(ansellUri, RDF.TYPE, EARL.ASSERTOR);
		con.add(ansellUri, RDF.TYPE, FOAF.PERSON);
		con.add(ansellUri, FOAF.NAME, vf.createLiteral("Peter Ansell"));
		con.add(broekstraUri, RDF.TYPE, EARL.ASSERTOR);
		con.add(broekstraUri, RDF.TYPE, FOAF.PERSON);
		con.add(broekstraUri, FOAF.NAME, vf.createLiteral("Jeen Broekstra"));

		con.add(projectNode, DOAP.RELEASE, releaseNode);
		con.add(releaseNode, RDF.TYPE, DOAP.VERSION);
		con.add(releaseNode, DOAP.NAME, vf.createLiteral("Sesame 2.7.0"));
		SimpleDateFormat xsdDataFormat = new SimpleDateFormat("yyyy-MM-dd");
		String currentDate = xsdDataFormat.format(new Date());
		con.add(releaseNode, DOAP.CREATED, vf.createLiteral(currentDate, XMLSchema.DATE));

		// Change this to whoever is running the tests
		asserterNode = ansellUri;

		TestResult testResult = new TestResult();
		EarlTestListener listener = new EarlTestListener();
		testResult.addListener(listener);

		logger.info("running Turtle tests..");
		TurtleParserTest.suite().run(testResult);

		logger.info("tests complete, generating EARL report...");

		con.commit();

		RDFWriterFactory factory = RDFWriterRegistry.getInstance().get(RDFFormat.TURTLE);
		File outFile = File.createTempFile("sesame-turtle-compliance",
				"." + RDFFormat.TURTLE.getDefaultFileExtension());
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
		System.out.println("EARL output written to " + outFile);
	}

	protected static class EarlTestListener implements TestListener {

		private int errorCount;

		private int failureCount;

		public void startTest(Test test) {
			errorCount = failureCount = 0;
		}

		public void endTest(Test test) {
			URI testURI = null;
			if (test instanceof TurtlePositiveParserTest) {
				testURI = ((TurtlePositiveParserTest)test).testUri;
			}
			else if (test instanceof TurtleNegativeParserTest) {
				testURI = ((TurtleNegativeParserTest)test).testUri;
			}
			else {
				throw new RuntimeException("Unexpected test type: " + test.getClass());
			}
			System.out.println("testURI: " + testURI.stringValue());
			try {
				BNode testNode = vf.createBNode();
				BNode resultNode = vf.createBNode();
				con.add(testNode, RDF.TYPE, EARL.ASSERTION);
				con.add(testNode, EARL.ASSERTEDBY, asserterNode);
				con.add(testNode, EARL.MODE, EARL.AUTOMATIC);
				con.add(testNode, EARL.SUBJECT, projectNode);
				con.add(testNode, EARL.TEST, testURI);
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
