/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import org.openrdf.sail.RDFStoreTest;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.rdbms.mysql.MySqlStore;

/**
 * An extension of RDFStoreTest for testing the class {@link MySqlStore}.
 */
//public class MySqlStoreTest extends RDFStoreTest {
//
//	/*--------------*
//	 * Constructors *
//	 *--------------*/
//
//	public MySqlStoreTest(String name) {
//		super(name);
//	}
//
//	/*---------*
//	 * Methods *
//	 *---------*/
//
//	@Override
//	protected Sail createSail()
//		throws SailException
//	{
//		MySqlStore sail = new MySqlStore("sesame_test");
//		sail.setUser("sesame");
//		sail.setPassword("opensesame");
//		sail.initialize();
//		SailConnection conn = sail.getConnection();
//		try {
//			conn.clear();
//			conn.clearNamespaces();
//			conn.commit();
//		}
//		finally {
//			conn.close();
//		}
//		return sail;
//	}
//}
