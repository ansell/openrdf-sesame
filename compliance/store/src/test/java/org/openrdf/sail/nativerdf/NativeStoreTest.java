/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;

import info.aduna.io.FileUtil;

import org.openrdf.StoreException;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.RDFNotifyingStoreTest;

/**
 * An extension of RDFStoreTest for testing the class {@link NativeStore}.
 */
public class NativeStoreTest extends RDFNotifyingStoreTest {

	/*-----------*
	 * Variables *
	 *-----------*/

	private File dataDir;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NativeStoreTest(String name) {
		super(name);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void setUp()
		throws Exception
	{
		dataDir = FileUtil.createTempDir("nativestore");
		super.setUp();
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		super.tearDown();
		FileUtil.deleteDir(dataDir);
	}

	@Override
	protected NotifyingSail createSail()
		throws StoreException
	{
		NotifyingSail sail = new NativeStore(dataDir, "spoc,posc");
		sail.initialize();
		return sail;
	}
}
