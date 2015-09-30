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
package org.openrdf.sail.solr.examples;

import org.openrdf.model.Statement;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.lucene.LuceneSail;
import org.openrdf.sail.lucene.LuceneSailSchema;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.solr.SolrIndex;

/**
 * Example code showing how to use the LuceneSail
 * 
 * @author sauermann
 */
public class SolrSailExample {

	/**
	 * Create a lucene sail and use it
	 * 
	 * @param args
	 */
	public static void main(String[] args)
		throws Exception
	{
		createSimple();
	}

	/**
	 * Create a LuceneSail and add some triples to it, ask a query.
	 */
	public static void createSimple()
		throws Exception
	{
		// create a sesame memory sail
		MemoryStore memoryStore = new MemoryStore();

		// create a lucenesail to wrap the memorystore
		LuceneSail lucenesail = new LuceneSail();
		lucenesail.setParameter(LuceneSail.INDEX_CLASS_KEY, SolrIndex.class.getName());
		lucenesail.setParameter(SolrIndex.SERVER_KEY, "embedded:");

		// wrap memorystore in a lucenesail
		lucenesail.setBaseSail(memoryStore);

		// create a Repository to access the sails
		SailRepository repository = new SailRepository(lucenesail);
		repository.initialize();

		// add some test data, the FOAF ont
		SailRepositoryConnection connection = repository.getConnection();
		try {
			connection.begin();
			connection.add(SolrSailExample.class.getResourceAsStream("/org/openrdf/sail/lucene/examples/foaf.rdfs"), "", RDFFormat.RDFXML);
			connection.commit();

			// search for resources that mention "person"
			String queryString = "PREFIX search:   <" + LuceneSailSchema.NAMESPACE + "> \n"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "SELECT * WHERE { \n"
					+ "?subject search:matches ?match . \n" + "?match search:query \"person\" ; \n"
					+ "       search:property ?property ; \n" + "       search:score ?score ; \n"
					+ "       search:snippet ?snippet . \n" + "?subject rdf:type ?type . \n" + "} LIMIT 3 \n"
					+ "BINDINGS ?type { \n" + " (<http://www.w3.org/2002/07/owl#Class>) \n" + "}";
			tupleQuery(queryString, connection);

			// search for property "name" with domain "person"
			queryString = "PREFIX search: <" + LuceneSailSchema.NAMESPACE + "> \n"
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT * WHERE { \n"
					+ "?subject rdfs:domain ?domain . \n" + "?subject search:matches ?match . \n"
					+ "?match search:query \"chat\" ; \n" + "       search:score ?score . \n"
					+ "?domain search:matches ?match2 . \n" + "?match2 search:query \"person\" ; \n"
					+ "        search:score ?score2 . \n" + "} LIMIT 5";
			tupleQuery(queryString, connection);

			// search in subquery and filter results
			queryString = "PREFIX search:   <" + LuceneSailSchema.NAMESPACE + "> \n" + "SELECT * WHERE { \n"
					+ "{ SELECT * WHERE { \n" + "  ?subject search:matches ?match . \n"
					+ "  ?match search:query \"person\" ; \n" + "         search:property ?property ; \n"
					+ "         search:score ?score ; \n" + "         search:snippet ?snippet . \n" + "} } \n"
					+ "FILTER(CONTAINS(STR(?subject), \"Person\")) \n" + "} \n" + "";
			tupleQuery(queryString, connection);

			// search for property "homepage" with domain foaf:Person
			queryString = "PREFIX search: <" + LuceneSailSchema.NAMESPACE + "> \n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
					+ "CONSTRUCT { ?x rdfs:domain foaf:Person } \n" + "WHERE { \n"
					+ "?x rdfs:domain foaf:Person . \n" + "?x search:matches ?match . \n"
					+ "?match search:query \"homepage\" ; \n" + "       search:property ?property ; \n"
					+ "       search:score ?score ; \n" + "       search:snippet ?snippet . \n" + "} LIMIT 3 \n";
			graphQuery(queryString, connection);
		}
		finally {
			connection.close();
			repository.shutDown();
		}
	}

	private static void tupleQuery(String queryString, RepositoryConnection connection)
		throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{
		System.out.println("Running query: \n" + queryString);
		TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult result = query.evaluate();
		try {
			// print the results
			System.out.println("Query results:");
			while (result.hasNext()) {
				BindingSet bindings = result.next();
				System.out.println("found match: ");
				for (Binding binding : bindings) {
					System.out.println("\t" + binding.getName() + ": " + binding.getValue());
				}
			}
		}
		finally {
			result.close();
		}
	}

	private static void graphQuery(String queryString, RepositoryConnection connection)
		throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		System.out.println("Running query: \n" + queryString);
		GraphQuery query = connection.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
		GraphQueryResult result = query.evaluate();
		try {
			// print the results
			while (result.hasNext()) {
				Statement stmt = result.next();
				System.out.println("found match: " + stmt.getSubject().stringValue() + "\t"
						+ stmt.getPredicate().stringValue() + "\t" + stmt.getObject().stringValue());
			}
		}
		finally {
			result.close();
		}

	}
}
