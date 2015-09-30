/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
