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
package org.openrdf.query.resultio.sparqljson;

import static org.junit.Assert.*;

import org.junit.Test;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.resultio.AbstractQueryResultIOBooleanTest;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.helpers.QueryResultCollector;

/**
 * @author Peter Ansell
 * @author Sebastian Schaffert
 */
public class SPARQLJSONBooleanTest extends AbstractQueryResultIOBooleanTest {

	@Override
	protected String getFileName() {
		return "test.srj";
	}

	@Override
	protected BooleanQueryResultFormat getBooleanFormat() {
		return BooleanQueryResultFormat.JSON;
	}

	@Override
	protected TupleQueryResultFormat getMatchingTupleFormatOrNull() {
		return TupleQueryResultFormat.JSON;
	}

	@Test
	public void testBoolean1()
		throws Exception
	{
		SPARQLBooleanJSONParser parser = new SPARQLBooleanJSONParser(ValueFactoryImpl.getInstance());
		QueryResultCollector handler = new QueryResultCollector();
		parser.setQueryResultHandler(handler);

		parser.parseQueryResult(this.getClass().getResourceAsStream("/sparqljson/boolean1.srj"));

		assertTrue(handler.getBoolean());
	}

	@Test
	public void testBoolean2()
		throws Exception
	{
		SPARQLBooleanJSONParser parser = new SPARQLBooleanJSONParser(ValueFactoryImpl.getInstance());
		QueryResultCollector handler = new QueryResultCollector();
		parser.setQueryResultHandler(handler);

		parser.parseQueryResult(this.getClass().getResourceAsStream("/sparqljson/boolean2.srj"));

		assertTrue(handler.getBoolean());
	}

	@Test
	public void testBoolean3()
		throws Exception
	{
		SPARQLBooleanJSONParser parser = new SPARQLBooleanJSONParser(ValueFactoryImpl.getInstance());
		QueryResultCollector handler = new QueryResultCollector();
		parser.setQueryResultHandler(handler);

		parser.parseQueryResult(this.getClass().getResourceAsStream("/sparqljson/boolean3.srj"));

		assertFalse(handler.getBoolean());
	}

	@Test
	public void testBoolean4()
		throws Exception
	{
		SPARQLBooleanJSONParser parser = new SPARQLBooleanJSONParser(ValueFactoryImpl.getInstance());
		QueryResultCollector handler = new QueryResultCollector();
		parser.setQueryResultHandler(handler);

		parser.parseQueryResult(this.getClass().getResourceAsStream("/sparqljson/boolean4.srj"));

		assertFalse(handler.getBoolean());
	}

}
