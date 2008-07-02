/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail.nativerdf;

import java.io.File;

import info.aduna.io.FileUtil;

import org.openrdf.repository.RDFSchemaRepositoryConnectionTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.nativerdf.NativeStore;

public class RDFSchemaNativeRepositoryConnectionTest extends RDFSchemaRepositoryConnectionTest {

	private File dataDir;

	public RDFSchemaNativeRepositoryConnectionTest(String name) {
		super(name);
	}

	@Override
	protected Repository createRepository()
		throws Exception
	{
		dataDir = FileUtil.createTempDir("nativestore");
		return new SailRepository(new ForwardChainingRDFSInferencer(new NativeStore(dataDir, "spoc")));
	}
}
