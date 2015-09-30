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
package org.eclipse.rdf4j.sail.lucene;

import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.eclipse.rdf4j.sail.lucene.AbstractReaderMonitor;

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
