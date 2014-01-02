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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;

/**
 * @author Peter Ansell
 */
public abstract class AbstractQueryResultIOTest {

	/**
	 * 
	 */
	public AbstractQueryResultIOTest() {
		super();
	}

	/**
	 * @return An example filename that will match the {@link QueryResultFormat}
	 *         returned by {@link #getFormat()}.
	 */
	protected abstract String getFileName();

	protected abstract QueryResultFormat getFormat();

	/**
	 * Test method for
	 * {@link org.openrdf.query.resultio.QueryResultIO#getParserFormatForFileName(java.lang.String)}
	 * .
	 */
	@Test
	public final void testGetParserFormatForFileNameString()
		throws Exception
	{
		String fileName = getFileName();

		QueryResultFormat format;

		if (getFormat() instanceof TupleQueryResultFormat) {
			format = QueryResultIO.getParserFormatForFileName(fileName);
		}
		else {
			format = QueryResultIO.getBooleanParserFormatForFileName(fileName);
		}

		assertNotNull("Could not find parser for this format.", format);
		assertEquals(getFormat(), format);
	}

	protected TupleQueryResult createTupleSingleVarMultipleBindingSets() {
		List<String> bindingNames = Arrays.asList("a");

		MapBindingSet solution1 = new MapBindingSet(bindingNames.size());
		solution1.addBinding("a", new URIImpl("foo:bar"));

		MapBindingSet solution2 = new MapBindingSet(bindingNames.size());
		solution2.addBinding("a", new LiteralImpl("2.0", XMLSchema.DOUBLE));

		MapBindingSet solution3 = new MapBindingSet(bindingNames.size());
		solution3.addBinding("a", new BNodeImpl("bnode3"));

		MapBindingSet solution4 = new MapBindingSet(bindingNames.size());
		solution4.addBinding("a", new LiteralImpl("''single-quoted string", XMLSchema.STRING));

		MapBindingSet solution5 = new MapBindingSet(bindingNames.size());
		solution5.addBinding("a", new LiteralImpl("\"\"double-quoted string", XMLSchema.STRING));

		MapBindingSet solution6 = new MapBindingSet(bindingNames.size());
		solution6.addBinding("a", new LiteralImpl("space at the end         ", XMLSchema.STRING));

		MapBindingSet solution7 = new MapBindingSet(bindingNames.size());
		solution7.addBinding("a", new LiteralImpl("space at the end         ", XMLSchema.STRING));

		MapBindingSet solution8 = new MapBindingSet(bindingNames.size());
		solution8.addBinding("a", new LiteralImpl("\"\"double-quoted string with no datatype"));

		MapBindingSet solution9 = new MapBindingSet(bindingNames.size());
		solution9.addBinding("a", new LiteralImpl("newline at the end \n", XMLSchema.STRING));

		List<? extends BindingSet> bindingSetList = Arrays.asList(solution1, solution2, solution3, solution4,
				solution5, solution6, solution7 , solution8, solution9);

		TupleQueryResultImpl result = new TupleQueryResultImpl(bindingNames, bindingSetList);

		return result;
	}

