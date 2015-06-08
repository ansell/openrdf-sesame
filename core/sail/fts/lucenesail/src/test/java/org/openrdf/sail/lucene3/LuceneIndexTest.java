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
package org.openrdf.sail.lucene3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.impl.ContextStatement;
import org.openrdf.model.impl.SimpleLiteral;
import org.openrdf.model.impl.SimpleStatement;
import org.openrdf.model.impl.SimpleIRI;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.lucene.LuceneSail;
import org.openrdf.sail.lucene.SearchFields;
import org.openrdf.sail.memory.MemoryStore;

public class LuceneIndexTest {

	public static final IRI CONTEXT_1 = new SimpleIRI("urn:context1");

	public static final IRI CONTEXT_2 = new SimpleIRI("urn:context2");

	public static final IRI CONTEXT_3 = new SimpleIRI("urn:context3");

	// create some objects that we will use throughout this test
	IRI subject = new SimpleIRI("urn:subj");

	IRI subject2 = new SimpleIRI("urn:subj2");

	IRI predicate1 = new SimpleIRI("urn:pred1");

	IRI predicate2 = new SimpleIRI("urn:pred2");

	Literal object1 = new SimpleLiteral("object1");

	Literal object2 = new SimpleLiteral("object2");

	Literal object3 = new SimpleLiteral("cats");

	Literal object4 = new SimpleLiteral("dogs");

	Literal object5 = new SimpleLiteral("chicken");

	Statement statement11 = new SimpleStatement(subject, predicate1, object1);

	Statement statement12 = new SimpleStatement(subject, predicate2, object2);

	Statement statement21 = new SimpleStatement(subject2, predicate1, object3);

	Statement statement22 = new SimpleStatement(subject2, predicate2, object4);

	Statement statement23 = new SimpleStatement(subject2, predicate2, object5);

	ContextStatement statementContext111 = new ContextStatement(subject, predicate1, object1,
			CONTEXT_1);

	ContextStatement statementContext121 = new ContextStatement(subject, predicate2, object2,
			CONTEXT_1);

	ContextStatement statementContext211 = new ContextStatement(subject2, predicate1, object3,
			CONTEXT_1);

	ContextStatement statementContext222 = new ContextStatement(subject2, predicate2, object4,
			CONTEXT_2);

	ContextStatement statementContext232 = new ContextStatement(subject2, predicate2, object5,
			CONTEXT_2);

	// add a statement to an index
	RAMDirectory directory;

	StandardAnalyzer analyzer;

	LuceneIndex index;

	@Before
	public void setUp()
		throws Exception
	{
		directory = new RAMDirectory();
		analyzer = new StandardAnalyzer(Version.LUCENE_35);
		index = new LuceneIndex(directory, analyzer);
	}

	@After
	public void tearDown()
		throws Exception
	{
		index.shutDown();
	}

	@Test
	public void testAddStatement()
		throws IOException, ParseException
	{
		// add a statement to an index
		index.begin();
		index.addStatement(statement11);
		index.commit();

		// check that it arrived properly
		IndexReader reader = IndexReader.open(directory);
		assertEquals(1, reader.numDocs());

		Term term = new Term(SearchFields.URI_FIELD_NAME, subject.toString());
		TermDocs docs = reader.termDocs(term);
		assertTrue(docs.next());

		int documentNr = docs.doc();
		Document document = reader.document(documentNr);
		assertEquals(subject.toString(), document.get(SearchFields.URI_FIELD_NAME));
		assertEquals(object1.getLabel(), document.get(predicate1.toString()));

		assertFalse(docs.next());
		docs.close();
		reader.close();

		// add another statement
		index.begin();
		index.addStatement(statement12);
		index.commit();

		// See if everything remains consistent. We must create a new IndexReader
		// in order to be able to see the updates
		reader = IndexReader.open(directory);
		assertEquals(1, reader.numDocs()); // #docs should *not* have increased

		docs = reader.termDocs(term);
		assertTrue(docs.next());

		documentNr = docs.doc();
		document = reader.document(documentNr);
		assertEquals(subject.toString(), document.get(SearchFields.URI_FIELD_NAME));
		assertEquals(object1.getLabel(), document.get(predicate1.toString()));
		assertEquals(object2.getLabel(), document.get(predicate2.toString()));

		assertFalse(docs.next());
		docs.close();

		// see if we can query for these literals
		IndexSearcher searcher = new IndexSearcher(reader);
		QueryParser parser = new QueryParser(Version.LUCENE_35, SearchFields.TEXT_FIELD_NAME, analyzer);

		Query query = parser.parse(object1.getLabel());
		System.out.println("query=" + query);
		TotalHitCountCollector results = new TotalHitCountCollector();
		searcher.search(query, results);
		assertEquals(1, results.getTotalHits());

		query = parser.parse(object2.getLabel());
		results = new TotalHitCountCollector();
		searcher.search(query, results);
		assertEquals(1, results.getTotalHits());

		searcher.close();
		reader.close();

		// remove the first statement
		index.begin();
		index.removeStatement(statement11);
		index.commit();

		// check that that statement is actually removed and that the other still
		// exists
		reader = IndexReader.open(directory);
		assertEquals(1, reader.numDocs());

		docs = reader.termDocs(term);
		assertTrue(docs.next());

		documentNr = docs.doc();
		document = reader.document(documentNr);
		assertEquals(subject.toString(), document.get(SearchFields.URI_FIELD_NAME));
		assertNull(document.get(predicate1.toString()));
		assertEquals(object2.getLabel(), document.get(predicate2.toString()));

		assertFalse(docs.next());
		docs.close();

		reader.close();

		// remove the other statement
		index.begin();
		index.removeStatement(statement12);
		index.commit();

		// check that there are no documents left (i.e. the last Document was
		// removed completely, rather than its remaining triple removed)
		reader = IndexReader.open(directory);
		assertEquals(0, reader.numDocs());
		reader.close();
	}

