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
package org.openrdf.query.resultio.sparqljson;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.Test;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.resultio.AbstractQueryResultIOTupleTest;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.helpers.QueryResultCollector;

/**
 * @author Peter Ansell
 * @author Sebastian Schaffert
 */
public class SPARQLJSONTupleTest extends AbstractQueryResultIOTupleTest {

	@Override
	protected String getFileName() {
		return "test.srj";
	}

	@Override
	protected TupleQueryResultFormat getTupleFormat() {
		return TupleQueryResultFormat.JSON;
	}

	@Override
	protected BooleanQueryResultFormat getMatchingBooleanFormatOrNull() {
		return BooleanQueryResultFormat.JSON;
	}

	@Test
	public void testBindings1()
		throws Exception
	{
		SPARQLResultsJSONParser parser = new SPARQLResultsJSONParser(SimpleValueFactory.getInstance());
		QueryResultCollector handler = new QueryResultCollector();
		parser.setQueryResultHandler(handler);

		InputStream stream = this.getClass().getResourceAsStream("/sparqljson/bindings1.srj");
		assertNotNull("Could not find test resource", stream);
		parser.parseQueryResult(stream);

		// there must be two variables
		assertEquals(2, handler.getBindingNames().size());

		// first must be called "book", the second "title"
		assertEquals("book", handler.getBindingNames().get(0));
		assertEquals("title", handler.getBindingNames().get(1));

		// should be 7 solutions alltogether
		assertEquals(7, handler.getBindingSets().size());

		// Results are ordered, so first should be book6
		assertEquals("http://example.org/book/book6",
				handler.getBindingSets().get(0).getValue("book").stringValue());

		for (BindingSet b : handler.getBindingSets()) {
			assertNotNull(b.getValue("book"));
			assertNotNull(b.getValue("title"));
			assertTrue(b.getValue("book") instanceof IRI);
			assertTrue(b.getValue("title") instanceof Literal);

			IRI book = (IRI)b.getValue("book");
			if (book.stringValue().equals("http://example.org/book/book6")) {
				assertEquals("Harry Potter and the Half-Blood Prince", b.getValue("title").stringValue());
			}
			else if (book.stringValue().equals("http://example.org/book/book7")) {
				assertEquals("Harry Potter and the Deathly Hallows", b.getValue("title").stringValue());
			}
			else if (book.stringValue().equals("http://example.org/book/book5")) {
				assertEquals("Harry Potter and the Order of the Phoenix", b.getValue("title").stringValue());
			}
			else if (book.stringValue().equals("http://example.org/book/book4")) {
				assertEquals("Harry Potter and the Goblet of Fire", b.getValue("title").stringValue());
			}
			else if (book.stringValue().equals("http://example.org/book/book2")) {
				assertEquals("Harry Potter and the Chamber of Secrets", b.getValue("title").stringValue());
			}
			else if (book.stringValue().equals("http://example.org/book/book3")) {
				assertEquals("Harry Potter and the Prisoner Of Azkaban", b.getValue("title").stringValue());
			}
			else if (book.stringValue().equals("http://example.org/book/book1")) {
				assertEquals("Harry Potter and the Philosopher's Stone", b.getValue("title").stringValue());
			}
			else {
				fail("Found unexpected binding set in result: " + b.toString());
			}
		}

	}

