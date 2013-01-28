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
// SES-1071 disabling rdbms-based tests
//package org.openrdf.sail.rdbms;
//
//import org.openrdf.sail.RDFStoreTest;
//import org.openrdf.sail.Sail;
//import org.openrdf.sail.SailConnection;
//import org.openrdf.sail.SailException;
//import org.openrdf.sail.rdbms.postgresql.PgSqlStore;
//
///**
// * An extension of RDFStoreTest for testing the class {@link PgSqlStore}.
// */
//public class PgSqlStoreTest extends RDFStoreTest {
//
//	/*--------------*
//	 * Constructors *
//	 *--------------*/
//
//	public PgSqlStoreTest(String name) {
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
//		PgSqlStore sail = new PgSqlStore("sesame_test");
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
