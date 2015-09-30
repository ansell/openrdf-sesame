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
package org.eclipse.rdf4j.query.resultio;

import java.util.Arrays;

import org.eclipse.rdf4j.query.resultio.BooleanQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
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
