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
package org.openrdf.query.parser;

import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Andreas Schwarte
 */
public class QueryParserUtilTest {

	
	@Test
	public void testGetRestOfQueryString() throws Exception {
			
		String queryString = "# this is a comment\n"
					+ "PREFIX : <http://example.com/base/>\n"
					+ "# one more comment\r\n"
					+ "SELECT * WHERE { ?s ?p ?o }";
			
		String restQuery = QueryParserUtil.removeSPARQLQueryProlog(queryString);
		Assert.assertEquals("SELECT * WHERE { ?s ?p ?o }", restQuery);
	}
}
