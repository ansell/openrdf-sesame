/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail.rdbms;

import java.io.IOException;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.mysql.MySqlStore;

public class MySqlStoreConnectionTest extends RepositoryConnectionTest {

	public MySqlStoreConnectionTest(String name) {
		super(name);
	}

	@Override
	protected Repository createRepository() throws IOException {
		return new SailRepository(new MySqlStore("sesame_test"));
	}
}
