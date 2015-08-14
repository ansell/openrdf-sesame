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
package org.openrdf.sail.lucene.examples;

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
import org.openrdf.sail.lucene.LuceneIndex;
import org.openrdf.sail.lucene.LuceneSail;
import org.openrdf.sail.lucene.LuceneSailSchema;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Example code showing how to use the LuceneSail
 * 
 * @author sauermann
 */
public class LuceneSailExample {

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
		lucenesail.setParameter(LuceneSail.INDEX_CLASS_KEY, LuceneIndex.class.getName());
		// set this parameter to let the lucene index store its data in ram
		lucenesail.setParameter(LuceneSail.LUCENE_RAMDIR_KEY, "true");
		// set this parameter to store the lucene index on disk
		// lucenesail.setParameter(LuceneSail.LUCENE_DIR_KEY,
		// "./data/mydirectory");

		// wrap memorystore in a lucenesail
		lucenesail.setBaseSail(memoryStore);

		// create a Repository to access the sails
		SailRepository repository = new SailRepository(lucenesail);
		repository.initialize();

		// add some test data, the FOAF ont
		SailRepositoryConnection connection = repository.getConnection();
		try {
			connection.begin();
			connection.add(
					LuceneSailExample.class.getResourceAsStream("/org/openrdf/sail/lucene/examples/foaf.rdfs"),
					"", RDFFormat.RDFXML);
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
