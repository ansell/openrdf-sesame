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

import java.io.IOException;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

/**
 * An extension of {@link MemoryStoreIsolationLevelTest} for testing the class
 * {@link MemoryStore} using on-disk persistence.
 */
public class PersistentMemoryStoreIsolationLevelTest extends MemoryStoreIsolationLevelTest {

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected Sail createSail()
		throws SailException
	{
		MemoryStore sail;
		try {
			sail = new MemoryStore(tempDir.newFolder("memory-store"));
		}
		catch (IOException e) {
			throw new AssertionError(e);
		}
		sail.setSyncDelay(100);
		return sail;
	}
}
