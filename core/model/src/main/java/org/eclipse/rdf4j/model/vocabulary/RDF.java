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
package org.eclipse.rdf4j.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

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
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

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
		ValueFactory factory = SimpleValueFactory.getInstance();
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
