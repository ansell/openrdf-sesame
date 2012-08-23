package org.openrdf.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the Dublin Core Metadata Element Set, version 1.1
 * 
 * @see http://dublincore.org/documents/dces/
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
