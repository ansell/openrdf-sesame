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
package org.openrdf.query.parser.sparql.manifest;

import java.util.Map;

import org.openrdf.model.IRI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import junit.framework.Test;


/**
 * @author Jeen Broekstra
 */
public class W3CApprovedSPARQL11UpdateTest extends SPARQLUpdateConformanceTest {

	public W3CApprovedSPARQL11UpdateTest(String testURI, String name, String requestFile,
			IRI defaultGraphURI, Map<String, IRI> inputNamedGraphs, IRI resultDefaultGraphURI,
			Map<String, IRI> resultNamedGraphs)
	{
		super(testURI, name, requestFile, defaultGraphURI, inputNamedGraphs, resultDefaultGraphURI,
				resultNamedGraphs);
	}

	public static Test suite()
		throws Exception
	{
		return SPARQL11ManifestTest.suite(new Factory() {

			public W3CApprovedSPARQL11UpdateTest createSPARQLUpdateConformanceTest(String testURI,
					String name, String requestFile, IRI defaultGraphURI, Map<String, IRI> inputNamedGraphs,
					IRI resultDefaultGraphURI, Map<String, IRI> resultNamedGraphs)
			{
				return new W3CApprovedSPARQL11UpdateTest(testURI, name, requestFile, defaultGraphURI,
						inputNamedGraphs, resultDefaultGraphURI, resultNamedGraphs);
			}

		}, true, true, false);
	}

	@Override
	protected Repository newRepository()
		throws Exception
	{
		SailRepository repo = new SailRepository(new MemoryStore());

		return repo;
	}

}