	protected TupleQueryResult createTupleMultipleBindingSets() {
		List<String> bindingNames = Arrays.asList("a", "b", "c");

		MapBindingSet solution1 = new MapBindingSet(bindingNames.size());
		solution1.addBinding("a", new URIImpl("foo:bar"));
		solution1.addBinding("b", new BNodeImpl("bnode"));
		solution1.addBinding("c", new LiteralImpl("baz"));

		MapBindingSet solution2 = new MapBindingSet(bindingNames.size());
		solution2.addBinding("a", new LiteralImpl("1", XMLSchema.INTEGER));
		solution2.addBinding("c", new LiteralImpl("Hello World!", "en"));

		MapBindingSet solution3 = new MapBindingSet(bindingNames.size());
		solution3.addBinding("a", new URIImpl("http://example.org/test/ns/bindingA"));
		solution3.addBinding("b", new LiteralImpl("http://example.com/other/ns/bindingB"));
		solution3.addBinding("c", new URIImpl("http://example.com/other/ns/binding,C"));

		MapBindingSet solution4 = new MapBindingSet(bindingNames.size());
		solution4.addBinding("a", new LiteralImpl("string with newline at the end       \n"));
		solution4.addBinding("b", new LiteralImpl("string with space at the end         "));
		solution4.addBinding("c", new LiteralImpl("    "));

		MapBindingSet solution5 = new MapBindingSet(bindingNames.size());
		solution5.addBinding("a", new LiteralImpl("''single-quoted string"));
		solution5.addBinding("b", new LiteralImpl("\"\"double-quoted string"));
		solution5.addBinding("c", new LiteralImpl("		unencoded tab characters followed by encoded \t\t"));

		List<? extends BindingSet> bindingSetList = Arrays.asList(solution1, solution2, solution3, solution4,
				solution5);

		TupleQueryResultImpl result = new TupleQueryResultImpl(bindingNames, bindingSetList);

		return result;
	}

