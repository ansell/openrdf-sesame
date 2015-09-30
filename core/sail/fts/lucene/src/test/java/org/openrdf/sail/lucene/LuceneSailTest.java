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

import static org.junit.Assert.assertEquals;
import static org.openrdf.sail.lucene.LuceneSailSchema.MATCHES;
import static org.openrdf.sail.lucene.LuceneSailSchema.QUERY;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;

public class LuceneSailTest extends AbstractLuceneSailTest {

	private LuceneIndex index;

	protected void configure(LuceneSail sail)
		throws IOException
	{
		index = new LuceneIndex(new RAMDirectory(), new StandardAnalyzer());
		sail.setLuceneIndex(index);
	}

	/**
	 * This test simulates possible flow of calls to the LuceneIndex. It assert
	 * does InexReader and IndexSearcher are not closed while iterating but are
	 * finally close.
	 *
	 * @throws Exception
	 */
	@Test
	public void testClosingIndexReaderAndSearcherAllCases()
		throws Exception
	{

		connection.add(SUBJECT_1, PREDICATE_1, vf.createLiteral("sfourponecone"), CONTEXT_1);
		connection.add(SUBJECT_2, PREDICATE_1, vf.createLiteral("sfourponecone"), CONTEXT_1);
		connection.add(SUBJECT_2, PREDICATE_1, vf.createLiteral("sfourponectwo"), CONTEXT_1);
		connection.add(SUBJECT_2, PREDICATE_1, vf.createLiteral("sfourponectwo"), CONTEXT_1);

		connection.commit();
		assertEquals(0, index.getOldMonitors().size());
		assertEquals(null, index.currentMonitor);
		// prepare the query

		// First search on the LuceneIndex
		String queryString = "SELECT Resource FROM {Resource} <" + MATCHES + "> {}  <" + QUERY
				+ "> {\"sfourponecone\"} ";
		TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SERQL, queryString);
		TupleQueryResult result1 = query.evaluate();

		assertEquals(0, index.getOldMonitors().size());
		assertEquals(1, index.currentMonitor.getReadingCount());
		// check the results is not needed, just assert iterator is not closed
		// assertTrue(result1.hasNext());
		// result1.next();

		// Second search on the LuceneIndex
		queryString = "SELECT Resource FROM {Resource} <" + MATCHES + "> {}  <" + QUERY
				+ "> {\"sfourponecone\"} ";
		query = connection.prepareTupleQuery(QueryLanguage.SERQL, queryString);
		TupleQueryResult result2 = query.evaluate();

		assertEquals(0, index.getOldMonitors().size());
		assertEquals(2, index.currentMonitor.getReadingCount());
		// CHECK value of the CurrentReader readersCount

		// check the results is not needed, just assert iterator is not closed
		// assertTrue(result2.hasNext());
		// result2.next();

		// CHECK value of the CurrentReader readersCount
		assertEquals(0, index.getOldMonitors().size());
		assertEquals(2, index.currentMonitor.getReadingCount());

		// empty commit without changes do not invalidate readers
		connection.commit();

		// CHECK value of the CurrentReader readersCount
		assertEquals(0, index.getOldMonitors().size());
		assertEquals(2, index.currentMonitor.getReadingCount());

		// This should invalidate readers
		connection.add(SUBJECT_2, PREDICATE_1, vf.createLiteral("sfourponecthree"), CONTEXT_1);
		connection.commit();
		// But readers can not be closed, they are being iterated
		assertEquals(1, index.getOldMonitors().size());
		assertEquals(null, index.currentMonitor);

		// Third search on the index should create new urrent ReaderMonitor
		queryString = "SELECT Resource FROM {Resource} <" + MATCHES + "> {}  <" + QUERY
				+ "> {\"sfourponecone\"} ";
		query = connection.prepareTupleQuery(QueryLanguage.SERQL, queryString);
		TupleQueryResult result3 = query.evaluate();

		assertEquals(1, index.getOldMonitors().size());
		assertEquals(1, index.currentMonitor.getReadingCount());

		// When iteration is finish remove old monitor
		result1.close();
		assertEquals(1, index.getOldMonitors().size());
		result2.close();
		assertEquals(1, index.getOldMonitors().size());
		// current monitor is not removed, there is no need
		result3.close();

		connection.close();
		assertEquals(0, index.currentMonitor.getReadingCount());

	}
}
