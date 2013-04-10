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
package org.openrdf.sail.nativerdf;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.openrdf.sail.SailLockedException;

public class NativeStoreDirLockTest {

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	@Test
	public void testLocking()
		throws Exception
	{
		File dataDir = tempDir.newFolder("nativestore-dir-lock-test");
		NativeStore sail = null;

		try {
			sail = new NativeStore(dataDir, "spoc,posc");
			sail.initialize();

			NativeStore sail2 = null;
			try {
				sail2 = new NativeStore(dataDir, "spoc,posc");
				sail2.initialize();
				fail("initialized a second native store with same dataDir");
			}
			finally {
				if (sail2 != null) {
					sail2.shutDown();
				}
			}
		}
		catch (SailLockedException e) {
			// Expected: should not be able to open two native stores with the
			// same dataDir
		}
		finally {
			if (sail != null) {
				sail.shutDown();
			}
		}
	}
}
