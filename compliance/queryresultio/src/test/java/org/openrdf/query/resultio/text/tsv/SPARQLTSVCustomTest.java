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

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.impl.IteratingTupleQueryResult;
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
	@Ignore("This test does not work with RDF-1.1")
	@Test
	public void testSES2126QuotedLiteralIntegerAsStringExplicitType()
		throws Exception
	{
		List<String> bindingNames = Arrays.asList("test");
		TupleQueryResult tqr = new IteratingTupleQueryResult(bindingNames, Arrays.asList(new ListBindingSet(
				bindingNames, SimpleValueFactory.getInstance().createLiteral("1", XMLSchema.STRING))));
		String result = writeTupleResult(tqr);
		assertEquals("?test\n\"1\"^^<http://www.w3.org/2001/XMLSchema#string>\n", result);
	}

	/**
	 * Only Literals with the XML Schema numeric types should be simplified.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSES2126QuotedLiteralIntegerAsStringImplicitType()
		throws Exception
	{
		List<String> bindingNames = Arrays.asList("test");
		TupleQueryResult tqr = new IteratingTupleQueryResult(bindingNames, Arrays.asList(new ListBindingSet(
				bindingNames, SimpleValueFactory.getInstance().createLiteral("1"))));
		String result = writeTupleResult(tqr);
		assertEquals("?test\n\"1\"\n", result);
	}

	private String writeTupleResult(TupleQueryResult tqr)
		throws IOException, TupleQueryResultHandlerException, QueryEvaluationException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		QueryResultIO.writeTuple(tqr, TupleQueryResultFormat.TSV, output);
		String result = new String(output.toByteArray(), Charset.forName("UTF-8"));
		return result;
	}

}
