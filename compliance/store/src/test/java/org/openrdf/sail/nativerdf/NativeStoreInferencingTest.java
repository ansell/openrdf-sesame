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

import static org.junit.Assume.assumeNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import info.aduna.io.FileUtil;

import org.openrdf.sail.InferencingTest;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.Sail;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;

public class NativeStoreInferencingTest extends InferencingTest {

	private File dataDir;

	@Before
	public void setUp()
		throws IOException
	{
		dataDir = FileUtil.createTempDir("nativestore");
		assumeNotNull("could not create datadir", dataDir);
	}

	@After
	public void tearDown()
		throws IOException
	{
		if (dataDir != null)
			FileUtil.deleteDir(dataDir);
	}

	@Override
	protected Sail createSail() {
		NotifyingSail sailStack = new NativeStore(dataDir, "spoc,posc");
		sailStack = new ForwardChainingRDFSInferencer(sailStack);
		return sailStack;
	}
}
