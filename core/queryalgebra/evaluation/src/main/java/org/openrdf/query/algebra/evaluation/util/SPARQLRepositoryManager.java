/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.repository.sparql.SPARQLRepository;

/**
 * Manage a set of (initialized) {@link SPARQLRepository} instances. Lookup
 * can be done via the sparqlEndpointUrl using the static method 
 * {@link #getRepository(String)}. If there is no repository for the specified
 * url, it is created and the reference is maintained for future use.
 * 
 * @author Andreas Schwarte
 */
public class SPARQLRepositoryManager {

	/*
	 * TODO maybe move to some other package ? 
	 * TODO shutdown method ?
	 */
	
	
	/**
	 * Map sparql endpoint url to the corresponding initialized SPARQL repository
	 */
	private static Map<String, SPARQLRepository> endpointToRep = new HashMap<String, SPARQLRepository>();


	/**
	 * Retrieve an initialized {@link SPARQLRepository} for the specified 
	 * sparqlEndpointUrl.
	 * 
	 * @param sparqlEndpointUrl
	 * @return
	 */
	public static SPARQLRepository getRepository(String sparqlEndpointUrl) {
		SPARQLRepository res = endpointToRep.get(sparqlEndpointUrl);
		if (res==null) {
			res = new SPARQLRepository(sparqlEndpointUrl);
			// res.initialize(); 	// is currently a no-op 
			endpointToRep.put(sparqlEndpointUrl, res);
		}
		return res;
	}

}
