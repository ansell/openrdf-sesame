/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class EarlReport {

	protected static Repository earlRepository;

	protected static ValueFactory vf;

	protected static RepositoryConnection con;

	protected static Resource projectNode;

	protected static Resource asserterNode;

	public static void main(String[] args)
		throws Exception
	{
		earlRepository = new SailRepository(new MemoryStore());
		earlRepository.initialize();
		con = earlRepository.getConnection();
		vf = con.getValueFactory();
		con.begin();

		con.setNamespace("rdf", RDF.NAMESPACE);
		con.setNamespace("xsd", XMLSchema.NAMESPACE);
		con.setNamespace("doap", DOAP.NAMESPACE);
		con.setNamespace("earl", EARL.NAMESPACE);
		con.setNamespace("dc", DC.NAMESPACE);

		projectNode = vf.createBNode();
		BNode releaseNode = vf.createBNode();
		con.add(projectNode, RDF.TYPE, DOAP.PROJECT);
		con.add(projectNode, DOAP.NAME, vf.createLiteral("OpenRDF Sesame"));
		con.add(projectNode, DOAP.RELEASE, releaseNode);
		con.add(releaseNode, RDF.TYPE, DOAP.VERSION);
		con.add(releaseNode, DOAP.NAME, vf.createLiteral("Sesame 2.0-SNAPSHOT"));
		SimpleDateFormat xsdDataFormat = new SimpleDateFormat("yyyy-MM-dd");
		String currentDate = xsdDataFormat.format(new Date());
		con.add(releaseNode, DOAP.CREATED, vf.createLiteral(currentDate, XMLSchema.DATE));

		asserterNode = vf.createBNode();
		con.add(asserterNode, RDF.TYPE, EARL.SOFTWARE);
		con.add(asserterNode, DC.TITLE, vf.createLiteral("OpenRDF SPARQL compliance test"));

		TestResult testResult = new TestResult();
		EarlTestListener listener = new EarlTestListener();
		testResult.addListener(listener);

		MemorySPARQLQueryTest.suite().run(testResult);
		SPARQLSyntaxTest.suite().run(testResult);

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

		System.out.println("EARL output written to " + outFile);
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
			else if (test instanceof SPARQLSyntaxTest) {
				testURI = ((SPARQLSyntaxTest)test).testURI;
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
				con.add(testNode, EARL.TEST, vf.createURI(testURI));
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
			catch (StoreException e) {
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
