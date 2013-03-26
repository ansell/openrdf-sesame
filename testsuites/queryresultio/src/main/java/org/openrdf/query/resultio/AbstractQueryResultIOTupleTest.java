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

	@Test
	public final void testSPARQLResultFormat()
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

}