	@Test
	public void testAddMultiple()
		throws Exception
	{
		// add a statement to an index
		HashSet<Statement> added = new HashSet<Statement>();
		HashSet<Statement> removed = new HashSet<Statement>();
		added.add(statement11);
		added.add(statement12);
		added.add(statement21);
		added.add(statement22);
		index.begin();
		index.addRemoveStatements(added, removed);
		index.commit();

		// check that it arrived properly
		IndexReader reader = IndexReader.open(directory);
		assertEquals(2, reader.numDocs());
		reader.close();

		// check the documents
		Document document = index.getDocuments(subject).iterator().next();
		assertEquals(subject.toString(), document.get(SearchFields.URI_FIELD_NAME));
		assertStatement(statement11, document);
		assertStatement(statement12, document);

		document = index.getDocuments(subject2).iterator().next();
		assertEquals(subject2.toString(), document.get(SearchFields.URI_FIELD_NAME));
		assertStatement(statement21, document);
		assertStatement(statement22, document);

		// check if the text field stores all added string values
		Set<String> texts = new HashSet<String>();
		texts.add("cats");
		texts.add("dogs");
		// FIXME
		// assertTexts(texts, document);

		// add/remove one
		added.clear();
		removed.clear();
		added.add(statement23);
		removed.add(statement22);
		index.begin();
		index.addRemoveStatements(added, removed);
		index.commit();

		// check doc 2
		document = index.getDocuments(subject2).iterator().next();
		assertEquals(subject2.toString(), document.get(SearchFields.URI_FIELD_NAME));
		assertStatement(statement21, document);
		assertStatement(statement23, document);
		assertNoStatement(statement22, document);

		// check if the text field stores all added and no deleted string values
		texts.remove("dogs");
		texts.add("chicken");
		// FIXME
		// assertTexts(texts, document);

		// TODO: check deletion of the rest

	}

	/**
	 * Contexts can only be tested in combination with a sail, as the triples
	 * have to be retrieved from the sail
	 *
	 * @throws Exception
	 */
	@Test
	public void testContexts()
		throws Exception
	{
		// add a sail
		MemoryStore memoryStore = new MemoryStore();
		// enable lock tracking
		info.aduna.concurrent.locks.Properties.setLockTrackingEnabled(true);
		LuceneSail sail = new LuceneSail();
		sail.setBaseSail(memoryStore);
		sail.setLuceneIndex(index);

		// create a Repository wrapping the LuceneSail
		SailRepository repository = new SailRepository(sail);
		repository.initialize();

		// now add the statements through the repo
		// add statements with context
		SailRepositoryConnection connection = repository.getConnection();
		try {
			connection.begin();
			connection.add(statementContext111, statementContext111.getContext());
			connection.add(statementContext121, statementContext121.getContext());
			connection.add(statementContext211, statementContext211.getContext());
			connection.add(statementContext222, statementContext222.getContext());
			connection.add(statementContext232, statementContext232.getContext());
			connection.commit();

			// check if they are there
			assertStatement(statementContext111);
			assertStatement(statementContext121);
			assertStatement(statementContext211);
			assertStatement(statementContext222);
			assertStatement(statementContext232);

			// delete context 1
			connection.begin();
			connection.clear(new Resource[] { CONTEXT_1 });
			connection.commit();
			assertNoStatement(statementContext111);
			assertNoStatement(statementContext121);
			assertNoStatement(statementContext211);
			assertStatement(statementContext222);
			assertStatement(statementContext232);
		}
		finally {
			// close repo
			connection.close();
			repository.shutDown();
		}
	}

