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
package org.openrdf.sail.lucene4;

import static org.junit.Assert.assertEquals;
import static org.openrdf.sail.lucene.LuceneSailSchema.MATCHES;
import static org.openrdf.sail.lucene.LuceneSailSchema.QUERY;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.sail.lucene.AbstractLuceneSailTest;
import org.openrdf.sail.lucene.LuceneSail;

public class LuceneSailTest extends AbstractLuceneSailTest {
	private LuceneIndex index;

	protected void configure(LuceneSail sail) throws IOException
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

		connection.add(SUBJECT_1, PREDICATE_1, new LiteralImpl("sfourponecone"), CONTEXT_1);
		connection.add(SUBJECT_2, PREDICATE_1, new LiteralImpl("sfourponecone"), CONTEXT_1);
		connection.add(SUBJECT_2, PREDICATE_1, new LiteralImpl("sfourponectwo"), CONTEXT_1);
		connection.add(SUBJECT_2, PREDICATE_1, new LiteralImpl("sfourponectwo"), CONTEXT_1);

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
		connection.add(SUBJECT_2, PREDICATE_1, new LiteralImpl("sfourponecthree"), CONTEXT_1);
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
