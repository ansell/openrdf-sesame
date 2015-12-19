package org.openrdf.query.resultio.sparqlxml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerBase;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLParser;

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

/**
 * @author James Leigh
 */
public class SPARQLResultsXMLParserTest {

	@Test
	public void testlocalname()
		throws Exception
	{
		assertEquals(4, countSolutions("localname-result.srx"));
	}

	@Test
	public void testNamespace()
		throws Exception
	{
		assertEquals(4, countSolutions("namespace-result.srx"));
	}

	private int countSolutions(String name)
		throws Exception
	{
		SPARQLResultsXMLParser parser = new SPARQLResultsXMLParser();
		CountingTupleQueryResultHandler countingHandler = new CountingTupleQueryResultHandler();
		parser.setTupleQueryResultHandler(countingHandler);
		InputStream in = SPARQLResultsXMLParserTest.class.getClassLoader().getResourceAsStream(name);
		assertNotNull(name + " is missing", in);
		try {
			parser.parseQueryResult(in);
		} finally {
			in.close();
		}
		return countingHandler.getCount();
	}

	static class CountingTupleQueryResultHandler extends TupleQueryResultHandlerBase
	{
		private int count;

		public int getCount()
		{
			return count;
		}

		public void handleSolution(BindingSet bindingSet)
			throws TupleQueryResultHandlerException
		{
			count++;
		}
	}
}
