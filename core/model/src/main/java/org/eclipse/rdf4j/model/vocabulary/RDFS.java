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
 * Constants for the <a href="http://www.w3.org/TR/rdf-schema/">RDF Vocabulary
 * Description Language 1.0: RDF Schema</a> (RDFS)
 * 
 * @see <a href="http://www.w3.org/TR/rdf-schema/">RDF Vocabulary Description
 *      Language 1.0: RDF Schema (RDFS)</a>
 */
public class RDFS {

	/** The RDF Schema namepace: http://www.w3.org/2000/01/rdf-schema# */
	public static final String NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";

	/** Recommended prefix for the RDF Schema namespace: "rdfs" */
	public static final String PREFIX = "rdfs";

	/**
	 * An immutable {@link Namespace} constant that represents the RDFS
	 * namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	/** http://www.w3.org/2000/01/rdf-schema#Resource */
	public final static IRI RESOURCE;

	/** http://www.w3.org/2000/01/rdf-schema#Literal */
	public final static IRI LITERAL;

	/** http://www.w3.org/2000/01/rdf-schema#Class */
	public final static IRI CLASS;

	/** http://www.w3.org/2000/01/rdf-schema#subClassOf */
	public final static IRI SUBCLASSOF;

	/** http://www.w3.org/2000/01/rdf-schema#subPropertyOf */
	public final static IRI SUBPROPERTYOF;

	/** http://www.w3.org/2000/01/rdf-schema#domain */
	public final static IRI DOMAIN;

	/** http://www.w3.org/2000/01/rdf-schema#range */
	public final static IRI RANGE;

	/** http://www.w3.org/2000/01/rdf-schema#comment */
	public final static IRI COMMENT;

	/** http://www.w3.org/2000/01/rdf-schema#label */
	public final static IRI LABEL;

	/** http://www.w3.org/2000/01/rdf-schema#Datatype */
	public final static IRI DATATYPE;

	/** http://www.w3.org/2000/01/rdf-schema#Container */
	public final static IRI CONTAINER;

	/** http://www.w3.org/2000/01/rdf-schema#member */
	public final static IRI MEMBER;

	/** http://www.w3.org/2000/01/rdf-schema#isDefinedBy */
	public final static IRI ISDEFINEDBY;

	/** http://www.w3.org/2000/01/rdf-schema#seeAlso */
	public final static IRI SEEALSO;

	/** http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty */
	public final static IRI CONTAINERMEMBERSHIPPROPERTY;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		RESOURCE = factory.createIRI(RDFS.NAMESPACE, "Resource");
		LITERAL = factory.createIRI(RDFS.NAMESPACE, "Literal");
		CLASS = factory.createIRI(RDFS.NAMESPACE, "Class");
		SUBCLASSOF = factory.createIRI(RDFS.NAMESPACE, "subClassOf");
		SUBPROPERTYOF = factory.createIRI(RDFS.NAMESPACE, "subPropertyOf");
		DOMAIN = factory.createIRI(RDFS.NAMESPACE, "domain");
		RANGE = factory.createIRI(RDFS.NAMESPACE, "range");
		COMMENT = factory.createIRI(RDFS.NAMESPACE, "comment");
		LABEL = factory.createIRI(RDFS.NAMESPACE, "label");
		DATATYPE = factory.createIRI(RDFS.NAMESPACE, "Datatype");
		CONTAINER = factory.createIRI(RDFS.NAMESPACE, "Container");
		MEMBER = factory.createIRI(RDFS.NAMESPACE, "member");
		ISDEFINEDBY = factory.createIRI(RDFS.NAMESPACE, "isDefinedBy");
		SEEALSO = factory.createIRI(RDFS.NAMESPACE, "seeAlso");
		CONTAINERMEMBERSHIPPROPERTY = factory.createIRI(RDFS.NAMESPACE, "ContainerMembershipProperty");
	}
}
