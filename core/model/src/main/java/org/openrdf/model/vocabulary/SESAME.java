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
 * Defines constants for the Sesame schema namespace.
 */
public class SESAME {

	/**
	 * The Sesame Schema namespace (
	 * <tt>http://www.openrdf.org/schema/sesame#</tt>).
	 */
	public static final String NAMESPACE = "http://www.openrdf.org/schema/sesame#";

	/**
	 * Recommended prefix for the Sesame Schema namespace: "sesame"
	 */
	public static final String PREFIX = "sesame";

	/**
	 * An immutable {@link Namespace} constant that represents the Sesame Schema
	 * namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

	/** <tt>http://www.openrdf.org/schema/sesame#directSubClassOf</tt> */
	public final static IRI DIRECTSUBCLASSOF;

	/** <tt>http://www.openrdf.org/schema/sesame#directSubPropertyOf</tt> */
	public final static IRI DIRECTSUBPROPERTYOF;

	/** <tt>http://www.openrdf.org/schema/sesame#directType</tt> */
	public final static IRI DIRECTTYPE;

	/**
	 * The SPARQL null context identifier (
	 * <tt>http://www.openrdf.org/schema/sesame#nil</tt>)
	 */
	public final static IRI NIL;

	/**
	 * <tt>http://www.openrdf.org/schema/sesame#wildcard</tt>
	 */
	public final static IRI WILDCARD;
	
	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		DIRECTSUBCLASSOF = factory.createIRI(SESAME.NAMESPACE, "directSubClassOf");
		DIRECTSUBPROPERTYOF = factory.createIRI(SESAME.NAMESPACE, "directSubPropertyOf");
		DIRECTTYPE = factory.createIRI(SESAME.NAMESPACE, "directType");

		NIL = factory.createIRI(NAMESPACE, "nil");
		
		WILDCARD = factory.createIRI(NAMESPACE, "wildcard");
	}
}
