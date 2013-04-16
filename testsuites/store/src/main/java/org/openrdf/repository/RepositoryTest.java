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
package org.openrdf.repository;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DC;

/**
 * Integration test suite for implementations of Repository.
 * 
 * @author Jeen Broekstra
 */
public abstract class RepositoryTest {

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

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

	protected URI name;

	protected URI mbox;

	protected final URI publisher = DC.PUBLISHER;

	protected URI unknownContext;

	protected URI context1;

	protected URI context2;

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

		name = vf.createURI(FOAF_NS + NAME);
		mbox = vf.createURI(FOAF_NS + MBOX);

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
		testRepository.shutDown();
		testRepository.initialize();

		RepositoryConnection conn = testRepository.getConnection();
		try {
			conn.add(bob, mbox, mboxBob);
			assertTrue(conn.hasStatement(bob, mbox, mboxBob, true));
		}
		finally {
			conn.close();
		}
	}

}