	/**
	 * @return A map of test namespaces for the writer to handle.
	 */
	protected Map<String, String> getNamespaces() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("test", "http://example.org/test/ns/");
		result.put("other", "http://example.com/other/ns/");
		return result;
	}

	/**
	 * @return A map of test namespaces for the writer to handle, including an
	 *         empty namespace.
	 */
	protected Map<String, String> getNamespacesWithEmpty() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("test", "http://example.org/test/ns/");
		result.put("other", "http://example.com/other/ns/");
		result.put("", "http://other.example.org/ns/");
		return result;
	}

	protected TupleQueryResult createTupleNoBindingSets() {
		List<String> bindingNames = Arrays.asList("a", "b", "c");

		List<? extends BindingSet> bindingSetList = Collections.emptyList();

		TupleQueryResultImpl result = new TupleQueryResultImpl(bindingNames, bindingSetList);

		return result;
	}

	protected void doTupleLinks(TupleQueryResultFormat format, TupleQueryResult input,
			TupleQueryResult expected, List<String> links)
		throws QueryResultHandlerException, QueryEvaluationException, QueryResultParseException,
		UnsupportedQueryResultFormatException, IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		TupleQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		writer.startDocument();
		writer.startHeader();
		writer.handleLinks(links);
		QueryResults.report(input, writer);

		// System.out.println("output: " + out.toString("UTF-8"));

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleQueryResult output = QueryResultIO.parse(in, format);

		assertQueryResultsEqual(expected, output);
	}

	protected void doTupleLinksAndStylesheet(TupleQueryResultFormat format, TupleQueryResult input,
			TupleQueryResult expected, List<String> links, String stylesheetUrl)
		throws QueryResultHandlerException, QueryEvaluationException, QueryResultParseException,
		UnsupportedQueryResultFormatException, IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		TupleQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		writer.startDocument();
		writer.handleStylesheet(stylesheetUrl);
		writer.startHeader();
		writer.handleLinks(links);
		QueryResults.report(input, writer);

		// System.out.println("output: " + out.toString("UTF-8"));

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleQueryResult output = QueryResultIO.parse(in, format);

		assertQueryResultsEqual(expected, output);
	}

	protected void doTupleLinksAndStylesheetAndNamespaces(TupleQueryResultFormat format,
			TupleQueryResult input, TupleQueryResult expected, List<String> links, String stylesheetUrl,
			Map<String, String> namespaces)
		throws QueryResultHandlerException, QueryEvaluationException, QueryResultParseException,
		UnsupportedQueryResultFormatException, IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		TupleQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		for (String nextPrefix : namespaces.keySet()) {
			writer.handleNamespace(nextPrefix, namespaces.get(nextPrefix));
		}
		writer.startDocument();
		writer.handleStylesheet(stylesheetUrl);
		writer.startHeader();
		writer.handleLinks(links);
		QueryResults.report(input, writer);

		// System.out.println("output: " + out.toString("UTF-8"));

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleQueryResult output = QueryResultIO.parse(in, format);

		assertQueryResultsEqual(expected, output);
	}

	/**
	 * Test specifically for QName support.
	 */
	protected void doTupleLinksAndStylesheetAndNamespacesQName(TupleQueryResultFormat format,
			TupleQueryResult input, TupleQueryResult expected, List<String> links, String stylesheetUrl,
			Map<String, String> namespaces)
		throws QueryResultHandlerException, QueryEvaluationException, QueryResultParseException,
		UnsupportedQueryResultFormatException, IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		TupleQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		if (writer.getSupportedSettings().contains(BasicQueryWriterSettings.ADD_SESAME_QNAME)) {
			// System.out.println("Enabling Sesame qname support");
			writer.getWriterConfig().set(BasicQueryWriterSettings.ADD_SESAME_QNAME, true);
		}

		for (String nextPrefix : namespaces.keySet()) {
			writer.handleNamespace(nextPrefix, namespaces.get(nextPrefix));
		}
		writer.startDocument();
		writer.handleStylesheet(stylesheetUrl);
		writer.startHeader();
		writer.handleLinks(links);
		QueryResults.report(input, writer);

		String result = out.toString("UTF-8");

		// System.out.println("output: " + result);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleQueryResult output = QueryResultIO.parse(in, format);

		assertQueryResultsEqual(expected, output);

		// only do this additional test if sesame q:qname is supported by this
		// writer
		if (writer.getSupportedSettings().contains(BasicQueryWriterSettings.ADD_SESAME_QNAME)) {
			assertTrue(result.contains("test:bindingA"));
			assertFalse(result.contains("other:bindingB"));
			assertTrue(result.contains("other:binding,C"));
		}

	}

	/**
	 * Test specifically for JSONP callback support.
	 */
	protected void doTupleJSONPCallback(TupleQueryResultFormat format, TupleQueryResult input,
			TupleQueryResult expected)
		throws QueryResultHandlerException, QueryEvaluationException, QueryResultParseException,
		UnsupportedQueryResultFormatException, IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		TupleQueryResultWriter writer = QueryResultIO.createWriter(format, out);

		// only do this test if the callback is enabled
		if (writer.getSupportedSettings().contains(BasicQueryWriterSettings.JSONP_CALLBACK)) {

			String callback = "nextfunctionname" + Math.abs(new Random().nextInt());

			writer.getWriterConfig().set(BasicQueryWriterSettings.JSONP_CALLBACK, callback);

			QueryResults.report(input, writer);

			String result = out.toString("UTF-8");

			// System.out.println("output: " + result);

			assertTrue(result.startsWith(callback + "("));
			assertTrue(result.endsWith(");"));

			// Strip off the callback function and verify that it contains a valid
			// JSON object containing the correct results
			result = result.substring(callback.length() + 1, result.length() - 2);

			ByteArrayInputStream in = new ByteArrayInputStream(result.getBytes("UTF-8"));
			TupleQueryResult output = QueryResultIO.parse(in, format);

			assertQueryResultsEqual(expected, output);
		}
	}

	protected void doTupleNoLinks(TupleQueryResultFormat format, TupleQueryResult input,
			TupleQueryResult expected)
		throws IOException, QueryResultParseException, UnsupportedQueryResultFormatException,
		QueryEvaluationException, QueryResultHandlerException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		QueryResultIO.write(input, format, out);
		input.close();

		// System.out.println("output: " + out.toString("UTF-8"));

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleQueryResult output = QueryResultIO.parse(in, format);

		assertQueryResultsEqual(expected, output);
	}

	protected void doTupleStylesheet(TupleQueryResultFormat format, TupleQueryResult input,
			TupleQueryResult expected, String stylesheetUrl)
		throws QueryResultHandlerException, QueryEvaluationException, QueryResultParseException,
		UnsupportedQueryResultFormatException, IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		TupleQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		writer.handleStylesheet(stylesheetUrl);
		QueryResults.report(input, writer);

		// System.out.println("output: " + out.toString("UTF-8"));

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleQueryResult output = QueryResultIO.parse(in, format);

		assertQueryResultsEqual(expected, output);
	}

	protected void doTupleLinksAndStylesheetNoStarts(TupleQueryResultFormat format, TupleQueryResult input,
			TupleQueryResult expected, List<String> links, String stylesheetUrl)
		throws QueryResultHandlerException, QueryEvaluationException, QueryResultParseException,
		UnsupportedQueryResultFormatException, IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		TupleQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		// Test for handling when startDocument and startHeader are not called
		writer.handleStylesheet(stylesheetUrl);
		writer.handleLinks(links);
		QueryResults.report(input, writer);

		// System.out.println("output: " + out.toString("UTF-8"));

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleQueryResult output = QueryResultIO.parse(in, format);

		assertQueryResultsEqual(expected, output);
	}

	protected void doTupleLinksAndStylesheetMultipleEndHeaders(TupleQueryResultFormat format,
			TupleQueryResult input, TupleQueryResult expected, List<String> links, String stylesheetUrl)
		throws QueryResultHandlerException, QueryEvaluationException, QueryResultParseException,
		UnsupportedQueryResultFormatException, IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		TupleQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		// Test for handling when startDocument and startHeader are not called
		writer.handleStylesheet(stylesheetUrl);
		writer.startQueryResult(input.getBindingNames());
		writer.handleLinks(links);
		writer.endHeader();
		writer.endHeader();
		try {
			while (input.hasNext()) {
				BindingSet bindingSet = input.next();
				writer.handleSolution(bindingSet);
			}
		}
		finally {
			input.close();
		}
		writer.endQueryResult();

		// System.out.println("output: " + out.toString("UTF-8"));

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleQueryResult output = QueryResultIO.parse(in, format);

		assertQueryResultsEqual(expected, output);
	}

	protected void assertQueryResultsEqual(TupleQueryResult expected, TupleQueryResult output)
		throws QueryEvaluationException, TupleQueryResultHandlerException, QueryResultHandlerException,
		UnsupportedEncodingException
	{
		assertTrue(QueryResults.equals(expected, output));
	}

	protected void doTupleMissingStartQueryResult(TupleQueryResultFormat format, TupleQueryResult input,
			TupleQueryResult expected, List<String> links, String stylesheetUrl)
		throws QueryResultHandlerException, QueryEvaluationException, QueryResultParseException,
		UnsupportedQueryResultFormatException, IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		TupleQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		// Test for handling when startDocument and startHeader are not called
		writer.startDocument();
		writer.handleStylesheet(stylesheetUrl);
		writer.startHeader();
		writer.handleLinks(links);
		writer.endHeader();
		try {
			while (input.hasNext()) {
				BindingSet bindingSet = input.next();
				writer.handleSolution(bindingSet);
			}
			writer.endQueryResult();
			fail("Expected exception when calling handleSolution without startQueryResult");
		}
		catch (IllegalStateException ise) {
			// Expected exception
		}
		finally {
			input.close();
		}
	}

	/**
	 * Tests that parsing a tuple results set without specifying a
	 * {@link TupleQueryResultHandler} does not throw any exceptions.
	 * 
	 * @param format
	 * @param input
	 * @throws QueryResultParseException
	 * @throws IOException
	 * @throws TupleQueryResultHandlerException
	 * @throws QueryEvaluationException
	 * @throws UnsupportedQueryResultFormatException
	 */
	protected void doTupleNoHandler(TupleQueryResultFormat format, TupleQueryResult input,
			TupleQueryResult expected)
		throws QueryResultParseException, IOException, TupleQueryResultHandlerException,
		UnsupportedQueryResultFormatException, QueryEvaluationException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		QueryResultIO.write(input, format, out);
		out.flush();
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleQueryResultParser parser = QueryResultIO.createParser(format);
		// This should perform a full parse to verify the document, even though
		// the handler is not set
		parser.parse(in);
	}

	/**
	 * Tests that the parser returned for a TupleQueryResultFormat is not able to
	 * parse a BooleanQueryResultFormat using the deprecated
	 * {@link TupleQueryResultParser#parse(java.io.InputStream)} method, and that
	 * it does indeed through an exception of type
	 * {@link QueryResultParseException}.
	 * 
	 * @param format
	 * @param tqr
	 * @param matchingBooleanFormat
	 *        A BooleanQueryResultFormat that matches the given
	 *        TupleQueryResultFormat .
	 * @throws IOException
	 * @throws QueryEvaluationException
	 * @throws UnsupportedQueryResultFormatException
	 * @throws QueryResultHandlerException
	 * @see SES-1860
	 */
	protected void doTupleParseNoHandlerOnBooleanResults(TupleQueryResultFormat format, boolean input,
			BooleanQueryResultFormat matchingBooleanFormat)
		throws UnsupportedQueryResultFormatException, QueryResultHandlerException, IOException
	{
		if (matchingBooleanFormat == null) {
			// This test is not supported for this boolean format
			return;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		BooleanQueryResultWriter writer = QueryResultIO.createWriter(matchingBooleanFormat, out);
		// Test for handling when handler is not set
		writer.handleBoolean(input);

		// System.out.println("output: " + out.toString("UTF-8"));

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleQueryResultParser parser = QueryResultIO.createParser(format);

		try {
			parser.parse(in);
			Assert.fail("Did not find expected parse exception");
		}
		catch (QueryResultParseException expected) {

		}
	}

	protected void doBooleanNoLinks(BooleanQueryResultFormat format, boolean input)
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

	protected void doBooleanLinksOnly(BooleanQueryResultFormat format, boolean input, List<String> links)
		throws IOException, QueryResultHandlerException, QueryResultParseException,
		UnsupportedQueryResultFormatException, QueryEvaluationException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		BooleanQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		writer.handleLinks(links);
		writer.handleBoolean(input);

		// System.out.println("output: " + out.toString("UTF-8"));

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		boolean output = QueryResultIO.parse(in, format);

		assertEquals(output, input);
	}

	protected void doInvalidBooleanAfterStartQueryResult(BooleanQueryResultFormat format, boolean input,
			List<String> links)
		throws IOException, QueryResultHandlerException, QueryResultParseException,
		UnsupportedQueryResultFormatException, QueryEvaluationException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		BooleanQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		writer.handleLinks(links);
		// Determine whether this writer also supports startQueryResult, if not,
		// this test is irrelevant as it will fail early
		boolean supported = true;
		try {
			writer.startQueryResult(Arrays.asList("foo"));
		}
		catch (UnsupportedOperationException uoe) {
			// Boolean writers are allowed to throw this for startQueryResult
			supported = false;
		}

		if (supported) {
			try {
				// After calling startQueryResult, we should not be able to call
				// handleBoolean without an exception occuring
				writer.handleBoolean(input);
				fail("Did not find expected exception");
			}
			catch (QueryResultHandlerException e) {
				// Expected
			}
		}
	}

	protected void doBooleanLinks(BooleanQueryResultFormat format, boolean input, List<String> links)
		throws IOException, QueryResultHandlerException, QueryResultParseException,
		UnsupportedQueryResultFormatException, QueryEvaluationException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		BooleanQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		writer.startDocument();
		writer.startHeader();
		writer.handleLinks(links);
		writer.handleBoolean(input);

		// System.out.println("output: " + out.toString("UTF-8"));

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		boolean output = QueryResultIO.parse(in, format);

		assertEquals(output, input);
	}

	protected void doBooleanLinksAndStylesheet(BooleanQueryResultFormat format, boolean input,
			List<String> links, String stylesheetUrl)
		throws IOException, QueryResultHandlerException, QueryResultParseException,
		UnsupportedQueryResultFormatException, QueryEvaluationException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		BooleanQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		writer.startDocument();
		writer.handleStylesheet(stylesheetUrl);
		writer.startHeader();
		writer.handleLinks(links);
		writer.handleBoolean(input);

		// System.out.println("output: " + out.toString("UTF-8"));

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		boolean output = QueryResultIO.parse(in, format);

		assertEquals(output, input);
	}

	protected void doBooleanLinksAndStylesheetAndNamespaces(BooleanQueryResultFormat format, boolean input,
			List<String> links, String stylesheetUrl, Map<String, String> namespaces)
		throws IOException, QueryResultHandlerException, QueryResultParseException,
		UnsupportedQueryResultFormatException, QueryEvaluationException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		BooleanQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		for (String nextPrefix : namespaces.keySet()) {
			writer.handleNamespace(nextPrefix, namespaces.get(nextPrefix));
		}
		writer.startDocument();
		writer.handleStylesheet(stylesheetUrl);
		writer.startHeader();
		writer.handleLinks(links);
		writer.handleBoolean(input);

		// System.out.println("output: " + out.toString("UTF-8"));

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		boolean output = QueryResultIO.parse(in, format);

		assertEquals(output, input);
	}

	protected void doBooleanStylesheet(BooleanQueryResultFormat format, boolean input, String stylesheetUrl)
		throws IOException, QueryResultHandlerException, QueryResultParseException,
		UnsupportedQueryResultFormatException, QueryEvaluationException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		BooleanQueryResultWriter writer = QueryResultIO.createWriter(format, out);
		writer.handleStylesheet(stylesheetUrl);
		writer.handleBoolean(input);

		// System.out.println("output: " + out.toString("UTF-8"));

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		boolean output = QueryResultIO.parse(in, format);

		assertEquals(output, input);
	}

	/**
	 * Tests that parsing a boolean without specifying a
	 * {@link BooleanQueryResultHandler} does not throw any exceptions.
	 * 
	 * @param format
	 * @param input
	 * @throws QueryResultParseException
	 * @throws IOException
	 */
	protected void doBooleanNoHandler(BooleanQueryResultFormat format, boolean input)
		throws QueryResultParseException, IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		QueryResultIO.write(input, format, out);
		out.flush();
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		BooleanQueryResultParser parser = QueryResultIO.createParser(format);
		assertEquals(input, parser.parse(in));
	}

	/**
	 * Tests that the parser returned for a BooleanQueryResultFormat is not able
	 * to parse a TupleQueryResultFormat using the deprecated
	 * {@link BooleanQueryResultParser#parse(java.io.InputStream)} method, and
	 * that it does indeed through an exception of type
	 * {@link QueryResultParseException}.
	 * 
	 * @param format
	 * @param tqr
	 * @param matchingTupleFormat
	 *        A TupleQueryResultFormat that matches the given
	 *        BooleanQueryResultFormat.
	 * @throws IOException
	 * @throws QueryEvaluationException
	 * @throws UnsupportedQueryResultFormatException
	 * @throws TupleQueryResultHandlerException
	 * @see SES-1860
	 */
	protected void doBooleanParseNoHandlerOnTupleResults(BooleanQueryResultFormat format,
			TupleQueryResult tqr, TupleQueryResultFormat matchingTupleFormat)
		throws TupleQueryResultHandlerException, UnsupportedQueryResultFormatException,
		QueryEvaluationException, IOException
	{
		if (matchingTupleFormat == null) {
			// This test is not supported for this boolean format
			return;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		QueryResultIO.write(tqr, matchingTupleFormat, out);
		out.flush();
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		BooleanQueryResultParser parser = QueryResultIO.createParser(format);
		try {
			parser.parse(in);
			Assert.fail("Did not find expected parse exception");
		}
		catch (QueryResultParseException e) {

		}
	}

}
