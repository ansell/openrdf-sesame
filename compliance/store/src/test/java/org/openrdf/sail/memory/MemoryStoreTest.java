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
package org.openrdf.sail.memory;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.RDFNotifyingStoreTest;
import org.openrdf.sail.SailException;


/**
 * An extension of RDFStoreTest for testing the class
 * <tt>org.openrdf.sesame.sail.memory.MemoryStore</tt>.
 */
public class MemoryStoreTest extends RDFNotifyingStoreTest {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MemoryStoreTest(String name) {
		super(name);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected NotifyingSail createSail()
		throws SailException
	{
		NotifyingSail sail = new MemoryStore();
		sail.initialize();
		return sail;
	}
}
