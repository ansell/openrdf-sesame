/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2010.
 *
 * Licensed under the Aduna BSD-style license.
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
