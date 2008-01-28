/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;

import junit.framework.TestCase;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author Arjohn Kampman
 */
public abstract class RDFWriterTest extends TestCase {

	protected RDFWriterFactory rdfWriterFactory;

	protected RDFParserFactory rdfParserFactory;

	protected RDFWriterTest(RDFWriterFactory writerF, RDFParserFactory parserF) {
		rdfWriterFactory = writerF;
		rdfParserFactory = parserF;
	}

	public void testPrefixRedefinition()
		throws RDFHandlerException, RDFParseException, IOException
	{
		String ns1 = "a:";
		String ns2 = "b:";
		String ns3 = "c:";

		ValueFactory vf = new ValueFactoryImpl();
		URI uri1 = vf.createURI(ns1, "r1");
		URI uri2 = vf.createURI(ns2, "r2");
		URI uri3 = vf.createURI(ns3, "r3");
		Statement st = vf.createStatement(uri1, uri2, uri3);

		StringWriter writer = new StringWriter();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(writer);
		rdfWriter.handleNamespace("", ns1);
		rdfWriter.handleNamespace("", ns2);
		rdfWriter.handleNamespace("", ns3);
		rdfWriter.startRDF();
		rdfWriter.handleStatement(st);
		rdfWriter.endRDF();

		StringReader reader = new StringReader(writer.toString());
		RDFParser rdfParser = rdfParserFactory.getParser();
		rdfParser.setValueFactory(vf);
		StatementCollector stCollector = new StatementCollector();
		rdfParser.setRDFHandler(stCollector);

		try {
			rdfParser.parse(reader, "foo:bar");
		}
		catch (RDFParseException e) {
			System.err.println("Failed to parse generated RDF document:");
			System.err.println(writer.toString());
			throw e;
		}

		Collection<Statement> statements = stCollector.getStatements();
		assertEquals("Unexpected number of statements", 1, statements.size());

		Statement parsedSt = statements.iterator().next();
		assertEquals("Written and parsed statements are not equal", st, parsedSt);
	}

	public void testIllegalPrefix()
		throws RDFHandlerException, RDFParseException, IOException
	{
		String ns1 = "a:";
		String ns2 = "b:";
		String ns3 = "c:";

		ValueFactory vf = new ValueFactoryImpl();
		URI uri1 = vf.createURI(ns1, "r1");
		URI uri2 = vf.createURI(ns2, "r2");
		URI uri3 = vf.createURI(ns3, "r3");
		Statement st = vf.createStatement(uri1, uri2, uri3);

		StringWriter writer = new StringWriter();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(writer);
		rdfWriter.handleNamespace("1", ns1);
		rdfWriter.handleNamespace("_", ns2);
		rdfWriter.handleNamespace("a%", ns3);
		rdfWriter.startRDF();
		rdfWriter.handleStatement(st);
		rdfWriter.endRDF();

		StringReader reader = new StringReader(writer.toString());
		RDFParser rdfParser = rdfParserFactory.getParser();
		rdfParser.setValueFactory(vf);
		StatementCollector stCollector = new StatementCollector();
		rdfParser.setRDFHandler(stCollector);

		try {
			rdfParser.parse(reader, "foo:bar");
		}
		catch (RDFParseException e) {
			System.err.println("Failed to parse generated RDF document:");
			System.err.println(writer.toString());
			throw e;
		}

		Collection<Statement> statements = stCollector.getStatements();
		assertEquals("Unexpected number of statements", 1, statements.size());

		Statement parsedSt = statements.iterator().next();
		assertEquals("Written and parsed statements are not equal", st, parsedSt);
	}
}
