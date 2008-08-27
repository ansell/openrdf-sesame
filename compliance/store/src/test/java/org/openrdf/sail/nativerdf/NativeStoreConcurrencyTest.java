/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;

import info.aduna.io.FileUtil;

import org.openrdf.StoreException;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConcurrencyTest;

/**
 * An extension of {@link SailConcurrencyTest} for testing the class
 * {@link NativeStore}.
 */
public class NativeStoreConcurrencyTest extends SailConcurrencyTest {

	/*-----------*
	 * Variables *
	 *-----------*/

	private File dataDir;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NativeStoreConcurrencyTest(String name) {
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
		try {
			super.tearDown();
		}
		finally {
			FileUtil.deleteDir(dataDir);
		}
	}

	@Override
	protected Sail createSail()
		throws StoreException
	{
		return new NativeStore(dataDir, "spoc,posc");
	}
}
