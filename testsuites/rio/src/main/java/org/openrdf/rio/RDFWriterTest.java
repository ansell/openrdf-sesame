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
package org.openrdf.rio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.EARL;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author Arjohn Kampman
 * @author Peter Ansell
 */
public abstract class RDFWriterTest {

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	/**
	 * One prng per testsuite run
	 */
	private static final Random prng = new SecureRandom();

	protected RDFWriterFactory rdfWriterFactory;

	protected RDFParserFactory rdfParserFactory;

	protected ValueFactory vf;

	private BNode bnode;

	private BNode bnodeEmpty;

	private BNode bnodeNumeric;

	private BNode bnodeDashes;

	private BNode bnodeSpecialChars;

	private URI uri1;

	private URI uri2;

	private Literal plainLit;

	private Literal dtLit;

	private Literal langLit;

	private Literal litWithNewlineAtEnd;

	private Literal litWithNewlineAtStart;

	private Literal litWithMultipleNewlines;

	private Literal litWithSingleQuotes;

	private Literal litWithDoubleQuotes;

	private String exNs;

	private List<Resource> potentialSubjects;

	private List<Value> potentialObjects;

	private List<URI> potentialPredicates;

	protected RDFWriterTest(RDFWriterFactory writerF, RDFParserFactory parserF) {
		rdfWriterFactory = writerF;
		rdfParserFactory = parserF;

		vf = new ValueFactoryImpl();

		exNs = "http://example.org/";

		bnode = vf.createBNode("anon");
		bnodeEmpty = vf.createBNode("");
		bnodeNumeric = vf.createBNode("123");
		bnodeDashes = vf.createBNode("a-b");
		bnodeSpecialChars = vf.createBNode("$%^&*()!@#$a-b<>?\"'[]{}|\\");
		uri1 = vf.createURI(exNs, "uri1");
		uri2 = vf.createURI(exNs, "uri2");
		plainLit = vf.createLiteral("plain");
		dtLit = vf.createLiteral(1);
		langLit = vf.createLiteral("test", "en");
		litWithNewlineAtEnd = vf.createLiteral("literal with newline at end\n");
		litWithNewlineAtStart = vf.createLiteral("\nliteral with newline at start");
		litWithMultipleNewlines = vf.createLiteral("\nliteral \nwith newline at start\n");
		litWithSingleQuotes = vf.createLiteral("'''some single quote text''' - abc");
		litWithDoubleQuotes = vf.createLiteral("\"\"\"some double quote text\"\"\" - abc");

		potentialSubjects = new ArrayList<Resource>();
		potentialSubjects.add(bnode);
		potentialSubjects.add(bnodeEmpty);
		potentialSubjects.add(bnodeNumeric);
		potentialSubjects.add(bnodeDashes);
		potentialSubjects.add(bnodeSpecialChars);
		potentialSubjects.add(uri1);
		potentialSubjects.add(uri2);
		for (int i = 0; i < 500; i++) {
			potentialSubjects.add(vf.createBNode());
		}
		for (int i = 0; i < 500; i++) {
			potentialSubjects.add(vf.createBNode("a" + Integer.toHexString(i)));
		}
		for (int i = 0; i < 500; i++) {
			potentialSubjects.add(vf.createURI(exNs + Integer.toHexString(i) + "/a"
					+ Integer.toOctalString(i % 133)));
		}
		Collections.shuffle(potentialSubjects, prng);

		potentialObjects = new ArrayList<Value>();
		potentialObjects.addAll(potentialSubjects);
		potentialObjects.add(plainLit);
		potentialObjects.add(dtLit);
		potentialObjects.add(langLit);
		// FIXME: SES-879: The following break the RDF/XML parser/writer
		// combination in terms of getting the same number of triples back as we
		// start with
		// potentialObjects.add(litWithNewlineAtEnd);
		// potentialObjects.add(litWithNewlineAtStart);
		// potentialObjects.add(litWithMultipleNewlines);
		potentialObjects.add(litWithSingleQuotes);
		potentialObjects.add(litWithDoubleQuotes);
		Collections.shuffle(potentialObjects, prng);

		potentialPredicates = new ArrayList<URI>();
		// In particular, the following fuzz tests the ability of the parser to
		// cater for rdf:type predicates with literal endings, in unknown
		// situations. All parsers/writers should preserve these statements, even
		// if they have shortcuts for URIs
		potentialPredicates.add(RDF.TYPE);
		potentialPredicates.add(RDF.NIL);
		potentialPredicates.add(RDF.FIRST);
		potentialPredicates.add(RDF.REST);
		potentialPredicates.add(SKOS.ALT_LABEL);
		potentialPredicates.add(SKOS.PREF_LABEL);
		potentialPredicates.add(SKOS.BROADER_TRANSITIVE);
		potentialPredicates.add(OWL.ONTOLOGY);
		potentialPredicates.add(OWL.ONEOF);
		potentialPredicates.add(DC.TITLE);
		potentialPredicates.add(DCTERMS.ACCESS_RIGHTS);
		potentialPredicates.add(FOAF.KNOWS);
		potentialPredicates.add(EARL.SUBJECT);
		potentialPredicates.add(RDFS.LABEL);
		potentialPredicates.add(SP.DEFAULT_PROPERTY);
		potentialPredicates.add(SP.TEXT_PROPERTY);
		potentialPredicates.add(SP.BIND_CLASS);
		potentialPredicates.add(SP.DOCUMENT_PROPERTY);
		potentialPredicates.add(SPIN.LABEL_TEMPLATE_PROPERTY);
		potentialPredicates.add(SESAME.DIRECTTYPE);
		Collections.shuffle(potentialPredicates, prng);
	}

