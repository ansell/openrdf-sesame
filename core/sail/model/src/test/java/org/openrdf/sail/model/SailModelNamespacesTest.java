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
package org.openrdf.sail.model;

import org.openrdf.model.Model;
import org.openrdf.model.ModelNamespacesTest;
import org.openrdf.model.util.ModelException;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;


/**
 *
 * @author Mark
 */
public class SailModelNamespacesTest extends ModelNamespacesTest {
	private Sail sail;
	private SailConnection conn;

	@Override
	protected Model getModelImplementation() {
		sail = new MemoryStore();
		try {
			sail.initialize();
			conn = sail.getConnection();
			conn.begin();
			return new SailModel(conn, false);
		} catch (SailException e) {
			throw new ModelException(e);
		}
	}

	@Override
	public void tearDown() throws Exception {
		if(conn != null) {
			conn.commit();
			conn.close();
			conn = null;
		}
		if(sail != null) {
			sail.shutDown();
			sail = null;
		}
		super.tearDown();
	}
}
