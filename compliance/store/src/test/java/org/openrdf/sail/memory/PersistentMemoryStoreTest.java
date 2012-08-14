/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import java.io.File;
import java.io.IOException;

import info.aduna.io.FileUtil;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.RDFNotifyingStoreTest;
import org.openrdf.sail.SailException;

/**
 * An extension of RDFStoreTest for testing the class
 * <tt>org.openrdf.sesame.sail.memory.MemoryStore</tt>.
 */
public class PersistentMemoryStoreTest extends RDFNotifyingStoreTest {

	private volatile File dataDir;

	public PersistentMemoryStoreTest(String name) {
		super(name);
	}

	@Override
	protected NotifyingSail createSail()
		throws SailException
	{
		try {
			dataDir = FileUtil.createTempDir(PersistentMemoryStoreTest.class.getSimpleName());
			NotifyingSail sail = new MemoryStore(dataDir);
			sail.initialize();
			return sail;
		}
		catch (IOException e) {
			throw new SailException(e);
		}
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
}
