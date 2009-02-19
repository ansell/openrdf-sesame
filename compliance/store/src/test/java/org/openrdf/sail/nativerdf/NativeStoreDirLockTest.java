/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;

import junit.framework.TestCase;

import info.aduna.io.file.FileUtil;

import org.openrdf.sail.SailLockedException;

public class NativeStoreDirLockTest extends TestCase {

	public void testLocking()
		throws Exception
	{
		File dataDir = FileUtil.createTempDir("nativestore");
		try {
			NativeStore sail = new NativeStore(dataDir, "spoc,posc");
			sail.initialize();

			try {
				NativeStore sail2 = new NativeStore(dataDir, "spoc,posc");
				sail2.initialize();
				try {
					fail("initialized a second native store with same dataDir");
				}
				finally {
					sail2.shutDown();
				}
			}
			catch (SailLockedException e) {
				// Expected: should not be able to open two native stores with the
				// same dataDir
			}
			finally {
				sail.shutDown();
			}
		}
		finally {
			FileUtil.deleteDir(dataDir);
		}
	}
}
