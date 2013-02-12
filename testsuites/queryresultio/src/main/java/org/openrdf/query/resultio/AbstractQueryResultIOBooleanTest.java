/*
 * Licensed to Aduna under one or more contributor license agreements. See the NOTICE.txt file
 * distributed with this work for additional information regarding copyright ownership.
 * 
 * Aduna licenses this file to you under the terms of the Aduna BSD License (the "License"); you may
 * not use this file except in compliance with the License. See the LICENSE.txt file distributed
 * with this work for the full License.
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.openrdf.query.resultio;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultIO;

/**
 * Abstract test for QueryResultIO.
 * 
 * @author jeen
 * @author Peter Ansell
 */
public abstract class AbstractQueryResultIOBooleanTest {

	/**
	 * @return An example filename that will match the
	 *         {@link BooleanQueryResultFormat} returned by
	 *         {@link #getBooleanFormat()}.
	 */
	protected abstract String getFileName();

	/**
	 * @return The {@link BooleanQueryResultFormat} that this test is running
	 *         against.
	 */
	protected abstract BooleanQueryResultFormat getBooleanFormat();

	/**
	 * Test method for
	 * {@link org.openrdf.query.resultio.QueryResultIO#getParserFormatForFileName(java.lang.String)}
	 * .
	 */
	@Test
	public final void testGetParserFormatForFileNameString() {
		String fileName = getFileName();
		BooleanQueryResultFormat format = QueryResultIO.getBooleanParserFormatForFileName(fileName);

		assertNotNull("Could not find parser for this format.", format);
		assertEquals(getBooleanFormat(), format);
	}

	@Test
	public final void testBooleanResultFormat()
		throws IOException, QueryResultHandlerException, QueryResultParseException,
		UnsupportedQueryResultFormatException, QueryEvaluationException
	{
		testQueryResultFormat(getBooleanFormat(), true);
		testQueryResultFormat(getBooleanFormat(), false);
	}

	private void testQueryResultFormat(BooleanQueryResultFormat format, boolean input)
		throws IOException, QueryResultHandlerException, QueryResultParseException,
		UnsupportedQueryResultFormatException, QueryEvaluationException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		QueryResultIO.writeBoolean(input, format, out);
		out.flush();
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		boolean output = QueryResultIO.parse(in, format);

		assertEquals(output, input);
	}
}
