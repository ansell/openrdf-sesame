/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation;

import java.io.IOException;

import org.openrdf.repository.RDFSchemaRepositoryConnectionTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

public class RDFSchemaFederationRepositoryConnectionTest extends RDFSchemaRepositoryConnectionTest {

	public RDFSchemaFederationRepositoryConnectionTest(String name) {
		super(name);
	}

	@Override
	protected Repository createRepository()
		throws IOException
	{
		// Note: adding more members, combined with the round-robin algorithm for
		// distributing new statements, cause test failures due to the fact that
		// the inferencing happens locally.

		Federation sail = new Federation();
		sail.addMember(new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore())));
		return new SailRepository(sail);
	}
}
