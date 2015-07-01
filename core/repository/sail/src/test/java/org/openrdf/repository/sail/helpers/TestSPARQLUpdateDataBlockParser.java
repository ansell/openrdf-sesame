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
package org.openrdf.repository.sail.helpers;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

/**
 * @author Damyan Ognyanov
 */
public class TestSPARQLUpdateDataBlockParser {

	/**
	 * A case reproducing SES-2258 using two cases with optional 'dot'. If not
	 * handled properly by SPARQLUpdateDataBlockParser.parseGraph(), an Exception
	 * is throws and test fails.
	 */
	@Test
	public void testParseGraph()
		throws RDFParseException, RDFHandlerException, IOException
	{
		SPARQLUpdateDataBlockParser parser = new SPARQLUpdateDataBlockParser();
		String blocksToCheck[] = new String[] {
				"graph <u:g1> {<u:1> <p:1> 1 } . <u:2> <p:2> 2.",
				"graph <u:g1> {<u:1> <p:1> 1 .} . <u:2> <p:2> 2." };
		for (String block : blocksToCheck) {
			parser.parse(new StringReader(block), "http://base.org");
		}
	}

}
