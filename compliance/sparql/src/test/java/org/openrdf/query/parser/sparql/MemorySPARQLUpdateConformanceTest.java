/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.util.Map;

import junit.framework.Test;

import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.contextaware.ContextAwareRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;


/**
 * @author jeen
 */
public class MemorySPARQLUpdateConformanceTest extends SPARQLUpdateConformanceTest {

	public MemorySPARQLUpdateConformanceTest(String testURI, String name, String requestFile,
			URI defaultGraphURI, Map<String, URI> inputNamedGraphs, URI resultDefaultGraphURI,
			Map<String, URI> resultNamedGraphs)
	{
		super(testURI, name, requestFile, defaultGraphURI, inputNamedGraphs, resultDefaultGraphURI,
				resultNamedGraphs);
	}

	public static Test suite()
		throws Exception
	{
		return SPARQL11ManifestTest.suite(new Factory() {

			public MemorySPARQLUpdateConformanceTest createSPARQLUpdateConformanceTest(String testURI,
					String name, String requestFile, URI defaultGraphURI, Map<String, URI> inputNamedGraphs,
					URI resultDefaultGraphURI, Map<String, URI> resultNamedGraphs)
			{
				return new MemorySPARQLUpdateConformanceTest(testURI, name, requestFile, defaultGraphURI,
						inputNamedGraphs, resultDefaultGraphURI, resultNamedGraphs);
			}

		});
	}

	@Override
	protected ContextAwareRepository newRepository()
		throws Exception
	{
		SailRepository repo = new SailRepository(new MemoryStore());

		return new ContextAwareRepository(repo);
	}

}

