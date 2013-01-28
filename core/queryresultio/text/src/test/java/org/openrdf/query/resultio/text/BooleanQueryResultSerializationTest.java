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
package org.openrdf.query.resultio.text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;

public class BooleanQueryResultSerializationTest extends TestCase {

	public void testTextResultFormat()
		throws IOException, QueryResultParseException, UnsupportedQueryResultFormatException,
		QueryEvaluationException
	{
		testQueryResultFormat(BooleanQueryResultFormat.TEXT, true);
		testQueryResultFormat(BooleanQueryResultFormat.TEXT, false);
	}

	private void testQueryResultFormat(BooleanQueryResultFormat format, boolean input)
		throws IOException, QueryResultParseException, UnsupportedQueryResultFormatException,
		QueryEvaluationException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		QueryResultIO.write(input, format, out);
		out.flush();
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		boolean output = QueryResultIO.parse(in, format);

		assertEquals(output, input);
	}
}
