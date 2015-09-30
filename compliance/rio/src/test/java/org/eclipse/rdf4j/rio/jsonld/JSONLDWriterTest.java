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
package org.eclipse.rdf4j.rio.jsonld;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RDFWriterTest;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.JSONLDMode;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.jsonld.JSONLDParserFactory;
import org.eclipse.rdf4j.rio.jsonld.JSONLDWriterFactory;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Peter Ansell
 */
public class JSONLDWriterTest extends RDFWriterTest {

	public JSONLDWriterTest() {
		super(new JSONLDWriterFactory(), new JSONLDParserFactory());
	}

	@Override
	protected void setupWriterConfig(WriterConfig config) {
		super.setupWriterConfig(config);
		config.set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
	}

	@Override
	protected void setupParserConfig(ParserConfig config) {
		super.setupParserConfig(config);
		config.set(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, true);
		config.set(BasicParserSettings.FAIL_ON_UNKNOWN_LANGUAGES, true);
	}

	@Test
	@Override
	@Ignore("TODO: Determine why this test is breaking")
	public void testIllegalPrefix()
		throws RDFHandlerException, RDFParseException, IOException
	{
	}

	@Test
	public void testRoundTripNamespaces()
		throws Exception
	{
		String exNs = "http://example.org/";
		IRI uri1 = vf.createIRI(exNs, "uri1");
		IRI uri2 = vf.createIRI(exNs, "uri2");
		Literal plainLit = vf.createLiteral("plain", XMLSchema.STRING);

		Statement st1 = vf.createStatement(uri1, uri2, plainLit);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
		rdfWriter.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
		rdfWriter.handleNamespace("ex", exNs);
		rdfWriter.startRDF();
		rdfWriter.handleStatement(st1);
		rdfWriter.endRDF();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		RDFParser rdfParser = rdfParserFactory.getParser();
		ParserConfig config = new ParserConfig();
		config.set(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, true);
		config.set(BasicParserSettings.FAIL_ON_UNKNOWN_LANGUAGES, true);
		rdfParser.setParserConfig(config);
		rdfParser.setValueFactory(vf);
		Model model = new LinkedHashModel();
		rdfParser.setRDFHandler(new StatementCollector(model));

		rdfParser.parse(in, "foo:bar");

		assertEquals("Unexpected number of statements, found " + model.size(), 1, model.size());

		assertTrue("missing namespaced statement", model.contains(st1));

		if (rdfParser.getRDFFormat().supportsNamespaces()) {
			assertTrue("Expected at least one namespace, found " + model.getNamespaces().size(),
					model.getNamespaces().size() >= 1);
			assertEquals(exNs, model.getNamespace("ex").get().getName());
		}
	}
}
