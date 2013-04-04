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
package org.openrdf.workbench.util;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;

/**
 * @author Dale Visser
 */
public class TestTupleResultBuilder {

	private TupleResultBuilder builder;

	@Before
	public void setUp()
		throws Exception
	{
		builder = new TupleResultBuilder(new SPARQLResultsJSONWriter(new ByteArrayOutputStream()),
				ValueFactoryImpl.getInstance());
		builder.start("test");
	}

	@After
	public void tearDown()
		throws Exception
	{
		builder.end();
	}

	@Test
	public final void testSES1780regression()
		throws MalformedURLException
	{
		try {
			builder.namedResult("test", new URL("http://www.foo.org/bar#"));
		}
		catch (QueryResultHandlerException e) {
			fail(e.getMessage());
		}
	}
}
