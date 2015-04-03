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
public class ReaderMonitor {

	int readingCount = 0;

	boolean doClose = false;

	// Remember index to be able to remove itself from the index list
	final private LuceneIndex index;

	/**
	 * IndexSearcher that can be used to read the current index' contents.
	 */
	private IndexReader indexReader;

	/**
	 * The IndexSearcher that can be used to query the current index' contents.
	 */
	private IndexSearcher indexSearcher;

	private IOException indexReaderSearcherCreateException;

	private boolean closed = false;

	/**
	 * If exception occur when create indexReader it will be thrown on
	 * getIndexReader or get IndexSearcher
	 * 
	 * @param index
	 * @param directory
	 *        Initializes IndexReader
	 */
	public ReaderMonitor(final LuceneIndex index, Directory directory) {
		this.index = index;
		try {
			indexReader = IndexReader.open(directory);
		}
		catch (IOException e) {
			indexReaderSearcherCreateException = e;
		}

		try {
			IndexReader reader = getIndexReader();
			indexSearcher = new IndexSearcher(reader);
		}
		catch (IOException e) {
			// do nothing exception was remembered
		}
	}

	/**
	 * 
	 */
	public void beginReading() {
		readingCount++;
	}

	/**
	 * called by the iterator
	 * 
	 * @throws IOException
	 */
	public void endReading()
		throws IOException
	{
		readingCount--;
		if (readingCount == 0 && doClose) {
			// when endReading is called on CurrentMonitor and it should be closed,
			// close it
			doClose();// close Lucene index remove them self from Lucene index
			synchronized (index.oldmonitors) {
				index.oldmonitors.remove(this); // if its not in the list, then this
															// is a no-operation
			}
		}
	}

	/**
	 * This method is called in LecenIndex invalidateReaders or on commit
	 * 
	 * @return <code>true</code> if the close succeeded, <code>false</code>
	 *         otherwise.
	 * @throws IOException
	 */
	public boolean closeWhenPossible()
		throws IOException
	{
		doClose = true;
		if (readingCount == 0) {
			doClose();
		}
		return closed;
	}

	/**
	 * @throws IOException
	 */
	public void doClose()
		throws IOException
	{
		try {
			try {
				if (indexSearcher != null) {
					indexSearcher.close();
				}
			}
			finally {
				if (indexReader != null) {
					indexReader.close();
				}
			}
		}
		finally {
			indexSearcher = null;
			indexReader = null;
		}
		closed = true;
	}

	// //////////////////////////////Methods for controlled index access

	protected IndexReader getIndexReader()
		throws IOException
	{
		if (indexReaderSearcherCreateException != null)
			throw indexReaderSearcherCreateException;
		return indexReader;
	}

	protected IndexSearcher getIndexSearcher()
		throws IOException
	{
		if (indexReaderSearcherCreateException != null)
			throw indexReaderSearcherCreateException;
		return indexSearcher;
	}

}
