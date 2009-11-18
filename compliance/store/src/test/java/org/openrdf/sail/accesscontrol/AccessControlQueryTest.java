/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.accesscontrol;

import java.util.List;

import junit.framework.TestCase;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.result.TupleResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.accesscontrol.vocabulary.ACL;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.store.Session;
import org.openrdf.store.SessionsManager;

/**
 * @author Jeen Broekstra
 */
public class AccessControlQueryTest extends TestCase {

	private String dataFile = "example-data.ttl";

	private String policyFile = "policies.ttl";

	private String resourcePath = "accesscontrol/";

	private ClassLoader cl = AccessControlQueryTest.class.getClassLoader();

	private Repository rep;

	protected void setUp()
		throws Exception
	{

		rep = new SailRepository(new AccessControlSail(new MemoryStore()));
		rep.initialize();

		RepositoryConnection conn = rep.getConnection();
		conn.add(cl.getResource(resourcePath + dataFile), "", RDFFormat.TURTLE);
		conn.add(cl.getResource(resourcePath + policyFile), "", RDFFormat.TURTLE, ACL.CONTEXT);
		conn.close();
	}

	public void testSimpleQuery()
		throws Exception
	{

		RepositoryConnection conn = rep.getConnection();
		
		Session session = SessionsManager.get();
		session.setCurrentUser(conn.getValueFactory().createURI("http://example.org/bob"));
		
		String simpleDocumentQuery = "SELECT DISTINCT ?X WHERE {?X a <http://example.org/Document>; ?P ?Y . } ";
		TupleResult tr = conn.prepareTupleQuery(QueryLanguage.SPARQL, simpleDocumentQuery).evaluate();

		List<String> headers = tr.getBindingNames();

		URI pmdoc1 = conn.getValueFactory().createURI("http://example.org/pmdocument1");
		URI pmdoc2 = conn.getValueFactory().createURI("http://example.org/pmdocument2");
		
		while (tr.hasNext()) {
			BindingSet bs = tr.next();

			for (String header : headers) {
				Value value = bs.getValue(header);
				assertNotSame(value, pmdoc1);
				assertNotSame(value, pmdoc2);
				System.out.println(header + " = " + value);
			}
			System.out.println();
		}
	}
}
