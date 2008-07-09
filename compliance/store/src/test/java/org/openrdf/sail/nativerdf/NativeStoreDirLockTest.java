/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import info.aduna.io.FileUtil;

public class NativeStoreDirLockTest extends TestCase {

	public void testLocking() throws Exception {
		File dataDir = FileUtil.createTempDir("nativestore");
		NativeStore sail = new NativeStore(dataDir, "spoc,posc");
		sail.initialize();
		try {
			NativeStore sail2 = new NativeStore(dataDir, "spoc,posc");
			sail2.initialize();
			fail("initialised a second native store with same dataDir");
		} catch (AssertionFailedError fail) {
			throw fail;
		} catch (Throwable t) {
			// cannot initialise two native stores with the same dataDir
		}
		sail.shutDown();
	}
}
