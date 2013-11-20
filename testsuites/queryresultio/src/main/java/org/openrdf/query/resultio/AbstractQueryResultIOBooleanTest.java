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
public abstract class AbstractQueryResultIOBooleanTest extends AbstractQueryResultIOTest {

	@Override
	protected final QueryResultFormat getFormat() {
		return getBooleanFormat();
	}

	/**
	 * @return The {@link BooleanQueryResultFormat} that this test is running
	 *         against.
	 */
	protected abstract BooleanQueryResultFormat getBooleanFormat();

	/**
	 * @return The {@link TupleQueryResultFormat} that may be parsed by the same
	 *         parser as the one for {@link #getBooleanFormat()}, or null if this
	 *         functionality is not supported.
	 */
	protected abstract TupleQueryResultFormat getMatchingTupleFormatOrNull();

	@Test
	public final void testBooleanNoLinks()
		throws Exception
	{
		doBooleanNoLinks(getBooleanFormat(), true);
		doBooleanNoLinks(getBooleanFormat(), false);
	}

	@Test
	public final void testBooleanEmptyLinks()
		throws Exception
	{
		doBooleanLinks(getBooleanFormat(), true, Arrays.<String> asList());
		doBooleanLinks(getBooleanFormat(), false, Arrays.<String> asList());
	}

	@Test
	public final void testBooleanOneLink()
		throws Exception
	{
		doBooleanLinks(getBooleanFormat(), true, Arrays.asList("info"));
		doBooleanLinks(getBooleanFormat(), false, Arrays.asList("info"));
	}

	@Test
	public final void testBooleanMultipleLinks()
		throws Exception
	{
		doBooleanLinks(getBooleanFormat(), true, Arrays.asList("info", "alternate", "other", "another"));
		doBooleanLinks(getBooleanFormat(), false, Arrays.asList("info", "alternate", "other", "another"));
	}

	@Test
	public final void testBooleanEmptyLinksOnly()
		throws Exception
	{
		doBooleanLinksOnly(getBooleanFormat(), true, Arrays.<String> asList());
		doBooleanLinksOnly(getBooleanFormat(), false, Arrays.<String> asList());
	}

	@Test
	public final void testBooleanOneLinkOnly()
		throws Exception
	{
		doBooleanLinksOnly(getBooleanFormat(), true, Arrays.asList("info"));
		doBooleanLinksOnly(getBooleanFormat(), false, Arrays.asList("info"));
	}

	@Test
	public final void testBooleanMultipleLinksOnly()
		throws Exception
	{
		doBooleanLinksOnly(getBooleanFormat(), true, Arrays.asList("info", "alternate", "other", "another"));
		doBooleanLinksOnly(getBooleanFormat(), false, Arrays.asList("info", "alternate", "other", "another"));
	}

	@Test
	public final void testBooleanMultipleLinksWithStylesheet()
		throws Exception
	{
		doBooleanLinksAndStylesheet(getBooleanFormat(), true,
				Arrays.asList("info", "alternate", "other", "another"), "test.xsl");
		doBooleanLinksAndStylesheet(getBooleanFormat(), false,
				Arrays.asList("info", "alternate", "other", "another"), "test.xsl");
	}

	@Test
	public final void testMultipleResultsAndStylesheet()
		throws Exception
	{
		doBooleanStylesheet(getBooleanFormat(), true, "test.xsl");
		doBooleanStylesheet(getBooleanFormat(), false, "test.xsl");
	}

	@Test
	public final void testInvalidBooleanAfterStartQueryResult()
		throws Exception
	{
		doInvalidBooleanAfterStartQueryResult(getBooleanFormat(), true,
				Arrays.asList("info", "alternate", "other", "another"));
		doInvalidBooleanAfterStartQueryResult(getBooleanFormat(), false,
				Arrays.asList("info", "alternate", "other", "another"));
	}

	@Test
	public final void testBooleanNoHandler()
		throws Exception
	{
		doBooleanNoHandler(getBooleanFormat(), true);
		doBooleanNoHandler(getBooleanFormat(), false);
	}

	@Test
	public final void testBooleanParseNoHandlerOnTupleResultsNoResults()
		throws Exception
	{
		doBooleanParseNoHandlerOnTupleResults(getBooleanFormat(), createTupleNoBindingSets(), getMatchingTupleFormatOrNull());
	}
	
	@Test
	public final void testBooleanParseNoHandlerOnTupleResultsSingleVarMultipleBindingSets()
		throws Exception
	{
		doBooleanParseNoHandlerOnTupleResults(getBooleanFormat(), createTupleSingleVarMultipleBindingSets(), getMatchingTupleFormatOrNull());
	}
	
	@Test
	public final void testBooleanParseNoHandlerOnTupleResultsMultipleBindingsMultipleBindingSets()
		throws Exception
	{
		doBooleanParseNoHandlerOnTupleResults(getBooleanFormat(), createTupleMultipleBindingSets(), getMatchingTupleFormatOrNull());
	}
	
}
