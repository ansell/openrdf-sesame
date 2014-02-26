/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;

import org.openrdf.sail.SailException;


/**
 * LuceneSail which uses a NIOFSDirectory instead of MMapDirectory to avoid the JVM crash
 * (see <a href="http://stackoverflow.com/questions/8224843/jvm-crashes-on-lucene-datainput-readvint">http://stackoverflow.com/questions/8224843/jvm-crashes-on-lucene-datainput-readvint</a>).
 *
 * @author andriy.nikolov
 */
public class LuceneSailNIOFS extends LuceneSail {

	@Override
	protected void initializeLuceneIndex(Analyzer analyzer)
		throws SailException, IOException
	{
		if (parameters.containsKey(LUCENE_DIR_KEY)) {
			FSDirectory dir = new NIOFSDirectory(new File(parameters.getProperty(LUCENE_DIR_KEY)), null);
			setLuceneIndex(new LuceneIndex(dir,analyzer));
		} else
			super.initializeLuceneIndex(analyzer);
	}
}