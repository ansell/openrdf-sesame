/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.Set;

import org.openrdf.model.URI;

/**
 * Represents a dataset against which queries can be evaluated. A dataset
 * consists of a default graph, which is the <a
 * href="http://www.w3.org/TR/rdf-mt/#defmerge">RDF merge</a> of one or more
 * graphs, and a set of named graphs. See <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#rdfDataset">SPARQL Query
 * Language for RDF</a> for more info.
 * 
 * @author Simon Schenk
 * @author Arjohn Kampman
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
}
