/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import org.openrdf.sail.RDFStoreTest;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.rdbms.postgresql.PgSqlStore;
import org.openrdf.store.StoreException;

/**
 * An extension of RDFStoreTest for testing the class {@link PgSqlStore}.
 */
public class PgSqlStoreTest extends RDFStoreTest {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public PgSqlStoreTest(String name) {
		super(name);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected Sail createSail()
		throws StoreException
	{
		PgSqlStore sail = new PgSqlStore("sesame_test");
		sail.setUser("sesame");
		sail.setPassword("opensesame");
		sail.initialize();
		SailConnection conn = sail.getConnection();
		try {
			conn.removeStatements(null, null, null);
			conn.clearNamespaces();
			conn.commit();
		}
		finally {
			conn.close();
		}
		return sail;
	}
}
