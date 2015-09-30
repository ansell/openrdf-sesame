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
package org.eclipse.rdf4j.repository.optimistic;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.OptimisticIsolationTest;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RemoveIsolationTest {

	private Repository repo;

	private RepositoryConnection con;

	private ValueFactory f;

	private IsolationLevel level = IsolationLevels.SNAPSHOT_READ;

	@Before
	public void setUp()
		throws Exception
	{
		repo = OptimisticIsolationTest.getEmptyInitializedRepository(RemoveIsolationTest.class);
		con = repo.getConnection();
		f = con.getValueFactory();
	}

	@After
	public void tearDown()
		throws Exception
	{
		con.close();
		repo.shutDown();
	}

	@Test
	public void testRemoveOptimisticIsolation()
		throws Exception
	{
		con.begin(level);

		con.add(f.createURI("http://example.org/people/alice"),
				f.createURI("http://example.org/ontology/name"), f.createLiteral("Alice"));

		con.remove(con.getStatements(null, null, null, true));

		RepositoryResult<Statement> stats = con.getStatements(null, null, null, true);
		assertEquals(Collections.emptyList(), QueryResults.asList(stats));
		con.rollback();
	}

	@Test
	public void testRemoveIsolation()
		throws Exception
	{
		con.begin(level);

		con.add(f.createURI("http://example.org/people/alice"),
				f.createURI("http://example.org/ontology/name"), f.createLiteral("Alice"));

		con.remove(con.getStatements(null, null, null, true));

		RepositoryResult<Statement> stats = con.getStatements(null, null, null, true);
		assertEquals(Collections.emptyList(), QueryResults.asList(stats));
		con.rollback();
	}
}
