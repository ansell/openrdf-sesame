/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.result.util.QueryResultUtil;
import org.openrdf.store.StoreException;

public class TupleQueryResultSerializationTest extends TestCase {

	public void testQueryResult()
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException,
		UnsupportedQueryResultFormatException, StoreException
	{
		testQueryResult(TupleQueryResultFormat.BINARY);
	}

	public void testSingletonQueryResult()
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException,
		UnsupportedQueryResultFormatException, StoreException
	{
		testSingletonQueryResult(TupleQueryResultFormat.BINARY);
	}

	private void testQueryResult(TupleQueryResultFormat format)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException,
		UnsupportedQueryResultFormatException, StoreException
	{
		TupleResult input = createQueryResult();
		TupleResult expected = createQueryResult();

		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		QueryResultIO.write(input, format, out);
		input.close();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleResult output = QueryResultIO.parse(in, format);

		assertTrue(QueryResultUtil.equals(expected, output));
	}

	private TupleResult createQueryResult() {
		List<String> bindingNames = Arrays.asList("a", "b", "c");

		MapBindingSet solution1 = new MapBindingSet(bindingNames.size());
		solution1.addBinding("a", new URIImpl("foo:bar"));
		solution1.addBinding("b", new BNodeImpl("bnode"));
		solution1.addBinding("c", new LiteralImpl("baz"));

		MapBindingSet solution2 = new MapBindingSet(bindingNames.size());
		solution2.addBinding("a", new LiteralImpl("1", XMLSchema.INTEGER));
		solution2.addBinding("c", new LiteralImpl("Hello World!", "en"));

		List<? extends BindingSet> bindingSetList = Arrays.asList(solution1, solution2);

		return new TupleResultImpl(bindingNames, bindingSetList);
	}

	private void testSingletonQueryResult(TupleQueryResultFormat format)
		throws StoreException, TupleQueryResultHandlerException, UnsupportedQueryResultFormatException,
		IOException, QueryResultParseException
	{
		TupleResult input = createSingletonQueryResult();
		TupleResult expected = createSingletonQueryResult();

		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		QueryResultIO.write(input, format, out);
		input.close();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleResult output = QueryResultIO.parse(in, format);

		assertTrue(QueryResultUtil.equals(expected, output));
	}

	private TupleResult createSingletonQueryResult() {
		List<String> bindingNames = Collections.emptyList();

		BindingSet solution = EmptyBindingSet.getInstance();
		List<? extends BindingSet> bindingSets = Arrays.asList(solution);

		return new TupleResultImpl(bindingNames, bindingSets);
	}
}
