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
package org.openrdf.sail.lucene;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

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
