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
package org.openrdf.query.resultio.text.tsv;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;

/**
 * Custom tests for the SPARQL TSV writer.
 *
 * @author Peter Ansell
 */
public class SPARQLTSVCustomTest {

	/**
	 * Only Literals with the XML Schema numeric types should be simplified.
	 * <p>
	 * NOTE: This will fail when using RDF-1.1, as the datatype
	 * {@link XMLSchema#STRING} is implied and hence is not generally
	 * represented.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSES2126QuotedLiteralIntegerAsStringExplicitType()
		throws Exception
	{
		List<String> bindingNames = Arrays.asList("test");
		TupleQueryResult tqr = new TupleQueryResultImpl(bindingNames, Arrays.asList(new ListBindingSet(
				bindingNames, ValueFactoryImpl.getInstance().createLiteral("1", XMLSchema.STRING))));
		String result = writeTupleResult(tqr);
		assertEquals("?test\n\"1\"^^<http://www.w3.org/2001/XMLSchema#string>\n", result);
	}

	/**
	 * Only Literals with the XML Schema numeric types should be simplified.
	 * NOTE: This will fail when using RDF-1.1, as the datatype
	 * {@link XMLSchema#STRING} is implied and hence is not generally
	 * represented.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSES2126QuotedLiteralIntegerAsStringImplicitType()
		throws Exception
	{
		List<String> bindingNames = Arrays.asList("test");
		TupleQueryResult tqr = new TupleQueryResultImpl(bindingNames, Arrays.asList(new ListBindingSet(
				bindingNames, ValueFactoryImpl.getInstance().createLiteral("1"))));
		String result = writeTupleResult(tqr);
		assertEquals("?test\n\"1\"\n", result);
	}

	private String writeTupleResult(TupleQueryResult tqr)
		throws IOException, TupleQueryResultHandlerException, QueryEvaluationException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		QueryResultIO.write(tqr, TupleQueryResultFormat.TSV, output);
		String result = new String(output.toByteArray(), Charset.forName("UTF-8"));
		return result;
	}

}
