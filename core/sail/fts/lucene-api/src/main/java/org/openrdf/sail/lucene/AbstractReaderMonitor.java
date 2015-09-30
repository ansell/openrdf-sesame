/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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

	private int readingCount = 0;

	private boolean doClose = false;

	// Remember index to be able to remove itself from the index list
	final private AbstractLuceneIndex index;

	private boolean closed = false;

	protected AbstractReaderMonitor(AbstractLuceneIndex index) {
		this.index = index;
	}

	public int getReadingCount() {
		return readingCount;
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
