/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * @author jeen
 */
public abstract class InferencingRepositoryConnectionTest extends RepositoryConnectionTest {

	private URI person;

	private URI woman;

	private URI man;

	public InferencingRepositoryConnectionTest(String name) {
		super(name);
	}

	@Override
	public void setUp()
		throws Exception
	{
		super.setUp();

		person = vf.createURI(FOAF_NS + "Person");
		woman = vf.createURI("http://example.org/Woman");
		man = vf.createURI("http://example.org/Man");
	}

	public void testDomainInference()
		throws Exception
	{
		testCon.add(name, RDFS.DOMAIN, person);
		testCon.add(bob, name, nameBob);

		assertTrue(testCon.hasStatement(bob, RDF.TYPE, person, true));
	}

	public void testSubClassInference()
		throws Exception
	{

		testCon.add(woman, RDFS.SUBCLASSOF, person);
		testCon.add(man, RDFS.SUBCLASSOF, person);

		testCon.add(alice, RDF.TYPE, woman);

		assertTrue(testCon.hasStatement(alice, RDF.TYPE, person, true));
	}

	public void testExplicitFlag()
		throws Exception
	{
		RepositoryResult<Statement> result = testCon.getStatements(RDF.TYPE, RDF.TYPE, null, true);

		boolean hasResults = result.hasNext();
		result.close();

		assertTrue("result should not be empty", hasResults);

		result = testCon.getStatements(RDF.TYPE, RDF.TYPE, null, false);

		hasResults = result.hasNext();
		result.close();

		assertFalse("result should be empty", hasResults);
	}
}
