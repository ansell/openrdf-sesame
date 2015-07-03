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
package org.openrdf.model.impl;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Default implementation of the ValueFactory interface that uses the RDF model
 * classes from this package.
 * 
 * @author Arjohn Kampman
 */
public class ValueFactoryImpl extends ValueFactoryBase {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final ValueFactoryImpl sharedInstance = new ValueFactoryImpl();

	public static ValueFactoryImpl getInstance() {
		return sharedInstance;
	}

	public static final Literal TRUE = getInstance().createLiteral(Boolean.TRUE.toString(), XMLSchema.BOOLEAN);
	public static final Literal FALSE = getInstance().createLiteral(Boolean.FALSE.toString(), XMLSchema.BOOLEAN);

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public URI createURI(String uri) {
		return new URIImpl(uri);
	}

	@Override
	public URI createURI(String namespace, String localName) {
		return createURI(namespace + localName);
	}

	@Override
	public BNode createBNode(String nodeID) {
		return new BNodeImpl(nodeID);
	}

	@Override
	public Literal createLiteral(boolean b) {
		return b ? TRUE : FALSE;
	}

	@Override
	public Literal createLiteral(String value) {
		return new LiteralImpl(value, XMLSchema.STRING);
	}

	@Override
	public Literal createLiteral(String value, String language) {
		return new LiteralImpl(value, language);
	}

	@Override
	public Literal createLiteral(String value, URI datatype) {
		return new LiteralImpl(value, datatype);
	}

	@Override
	public Statement createStatement(Resource subject, URI predicate, Value object) {
		return new StatementImpl(subject, predicate, object);
	}

	@Override
	public Statement createStatement(Resource subject, URI predicate, Value object, Resource context) {
		return new ContextStatementImpl(subject, predicate, object, context);
	}
}
