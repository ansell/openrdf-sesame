/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;

import info.aduna.io.FileUtil;

import org.openrdf.sail.Sail;
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

	private File dataDir;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NativeStoreInterruptTest(String name) {
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
		throws SailException
	{
		return new NativeStore(dataDir, "spoc,posc");
	}
}
