/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail.nativerdf;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;

import info.aduna.io.file.FileUtil;

import org.openrdf.query.Dataset;
import org.openrdf.query.parser.sparql.ManifestTest;
import org.openrdf.query.parser.sparql.SPARQLQueryTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.dataset.DatasetRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

public class NativeSPARQLQueryTest extends SPARQLQueryTest {

	public static Test suite()
		throws Exception
	{
		return ManifestTest.suite(new Factory() {

			public NativeSPARQLQueryTest createSPARQLQueryTest(String testURI, String name, String queryFileURL,
					String resultFileURL, Dataset dataSet, boolean laxCardinality)
			{
				return new NativeSPARQLQueryTest(testURI, name, queryFileURL, resultFileURL, dataSet,
						laxCardinality);
			}
		});
	}

	private File dataDir;

	protected NativeSPARQLQueryTest(String testURI, String name, String queryFileURL, String resultFileURL,
			Dataset dataSet, boolean laxCardinality)
	{
		super(testURI, name, queryFileURL, resultFileURL, dataSet, laxCardinality);
	}

	@Override
	protected Repository newRepository()
		throws IOException
	{
		dataDir = FileUtil.createTempDir("nativestore");
		return new DatasetRepository(new SailRepository(new NativeStore(dataDir, "spoc")));
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
