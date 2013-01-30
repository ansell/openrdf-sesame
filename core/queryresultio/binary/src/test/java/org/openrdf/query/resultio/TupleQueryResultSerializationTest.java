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
package org.openrdf.query.resultio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.impl.MutableTupleQueryResult;

public class TupleQueryResultSerializationTest extends TestCase {

	public void test3x2Result()
		throws Exception
	{
		MutableTupleQueryResult tqr = createQueryResult();
		testResultSerialization(tqr, TupleQueryResultFormat.BINARY);
	}

	public void testEmptyBindingSetResult()
		throws Exception
	{
		MutableTupleQueryResult tqr = createEmptyBindingSetResult();
		testResultSerialization(tqr, TupleQueryResultFormat.BINARY);
	}

	public void testEmptyQueryResult()
		throws Exception
	{
		MutableTupleQueryResult tqr = new MutableTupleQueryResult(Collections.<String> emptyList());
		testResultSerialization(tqr, TupleQueryResultFormat.BINARY);
	}

	private void testResultSerialization(MutableTupleQueryResult input, TupleQueryResultFormat format)
		throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		QueryResultIO.write(input, format, out);
		input.close();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleQueryResult output = QueryResultIO.parse(in, format);

		input.beforeFirst();
		assertTrue(QueryResults.equals(input, output));
	}

	private MutableTupleQueryResult createQueryResult() {
		List<String> bindingNames = Arrays.asList("a", "b", "c");
		MutableTupleQueryResult tqr = new MutableTupleQueryResult(bindingNames);

		MapBindingSet solution1 = new MapBindingSet(bindingNames.size());
		solution1.addBinding("a", new URIImpl("foo:bar"));
		solution1.addBinding("b", new BNodeImpl("bnode"));
		solution1.addBinding("c", new LiteralImpl("baz"));
		tqr.append(solution1);

		MapBindingSet solution2 = new MapBindingSet(bindingNames.size());
		solution2.addBinding("a", new LiteralImpl("1", XMLSchema.INTEGER));
		solution2.addBinding("c", new LiteralImpl("Hello World!", "en"));
		tqr.append(solution2);

		return tqr;
	}

	private MutableTupleQueryResult createEmptyBindingSetResult()
		throws Exception
	{
		List<String> bindingNames = Collections.<String> emptyList();
		MutableTupleQueryResult tqr = new MutableTupleQueryResult(bindingNames);

		tqr.append(EmptyBindingSet.getInstance());

		return tqr;
	}
}
