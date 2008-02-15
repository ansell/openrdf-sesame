/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.mysql;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.RdbmsStore;

public class MySqlConnectionTest extends RepositoryConnectionTest {

	public MySqlConnectionTest(String name) {
		super(name);
	}

	@Override
	protected Repository createRepository() throws IOException {
		enableLogging("org.openrdf");
		RdbmsStore sail = new MySqlStore("sesame_test");
		return new SailRepository(sail);
	}

	private void enableLogging(String pkg) {
		Logger logger = Logger.getLogger(pkg);
		ConsoleHandler handler = new ConsoleHandler();
		logger.addHandler(handler);
		handler.setLevel(Level.FINE);
		logger.setLevel(Level.FINE);
	}
}
