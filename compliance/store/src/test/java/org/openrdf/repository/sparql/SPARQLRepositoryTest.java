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
package org.openrdf.repository.sparql;

import java.nio.file.Path;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryTest;
import org.openrdf.repository.http.HTTPMemServer;

/**
 * @author Jeen Broekstra
 */
public class SPARQLRepositoryTest extends RepositoryTest {

	private HTTPMemServer server;

	private Path dataDir;

	@Override
	public void setUp()
		throws Exception
	{
		dataDir = tempDir.newFolder("nativestore").toPath();

		server = new HTTPMemServer(dataDir);
		try {
			server.start();
			super.setUp();
		}
		catch (Exception e) {
			server.stop();
			throw e;
		}

		// overwrite bnode test values as SPARQL endpoints do not generally work
		// well with bnodes
		bob = testRepository.getValueFactory().createURI("urn:x-local:bob");
		alice = testRepository.getValueFactory().createURI("urn:x-local:alice");
		alexander = testRepository.getValueFactory().createURI("urn:x-local:alexander");
	}

	@Override
	public void tearDown()
		throws Exception
	{
		try {
			super.tearDown();
		}
		finally {
			server.stop();
		}
	}

	@Override
	protected Repository createRepository()
		throws Exception
	{
		return new SPARQLRepository(server.getRepositoryUrl(),
				Protocol.getStatementsLocation(server.getRepositoryUrl()));

	}

}
