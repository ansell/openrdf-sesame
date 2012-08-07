/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 * A set of compliance tests on SPARQL query functionality which can not be
 * easily executed using the {@link SPARQL11ManifestTest} format. This includes
 * tests on queries with non-deterministic output (e.g. GROUP_CONCAT).
 * 
 * @author Jeen Broekstra
 */
public abstract class ComplexSPARQLQueryTest {

	static final Logger logger = LoggerFactory.getLogger(ComplexSPARQLQueryTest.class);

	private Repository rep;

	protected RepositoryConnection conn;

	protected ValueFactory f;

	protected static final String EX_NS = "http://example.org/";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		logger.debug("setting up test");
		this.rep = newRepository();
		rep.initialize();

		f = rep.getValueFactory();
		conn = rep.getConnection();

		loadTestData();

		logger.debug("test setup complete.");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
		logger.debug("tearing down...");
		conn.close();
		conn = null;

		rep.shutDown();
		rep = null;

		logger.debug("tearDown complete.");
	}

	@Test
	public void testGroupConcatDistinct() {
		StringBuilder query = new StringBuilder();
		query.append(getNamespaceDeclarations());
		query.append("SELECT (GROUP_CONCAT(DISTINCT ?l) AS ?concat)");
		query.append("WHERE { ex:groupconcat-test ?p ?l . }");

		TupleQuery tq = null;
		try {
			tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
		}
		catch (RepositoryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		catch (MalformedQueryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			TupleQueryResult result = tq.evaluate();
			assertNotNull(result);

			while (result.hasNext()) {
				BindingSet bs = result.next();
				assertNotNull(bs);

				Value concat = bs.getValue("concat");

				assertTrue(concat instanceof Literal);

				String lexValue = ((Literal)concat).getLabel();

				int occ = countCharOccurrences(lexValue, 'a');
				assertEquals(1, occ);
				occ = countCharOccurrences(lexValue, 'b');
				assertEquals(1, occ);
				occ = countCharOccurrences(lexValue, 'c');
				assertEquals(1, occ);
				occ = countCharOccurrences(lexValue, 'd');
				assertEquals(1, occ);
			}
			result.close();
		}
		catch (QueryEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
	
	@Test
	public void testSameTermRepeatInOptional() {
		StringBuilder query = new StringBuilder();
		query.append(getNamespaceDeclarations());
		query.append(" SELECT ?l ?opt1 ?opt2 ");
		query.append(" FROM ex:optional-sameterm-graph ");
		query.append(" WHERE { ");
		query.append("          ?s ex:p ex:A ; " );
		query.append("          { ");
		query.append("              { ");
		query.append("                 ?s ?p ?l .");
		query.append("                 FILTER(?p = rdfs:label) ");
		query.append("              } ");
		query.append("              OPTIONAL { ");
		query.append("                 ?s ?p ?opt1 . ");
		query.append("                 FILTER (?p = ex:prop1) ");
		query.append("              } ");
		query.append("              OPTIONAL { ");
		query.append("                 ?s ?p ?opt2 . ");
		query.append("                 FILTER (?p = ex:prop2) ");
		query.append("              } ");
		query.append("          }");
		query.append(" } ");

		TupleQuery tq = null;
		try {
			tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
		}
		catch (RepositoryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		catch (MalformedQueryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			TupleQueryResult result = tq.evaluate();
			assertNotNull(result);

			int count = 0;
			while (result.hasNext()) {
				BindingSet bs = result.next();
				count++;
				assertNotNull(bs);
				
				System.out.println(bs);
				
				Value l = bs.getValue("l");
				assertTrue(l instanceof Literal);
				assertEquals("label", ((Literal)l).getLabel());
				
				Value opt1 = bs.getValue("opt1");
				assertNull(opt1);
				
				Value opt2 = bs.getValue("opt2");
				assertNull(opt2);
			}
			result.close();
			
			assertEquals(1, count);
		}
		catch (QueryEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		
	}
	
	@Test
	public void testSameTermRepeatInUnion() {
		StringBuilder query = new StringBuilder();
		query.append("PREFIX foaf:<http://xmlns.com/foaf/0.1/>\n");
		query.append("SELECT * {\n");
		query.append("    {\n");
		query.append("        ?sameTerm foaf:mbox ?mbox\n");
		query.append("        FILTER sameTerm(?sameTerm,$william)\n");
		query.append("    } UNION {\n");
		query.append("        ?x foaf:knows ?sameTerm\n");
		query.append("        FILTER sameTerm(?sameTerm,$william)\n");
		query.append("    }\n");
		query.append("}");

		TupleQuery tq = null;
		try {
			tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
			tq.setBinding("william", conn.getValueFactory().createURI("http://example.org/william"));
		}
		catch (RepositoryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		catch (MalformedQueryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			TupleQueryResult result = tq.evaluate();
			assertNotNull(result);

			int count = 0;
			while (result.hasNext()) {
				BindingSet bs = result.next();
				count++;
				assertNotNull(bs);
				
				System.out.println(bs);
				
				Value mbox = bs.getValue("mbox");
				Value x = bs.getValue("x");

				assertTrue(mbox instanceof Literal || x instanceof URI);
			}
			result.close();
			
			assertEquals(3, count);
		}
		catch (QueryEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		
	}
	
	@Test
	public void testSameTermRepeatInUnionAndOptional() {
		StringBuilder query = new StringBuilder();
		query.append(getNamespaceDeclarations());
		query.append("SELECT * {\n");
		query.append("    {\n");
		query.append("        ex:a ?p ?prop1\n");
		query.append("        FILTER (?p = ex:prop1)\n");
		query.append("    } UNION {\n");
		query.append("          ?s ex:p ex:A ; " );
		query.append("          { ");
		query.append("              { ");
		query.append("                 ?s ?p ?l .");
		query.append("                 FILTER(?p = rdfs:label) ");
		query.append("              } ");
		query.append("              OPTIONAL { ");
		query.append("                 ?s ?p ?opt1 . ");
		query.append("                 FILTER (?p = ex:prop1) ");
		query.append("              } ");
		query.append("              OPTIONAL { ");
		query.append("                 ?s ?p ?opt2 . ");
		query.append("                 FILTER (?p = ex:prop2) ");
		query.append("              } ");
		query.append("          }");
		query.append("    }\n");
		query.append("}");

		TupleQuery tq = null;
		try {
			tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
		}
		catch (RepositoryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		catch (MalformedQueryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			TupleQueryResult result = tq.evaluate();
			assertNotNull(result);

			int count = 0;
			while (result.hasNext()) {
				BindingSet bs = result.next();
				count++;
				assertNotNull(bs);
				
				System.out.println(bs);
				
				Value prop1 = bs.getValue("prop1");
				Value l = bs.getValue("l");

				assertTrue(prop1 instanceof Literal || l instanceof Literal);
				if (l instanceof Literal) {
					Value opt1 = bs.getValue("opt1");
					assertNull(opt1);
					
					Value opt2 = bs.getValue("opt2");
					assertNull(opt2);
				}
			}
			result.close();
			
			assertEquals(2, count);
		}
		catch (QueryEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		
	}
	
	@Test 
	public void testPropertyPathInTree() {
		StringBuilder query = new StringBuilder();
		query.append(getNamespaceDeclarations());
		query.append(" SELECT ?node ?name ");
		query.append(" FROM ex:tree-graph ");
		query.append(" WHERE { ?node ex:hasParent+ ex:b . ?node ex:name ?name . }");

		TupleQuery tq = null;
		try {
			tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
		}
		catch (RepositoryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		catch (MalformedQueryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			TupleQueryResult result = tq.evaluate();
			assertNotNull(result);

			while (result.hasNext()) {
				BindingSet bs = result.next();
				assertNotNull(bs);
				
				System.out.println(bs);
				
			}
			result.close();
		}
		catch (QueryEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
				
	}

	@Test
	public void testFilterRegexBoolean() {
		
		// test case for issue SES-1050
		StringBuilder query = new StringBuilder();
		query.append(getNamespaceDeclarations());
		query.append(" SELECT *");
		query.append(" WHERE { ");
		query.append("       ?x foaf:name ?name ; ");
		query.append("          foaf:mbox ?mbox . ");
		query.append("       FILTER(EXISTS { ");
		query.append("            FILTER(REGEX(?name, \"Bo\") && REGEX(?mbox, \"bob\")) ");
//		query.append("            FILTER(REGEX(?mbox, \"bob\")) ");
		query.append("            } )");
		query.append(" } ");
		
		TupleQuery tq = null;
		try {
			tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
		}
		catch (RepositoryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		catch (MalformedQueryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			TupleQueryResult result = tq.evaluate();
			assertNotNull(result);
			
			assertTrue(result.hasNext());
			int count = 0;
			while (result.hasNext()) {
				BindingSet bs = result.next();
				count++;
				System.out.println(bs);
			}
			assertEquals(1, count);
			
			result.close();
		}
		catch (QueryEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	} 
	
	@Test
	public void testGroupConcatNonDistinct() {
		StringBuilder query = new StringBuilder();
		query.append(getNamespaceDeclarations());
		query.append("SELECT (GROUP_CONCAT(?l) AS ?concat)");
		query.append("WHERE { ex:groupconcat-test ?p ?l . }");

		TupleQuery tq = null;
		try {
			tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
		}
		catch (RepositoryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		catch (MalformedQueryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			TupleQueryResult result = tq.evaluate();
			assertNotNull(result);

			while (result.hasNext()) {
				BindingSet bs = result.next();
				assertNotNull(bs);

				Value concat = bs.getValue("concat");

				assertTrue(concat instanceof Literal);

				String lexValue = ((Literal)concat).getLabel();

				int occ = countCharOccurrences(lexValue, 'a');
				assertEquals(1, occ);
				occ = countCharOccurrences(lexValue, 'b');
				assertEquals(2, occ);
				occ = countCharOccurrences(lexValue, 'c');
				assertEquals(2, occ);
				occ = countCharOccurrences(lexValue, 'd');
				assertEquals(1, occ);
			}
			result.close();
		}
		catch (QueryEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	private int countCharOccurrences(String string, char ch) {
		int count = 0;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == ch) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Get a set of useful namespace prefix declarations.
	 * 
	 * @return namespace prefix declarations for rdf, rdfs, dc, foaf and ex.
	 */
	protected String getNamespaceDeclarations() {
		StringBuilder declarations = new StringBuilder();
		declarations.append("PREFIX rdf: <" + RDF.NAMESPACE + "> \n");
		declarations.append("PREFIX rdfs: <" + RDFS.NAMESPACE + "> \n");
		declarations.append("PREFIX dc: <" + DC.NAMESPACE + "> \n");
		declarations.append("PREFIX foaf: <" + FOAF.NAMESPACE + "> \n");
		declarations.append("PREFIX ex: <" + EX_NS + "> \n");
		declarations.append("\n");

		return declarations.toString();
	}

	protected abstract Repository newRepository()
		throws Exception;

	protected void loadTestData()
		throws RDFParseException, RepositoryException, IOException
	{
		logger.debug("loading dataset...");
		InputStream dataset = ComplexSPARQLQueryTest.class.getResourceAsStream("/testdata-query/dataset-query.trig");
		try {
			conn.add(dataset, "", RDFFormat.TRIG);
		}
		finally {
			dataset.close();
		}
		logger.debug("dataset loaded.");
	}
}
