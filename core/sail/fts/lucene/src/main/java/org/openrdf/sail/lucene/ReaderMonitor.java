/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.lucene;

import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

/**
 * ReaderMonitor holds IndexReader and IndexSearcher. When ReaderMonitor is
 * closed it do not close IndexReader and IndexSearcher as long as someone reads
 * from them. Variable readingCount remember how many times it was read.
 * 
 * @author Tomasz Trela, DFKI Gmbh
 */
public class ReaderMonitor extends AbstractReaderMonitor {

	/**
	 * The IndexSearcher that can be used to query the current index' contents.
	 */
	private IndexSearcher indexSearcher;

	private IOException indexSearcherCreateException;

	/**
	 * If exception occur when create indexReader it will be thrown on
	 * getIndexReader or get IndexSearcher
	 * 
	 * @param index
	 * @param directory
	 *        Initializes IndexReader
	 */
	public ReaderMonitor(final LuceneIndex index, Directory directory) {
		super(index);
		try {
			IndexReader indexReader = DirectoryReader.open(directory);
			indexSearcher = new IndexSearcher(indexReader);
		}
		catch (IOException e) {
			indexSearcherCreateException = e;
		}
	}

	/**
	 * @throws IOException
	 */
	@Override
	protected void handleClose()
		throws IOException
	{
		try {
			if (indexSearcher != null) {
				indexSearcher.getIndexReader().close();
			}
		}
		finally {
			indexSearcher = null;
		}
	}

	// //////////////////////////////Methods for controlled index access

	protected IndexSearcher getIndexSearcher()
		throws IOException
	{
		if (indexSearcherCreateException != null)
			throw indexSearcherCreateException;
		return indexSearcher;
	}

}
