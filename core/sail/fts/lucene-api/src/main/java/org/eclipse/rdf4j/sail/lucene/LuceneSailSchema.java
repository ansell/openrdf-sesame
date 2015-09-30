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
package org.eclipse.rdf4j.sail.lucene;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * LuceneSailSchema defines predicates that can be used for expressing a Lucene
 * query in a RDF query.
 */
public class LuceneSailSchema {

	public static final String NAMESPACE = "http://www.openrdf.org/contrib/lucenesail#";

	public static final IRI LUCENE_QUERY;

	public static final IRI SCORE;

	public static final IRI QUERY;

	public static final IRI PROPERTY;

	public static final IRI SNIPPET;

	public static final IRI MATCHES;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance(); // compatible with beta4:
																		// creating a new factory
		LUCENE_QUERY = factory.createIRI(NAMESPACE + "LuceneQuery");
		SCORE = factory.createIRI(NAMESPACE + "score");
		QUERY = factory.createIRI(NAMESPACE + "query");
		PROPERTY = factory.createIRI(NAMESPACE + "property");
		SNIPPET = factory.createIRI(NAMESPACE + "snippet");
		MATCHES = factory.createIRI(NAMESPACE + "matches");
	}
}
