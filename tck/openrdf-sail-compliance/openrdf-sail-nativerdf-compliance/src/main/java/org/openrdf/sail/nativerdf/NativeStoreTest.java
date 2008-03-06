/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import org.openrdf.sail.RDFStoreTest;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

import info.aduna.io.FileUtil;

import java.io.File;

/**
 * An extension of RDFStoreTest for testing the class {@link NativeStore}.
 */
public class NativeStoreTest extends RDFStoreTest {

	/*-----------*
	 * Variables *
	 *-----------*/

	private File _dataDir;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NativeStoreTest(String name) {
		super(name);
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected void setUp()
		throws Exception
	{
		_dataDir = FileUtil.createTempDir("nativestore");
		super.setUp();
	}

	protected void tearDown()
		throws Exception
	{
		super.tearDown();
		FileUtil.deleteDir(_dataDir);
	}

	protected Sail createSail()
		throws SailException
	{
		Sail sail = new NativeStore(_dataDir, "spoc,posc");
		sail.initialize();
		return sail;
	}
}
