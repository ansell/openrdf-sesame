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
package org.openrdf.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Constants for RDF primitives and for the RDF namespace.
 * 
 * @see http://www.w3.org/TR/REC-rdf-syntax/
 */
public class RDF {

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns# */
	public static final String NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	/**
	 * Recommended prefix for the RDF namespace: "rdf"
	 */
	public static final String PREFIX = "rdf";

	/**
	 * An immutable {@link Namespace} constant that represents the RDF namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#type */
	public final static URI TYPE;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Property */
	public final static URI PROPERTY;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral */
	public final static URI XMLLITERAL;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#subject */
	public final static URI SUBJECT;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate */
	public final static URI PREDICATE;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#object */
	public final static URI OBJECT;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement */
	public final static URI STATEMENT;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag */
	public final static URI BAG;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt */
	public final static URI ALT;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq */
	public final static URI SEQ;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#value */
	public final static URI VALUE;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#li */
	public final static URI LI;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#List */
	public final static URI LIST;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#first */
	public final static URI FIRST;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#rest */
	public final static URI REST;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#nil */
	public final static URI NIL;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#langString */
	public static final URI LANGSTRING;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		TYPE = factory.createURI(RDF.NAMESPACE, "type");
		PROPERTY = factory.createURI(RDF.NAMESPACE, "Property");
		XMLLITERAL = factory.createURI(RDF.NAMESPACE, "XMLLiteral");
		SUBJECT = factory.createURI(RDF.NAMESPACE, "subject");
		PREDICATE = factory.createURI(RDF.NAMESPACE, "predicate");
		OBJECT = factory.createURI(RDF.NAMESPACE, "object");
		STATEMENT = factory.createURI(RDF.NAMESPACE, "Statement");
		BAG = factory.createURI(RDF.NAMESPACE, "Bag");
		ALT = factory.createURI(RDF.NAMESPACE, "Alt");
		SEQ = factory.createURI(RDF.NAMESPACE, "Seq");
		VALUE = factory.createURI(RDF.NAMESPACE, "value");
		LI = factory.createURI(RDF.NAMESPACE, "li");
		LIST = factory.createURI(RDF.NAMESPACE, "List");
		FIRST = factory.createURI(RDF.NAMESPACE, "first");
		REST = factory.createURI(RDF.NAMESPACE, "rest");
		NIL = factory.createURI(RDF.NAMESPACE, "nil");
		LANGSTRING = factory.createURI(RDF.NAMESPACE, "langString");
	}
}
