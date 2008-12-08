/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail.federation;

import static org.openrdf.query.QueryLanguage.SPARQL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import info.aduna.iteration.Iterations;
import info.aduna.text.StringUtil;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultUtil;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.MutableTupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.federation.Federation;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class FederationQueryTest extends TestCase {

	private static String PREFIX = "PREFIX shared:<urn:shared:>		# subject namespace in all members\n"
			+ "PREFIX one:<urn:member:one:>		# subject namespace in only member 1\n"
			+ "PREFIX two:<urn:member:two:>		# subject namespace in only member 2\n"
			+ "PREFIX three:<urn:member:three:>	# subject namespace in only member 3\n"
			+ "PREFIX a:<urn:schema:a:>			# all members\n"
			+ "PREFIX b:<urn:schema:b:>			# each subject is described in only one member\n"
			+ "PREFIX c:<urn:schema:c:>			# member 2 exclusively\n"
			+ "PREFIX d:<urn:schema:d:>			# member 2 exclusively and declared as local\n";

	private static String WHERE = PREFIX + "SELECT * WHERE ";

	private RepositoryConnection con;

	private RepositoryConnection reference;

	private ClassLoader cl = FederationQueryTest.class.getClassLoader();

	public void testJoinAA()
		throws Exception
	{
		assertQuery("{ ?person a:spouse ?spouse ; a:parentOf ?child }");
	}

	public void testJoinAC()
		throws Exception
	{
		assertQuery("{ ?person a:spouse ?spouse ; c:job ?job }");
	}

	public void testJoinBB()
		throws Exception
	{
		assertQuery("{ ?person b:name ?name ; b:desc ?desc }");
	}

	public void testJoinBC()
		throws Exception
	{
		assertQuery("{ ?person b:name ?name ;  b:desc ?desc ; c:job ?job }");
	}

	public void testJoinCC()
		throws Exception
	{
		assertQuery("{ ?person c:livesIn ?home ; c:job ?job }");
	}

	public void testLeftJoinAA()
		throws Exception
	{
		assertQuery("{ ?person a:spouse ?spouse OPTIONAL { ?person a:parentOf ?child }}");
	}

	public void testLeftJoinAC()
		throws Exception
	{
		assertQuery("{ ?person a:spouse ?spouse OPTIONAL {?person c:job ?job }}");
	}

	public void testLeftJoinAD()
		throws Exception
	{
		assertQuery("{ ?person a:spouse ?spouse OPTIONAL {?person d:worksIn ?work }}");
	}

	public void testLeftJoinBB()
		throws Exception
	{
		assertQuery("{ ?person b:name ?name OPTIONAL { ?person b:desc ?desc }}");
	}

	public void testLeftJoinBC()
		throws Exception
	{
		assertQuery("{ ?person b:name ?name OPTIONAL {?person c:job ?job }}");
	}

	public void testLeftJoinBD()
		throws Exception
	{
		assertQuery("{ ?person b:name ?name OPTIONAL { ?person d:worksIn ?work }}");
	}

	public void testLeftJoinCA()
		throws Exception
	{
		assertQuery("{ ?person c:livesIn ?home OPTIONAL {?person a:mother ?mother }}");
	}

	public void testLeftJoinCB()
		throws Exception
	{
		assertQuery("{ ?person c:livesIn ?home OPTIONAL {?person b:desc ?desc }}");
	}

	public void testLeftJoinCC()
		throws Exception
	{
		assertQuery("{ ?person c:livesIn ?home OPTIONAL {?person c:job ?job }}");
	}

	public void testLeftJoinCD()
		throws Exception
	{
		assertQuery("{ ?person c:livesIn ?home OPTIONAL {?person d:worksIn ?work }}");
	}

	public void testLeftJoinDA()
		throws Exception
	{
		assertQuery("{ ?person d:worksIn ?work OPTIONAL {?person a:mother ?mother }}");
	}

	public void testUnionAA()
		throws Exception
	{
		assertQuery("{ {?person a:mother ?parent} UNION {?person a:father ?parent}}");
	}

	public void testUnionCD()
		throws Exception
	{
		assertQuery("{ {?person c:livesIn ?town} UNION {?person d:worksIn ?town}}");
	}

	public void testUnionAC()
		throws Exception
	{
		assertQuery("{ {?person a:parentOf ?child} UNION {?person c:job one:teacher}}");
	}

	private void assertQuery(String qry)
		throws Exception
	{
		TupleQueryResult expected = reference.prepareTupleQuery(SPARQL, WHERE + qry).evaluate();
		TupleQueryResult result = con.prepareTupleQuery(SPARQL, WHERE + qry).evaluate();
		compareTupleQueryResults(expected, result);
	}

	protected void setUp()
		throws Exception
	{
		SailRepository ref = new SailRepository(new MemoryStore());
		ref.initialize();
		reference = ref.getConnection();
		Federation federation = new Federation();
		SailRepository repo = new SailRepository(federation);
		repo.initialize();
		federation.addMember(createMember("1"));
		federation.addMember(createMember("2"));
		federation.addMember(createMember("3"));
		federation.setLocalPropertySpace(Arrays.asList("urn:schema:b:", "urn:schema:d:"));
		con = repo.getConnection();
	}

	private Repository createMember(String id)
		throws StoreException, RDFParseException, IOException
	{
		SailRepository member = new SailRepository(new MemoryStore());
		member.initialize();
		SailRepositoryConnection con = member.getConnection();
		try {
			String resource = "testcases/federation-member-" + id + ".ttl";
			con.add(cl.getResource(resource), "", RDFFormat.TURTLE);
			reference.add(cl.getResource(resource), "", RDFFormat.TURTLE);
		}
		finally {
			con.close();
		}
		return member;
	}

	private void compareTupleQueryResults(TupleQueryResult expectedResult, TupleQueryResult queryResult)
		throws Exception
	{
		// Create MutableTupleQueryResult to be able to re-iterate over the
		// results
		MutableTupleQueryResult queryResultTable = new MutableTupleQueryResult(queryResult);
		MutableTupleQueryResult expectedResultTable = new MutableTupleQueryResult(expectedResult);

		if (!QueryResultUtil.equals(expectedResultTable, queryResultTable)) {
			queryResultTable.beforeFirst();
			expectedResultTable.beforeFirst();

			/*
			 * StringBuilder message = new StringBuilder(128);
			 * message.append("\n============ "); message.append(getName());
			 * message.append(" =======================\n");
			 * message.append("Expected result: \n"); while
			 * (expectedResultTable.hasNext()) {
			 * message.append(expectedResultTable.next()); message.append("\n"); }
			 * message.append("============="); StringUtil.appendN('=',
			 * getName().length(), message);
			 * message.append("========================\n"); message.append("Query
			 * result: \n"); while (queryResultTable.hasNext()) {
			 * message.append(queryResultTable.next()); message.append("\n"); }
			 * message.append("============="); StringUtil.appendN('=',
			 * getName().length(), message);
			 * message.append("========================\n");
			 */

			List<BindingSet> queryBindings = Iterations.asList(queryResultTable);
			List<BindingSet> expectedBindings = Iterations.asList(expectedResultTable);

			List<BindingSet> missingBindings = new ArrayList<BindingSet>(expectedBindings);
			missingBindings.removeAll(queryBindings);

			List<BindingSet> unexpectedBindings = new ArrayList<BindingSet>(queryBindings);
			unexpectedBindings.removeAll(expectedBindings);

			StringBuilder message = new StringBuilder(128);
			message.append("\n============ ");
			message.append(getName());
			message.append(" =======================\n");

			if (!missingBindings.isEmpty()) {
				message.append("Missing bindings: \n");
				for (BindingSet bs : missingBindings) {
					message.append(bs);
					message.append("\n");
				}

				message.append("=============");
				StringUtil.appendN('=', getName().length(), message);
				message.append("========================\n");
			}

			if (!unexpectedBindings.isEmpty()) {
				message.append("Unexpected bindings: \n");
				for (BindingSet bs : unexpectedBindings) {
					message.append(bs);
					message.append("\n");
				}

				message.append("=============");
				StringUtil.appendN('=', getName().length(), message);
				message.append("========================\n");
			}

			fail(message.toString());
		}
	}
}
