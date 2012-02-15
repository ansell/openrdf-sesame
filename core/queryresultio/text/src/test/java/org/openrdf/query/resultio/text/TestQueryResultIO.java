/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.text;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;


/**
 *
 * @author jeen
 */
public class TestQueryResultIO {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
	}

	/**
	 * Test method for {@link org.openrdf.query.resultio.QueryResultIO#getParserFormatForFileName(java.lang.String)}.
	 */
	@Test
	public void testGetParserFormatForFileNameString() {
		String fileName = "foo.csv";
		TupleQueryResultFormat format = QueryResultIO.getParserFormatForFileName(fileName);
		
		assertNotNull(format);
		assertEquals(TupleQueryResultFormat.CSV, format);
	}

}
