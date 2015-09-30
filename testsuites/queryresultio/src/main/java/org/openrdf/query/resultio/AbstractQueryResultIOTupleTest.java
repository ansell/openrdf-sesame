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
package org.openrdf.query.resultio;

import java.util.Arrays;

import org.junit.Test;

/**
 * Abstract test for QueryResultIO.
 * 
 * @author jeen
 * @author Peter Ansell
 */
public abstract class AbstractQueryResultIOTupleTest extends AbstractQueryResultIOTest {

	@Override
	protected final QueryResultFormat getFormat() {
		return getTupleFormat();
	}

	/**
	 * @return The {@link TupleQueryResultFormat} that this test is running
	 *         against.
	 */
	protected abstract TupleQueryResultFormat getTupleFormat();

	/**
	 * @return The {@link BooleanQueryResultFormat} that may be parsed by the
	 *         same parser as the one for {@link #getTupleFormat()}, or null if
	 *         this functionality is not supported.
	 */
	protected abstract BooleanQueryResultFormat getMatchingBooleanFormatOrNull();

	@Test
	public final void testSPARQLResultFormatSingleVarMultipleBindingSets()
		throws Exception
	{
		doTupleNoLinks(getTupleFormat(), createTupleSingleVarMultipleBindingSets(),
				createTupleSingleVarMultipleBindingSets());
	}

	@Test
	public final void testSPARQLResultFormatMultipleBindingsMultipleBindingSets()
		throws Exception
	{
		doTupleNoLinks(getTupleFormat(), createTupleMultipleBindingSets(), createTupleMultipleBindingSets());
	}

	@Test
	public final void testSPARQLResultFormatNoResults()
		throws Exception
	{
		doTupleNoLinks(getTupleFormat(), createTupleNoBindingSets(), createTupleNoBindingSets());
	}

	@Test
	public final void testNoHandlerNoResults()
		throws Exception
	{
		doTupleNoHandler(getTupleFormat(), createTupleNoBindingSets(), createTupleNoBindingSets());
	}

	@Test
	public final void testNoHandlerWithResults()
		throws Exception
	{
		doTupleNoHandler(getTupleFormat(), createTupleMultipleBindingSets(), createTupleMultipleBindingSets());
	}

	@Test
	public final void testTupleParseNoHandlerOnBooleanResults()
		throws Exception
	{
		doTupleParseNoHandlerOnBooleanResults(getTupleFormat(), true, getMatchingBooleanFormatOrNull());
		doTupleParseNoHandlerOnBooleanResults(getTupleFormat(), false, getMatchingBooleanFormatOrNull());
	}

	@Test
	public final void testNoLinksNoResults()
		throws Exception
	{
		doTupleLinks(getTupleFormat(), createTupleNoBindingSets(), createTupleNoBindingSets(),
				Arrays.<String> asList());
	}

	@Test
	public final void testNoLinksWithResults()
		throws Exception
	{
		doTupleLinks(getTupleFormat(), createTupleMultipleBindingSets(), createTupleMultipleBindingSets(),
				Arrays.<String> asList());
	}

	@Test
	public final void testOneLinkNoResults()
		throws Exception
	{
		doTupleLinks(getTupleFormat(), createTupleNoBindingSets(), createTupleNoBindingSets(),
				Arrays.asList("info"));
	}

	@Test
	public final void testOneLinkWithResults()
		throws Exception
	{
		doTupleLinks(getTupleFormat(), createTupleMultipleBindingSets(), createTupleMultipleBindingSets(),
				Arrays.asList("info"));
	}

	@Test
	public final void testMultipleLinksNoResults()
		throws Exception
	{
		doTupleLinks(getTupleFormat(), createTupleNoBindingSets(), createTupleNoBindingSets(),
				Arrays.asList("info", "alternate", "other", "another"));
	}

	@Test
	public final void testMultipleLinksWithResults()
		throws Exception
	{
		doTupleLinks(getTupleFormat(), createTupleMultipleBindingSets(), createTupleMultipleBindingSets(),
				Arrays.asList("info", "alternate", "other", "another"));
	}

