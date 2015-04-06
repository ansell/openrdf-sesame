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

/**
 * ReaderMonitor holds IndexReader and IndexSearcher. When ReaderMonitor is
 * closed it do not close IndexReader and IndexSearcher as long as someone reads
 * from them. Variable readingCount remember how many times it was read.
 * 
 * @author Tomasz Trela, DFKI Gmbh
 */
public abstract class AbstractReaderMonitor {

	int readingCount = 0;

	boolean doClose = false;

	// Remember index to be able to remove itself from the index list
	final private AbstractLuceneIndex index;

	private boolean closed = false;

	protected AbstractReaderMonitor(AbstractLuceneIndex index) {
		this.index = index;
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
			close();// close Lucene index remove them self from Lucene index
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
			close();
		}
		return closed;
	}

	public void close()
		throws IOException
	{
		if(!closed)
		{
			handleClose();
		}
		closed = true;
	}

	/**
	 * @throws IOException
	 */
	protected abstract void handleClose()
		throws IOException;
}
