/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.rdfxml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.RepositoryUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterTest;
import org.openrdf.sail.memory.MemoryStore;

public class RDFXMLWriterTest extends RDFWriterTest {

	public RDFXMLWriterTest() {
		this(new RDFXMLWriterFactory(), new RDFXMLParserFactory());
	}

	protected RDFXMLWriterTest(RDFWriterFactory writerF, RDFParserFactory parserF) {
		super(writerF, parserF);
	}

	public void testWrite()
		throws RepositoryException, RDFParseException, IOException, RDFHandlerException
	{
		Repository rep1 = new SailRepository(new MemoryStore());
		rep1.initialize();

		RepositoryConnection con1 = rep1.getConnection();

		URL ciaScheme = this.getClass().getResource("/cia-factbook/CIA-onto-enhanced.rdf");
		URL ciaFacts = this.getClass().getResource("/cia-factbook/CIA-facts-enhanced.rdf");

		con1.add(ciaScheme, ciaScheme.toExternalForm(), RDFFormat.forFileName(ciaScheme.toExternalForm()));
		con1.add(ciaFacts, ciaFacts.toExternalForm(), RDFFormat.forFileName(ciaFacts.toExternalForm()));

		StringWriter writer = new StringWriter();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(writer);
		con1.export(rdfWriter);

		con1.close();

		Repository rep2 = new SailRepository(new MemoryStore());
		rep2.initialize();

		RepositoryConnection con2 = rep2.getConnection();

		con2.add(new StringReader(writer.toString()), "foo:bar", RDFFormat.RDFXML);
		con2.close();

		assertTrue("result of serialization and re-upload should be equal to original", RepositoryUtil.equals(
				rep1, rep2));
	}
}