	@Test
	public void testRoundTrip()
		throws RDFHandlerException, IOException, RDFParseException
	{
		Statement st1 = vf.createStatement(bnode, uri1, plainLit);
		Statement st2 = vf.createStatement(bnodeEmpty, uri1, plainLit);
		Statement st3 = vf.createStatement(bnodeNumeric, uri1, plainLit);
		Statement st4 = vf.createStatement(bnodeDashes, uri1, plainLit);
		Statement st5 = vf.createStatement(bnodeSpecialChars, uri1, plainLit);
		Statement st6 = vf.createStatement(uri2, uri1, bnode);
		Statement st7 = vf.createStatement(uri2, uri1, bnodeEmpty);
		Statement st8 = vf.createStatement(uri2, uri1, bnodeNumeric);
		Statement st9 = vf.createStatement(uri2, uri1, bnodeDashes);
		Statement st10 = vf.createStatement(uri2, uri1, bnodeSpecialChars);
		Statement st11 = vf.createStatement(uri1, uri2, langLit, uri2);
		Statement st12 = vf.createStatement(uri1, uri2, dtLit);
		Statement st13 = vf.createStatement(uri1, uri2, litWithNewlineAtEnd);
		Statement st14 = vf.createStatement(uri1, uri2, litWithNewlineAtStart);
		Statement st15 = vf.createStatement(uri1, uri2, litWithMultipleNewlines);
		Statement st16 = vf.createStatement(uri1, uri2, litWithSingleQuotes);
		Statement st17 = vf.createStatement(uri1, uri2, litWithDoubleQuotes);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
		rdfWriter.handleNamespace("ex", exNs);
		rdfWriter.startRDF();
		rdfWriter.handleStatement(st1);
		rdfWriter.handleStatement(st2);
		rdfWriter.handleStatement(st3);
		rdfWriter.handleStatement(st4);
		rdfWriter.handleStatement(st5);
		rdfWriter.handleStatement(st6);
		rdfWriter.handleStatement(st7);
		rdfWriter.handleStatement(st8);
		rdfWriter.handleStatement(st9);
		rdfWriter.handleStatement(st10);
		rdfWriter.handleStatement(st11);
		rdfWriter.handleStatement(st12);
		rdfWriter.handleStatement(st13);
		rdfWriter.handleStatement(st14);
		rdfWriter.handleStatement(st15);
		rdfWriter.handleStatement(st16);
		rdfWriter.handleStatement(st17);
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

		assertEquals("Unexpected number of statements", 17, model.size());

		if (rdfParser.getRDFFormat().supportsNamespaces()) {
			assertTrue(model.getNamespaces().size() >= 1);
			assertEquals(exNs, model.getNamespace("ex").getName());
		}

		// Test for four unique statements for blank nodes in subject position
		assertEquals(5, model.filter(null, uri1, plainLit).size());
		// Test for four unique statements for blank nodes in object position
		assertEquals(5, model.filter(uri2, uri1, null).size());
		if (rdfParser.getRDFFormat().supportsContexts()) {
			assertTrue("missing statement with language literal and context", model.contains(st11));
		}
		else {
			assertTrue("missing statement with language literal",
					model.contains(vf.createStatement(uri1, uri2, langLit)));
		}
		assertTrue("missing statement with datatype", model.contains(st12));
		if (rdfParser.getRDFFormat().equals(RDFFormat.RDFXML)) {
			System.out.println("FIXME: SES-879: RDFXML Parser does not preserve literals starting or ending in newline character");
		}
		else {
			assertTrue("missing statement with literal ending with newline", model.contains(st13));
			assertTrue("missing statement with literal starting with newline", model.contains(st14));
			assertTrue("missing statement with literal containing multiple newlines", model.contains(st15));
		}
		assertTrue("missing statement with single quotes", model.contains(st16));
		assertTrue("missing statement with double quotes", model.contains(st17));
	}

