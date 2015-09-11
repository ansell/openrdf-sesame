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
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Default implementation of the ValueFactory interface that uses the RDF model
 * classes from this package.
 * 
 * @author Arjohn Kampman
 */
public class SimpleValueFactory extends AbstractValueFactory {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final SimpleValueFactory sharedInstance = new SimpleValueFactory();

	/**
	 * Provide a single shared instance of a SimpleValueFactory.
	 * 
	 * @return a singleton instance of SimpleValueFactory.
	 */
	public static SimpleValueFactory getInstance() {
		return sharedInstance;
	}

	/**
	 * Hidden constructor to enforce singleton pattern.
	 */
	protected SimpleValueFactory() {
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public IRI createIRI(String iri) {
		return new SimpleIRI(iri);
	}

	@Override
	public IRI createIRI(String namespace, String localName) {
		return createIRI(namespace + localName);
	}

	@Override
	public BNode createBNode(String nodeID) {
		return new SimpleBNode(nodeID);
	}

	@Override
	public Literal createLiteral(String value) {
		return new SimpleLiteral(value, XMLSchema.STRING);
	}

	@Override
	public Literal createLiteral(String value, String language) {
		return new SimpleLiteral(value, language);
	}

	@Override
	public Literal createLiteral(boolean b) {
		return b ? BooleanLiteral.TRUE : BooleanLiteral.FALSE;
	}

	@Override
	public Literal createLiteral(String value, IRI datatype) {
		return new SimpleLiteral(value, datatype);
	}

	@Override
	public Statement createStatement(Resource subject, IRI predicate, Value object) {
		return new SimpleStatement(subject, predicate, object);
	}

	@Override
	public Statement createStatement(Resource subject, IRI predicate, Value object, Resource context) {
		return new ContextStatement(subject, predicate, object, context);
	}
}
