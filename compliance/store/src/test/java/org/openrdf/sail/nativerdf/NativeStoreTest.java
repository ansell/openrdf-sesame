/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;

import info.aduna.io.FileUtil;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.RDFNotifyingStoreTest;
import org.openrdf.store.StoreException;

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
		try {
			super.tearDown();
		}
		finally {
			FileUtil.deleteDir(dataDir);
		}
	}

	@Override
	protected NotifyingSail createSail()
		throws StoreException
	{
		NotifyingSail sail = new NativeStore(dataDir, "spoc,posc");
		sail.initialize();
		return sail;
	}

	// Test for SES-542
	public void testGetNamespacePersistence()
		throws Exception
	{
		con.setNamespace("rdf", RDF.NAMESPACE);
		con.commit();
		assertEquals(RDF.NAMESPACE, con.getNamespace("rdf"));

		con.close();
		sail.shutDown();
		sail.initialize();
		con = sail.getConnection();

		assertEquals(RDF.NAMESPACE, con.getNamespace("rdf"));
	}

}
