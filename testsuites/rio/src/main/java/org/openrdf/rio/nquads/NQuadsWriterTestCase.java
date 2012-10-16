/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.nquads;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.RepositoryUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterTest;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.memory.MemoryStore;

public abstract class NQuadsWriterTestCase extends RDFWriterTest {

	private RDFParser parser;
	private RDFWriter writer;
   private ValueFactory vf;
	
	protected NQuadsWriterTestCase(RDFWriterFactory writerF, RDFParserFactory parserF) {
		super(writerF, parserF);
	}

	@Before
	public void setUp() 
			throws Exception 
	{
      parser = rdfParserFactory.getParser();
      vf = ValueFactoryImpl.getInstance();
	}
	
   @After
   public void tearDown() 
   {
   	 parser = null;
       writer = null;
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

		con2.add(new StringReader(writer.toString()), "foo:bar", RDFFormat.NQUADS);
		con2.close();

		Assert.assertTrue("result of serialization and re-upload should be equal to original",
				RepositoryUtil.equals(rep1, rep2));
	}

   @Test
   public void testReadWrite() 
   		throws RDFHandlerException, IOException, RDFParseException 
   {
		StatementCollector statementCollector = new StatementCollector();
		parser.setRDFHandler(statementCollector);
		parser.parse(this.getClass().getClassLoader().getResourceAsStream("testcases/nquads/test2.nq"),
				"http://test.base.uri");

		Assert.assertEquals(400, statementCollector.getStatements().size());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writer = rdfWriterFactory.getWriter(baos);
		writer.startRDF();
		for (Statement nextStatement : statementCollector.getStatements()) {
			writer.handleStatement(nextStatement);
		}
		writer.endRDF();

		Assert.assertEquals("Unexpected number of lines.", 400, baos.toString().split("\n").length);
   }
   
   @Test
   public void testNoContext() 
   		throws RDFHandlerException 
   {
       Statement s1 = vf.createStatement(vf.createURI("http://test.example.org/test/subject/1"), vf.createURI("http://other.example.com/test/predicate/1"), vf.createLiteral("test literal"));
       
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       writer = rdfWriterFactory.getWriter(baos);
       writer.startRDF();
       writer.handleStatement(s1);
       writer.endRDF();
       
       String content = baos.toString();
       String[] lines = content.split("\n");
       Assert.assertEquals("Unexpected number of lines.", 1, lines.length);
       Assert.assertEquals("<http://test.example.org/test/subject/1> <http://other.example.com/test/predicate/1> \"test literal\" .", lines[0] );
   }
   
   @Test
   public void testBlankNodeContext() 
   		throws RDFHandlerException
   {
       Statement s1 = vf.createStatement(vf.createURI("http://test.example.org/test/subject/1"), vf.createURI("http://other.example.com/test/predicate/1"), vf.createLiteral("test literal"), vf.createBNode());
       
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       writer = rdfWriterFactory.getWriter(baos);
       writer.startRDF();
       writer.handleStatement(s1);
       writer.endRDF();
       
       String content = baos.toString();
       String[] lines = content.split("\n");
       Assert.assertEquals("Unexpected number of lines.", 1, lines.length);
       Assert.assertTrue(lines[0].startsWith("<http://test.example.org/test/subject/1> <http://other.example.com/test/predicate/1> \"test literal\" _:"));
   }	
}
