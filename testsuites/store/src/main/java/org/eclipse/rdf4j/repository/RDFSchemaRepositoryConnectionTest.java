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
package org.eclipse.rdf4j.repository;

import static org.junit.Assert.*;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author jeen
 * @author Arjohn Kampman
 */
@RunWith(Parameterized.class)
public abstract class RDFSchemaRepositoryConnectionTest extends RepositoryConnectionTest {

	@Parameters(name="{0}")
	public static final IsolationLevel[] parametersREAD_COMMITTED() {
		return new IsolationLevel[] {
				IsolationLevels.READ_COMMITTED,
				IsolationLevels.SNAPSHOT_READ,
				IsolationLevels.SNAPSHOT,
				IsolationLevels.SERIALIZABLE };
	}


	private IRI person;
	
	private IRI woman;

	private IRI man;

	public RDFSchemaRepositoryConnectionTest(IsolationLevel level) {
		super(level);
	}

	@Override
	public void setUp()
		throws Exception
	{
		super.setUp();

		person = vf.createIRI(FOAF_NS + "Person");
		woman = vf.createIRI("http://example.org/Woman");
		man = vf.createIRI("http://example.org/Man");
	}

	@Test
	public void testDomainInference()
		throws Exception
	{
		testCon.begin();
		testCon.add(name, RDFS.DOMAIN, person);
		testCon.add(bob, name, nameBob);
		testCon.commit();

		assertTrue(testCon.hasStatement(bob, RDF.TYPE, person, true));
	}

	@Test
	public void testSubClassInference()
		throws Exception
	{
		testCon.begin();
		testCon.add(woman, RDFS.SUBCLASSOF, person);
		testCon.add(man, RDFS.SUBCLASSOF, person);
		testCon.add(alice, RDF.TYPE, woman);
		testCon.commit();

		assertTrue(testCon.hasStatement(alice, RDF.TYPE, person, true));
	}

	@Test
	public void testMakeExplicit()
		throws Exception
	{
		testCon.begin();
		testCon.add(woman, RDFS.SUBCLASSOF, person);
		testCon.add(alice, RDF.TYPE, woman);
		testCon.commit();

		assertTrue(testCon.hasStatement(alice, RDF.TYPE, person, true));

		testCon.begin();
		testCon.add(alice, RDF.TYPE, person);
		testCon.commit();

		assertTrue(testCon.hasStatement(alice, RDF.TYPE, person, true));
	}

	@Test
	public void testExplicitFlag()
		throws Exception
	{
		RepositoryResult<Statement> result = testCon.getStatements(RDF.TYPE, RDF.TYPE, null, true);
		try {
			assertTrue("result should not be empty", result.hasNext());
		}
		finally {
			result.close();
		}

		result = testCon.getStatements(RDF.TYPE, RDF.TYPE, null, false);
		try {
			assertFalse("result should be empty", result.hasNext());
		}
		finally {
			result.close();
		}
	}

	@Test
	public void testInferencerUpdates()
		throws Exception
	{
		testCon.begin(IsolationLevels.READ_COMMITTED);

		testCon.add(bob, name, nameBob);
		testCon.remove(bob, name, nameBob);

		testCon.commit();

		assertFalse(testCon.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));
	}

	@Test
	public void testInferencerQueryDuringTransaction()
		throws Exception
	{
		testCon.begin();

		testCon.add(bob, name, nameBob);
		assertTrue(testCon.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));

		testCon.commit();
	}

	@Test
	public void testInferencerTransactionIsolation()
		throws Exception
	{
		if (IsolationLevels.NONE.isCompatibleWith(level)) {
			return;
		}
		testCon.begin();
		testCon.add(bob, name, nameBob);

		assertTrue(testCon.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));
		assertFalse(testCon2.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));

		testCon.commit();

		assertTrue(testCon.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));
		assertTrue(testCon2.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));
	}

	@Ignore
	@Test
	@Override
	public void testDefaultContext()
		throws Exception
	{
		// ignore
	}

	@Ignore
	@Test
	@Override
	public void testDefaultInsertContext()
		throws Exception
	{
		// ignore
	}

	@Ignore
	@Test
	@Override
	public void testExclusiveNullContext()
		throws Exception
	{
		// ignore
	}
}
