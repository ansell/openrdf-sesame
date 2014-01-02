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

import java.io.IOException;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.SailConcurrencyTest;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailInterruptTest;

/**
 * An extension of {@link SailConcurrencyTest} for testing the class
 * {@link NativeStore}.
 */
public class NativeStoreInterruptTest extends SailInterruptTest {

	/*-----------*
	 * Variables *
	 *-----------*/

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected NotifyingSail createSail()
		throws SailException
	{
		try {
			return new NativeStore(tempDir.newFolder("nativestore"), "spoc,posc");
		}
		catch (IOException e) {
			throw new AssertionError(e);
		}
	}

}
