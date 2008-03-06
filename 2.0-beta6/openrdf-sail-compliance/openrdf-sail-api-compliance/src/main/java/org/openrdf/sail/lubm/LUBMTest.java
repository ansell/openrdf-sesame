/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.lubm;

import info.aduna.io.FileUtil;

import java.io.File;

import junit.framework.TestCase;
import edu.lehigh.swat.bench.uba.Generator;

/**
 * 
 */
public class LUBMTest extends TestCase {

	public LUBMTest(String name) {
		super(name);
	}
	
	public void test1()
		throws Exception
	{
		File tmpDir = FileUtil.createTempDir("lubm-data");
		_generateDataFiles(tmpDir);
	}
	
	private void _generateDataFiles(File dataDir) {
		// LUBM Generator writes the data files in the directory
		// indicated by the "user.dir" system property
		String userDir = System.getProperty("user.dir");
		System.setProperty("user.dir", dataDir.getPath());
	
		int univNum = 1;
		int startIndex = 0;
		int seed = 0;
		boolean daml = false;
		String ontology = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl";

		System.out.println("Generating data files in directory " + dataDir);
		
		Generator dataGenerator = new Generator();
		dataGenerator.start(univNum, startIndex, seed, daml, ontology);
		
		System.setProperty("user.dir", userDir);
	}	
}
