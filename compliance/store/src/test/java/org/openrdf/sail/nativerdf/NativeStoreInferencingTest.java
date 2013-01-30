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

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import info.aduna.io.FileUtil;

import org.openrdf.sail.InferencingTest;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;

public class NativeStoreInferencingTest extends TestCase {

	public static Test suite()
		throws SailException, IOException
	{
		final File dataDir = FileUtil.createTempDir("nativestore");
		NotifyingSail sailStack = new NativeStore(dataDir, "spoc,posc");
		sailStack = new ForwardChainingRDFSInferencer(sailStack);

		TestSuite suite = new TestSuite(NativeStoreInferencingTest.class.getName()) {

			@Override
			public void run(TestResult result) {
				try {
					super.run(result);
				}
				finally {
					try {
						FileUtil.deleteDir(dataDir);
					}
					catch (IOException e) {
					}
				}
			}
		};

		InferencingTest.addTests(suite, sailStack);
		return suite;
	}
}
