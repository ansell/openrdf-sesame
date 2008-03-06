/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import org.openrdf.model.Statement;

/**
 * An interface defining methods related to RDF data handling.
 * <tt>RDFHandler</tt> is both used as a "consumer" and as a "producer"
 * interface. As such it can be used both as an interface for receiving RDF
 * data, for example by listening to the results of an RDF parser, and as an
 * interface for reporting RDF data, for example to an object that serializes
 * RDF data to an RDF/XML document.
 */
public interface RDFHandler {

	/**
	 * Signals the start of the RDF data. This method is called before any data
	 * is reported.
	 * 
	 * @throws RDFHandlerException
	 *         If the RDF handler has encountered an unrecoverable error.
	 */
	public void startRDF()
		throws RDFHandlerException;

	/**
	 * Signals the end of the RDF data. This method is called when all data has
	 * been reported.
	 * 
	 * @throws RDFHandlerException
	 *         If the RDF handler has encountered an unrecoverable error.
	 */
	public void endRDF()
		throws RDFHandlerException;

	/**
	 * Handles a namespace declaration/definition. A namespace declaration
	 * associates a (short) prefix string with the namespace's URI. The prefix
	 * for default namespaces, which do not have an associated prefix, are
	 * represented as empty strings.
	 * 
	 * @param prefix
	 *        The prefix for the namespace, or an empty string in case of a
	 *        default namespace.
	 * @param uri
	 *        The URI that the prefix maps to.
	 * @throws RDFHandlerException
	 *         If the RDF handler has encountered an unrecoverable error.
	 */
	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException;

	/**
	 * Handles a statement.
	 * 
	 * @param st
	 *        The statement.
	 * @throws RDFHandlerException
	 *         If the RDF handler has encountered an unrecoverable error.
	 */
	public void handleStatement(Statement st)
		throws RDFHandlerException;

	/**
	 * Handles a comment.
	 * 
	 * @param comment
	 *        The comment.
	 * @throws RDFHandlerException
	 *         If the RDF handler has encountered an unrecoverable error.
	 */
	public void handleComment(String comment)
		throws RDFHandlerException;
}
