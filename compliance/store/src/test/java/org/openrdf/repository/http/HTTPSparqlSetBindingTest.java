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
package org.openrdf.repository.http;

import java.io.File;

import org.openrdf.repository.Repository;
import org.openrdf.repository.SparqlSetBindingTest;

public class HTTPSparqlSetBindingTest extends SparqlSetBindingTest {

	private HTTPMemServer server;

	@Override
	public void setUp()
		throws Exception
	{
		File testFolder = tempDir.newFolder("sesame-http-sparql-set-binding-datadir");
		testFolder.mkdirs();
		server = new HTTPMemServer(testFolder);
		
		try {
			server.start();
			super.setUp();
		}
		catch (Exception e) {
			server.stop();
			throw e;
		}
	}

	@Override
	public void tearDown()
		throws Exception
	{
		super.tearDown();
		server.stop();
	}

	protected Repository newRepository() {
		return new HTTPRepository(server.getRepositoryUrl());
	}
}