	/**
	 * Contexts can only be tested in combination with a sail, as the triples
	 * have to be retrieved from the sail
	 *
	 * @throws Exception
	 */
	@Test
	public void testContextsRemoveContext2()
		throws Exception
	{
		// add a sail
		MemoryStore memoryStore = new MemoryStore();
		// enable lock tracking
		info.aduna.concurrent.locks.Properties.setLockTrackingEnabled(true);
		LuceneSail sail = new LuceneSail();
		sail.setBaseSail(memoryStore);
		sail.setLuceneIndex(index);

		// create a Repository wrapping the LuceneSail
		SailRepository repository = new SailRepository(sail);
		repository.initialize();

		// now add the statements through the repo
		// add statements with context
		SailRepositoryConnection connection = repository.getConnection();
		try {
			connection.begin();
			connection.add(statementContext111, statementContext111.getContext());
			connection.add(statementContext121, statementContext121.getContext());
			connection.add(statementContext211, statementContext211.getContext());
			connection.add(statementContext222, statementContext222.getContext());
			connection.add(statementContext232, statementContext232.getContext());
			connection.commit();

			// check if they are there
			assertStatement(statementContext111);
			assertStatement(statementContext121);
			assertStatement(statementContext211);
			assertStatement(statementContext222);
			assertStatement(statementContext232);

			// delete context 2
			connection.begin();
			connection.clear(new Resource[] { CONTEXT_2 });
			connection.commit();
			assertStatement(statementContext111);
			assertStatement(statementContext121);
			assertStatement(statementContext211);
			assertNoStatement(statementContext222);
			assertNoStatement(statementContext232);
		}
		finally {
			// close repo
			connection.close();
			repository.shutDown();
		}
	}

	@Test
	public void testRejectedDatatypes() {
		IRI STRING = new SimpleIRI("http://www.w3.org/2001/XMLSchema#string");
		IRI FLOAT = new SimpleIRI("http://www.w3.org/2001/XMLSchema#float");
		Literal literal1 = new SimpleLiteral("hi there");
		Literal literal2 = new SimpleLiteral("hi there, too", STRING);
		Literal literal3 = new SimpleLiteral("1.0");
		Literal literal4 = new SimpleLiteral("1.0", FLOAT);
		assertEquals("Is the first literal accepted?", true, index.accept(literal1));
		assertEquals("Is the second literal accepted?", true, index.accept(literal2));
		assertEquals("Is the third literal accepted?", true, index.accept(literal3));
		assertEquals("Is the fourth literal accepted?", false, index.accept(literal4));
	}

	private void assertStatement(Statement statement)
		throws Exception
	{
		Document document = index.getDocument(statement.getSubject(), statement.getContext());
		if (document == null)
			fail("Missing document " + statement.getSubject());
		assertStatement(statement, document);
	}

	private void assertNoStatement(Statement statement)
		throws Exception
	{
		Document document = index.getDocument(statement.getSubject(), statement.getContext());
		if (document == null)
			return;
		assertNoStatement(statement, document);
	}

	/**
	 * @param statement112
	 * @param document
	 */
	private void assertStatement(Statement statement, Document document) {
		Field[] fields = document.getFields(statement.getPredicate().toString());
		assertNotNull("field " + statement.getPredicate() + " not found in document " + document, fields);
		for (Field f : fields) {
			if (((Literal)statement.getObject()).getLabel().equals(f.stringValue()))
				return;
		}
		fail("Statement not found in document " + statement);
	}

	/**
	 * @param statement112
	 * @param document
	 */
	private void assertNoStatement(Statement statement, Document document) {
		Field[] fields = document.getFields(statement.getPredicate().toString());
		if (fields == null)
			return;
		for (Field f : fields) {
			if (((Literal)statement.getObject()).getLabel().equals(f.stringValue()))
				fail("Statement should not be found in document " + statement);
		}

	}

	/*private void assertTexts(Set<String> texts, Document document) {
		Set<String> toFind = new HashSet<String>(texts);
		Set<String> found = new HashSet<String>();
		for(Field field : document.getFields(LuceneIndex.TEXT_FIELD_NAME)) {
			// is the field value expected and not yet been found?
			if(toFind.remove(field.stringValue())) {
				// add it to the found set
				// (it was already remove from the toFind list in the if clause)
				found.add(field.stringValue());
			} else {
				assertEquals("Was the text value '" + field.stringValue() + "' expected to exist?", false, true);
			}
		}

		for(String notFound : toFind) {
			assertEquals("Was the expected text value '" + notFound + "' found?", true, false);
		}
	}*/
}
