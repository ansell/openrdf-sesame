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
	public static final URI TITLE;

	/**
	 * dc:source
	 */
	public static final URI SOURCE;

	/**
	 * dc:contributor
	 */
	public static final URI CONTRIBUTOR;

	/**
	 * dc:coverage
	 */
	public static final URI COVERAGE;

	/**
	 * dc:creator
	 */
	public static final URI CREATOR;

	/**
	 * dc:date
	 */
	public static final URI DATE;

	/**
	 * dc:description
	 */
	public static final URI DESCRIPTION;

	/**
	 * dc:format
	 */
	public static final URI FORMAT;

	/**
	 * dc:identifier
	 */
	public static final URI IDENTIFIER;

	/**
	 * dc:language
	 */
	public static final URI LANGUAGE;

	/**
	 * dc:publisher
	 */
	public static final URI PUBLISHER;

	/**
	 * dc:relation
	 */
	public static final URI RELATION;

	/**
	 * dc:rights
	 */
	public static final URI RIGHTS;

	/**
	 * dc:subject
	 */
	public static final URI SUBJECT;

	/**
	 * dc:type
	 */
	public static final URI TYPE;

	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		CONTRIBUTOR = f.createURI(NAMESPACE, "contributor");
		COVERAGE = f.createURI(NAMESPACE, "coverage");
		CREATOR = f.createURI(NAMESPACE, "creator");
		DATE = f.createURI(NAMESPACE, "date");
		DESCRIPTION = f.createURI(NAMESPACE, "description");
		FORMAT = f.createURI(NAMESPACE, "format");
		IDENTIFIER = f.createURI(NAMESPACE, "identifier");
		LANGUAGE = f.createURI(NAMESPACE, "language");
		PUBLISHER = f.createURI(NAMESPACE, "publisher");
		RELATION = f.createURI(NAMESPACE, "relation");
		RIGHTS = f.createURI(NAMESPACE, "rights");
		SOURCE = f.createURI(NAMESPACE, "source");
		SUBJECT = f.createURI(NAMESPACE, "subject");
		TITLE = f.createURI(NAMESPACE, "title");
		TYPE = f.createURI(NAMESPACE, "type");
	}
}