	@Test
	public void testPrefixRedefinition()
		throws RDFHandlerException, RDFParseException, IOException
	{
		String ns1 = "a:";
		String ns2 = "b:";
		String ns3 = "c:";

		URI uri1 = vf.createURI(ns1, "r1");
		URI uri2 = vf.createURI(ns2, "r2");
		URI uri3 = vf.createURI(ns3, "r3");
		Statement st = vf.createStatement(uri1, uri2, uri3);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
		rdfWriter.handleNamespace("", ns1);
		rdfWriter.handleNamespace("", ns2);
		rdfWriter.handleNamespace("", ns3);
		rdfWriter.startRDF();
		rdfWriter.handleStatement(st);
		rdfWriter.endRDF();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		RDFParser rdfParser = rdfParserFactory.getParser();
		rdfParser.setValueFactory(vf);
		StatementCollector stCollector = new StatementCollector();
		rdfParser.setRDFHandler(stCollector);

		rdfParser.parse(in, "foo:bar");

		Collection<Statement> statements = stCollector.getStatements();
		assertEquals("Unexpected number of statements", 1, statements.size());

		Statement parsedSt = statements.iterator().next();
		assertEquals("Written and parsed statements are not equal", st, parsedSt);
	}

	@Test
	public void testIllegalPrefix()
		throws RDFHandlerException, RDFParseException, IOException
	{
		String ns1 = "a:";
		String ns2 = "b:";
		String ns3 = "c:";

		URI uri1 = vf.createURI(ns1, "r1");
		URI uri2 = vf.createURI(ns2, "r2");
		URI uri3 = vf.createURI(ns3, "r3");
		Statement st = vf.createStatement(uri1, uri2, uri3);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
		rdfWriter.handleNamespace("1", ns1);
		rdfWriter.handleNamespace("_", ns2);
		rdfWriter.handleNamespace("a%", ns3);
		rdfWriter.startRDF();
		rdfWriter.handleStatement(st);
		rdfWriter.endRDF();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		RDFParser rdfParser = rdfParserFactory.getParser();
		rdfParser.setValueFactory(vf);
		StatementCollector stCollector = new StatementCollector();
		rdfParser.setRDFHandler(stCollector);

		rdfParser.parse(in, "foo:bar");

		Collection<Statement> statements = stCollector.getStatements();
		assertEquals("Unexpected number of statements", 1, statements.size());

		Statement parsedSt = statements.iterator().next();
		assertEquals("Written and parsed statements are not equal", st, parsedSt);
	}

