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
package org.openrdf.query;

import java.util.Set;

import org.openrdf.model.IRI;

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
	 * Gets the default remove graph URIs of this dataset. An empty set indicates
	 * the the store's default behaviour should be used, if not otherwise
	 * indicated in the operation.
	 */
	public Set<IRI> getDefaultRemoveGraphs();

	/**
	 * Gets the default insert graph URI of this dataset. An null value indicates
	 * that the store's default behaviour should be used, if not otherwise
	 * indicated in the operation.
	 */
	public IRI getDefaultInsertGraph();

	/**
	 * Gets the default graph URIs of this dataset. An empty default graph set
	 * and a non-empty named graph set indicates that the default graph is an
	 * empty graph. However, if both the default graph set and the named graph
	 * set are empty, that indicates that the store's default behaviour should be
	 * used.
	 */
	public Set<IRI> getDefaultGraphs();

	/**
	 * Gets the named graph URIs of this dataset. An empty named graph set and a
	 * non-empty default graph set indicates that there are no named graphs.
	 * However, if both the default graph set and the named graph set are empty,
	 * that indicates that the store's default behaviour should be used.
	 */
	public Set<IRI> getNamedGraphs();
}
