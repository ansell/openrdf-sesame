/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Constants for RDF Schema primitives and for the RDF Schema namespace.
 */
public class RDFS {

	/** http://www.w3.org/2000/01/rdf-schema# */
	public static final String NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";

	/** http://www.w3.org/2000/01/rdf-schema#Resource */
	public final static URI RESOURCE;

	/** http://www.w3.org/2000/01/rdf-schema#Literal */
	public final static URI LITERAL;

	/** http://www.w3.org/2000/01/rdf-schema#Class */
	public final static URI CLASS;

	/** http://www.w3.org/2000/01/rdf-schema#subClassOf */
	public final static URI SUBCLASSOF;

	/** http://www.w3.org/2000/01/rdf-schema#subPropertyOf */
	public final static URI SUBPROPERTYOF;

	/** http://www.w3.org/2000/01/rdf-schema#domain */
	public final static URI DOMAIN;

	/** http://www.w3.org/2000/01/rdf-schema#range */
	public final static URI RANGE;

	/** http://www.w3.org/2000/01/rdf-schema#comment */
	public final static URI COMMENT;

	/** http://www.w3.org/2000/01/rdf-schema#label */
	public final static URI LABEL;

	/** http://www.w3.org/2000/01/rdf-schema#Datatype */
	public final static URI DATATYPE;

	/** http://www.w3.org/2000/01/rdf-schema#Container */
	public final static URI CONTAINER;

	/** http://www.w3.org/2000/01/rdf-schema#member */
	public final static URI MEMBER;

	/** http://www.w3.org/2000/01/rdf-schema#isDefinedBy */
	public final static URI ISDEFINEDBY;

	/** http://www.w3.org/2000/01/rdf-schema#seeAlso */
	public final static URI SEEALSO;

	/** http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty */
	public final static URI CONTAINERMEMBERSHIPPROPERTY;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		RESOURCE = factory.createURI(RDFS.NAMESPACE, "Resource");
		LITERAL = factory.createURI(RDFS.NAMESPACE, "Literal");
		CLASS = factory.createURI(RDFS.NAMESPACE, "Class");
		SUBCLASSOF = factory.createURI(RDFS.NAMESPACE, "subClassOf");
		SUBPROPERTYOF = factory.createURI(RDFS.NAMESPACE, "subPropertyOf");
		DOMAIN = factory.createURI(RDFS.NAMESPACE, "domain");
		RANGE = factory.createURI(RDFS.NAMESPACE, "range");
		COMMENT = factory.createURI(RDFS.NAMESPACE, "comment");
		LABEL = factory.createURI(RDFS.NAMESPACE, "label");
		DATATYPE = factory.createURI(RDFS.NAMESPACE, "Datatype");
		CONTAINER = factory.createURI(RDFS.NAMESPACE, "Container");
		MEMBER = factory.createURI(RDFS.NAMESPACE, "member");
		ISDEFINEDBY = factory.createURI(RDFS.NAMESPACE, "isDefinedBy");
		SEEALSO = factory.createURI(RDFS.NAMESPACE, "seeAlso");
		CONTAINERMEMBERSHIPPROPERTY = factory.createURI(RDFS.NAMESPACE, "ContainerMembershipProperty");
	}
}
