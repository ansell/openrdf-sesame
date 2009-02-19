/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import junit.framework.TestSuite;

import org.openrdf.repository.EquivalentTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.mysql.MySqlStore;


public class MySqlEquivalentTest extends EquivalentTest {

	public static TestSuite suite() throws Exception {
		return EquivalentTest.suite(MySqlEquivalentTest.class);
	}

	public MySqlEquivalentTest() {
		super();
	}

	@Override
	protected Repository newRepository() {
		MySqlStore sail = new MySqlStore("sesame_test");
		sail.setUser("sesame");
		sail.setPassword("opensesame");
		return new SailRepository(sail);
	}

}
