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
package org.openrdf.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleNamespace;
import org.openrdf.model.impl.SimpleValueFactory;

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
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

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
		final ValueFactory f = SimpleValueFactory.getInstance();

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
