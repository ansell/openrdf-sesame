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
package org.openrdf.sail.federation;

import static org.junit.Assert.fail;
import static org.openrdf.query.QueryLanguage.SPARQL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import info.aduna.iteration.Iterations;
import info.aduna.text.StringUtil;

import org.openrdf.OpenRDFException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.MutableTupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author James Leigh
 */
@RunWith(Parameterized.class)
public class FederationQueryTest {

	@Rule public TestName name = new TestName();

	private String getName()
	{
		return name.getMethodName();
	}

	@Parameters(name = "{index}:{0}")
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ "JoinAA", "{ ?person a:spouse ?spouse ; a:parentOf ?child }" },
				{ "JoinAC", "{ ?person a:spouse ?spouse ; c:job ?job }" },
				{ "JoinBB", "{ ?person b:name ?name ; b:desc ?desc }" },
				{ "JoinBC", "{ ?person b:name ?name ;  b:desc ?desc ; c:job ?job }" },
				{ "JoinCC", "{ ?person c:livesIn ?home ; c:job ?job }" },
				{ "LeftJoinAA", "{ ?person a:spouse ?spouse OPTIONAL { ?person a:parentOf ?child }}" },
				{ "LeftJoinAC", "{ ?person a:spouse ?spouse OPTIONAL {?person c:job ?job }}" },
				{ "LeftJoinAD", "{ ?person a:spouse ?spouse OPTIONAL {?person d:worksIn ?work }}" },
				{ "LeftJoinBB", "{ ?person b:name ?name OPTIONAL { ?person b:desc ?desc }}" },
				{ "LeftJoinBC", "{ ?person b:name ?name OPTIONAL {?person c:job ?job }}" },
				{ "LeftJoinBD", "{ ?person b:name ?name OPTIONAL { ?person d:worksIn ?work }}" },
				{ "LeftJoinCA", "{ ?person c:livesIn ?home OPTIONAL {?person a:mother ?mother }}" },
				{ "LeftJoinCB", "{ ?person c:livesIn ?home OPTIONAL {?person b:desc ?desc }}" },
				{ "LeftJoinCC", "{ ?person c:livesIn ?home OPTIONAL {?person c:job ?job }}" },
				{ "LeftJoinCD", "{ ?person c:livesIn ?home OPTIONAL {?person d:worksIn ?work }}" },
				{ "LeftJoinDA", "{ ?person d:worksIn ?work OPTIONAL {?person a:mother ?mother }}" },
				{ "UnionAA", "{ {?person a:mother ?parent} UNION {?person a:father ?parent}}" },
				{ "UnionCD", "{ {?person c:livesIn ?town} UNION {?person d:worksIn ?town}}" },
				{ "UnionAC", "{ {?person a:parentOf ?child} UNION {?person c:job one:teacher}}" } });
	}

	private static final String PREFIX = "PREFIX shared:<urn:shared:>		# subject namespace in all members\n"
			+ "PREFIX one:<urn:member:one:>		# subject namespace in only member 1\n"
			+ "PREFIX two:<urn:member:two:>		# subject namespace in only member 2\n"
			+ "PREFIX three:<urn:member:three:>	# subject namespace in only member 3\n"
			+ "PREFIX a:<urn:schema:a:>			# all members\n"
			+ "PREFIX b:<urn:schema:b:>			# each subject is described in only one member\n"
			+ "PREFIX c:<urn:schema:c:>			# member 2 exclusively\n"
			+ "PREFIX d:<urn:schema:d:>			# member 2 exclusively and declared as local\n";

	private static final String WHERE = PREFIX + "SELECT * WHERE ";

	private RepositoryConnection con;

	private RepositoryConnection reference;

	private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

	private final String pattern;

	public FederationQueryTest(String name, String pattern) {
		super();
		assert !name.isEmpty();
		this.pattern = pattern;
	}

	@Test
	public void test()
		throws OpenRDFException
	{
		assertQuery(pattern);
	}

	private void assertQuery(String qry)
		throws OpenRDFException
	{
		TupleQueryResult expected = reference.prepareTupleQuery(SPARQL, WHERE + qry).evaluate();
		TupleQueryResult result = con.prepareTupleQuery(SPARQL, WHERE + qry).evaluate();
		compareTupleQueryResults(expected, result);
	}

	@Before
	public void setUp()
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

	private Repository createMember(String memberID)
		throws RepositoryException, RDFParseException, IOException
	{
		SailRepository member = new SailRepository(new MemoryStore());
		member.initialize();
		SailRepositoryConnection con = member.getConnection();
		try {
			String resource = "testcases/federation-member-" + memberID + ".ttl";
			con.add(classLoader.getResource(resource), "", RDFFormat.TURTLE);
			reference.add(classLoader.getResource(resource), "", RDFFormat.TURTLE);
		}
		finally {
			con.close();
		}
		return member;
	}

	private void compareTupleQueryResults(TupleQueryResult expectedResult, TupleQueryResult queryResult)
		throws QueryEvaluationException
	{
		// Create MutableTupleQueryResult to be able to re-iterate over the
		// results
		MutableTupleQueryResult queryResultTable = new MutableTupleQueryResult(queryResult);
		MutableTupleQueryResult expectedTable = new MutableTupleQueryResult(expectedResult);
		if (!QueryResults.equals(expectedTable, queryResultTable)) {
			queryResultTable.beforeFirst();
			expectedTable.beforeFirst();
			List<BindingSet> queryBindings = Iterations.asList(queryResultTable);
			List<BindingSet> expectedBindings = Iterations.asList(expectedTable);

			List<BindingSet> missingBindings = new ArrayList<BindingSet>(expectedBindings);
			missingBindings.removeAll(queryBindings);
			List<BindingSet> unexpected = new ArrayList<BindingSet>(queryBindings);
			unexpected.removeAll(expectedBindings);
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
			if (!unexpected.isEmpty()) {
				message.append("Unexpected bindings: \n");
				for (BindingSet bs : unexpected) {
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
