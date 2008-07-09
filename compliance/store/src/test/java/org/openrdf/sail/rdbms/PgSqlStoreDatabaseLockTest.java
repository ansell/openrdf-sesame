/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.openrdf.sail.rdbms.postgresql.PgSqlStore;

public class PgSqlStoreDatabaseLockTest extends TestCase {

	public void testLocking() throws Exception {
		PgSqlStore sail = new PgSqlStore("sesame_test");
		sail.initialize();
		try {
			PgSqlStore sail2 = new PgSqlStore("sesame_test");
			sail2.initialize();
			fail("initialised a second store with same database");
		} catch (AssertionFailedError fail) {
			throw fail;
		} catch (Throwable t) {
			// cannot initialise two native stores with the same dataDir
		}
		sail.shutDown();
	}
}
