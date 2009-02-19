/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import java.io.IOException;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.postgresql.PgSqlStore;

public class PgSqlStoreConnectionTest extends RepositoryConnectionTest {

	public PgSqlStoreConnectionTest(String name) {
		super(name);
	}

	@Override
	protected Repository createRepository()
		throws IOException
	{
		PgSqlStore sail = new PgSqlStore("sesame_test");
		sail.setUser("sesame");
		sail.setPassword("opensesame");
		return new SailRepository(sail);
	}
}
