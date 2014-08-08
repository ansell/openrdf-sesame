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
package org.openrdf.http.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.commons.httpclient.HttpMethodBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.openrdf.http.client.BackgroundGraphResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;

/**
 * @author Damyan Ognyanov
 */
public class BackgroundGraphResultHangTest {

	static class DummyParser extends RDFParserBase {

		@Override
		public RDFFormat getRDFFormat() {
			return null;
		}

		@Override
		public void parse(InputStream in, String baseURI)
			throws IOException, RDFParseException, RDFHandlerException
		{
			throw new RDFParseException("invalid RDF ");
		}

		@Override
		public void parse(Reader reader, String baseURI)
			throws IOException, RDFParseException, RDFHandlerException
		{
			throw new RDFParseException("invalid RDF ");
		}

	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test(timeout = 1000)
	public void testBGRHang()
		throws Exception
	{
		String data = "@prefix a:<http:base.org>\n" + "<u:1> <u:2> <u:3 .";

		BackgroundGraphResult gRes = new BackgroundGraphResult(new DummyParser(), new ByteArrayInputStream(
				data.getBytes(Charset.forName("UTF-8"))), Charset.forName("UTF-8"), "http://base.org",
				new HttpMethodBase() {

					@Override
					public String getName() {
						return null;
					}
				});

		gRes.run();

		gRes.getNamespaces();

		thrown.expect(QueryEvaluationException.class);
		gRes.hasNext();
	}
}
