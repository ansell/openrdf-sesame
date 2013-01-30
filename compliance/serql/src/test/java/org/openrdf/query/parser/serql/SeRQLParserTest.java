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
package org.openrdf.query.parser.serql;

import junit.framework.Test;

import org.openrdf.model.Value;
import org.openrdf.query.parser.QueryParser;

public class SeRQLParserTest extends SeRQLParserTestCase {
	public static Test suite() throws Exception {
		return SeRQLParserTestCase.suite(new Factory() {
			public Test createTest(String name, String queryFile, Value result) {
				return new SeRQLParserTest(name, queryFile, result);
			}
		});
	}

	public SeRQLParserTest(String name, String queryFile, Value result) {
		super(name, queryFile, result);
	}

	@Override
	protected QueryParser createParser() {
		return new SeRQLParser();
	}
}
