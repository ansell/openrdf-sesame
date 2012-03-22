/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.Set;

import org.openrdf.model.URI;

/**
 * Represents a dataset against which operations can be evaluated. A dataset
 * consists of a default graph for read and using operations, which is the <a
 * href="http://www.w3.org/TR/rdf-mt/#defmerge">RDF merge</a> of one or more
 * graphs, a set of named graphs, and a single update graph for INSERT and
 * DELETE. See <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#rdfDataset">SPARQL Query
 * Language for RDF</a> for more info.
 * 
 * @author Simon Schenk
 * @author Arjohn Kampman
 * @author James Leigh
 */
public interface Dataset {

	/**
	 * Gets the default graph URIs of this dataset. An empty set indicates that
	 * the default graph is an empty graph.
	 */
	public Set<URI> getDefaultGraphs();

	/**
	 * Gets the named graph URIs of this dataset. An empty set indicates that
	 * there are no named graphs in this dataset.
	 */
	public Set<URI> getNamedGraphs();

	/**
	 * Gets the default update graph URI of this dataset. An null value indicates
	 * the default graph of the store should be used if nothing specified in the
	 * operation.
	 */
	public URI getDefaultUpdateGraph();
}
