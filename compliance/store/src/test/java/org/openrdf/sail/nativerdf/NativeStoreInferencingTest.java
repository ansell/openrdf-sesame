/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;

import info.aduna.io.file.FileUtil;

import org.openrdf.sail.InferencingTest;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.store.StoreException;

public class NativeStoreInferencingTest extends TestCase {

	private static File dataDir;

	public static Test suite()
		throws StoreException, IOException
	{
		dataDir = FileUtil.createTempDir("nativestore");
		NotifyingSail sailStack = new NativeStore(dataDir, "spoc,posc");
		sailStack = new ForwardChainingRDFSInferencer(sailStack);
		return InferencingTest.suite(sailStack, NativeStoreInferencingTest.class.getName());
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		if (dataDir != null) {
			FileUtil.deleteDir(dataDir);
			dataDir = null;
		}

		super.finalize();
	}
}
