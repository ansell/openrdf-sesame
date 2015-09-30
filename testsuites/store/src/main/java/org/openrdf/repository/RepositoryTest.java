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
package org.openrdf.repository;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DC;

/**
 * Integration test suite for implementations of Repository.
 * 
 * @author Jeen Broekstra
 */
public abstract class RepositoryTest {

	/**
	 * Timeout all individual tests after 1 minute.
	 */
	@Rule
	public Timeout to = new Timeout(60000);

	private static final String MBOX = "mbox";

	private static final String NAME = "name";

	protected static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";

	public static final String TEST_DIR_PREFIX = "/testcases/";

	protected Repository testRepository;

	protected ValueFactory vf;

	protected Resource bob;

	protected Resource alice;

	protected Resource alexander;

	protected IRI name;

	protected IRI mbox;

	protected final IRI publisher = DC.PUBLISHER;

	protected IRI unknownContext;

	protected IRI context1;

	protected IRI context2;

	protected Literal nameAlice;

	protected Literal nameBob;

	protected Literal mboxAlice;

	protected Literal mboxBob;

	protected Literal Александър;

	@Before
	public void setUp()
		throws Exception
	{
		testRepository = createRepository();
		testRepository.initialize();

		vf = testRepository.getValueFactory();

		// Initialize values
		bob = vf.createBNode();
		alice = vf.createBNode();

		name = vf.createIRI(FOAF_NS + NAME);
		mbox = vf.createIRI(FOAF_NS + MBOX);

		nameAlice = vf.createLiteral("Alice");
		nameBob = vf.createLiteral("Bob");

		mboxAlice = vf.createLiteral("alice@example.org");
		mboxBob = vf.createLiteral("bob@example.org");

	}

	@After
	public void tearDown()
		throws Exception
	{
		testRepository.shutDown();
	}

	/**
	 * Gets an (uninitialized) instance of the repository that should be tested.
	 * 
	 * @return an uninitialized repository.
	 */
	protected abstract Repository createRepository()
		throws Exception;

	@Test
	public void testShutdownFollowedByInit()
		throws Exception
	{

		RepositoryConnection conn = testRepository.getConnection();
		try {
			conn.add(bob, mbox, mboxBob);
			assertTrue(conn.hasStatement(bob, mbox, mboxBob, true));
		}
		finally {
			conn.close();
		}
		
		testRepository.shutDown();
		testRepository.initialize();

		conn = testRepository.getConnection();
		try {
			conn.add(bob, mbox, mboxBob);
			assertTrue(conn.hasStatement(bob, mbox, mboxBob, true));
		}
		finally {
			conn.close();
		}
	}

}
