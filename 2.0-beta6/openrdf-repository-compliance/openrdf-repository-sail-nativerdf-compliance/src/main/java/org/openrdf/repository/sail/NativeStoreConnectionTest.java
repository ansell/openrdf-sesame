/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import info.aduna.io.FileUtil;

import java.io.File;
import java.io.IOException;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;
import org.openrdf.sail.nativerdf.NativeStore;

public class NativeStoreConnectionTest extends RepositoryConnectionTest {

	private File _dataDir;

	public NativeStoreConnectionTest(String name) {
		super(name);
	}

	protected Repository createRepository()
		throws IOException
	{
		_dataDir = FileUtil.createTempDir("nativestore");
		return new SailRepository(new NativeStore(_dataDir, "spoc"));
	}

	protected void tearDown()
		throws Exception
	{
		try {
			super.tearDown();
		}
		finally {
			FileUtil.deleteDir(_dataDir);
		}
	}
}
