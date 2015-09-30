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
import org.openrdf.model.impl.SimpleValueFactory;
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
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.memory.MemoryStore;

public abstract class AbstractNQuadsWriterTest extends RDFWriterTest {

	private RDFParser parser;

	private RDFWriter writer;

	private ValueFactory vf;

	protected AbstractNQuadsWriterTest(RDFWriterFactory writerF, RDFParserFactory parserF) {
		super(writerF, parserF);
	}

	@Before
	public void setUp()
		throws Exception
	{
		parser = rdfParserFactory.getParser();
		vf = SimpleValueFactory.getInstance();
	}

	@After
	public void tearDown() {
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

		con1.add(
				ciaScheme,
				ciaScheme.toExternalForm(),
				Rio.getParserFormatForFileName(ciaScheme.toExternalForm()).orElseThrow(
						Rio.unsupportedFormat(ciaScheme.toExternalForm())));
		con1.add(
				ciaFacts,
				ciaFacts.toExternalForm(),
				Rio.getParserFormatForFileName(ciaFacts.toExternalForm()).orElseThrow(
						Rio.unsupportedFormat(ciaFacts.toExternalForm())));

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
		parser.parse(this.getClass().getResourceAsStream("/testcases/nquads/test2.nq"), "http://test.base.uri");

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
		Statement s1 = vf.createStatement(vf.createIRI("http://test.example.org/test/subject/1"),
				vf.createIRI("http://other.example.com/test/predicate/1"), vf.createLiteral("test literal"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writer = rdfWriterFactory.getWriter(baos);
		writer.startRDF();
		writer.handleStatement(s1);
		writer.endRDF();

		String content = baos.toString();
		String[] lines = content.split("\n");
		Assert.assertEquals("Unexpected number of lines.", 1, lines.length);
		Assert.assertEquals(
				"<http://test.example.org/test/subject/1> <http://other.example.com/test/predicate/1> \"test literal\" .",
				lines[0]);
	}

	@Test
	public void testNoContextAddXSDString()
		throws RDFHandlerException
	{
		Statement s1 = vf.createStatement(vf.createIRI("http://test.example.org/test/subject/1"),
				vf.createIRI("http://other.example.com/test/predicate/1"), vf.createLiteral("test literal"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writer = rdfWriterFactory.getWriter(baos);
		writer.getWriterConfig().set(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, false);
		writer.startRDF();
		writer.handleStatement(s1);
		writer.endRDF();

		String content = baos.toString();
		String[] lines = content.split("\n");
		Assert.assertEquals("Unexpected number of lines.", 1, lines.length);
		Assert.assertEquals(
				"<http://test.example.org/test/subject/1> <http://other.example.com/test/predicate/1> \"test literal\"^^<http://www.w3.org/2001/XMLSchema#string> .",
				lines[0]);
	}

	@Test
	public void testBlankNodeContext()
		throws RDFHandlerException
	{
		Statement s1 = vf.createStatement(vf.createIRI("http://test.example.org/test/subject/1"),
				vf.createIRI("http://other.example.com/test/predicate/1"), vf.createLiteral("test literal"),
				vf.createBNode());

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

	@Test
	public void testBlankNodeContextAddXSDString()
		throws RDFHandlerException
	{
		Statement s1 = vf.createStatement(vf.createIRI("http://test.example.org/test/subject/1"),
				vf.createIRI("http://other.example.com/test/predicate/1"), vf.createLiteral("test literal"),
				vf.createBNode());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writer = rdfWriterFactory.getWriter(baos);
		writer.getWriterConfig().set(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, false);
		writer.startRDF();
		writer.handleStatement(s1);
		writer.endRDF();

		String content = baos.toString();
		String[] lines = content.split("\n");
		Assert.assertEquals("Unexpected number of lines.", 1, lines.length);
		Assert.assertTrue(lines[0].startsWith("<http://test.example.org/test/subject/1> <http://other.example.com/test/predicate/1> \"test literal\"^^<http://www.w3.org/2001/XMLSchema#string> _:"));
	}
}
