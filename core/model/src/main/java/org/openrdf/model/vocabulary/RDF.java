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
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Constants for RDF primitives and for the RDF namespace.
 * 
 * @see <a href="http://www.w3.org/TR/REC-rdf-syntax/">RDF/XML Syntax
 *      Specification (Revised)</a>
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
	public final static IRI TYPE;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Property */
	public final static IRI PROPERTY;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral */
	public final static IRI XMLLITERAL;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#subject */
	public final static IRI SUBJECT;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate */
	public final static IRI PREDICATE;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#object */
	public final static IRI OBJECT;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement */
	public final static IRI STATEMENT;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag */
	public final static IRI BAG;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt */
	public final static IRI ALT;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq */
	public final static IRI SEQ;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#value */
	public final static IRI VALUE;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#li */
	public final static IRI LI;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#List */
	public final static IRI LIST;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#first */
	public final static IRI FIRST;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#rest */
	public final static IRI REST;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#nil */
	public final static IRI NIL;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#langString */
	public static final IRI LANGSTRING;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML */
	public static final IRI HTML;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		TYPE = factory.createIRI(RDF.NAMESPACE, "type");
		PROPERTY = factory.createIRI(RDF.NAMESPACE, "Property");
		XMLLITERAL = factory.createIRI(RDF.NAMESPACE, "XMLLiteral");
		SUBJECT = factory.createIRI(RDF.NAMESPACE, "subject");
		PREDICATE = factory.createIRI(RDF.NAMESPACE, "predicate");
		OBJECT = factory.createIRI(RDF.NAMESPACE, "object");
		STATEMENT = factory.createIRI(RDF.NAMESPACE, "Statement");
		BAG = factory.createIRI(RDF.NAMESPACE, "Bag");
		ALT = factory.createIRI(RDF.NAMESPACE, "Alt");
		SEQ = factory.createIRI(RDF.NAMESPACE, "Seq");
		VALUE = factory.createIRI(RDF.NAMESPACE, "value");
		LI = factory.createIRI(RDF.NAMESPACE, "li");
		LIST = factory.createIRI(RDF.NAMESPACE, "List");
		FIRST = factory.createIRI(RDF.NAMESPACE, "first");
		REST = factory.createIRI(RDF.NAMESPACE, "rest");
		NIL = factory.createIRI(RDF.NAMESPACE, "nil");
		LANGSTRING = factory.createIRI(RDF.NAMESPACE, "langString");
		HTML = factory.createIRI(RDF.NAMESPACE, "HTML");
	}
}
