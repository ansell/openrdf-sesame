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
 * Constants for the <a href="http://www.w3.org/TR/rdf-schema/">RDF Vocabulary
 * Description Language 1.0: RDF Schema</a> (RDFS)
 * 
 * @see http://www.w3.org/TR/rdf-schema/
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
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

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
