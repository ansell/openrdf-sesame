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
package org.openrdf.rio.rdfxml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import org.openrdf.model.Resource;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.IRIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriterFactory;

public class RDFXMLPrettyWriterTest extends RDFXMLWriterTestCase {

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

		Resource res = new IRIImpl("http://example.com/#");

		rdfWriter.handleStatement(new StatementImpl(res, RDF.TYPE, RDF.BAG));

		rdfWriter.handleStatement(new StatementImpl(res, new IRIImpl(RDF.NAMESPACE + "_1"), new IRIImpl(
				"http://example.com/#1")));
		rdfWriter.handleStatement(new StatementImpl(res, new IRIImpl(RDF.NAMESPACE + "_2"), new IRIImpl(
				"http://example.com/#2")));
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

		Resource res = new IRIImpl("http://example.com/#");

		rdfWriter.handleStatement(new StatementImpl(res, RDF.TYPE, RDF.BAG));

		rdfWriter.handleStatement(new StatementImpl(res, new IRIImpl(RDF.NAMESPACE + "_0"), new IRIImpl(
				"http://example.com/#0")));
		rdfWriter.handleStatement(new StatementImpl(res, new IRIImpl(RDF.NAMESPACE + "_2"), new IRIImpl(
				"http://example.com/#2")));
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

		Resource res = new IRIImpl("http://example.com/#");

		rdfWriter.handleStatement(new StatementImpl(res, RDF.TYPE, RDF.BAG));

		rdfWriter.handleStatement(new StatementImpl(res, new IRIImpl(RDF.NAMESPACE + "_2"), new IRIImpl(
				"http://example.com/#2")));
		rdfWriter.handleStatement(new StatementImpl(res, new IRIImpl(RDF.NAMESPACE + "_1"), new IRIImpl(
				"http://example.com/#1")));
		rdfWriter.handleStatement(new StatementImpl(res, new IRIImpl(RDF.NAMESPACE + "_3"), new IRIImpl(
				"http://example.com/#3")));
		rdfWriter.handleStatement(new StatementImpl(res, new IRIImpl(RDF.NAMESPACE + "_2"), new IRIImpl(
				"http://example.com/#2")));
		rdfWriter.endRDF();

		List<String> rdfLines = rdfOpenTags(writer.toString());

		assertEquals(Arrays.asList("<rdf:RDF", "<rdf:Bag", "<rdf:_2", "<rdf:li", "<rdf:_3", "<rdf:li"),
				rdfLines);
	}
}
