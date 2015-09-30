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
