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
//package org.openrdf.repository.sail.rdbms;
//
//import java.io.IOException;
//
//import org.openrdf.repository.Repository;
//import org.openrdf.repository.RepositoryConnectionTest;
//import org.openrdf.repository.sail.SailRepository;
//import org.openrdf.sail.rdbms.mysql.MySqlStore;
//
//public class MySqlStoreConnectionTest extends RepositoryConnectionTest {
//
//	public MySqlStoreConnectionTest(String name) {
//		super(name);
//	}
//
//	@Override
//	protected Repository createRepository()
//		throws IOException
//	{
//		MySqlStore sail = new MySqlStore("sesame_test");
//		sail.setUser("sesame");
//		sail.setPassword("opensesame");
//		return new SailRepository(sail);
//	}
//
//	@Override
//	public void testOrderByQueriesAreInterruptable() {
//		System.err.println("temporarily disabled testOrderByQueriesAreInterruptable() for RDBMS store");
//	}
//}
