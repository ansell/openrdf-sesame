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

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * LuceneSailSchema defines predicates that can be used for expressing a Lucene
 * query in a RDF query.
 */
public class LuceneSailSchema {

	public static final String NAMESPACE = "http://www.openrdf.org/contrib/lucenesail#";

	public static final URI LUCENE_QUERY;

	public static final URI SCORE;

	public static final URI QUERY;

	public static final URI PROPERTY;

	public static final URI SNIPPET;

	public static final URI MATCHES;

	static {
		ValueFactory factory = new ValueFactoryImpl(); // compatible with beta4: creating a new factory
		LUCENE_QUERY = factory.createURI(NAMESPACE + "LuceneQuery");
		SCORE = factory.createURI(NAMESPACE + "score");
		QUERY = factory.createURI(NAMESPACE + "query");
		PROPERTY = factory.createURI(NAMESPACE + "property");
		SNIPPET = factory.createURI(NAMESPACE + "snippet");
		MATCHES = factory.createURI(NAMESPACE + "matches");
	}
}