	@Test
	public void testBindings2()
		throws Exception
	{
		SPARQLResultsJSONParser parser = new SPARQLResultsJSONParser(SimpleValueFactory.getInstance());
		QueryResultCollector handler = new QueryResultCollector();
		parser.setQueryResultHandler(handler);

		InputStream stream = this.getClass().getResourceAsStream("/sparqljson/bindings2.srj");
		assertNotNull("Could not find test resource", stream);
		parser.parseQueryResult(stream);

		// there must be 7 variables
		assertEquals(7, handler.getBindingNames().size());

		// first must be called "x", etc.,
		assertEquals("x", handler.getBindingNames().get(0));
		assertEquals("hpage", handler.getBindingNames().get(1));
		assertEquals("name", handler.getBindingNames().get(2));
		assertEquals("mbox", handler.getBindingNames().get(3));
		assertEquals("age", handler.getBindingNames().get(4));
		assertEquals("blurb", handler.getBindingNames().get(5));
		assertEquals("friend", handler.getBindingNames().get(6));

		// 2 results
		assertEquals(2, handler.getBindingSets().size());

		// Results are ordered, so first should be alice
		assertEquals("http://work.example.org/alice/",
				handler.getBindingSets().get(0).getValue("hpage").stringValue());

		for (BindingSet b : handler.getBindingSets()) {

			assertNotNull(b.getValue("x"));
			assertNotNull(b.getValue("hpage"));
			assertNotNull(b.getValue("name"));
			assertNotNull(b.getValue("mbox"));
			assertNotNull(b.getValue("friend"));

			assertTrue(b.getValue("x") instanceof BNode);
			assertTrue(b.getValue("hpage") instanceof IRI);
			assertTrue(b.getValue("name") instanceof Literal);
			assertTrue(b.getValue("friend") instanceof BNode);

			BNode value = (BNode)b.getValue("x");

			if (value.getID().equals("r1")) {
				assertNotNull(b.getValue("blurb"));

				assertTrue(b.getValue("mbox") instanceof Literal);
				assertTrue(b.getValue("blurb") instanceof Literal);

				assertEquals("http://work.example.org/alice/", b.getValue("hpage").stringValue());

				Literal name = (Literal)b.getValue("name");
				assertEquals("Alice", name.stringValue());
				assertNull(name.getLanguage());
				assertEquals(XMLSchema.STRING, name.getDatatype());

				Literal mbox = (Literal)b.getValue("mbox");
				assertEquals("", mbox.stringValue());
				assertNull(mbox.getLanguage());
				assertEquals(XMLSchema.STRING, mbox.getDatatype());

				Literal blurb = (Literal)b.getValue("blurb");
				assertEquals("<p xmlns=\"http://www.w3.org/1999/xhtml\">My name is <b>alice</b></p>",
						blurb.stringValue());
				assertNull(blurb.getLanguage());
				assertEquals(RDF.XMLLITERAL, blurb.getDatatype());
			}
			else if (value.getID().equals("r2")) {
				assertNull(b.getValue("blurb"));

				assertTrue(b.getValue("mbox") instanceof IRI);

				assertEquals("http://work.example.org/bob/", b.getValue("hpage").stringValue());

				Literal name = (Literal)b.getValue("name");
				assertEquals("Bob", name.stringValue());
				assertEquals("en", name.getLanguage());
				assertEquals(RDF.LANGSTRING, name.getDatatype());

				assertEquals("mailto:bob@work.example.org", b.getValue("mbox").stringValue());
			}
			else {
				fail("Found unexpected binding set in result: " + b.toString());
			}
		}

		assertEquals(1, handler.getLinks().size());
		assertEquals("http://www.w3.org/TR/2013/REC-sparql11-results-json-20130321/#example",
				handler.getLinks().get(0));

	}

	@Test
	public void testNonStandardDistinct()
		throws Exception
	{
		SPARQLResultsJSONParser parser = new SPARQLResultsJSONParser(SimpleValueFactory.getInstance());
		QueryResultCollector handler = new QueryResultCollector();
		parser.setQueryResultHandler(handler);

		InputStream stream = this.getClass().getResourceAsStream("/sparqljson/non-standard-distinct.srj");
		assertNotNull("Could not find test resource", stream);
		parser.parseQueryResult(stream);

		// there must be 1 variable
		assertEquals(1, handler.getBindingNames().size());

		// first must be called "Concept", etc.,
		assertEquals("Concept", handler.getBindingNames().get(0));

		// -1 results
		assertEquals(100, handler.getBindingSets().size());
	}

	@Test
	public void testNonStandardOrdered()
		throws Exception
	{
		SPARQLResultsJSONParser parser = new SPARQLResultsJSONParser(SimpleValueFactory.getInstance());
		QueryResultCollector handler = new QueryResultCollector();
		parser.setQueryResultHandler(handler);

		InputStream stream = this.getClass().getResourceAsStream("/sparqljson/non-standard-ordered.srj");
		assertNotNull("Could not find test resource", stream);
		parser.parseQueryResult(stream);

		// there must be 1 variable
		assertEquals(1, handler.getBindingNames().size());

		// first must be called "Concept", etc.,
		assertEquals("Concept", handler.getBindingNames().get(0));

		// -1 results
		assertEquals(100, handler.getBindingSets().size());
	}

	@Test
	public void testNonStandardDistinctOrdered()
		throws Exception
	{
		SPARQLResultsJSONParser parser = new SPARQLResultsJSONParser(SimpleValueFactory.getInstance());
		QueryResultCollector handler = new QueryResultCollector();
		parser.setQueryResultHandler(handler);

		InputStream stream = this.getClass().getResourceAsStream(
				"/sparqljson/non-standard-distinct-ordered.srj");
		assertNotNull("Could not find test resource", stream);
		parser.parseQueryResult(stream);

		// there must be 1 variable
		assertEquals(1, handler.getBindingNames().size());

		// first must be called "Concept", etc.,
		assertEquals("Concept", handler.getBindingNames().get(0));

		// -1 results
		assertEquals(100, handler.getBindingSets().size());
	}
}
