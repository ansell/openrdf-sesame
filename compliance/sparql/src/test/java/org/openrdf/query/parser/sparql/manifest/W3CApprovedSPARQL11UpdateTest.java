/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql.manifest;

import java.util.Map;

import junit.framework.Test;

import org.openrdf.model.URI;
import org.openrdf.query.parser.sparql.manifest.SPARQL11ManifestTest;
import org.openrdf.query.parser.sparql.manifest.SPARQLUpdateConformanceTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.contextaware.ContextAwareRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;


/**
 * @author Jeen Broekstra
 */
public class W3CApprovedSPARQL11UpdateTest extends SPARQLUpdateConformanceTest {

	public W3CApprovedSPARQL11UpdateTest(String testURI, String name, String requestFile,
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

			public W3CApprovedSPARQL11UpdateTest createSPARQLUpdateConformanceTest(String testURI,
					String name, String requestFile, URI defaultGraphURI, Map<String, URI> inputNamedGraphs,
					URI resultDefaultGraphURI, Map<String, URI> resultNamedGraphs)
			{
				return new W3CApprovedSPARQL11UpdateTest(testURI, name, requestFile, defaultGraphURI,
						inputNamedGraphs, resultDefaultGraphURI, resultNamedGraphs);
			}

		}, true, true, false);
	}

	@Override
	protected ContextAwareRepository newRepository()
		throws Exception
	{
		SailRepository repo = new SailRepository(new MemoryStore());

		return new ContextAwareRepository(repo);
	}

}

