import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import org.openrdf.query.BindingSet;
import org.openrdf.query.AbstractTupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLParser;

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

/**
 * @author James Leigh
 */
public class SPARQLResultsXMLParserTest {

	@Test
	public void testlocalname()
		throws Exception
	{
		assertEquals(4, countSolutions("localname-result.srx"));
	}

	@Test
	public void testNamespace()
		throws Exception
	{
		assertEquals(4, countSolutions("namespace-result.srx"));
	}

	private int countSolutions(String name)
		throws Exception
	{
		SPARQLResultsXMLParser parser = new SPARQLResultsXMLParser();
		CountingTupleQueryResultHandler countingHandler = new CountingTupleQueryResultHandler();
		parser.setTupleQueryResultHandler(countingHandler);
		InputStream in = SPARQLResultsXMLParserTest.class.getClassLoader().getResourceAsStream(name);
		assertNotNull(name + " is missing", in);
		try {
			parser.parseQueryResult(in);
		} finally {
			in.close();
		}
		return countingHandler.getCount();
	}

	static class CountingTupleQueryResultHandler extends AbstractTupleQueryResultHandler
	{
		private int count;

		public int getCount()
		{
			return count;
		}

		public void handleSolution(BindingSet bindingSet)
			throws TupleQueryResultHandlerException
		{
			count++;
		}
	}
}
