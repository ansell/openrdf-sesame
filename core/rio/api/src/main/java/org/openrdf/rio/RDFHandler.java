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

	/**
	 * Handles a change in the Base URI.
	 * <p>
	 * The statements sent to {@link #handleStatement(Statement)} will still
	 * contain complete URIs, but this method can be used to signal that the
	 * document contains a declaration that changes the default base URI in some
	 * way.
	 * 
	 * @param baseURI
	 *        The next base URI to handle. May be null to reset the Base URI to
	 *        either a default or no base URI depending on the handler.
	 * @throws RDFHandlerException
	 *         If the RDF handler has encountered an unrecoverable error.
	 * @since 2.8.0
	 * @see <a
	 *      href="http://www.w3.org/TR/rdf-syntax-grammar/#section-Syntax-ID-xml-base">RDF/XML
	 *      Relative URIs</a>
	 * @see <a href="http://www.w3.org/TR/turtle/#relative-iri">Turtle Relative
	 *      URIs</a>
	 */
	public void handleBaseURI(String baseURI)
		throws RDFHandlerException;
}