	@Test
	public final void testMultipleLinksWithResultsAndStylesheet()
		throws Exception
	{
		doTupleLinksAndStylesheet(getTupleFormat(), createTupleMultipleBindingSets(),
				createTupleMultipleBindingSets(), Arrays.asList("info", "alternate", "other", "another"),
				"test.xsl");
	}

	@Test
	public final void testMultipleLinksWithResultsAndStylesheetAndNamespaces()
		throws Exception
	{
		doTupleLinksAndStylesheetAndNamespaces(getTupleFormat(), createTupleMultipleBindingSets(),
				createTupleMultipleBindingSets(), Arrays.asList("info", "alternate", "other", "another"),
				"test.xsl", getNamespaces());
	}

	@Test
	public final void testMultipleLinksWithResultsAndStylesheetAndNamespacesQName()
		throws Exception
	{
		doTupleLinksAndStylesheetAndNamespacesQName(getTupleFormat(), createTupleMultipleBindingSets(),
				createTupleMultipleBindingSets(), Arrays.asList("info", "alternate", "other", "another"),
				"test.xsl", getNamespaces());
	}

	@Test
	public final void testMultipleLinksWithResultsAndStylesheetAndNamespacesWithEmpty()
		throws Exception
	{
		doTupleLinksAndStylesheetAndNamespaces(getTupleFormat(), createTupleMultipleBindingSets(),
				createTupleMultipleBindingSets(), Arrays.asList("info", "alternate", "other", "another"),
				"test.xsl", getNamespacesWithEmpty());
	}

	@Test
	public final void testMultipleLinksWithResultsAndStylesheetAndNamespacesQNameWithEmpty()
		throws Exception
	{
		doTupleLinksAndStylesheetAndNamespacesQName(getTupleFormat(), createTupleMultipleBindingSets(),
				createTupleMultipleBindingSets(), Arrays.asList("info", "alternate", "other", "another"),
				"test.xsl", getNamespacesWithEmpty());
	}

	@Test
	public final void testMultipleLinksWithResultsAndStylesheetNoStarts()
		throws Exception
	{
		doTupleLinksAndStylesheetNoStarts(getTupleFormat(), createTupleMultipleBindingSets(),
				createTupleMultipleBindingSets(), Arrays.asList("info", "alternate", "other", "another"),
				"test.xsl");
	}

	@Test
	public final void testMultipleLinksWithResultsAndStylesheetMultipleEndHeaders()
		throws Exception
	{
		doTupleLinksAndStylesheetMultipleEndHeaders(getTupleFormat(), createTupleMultipleBindingSets(),
				createTupleMultipleBindingSets(), Arrays.asList("info", "alternate", "other", "another"),
				"test.xsl");
	}

	@Test
	public final void testNoResultsAndStylesheet()
		throws Exception
	{
		doTupleStylesheet(getTupleFormat(), createTupleNoBindingSets(), createTupleNoBindingSets(), "test.xsl");
	}

	@Test
	public final void testMultipleResultsAndStylesheet()
		throws Exception
	{
		doTupleStylesheet(getTupleFormat(), createTupleMultipleBindingSets(), createTupleMultipleBindingSets(),
				"test.xsl");
	}

	@Test
	public final void testMultipleResultsJSONPCallback()
		throws Exception
	{
		doTupleJSONPCallback(getTupleFormat(), createTupleMultipleBindingSets(),
				createTupleMultipleBindingSets());
	}

	@Test
	public final void testNoResultsJSONPCallback()
		throws Exception
	{
		doTupleJSONPCallback(getTupleFormat(), createTupleNoBindingSets(), createTupleNoBindingSets());
	}

	@Test
	public final void testNoResultsExceptionHandleSolutionBeforeStartQueryResult()
		throws Exception
	{
		doTupleMissingStartQueryResult(getTupleFormat(), createTupleNoBindingSets(),
				createTupleNoBindingSets(), Arrays.asList("info", "alternate", "other", "another"), "test.xsl");
	}

	@Test
	public final void testMultipleExceptionHandleSolutionBeforeStartQueryResult()
		throws Exception
	{
		doTupleMissingStartQueryResult(getTupleFormat(), createTupleMultipleBindingSets(),
				createTupleMultipleBindingSets(), Arrays.asList("info", "alternate", "other", "another"),
				"test.xsl");
	}
}
