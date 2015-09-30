/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.query.resultio.text.tsv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.junit.Test;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.MutableTupleQueryResult;
import org.openrdf.query.resultio.AbstractQueryResultIOTupleTest;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

/**
 * @author Peter Ansell
 * @author James Leigh
 */
public class SPARQLTSVTupleTest extends AbstractQueryResultIOTupleTest {

	@Override
	protected String getFileName() {
		return "test.tsv";
	}

	@Override
	protected TupleQueryResultFormat getTupleFormat() {
		return TupleQueryResultFormat.TSV;
	}

	@Override
	protected BooleanQueryResultFormat getMatchingBooleanFormatOrNull() {
		return null;
	}

	@Test
	public void testEndOfLine()
		throws Exception
	{
		assertEquals("\n", toString(createTupleNoBindingSets()).replaceAll("\\S+|\t", ""));
	}

	@Test
	public void testEmptyResults()
		throws Exception
	{
		assertRegex("\\?a\t\\?b\t\\?c\n?", toString(createTupleNoBindingSets()));
	}

	@Test
	public void testSingleVarResults()
		throws Exception
	{
		assertRegex("\\?a\n" + "<foo:bar>\n" + "(2.0(E0)?|\"2.0\"\\^\\^<http://www.w3.org/2001/XMLSchema#double>)\n"
				+ "_:bnode3\n" + "\"''single-quoted string\"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\n"
				+ "\"\\\\\"\\\\\"double-quoted string\"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\n"
				+ "\"space at the end         \"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\n"
				+ "\"space at the end         \"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\n"
				+ "\"\\\\\"\\\\\"double-quoted string with no datatype\"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\n"
				+ "\"newline at the end \\\\n\"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\n?",
				toString(createTupleSingleVarMultipleBindingSets()));
	}

	@Test
	public void testmultipleVarResults()
		throws Exception
	{
		assertRegex(
				"\\?a\t\\?b\t\\?c\n"
						+ "<foo:bar>\t_:bnode\t\"baz\"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\n"
						+ "(1|\"1\"\\^\\^<http://www.w3.org/2001/XMLSchema#integer>)\t\t\"Hello World!\"@en\n"
						+ "<http://example.org/test/ns/bindingA>\t\"http://example.com/other/ns/bindingB\"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\t<http://example.com/other/ns/binding,C>\n"
						+ "\"string with newline at the end       \\\\n\"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\t\"string with space at the end         \"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\t\"    \"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\n"
						+ "\"''single-quoted string\"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\t\"\\\\\"\\\\\"double-quoted string\"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\t\"\\\\t\\\\tunencoded tab characters followed by encoded \\\\t\\\\t\"(\\^\\^<http://www.w3.org/2001/XMLSchema#string>)?\n?",
				toString(createTupleMultipleBindingSets()));
	}

	private String toString(TupleQueryResult results)
		throws QueryResultHandlerException, TupleQueryResultHandlerException, QueryEvaluationException,
		UnsupportedEncodingException
	{
		TupleQueryResultFormat format = getTupleFormat();
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		TupleQueryResultWriter writer = QueryResultIO.createTupleWriter(format, out);
		writer.startDocument();
		writer.startHeader();
		writer.handleLinks(Arrays.<String> asList());
		QueryResults.report(results, writer);

		return out.toString("UTF-8");
	}

	private void assertRegex(String pattern, String actual) {
		if (!Pattern.compile(pattern, Pattern.DOTALL).matcher(actual).matches()) {
			assertEquals(pattern, actual);
		}
	}

	protected void assertQueryResultsEqual(TupleQueryResult expected, TupleQueryResult output)
		throws QueryEvaluationException, TupleQueryResultHandlerException, QueryResultHandlerException,
		UnsupportedEncodingException
	{
		MutableTupleQueryResult r1 = new MutableTupleQueryResult(expected);
		MutableTupleQueryResult r2 = new MutableTupleQueryResult(output);
		if (!QueryResults.equals(r1, r2)) {
			r1.beforeFirst();
			r2.beforeFirst();
			assertEquals(toString(r1), toString(r2));
			r2.beforeFirst();
			fail(toString(r2));
		}
	}

}
