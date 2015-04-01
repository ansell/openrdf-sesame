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
 * Vocabulary constants for the Dublin Core Metadata Element Set, version 1.1
 * 
 * @see <a href="http://dublincore.org/documents/dces/">Dublin Core Metadata
 *      Element Set, Version 1.1</a>
 * @author Jeen Broekstra
 */
public class DC {

	/**
	 * Dublin Core elements namespace: http://purl.org/dc/elements/1.1/
	 */
	public static final String NAMESPACE = "http://purl.org/dc/elements/1.1/";

	/**
	 * Recommend prefix for the Dublin Core elements namespace: "dc"
	 */
	public static final String PREFIX = "dc";

	/**
	 * An immutable {@link Namespace} constant that represents the Dublin Core
	 * namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

	/**
	 * dc:title
	 */
	public static final IRI TITLE;

	/**
	 * dc:source
	 */
	public static final IRI SOURCE;

	/**
	 * dc:contributor
	 */
	public static final IRI CONTRIBUTOR;

	/**
	 * dc:coverage
	 */
	public static final IRI COVERAGE;

	/**
	 * dc:creator
	 */
	public static final IRI CREATOR;

	/**
	 * dc:date
	 */
	public static final IRI DATE;

	/**
	 * dc:description
	 */
	public static final IRI DESCRIPTION;

	/**
	 * dc:format
	 */
	public static final IRI FORMAT;

	/**
	 * dc:identifier
	 */
	public static final IRI IDENTIFIER;

	/**
	 * dc:language
	 */
	public static final IRI LANGUAGE;

	/**
	 * dc:publisher
	 */
	public static final IRI PUBLISHER;

	/**
	 * dc:relation
	 */
	public static final IRI RELATION;

	/**
	 * dc:rights
	 */
	public static final IRI RIGHTS;

	/**
	 * dc:subject
	 */
	public static final IRI SUBJECT;

	/**
	 * dc:type
	 */
	public static final IRI TYPE;

	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		CONTRIBUTOR = f.createIRI(NAMESPACE, "contributor");
		COVERAGE = f.createIRI(NAMESPACE, "coverage");
		CREATOR = f.createIRI(NAMESPACE, "creator");
		DATE = f.createIRI(NAMESPACE, "date");
		DESCRIPTION = f.createIRI(NAMESPACE, "description");
		FORMAT = f.createIRI(NAMESPACE, "format");
		IDENTIFIER = f.createIRI(NAMESPACE, "identifier");
		LANGUAGE = f.createIRI(NAMESPACE, "language");
		PUBLISHER = f.createIRI(NAMESPACE, "publisher");
		RELATION = f.createIRI(NAMESPACE, "relation");
		RIGHTS = f.createIRI(NAMESPACE, "rights");
		SOURCE = f.createIRI(NAMESPACE, "source");
		SUBJECT = f.createIRI(NAMESPACE, "subject");
		TITLE = f.createIRI(NAMESPACE, "title");
		TYPE = f.createIRI(NAMESPACE, "type");
	}
}
