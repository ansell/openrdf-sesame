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
package org.eclipse.rdf4j.rio.rdfxml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParserFactory;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriterTestCase;
import org.eclipse.rdf4j.rio.rdfxml.util.RDFXMLPrettyWriterFactory;
import org.junit.Test;

public class RDFXMLPrettyWriterTest extends RDFXMLWriterTestCase {

	private static ValueFactory vf = SimpleValueFactory.getInstance();

	public RDFXMLPrettyWriterTest() {
		super(new RDFXMLPrettyWriterFactory(), new RDFXMLParserFactory());
	}

	/**
	 * Extract lines that start an rdf element so basic assertions can be made.
	 */
	private static List<String> rdfOpenTags(String s)
		throws IOException
	{
		String withoutSpaces = Pattern.compile("^\\s+", Pattern.MULTILINE).matcher(s).replaceAll("");

		List<String> rdfLines = new ArrayList<String>();

		for (String l : IOUtils.readLines(new StringReader(withoutSpaces))) {
			if (l.startsWith("<rdf:")) {
				rdfLines.add(l.replaceAll(" .*", ""));
			}
		}

		return rdfLines;
	}

	@Test
	public void sequenceItemsAreAbbreviated()
		throws RDFHandlerException, IOException
	{
		StringWriter writer = new StringWriter();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(writer);

		rdfWriter.startRDF();

		Resource res = vf.createIRI("http://example.com/#");

		rdfWriter.handleStatement(vf.createStatement(res, RDF.TYPE, RDF.BAG));

		rdfWriter.handleStatement(vf.createStatement(res, vf.createIRI(RDF.NAMESPACE + "_1"),
				vf.createIRI("http://example.com/#1")));
		rdfWriter.handleStatement(vf.createStatement(res, vf.createIRI(RDF.NAMESPACE + "_2"),
				vf.createIRI("http://example.com/#2")));
		rdfWriter.endRDF();

		List<String> rdfLines = rdfOpenTags(writer.toString());

		assertEquals(Arrays.asList("<rdf:RDF", "<rdf:Bag", "<rdf:li", "<rdf:li"), rdfLines);
	}

	@Test
	public void outOfSequenceItemsAreNotAbbreviated()
		throws RDFHandlerException, IOException
	{
		StringWriter writer = new StringWriter();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(writer);

		rdfWriter.startRDF();

		Resource res = vf.createIRI("http://example.com/#");

		rdfWriter.handleStatement(vf.createStatement(res, RDF.TYPE, RDF.BAG));

		rdfWriter.handleStatement(vf.createStatement(res, vf.createIRI(RDF.NAMESPACE + "_0"),
				vf.createIRI("http://example.com/#0")));
		rdfWriter.handleStatement(vf.createStatement(res, vf.createIRI(RDF.NAMESPACE + "_2"),
				vf.createIRI("http://example.com/#2")));
		rdfWriter.endRDF();

		List<String> rdfLines = rdfOpenTags(writer.toString());

		assertEquals(Arrays.asList("<rdf:RDF", "<rdf:Bag", "<rdf:_0", "<rdf:_2"), rdfLines);
	}

	@Test
	public void inSequenceItemsMixedWithOtherElementsAreAbbreviated()
		throws RDFHandlerException, IOException
	{
		StringWriter writer = new StringWriter();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(writer);

		rdfWriter.startRDF();

		Resource res = vf.createIRI("http://example.com/#");

		rdfWriter.handleStatement(vf.createStatement(res, RDF.TYPE, RDF.BAG));

		rdfWriter.handleStatement(vf.createStatement(res, vf.createIRI(RDF.NAMESPACE + "_2"),
				vf.createIRI("http://example.com/#2")));
		rdfWriter.handleStatement(vf.createStatement(res, vf.createIRI(RDF.NAMESPACE + "_1"),
				vf.createIRI("http://example.com/#1")));
		rdfWriter.handleStatement(vf.createStatement(res, vf.createIRI(RDF.NAMESPACE + "_3"),
				vf.createIRI("http://example.com/#3")));
		rdfWriter.handleStatement(vf.createStatement(res, vf.createIRI(RDF.NAMESPACE + "_2"),
				vf.createIRI("http://example.com/#2")));
		rdfWriter.endRDF();

		List<String> rdfLines = rdfOpenTags(writer.toString());

		assertEquals(Arrays.asList("<rdf:RDF", "<rdf:Bag", "<rdf:_2", "<rdf:li", "<rdf:_3", "<rdf:li"),
				rdfLines);
	}
}