	@Test
	public void testDefaultNamespace()
		throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
		rdfWriter.handleNamespace("", RDF.NAMESPACE);
		rdfWriter.handleNamespace("rdf", RDF.NAMESPACE);
		rdfWriter.startRDF();
		rdfWriter.handleStatement(vf.createStatement(vf.createURI(RDF.NAMESPACE), RDF.TYPE, OWL.ONTOLOGY));
		rdfWriter.endRDF();
	}

	/**
	 * Fuzz and performance test designed to find cases where parsers and/or
	 * writers are incompatible with each other.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPerformance()
		throws Exception
	{
		Model model = new LinkedHashModel();

		for (int i = 0; i < 200000; i++) {
			model.add(potentialSubjects.get(prng.nextInt(potentialSubjects.size())),
					potentialPredicates.get(prng.nextInt(potentialPredicates.size())),
					potentialObjects.get(prng.nextInt(potentialObjects.size())));
		}
		System.out.println("Test class: " + this.getClass().getName());
		System.out.println("Test statements size: " + model.size() + " (" + rdfWriterFactory.getRDFFormat()
				+ ")");
		assertFalse("Did not generate any test statements", model.isEmpty());

		File testFile = tempDir.newFile("performancetest"
				+ rdfWriterFactory.getRDFFormat().getDefaultFileExtension());

		FileOutputStream out = new FileOutputStream(testFile);
		try {
			long startWrite = System.currentTimeMillis();
			RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
			// Test prefixed URIs for only some of the URIs available
			rdfWriter.handleNamespace(RDF.PREFIX, RDF.NAMESPACE);
			rdfWriter.handleNamespace(SKOS.PREFIX, SKOS.NAMESPACE);
			rdfWriter.handleNamespace(FOAF.PREFIX, FOAF.NAMESPACE);
			rdfWriter.handleNamespace(EARL.PREFIX, EARL.NAMESPACE);
			rdfWriter.handleNamespace("ex", exNs);
			rdfWriter.startRDF();

			for (Statement nextSt : model) {
				rdfWriter.handleStatement(nextSt);
			}

			rdfWriter.endRDF();
			long endWrite = System.currentTimeMillis();
			System.out.println("Write took: " + (endWrite - startWrite) + " ms ("
					+ rdfWriterFactory.getRDFFormat() + ")");
		}
		finally {
			out.close();
		}

		FileInputStream in = new FileInputStream(testFile);
		try {
			RDFParser rdfParser = rdfParserFactory.getParser();
			ParserConfig config = new ParserConfig();
			config.set(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, true);
			config.set(BasicParserSettings.FAIL_ON_UNKNOWN_LANGUAGES, true);
			rdfParser.setParserConfig(config);
			rdfParser.setValueFactory(vf);
			Model parsedModel = new LinkedHashModel();
			rdfParser.setRDFHandler(new StatementCollector(parsedModel));
			long startParse = System.currentTimeMillis();
			rdfParser.parse(in, "foo:bar");
			long endParse = System.currentTimeMillis();
			System.out.println("Parse took: " + (endParse - startParse) + " ms ("
					+ rdfParserFactory.getRDFFormat() + ")");

			assertEquals("Unexpected number of statements", model.size(), parsedModel.size());

			if (rdfParser.getRDFFormat().supportsNamespaces()) {
				assertTrue(parsedModel.getNamespaces().size() >= 5);
				assertEquals(exNs, parsedModel.getNamespace("ex").getName());
			}
		}
		finally {
			in.close();
		}
	}
}
