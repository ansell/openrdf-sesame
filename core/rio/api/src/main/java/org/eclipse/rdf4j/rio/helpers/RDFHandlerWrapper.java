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
package org.eclipse.rdf4j.rio.helpers;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;

/**
 * Convenience base class for RDF handlers that wrap one or more other RDF
 * handler. This class provides default methods that forward method calls to the
 * wrapped RDF handler(s).
 * 
 * @author Arjohn Kampman
 * @author Jeen Broekstra
 */
public class RDFHandlerWrapper implements RDFHandler {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The wrapped RDF handler(s).
	 */
	private final RDFHandler[] rdfHandlers;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFHandlerWrapper that wraps the supplied RDF handler(s). If
	 * more than one RDFHandler is supplied for wrapping, the RDFHandlerWrapper
	 * forwards every method call to each of the supplied handler, in the order
	 * in which the handlers are supplied.
	 * 
	 * @param rdfHandlers
	 *        One or more wrapped RDF handlers for this
	 *        <tt>RDFHandlerWrapper</tt>, must not be <tt>null</tt>.
	 */
	public RDFHandlerWrapper(RDFHandler... rdfHandlers) {
		assert rdfHandlers != null;
		this.rdfHandlers = rdfHandlers;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void startRDF()
		throws RDFHandlerException
	{
		for (RDFHandler rdfHandler : rdfHandlers) {
			rdfHandler.startRDF();
		}
	}

	public void endRDF()
		throws RDFHandlerException
	{
		for (RDFHandler rdfHandler : rdfHandlers) {
			rdfHandler.endRDF();
		}
	}

	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException
	{
		for (RDFHandler rdfHandler : rdfHandlers) {
			rdfHandler.handleNamespace(prefix, uri);
		}
	}

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		for (RDFHandler rdfHandler : rdfHandlers) {
			rdfHandler.handleStatement(st);
		}
	}

	public void handleComment(String comment)
		throws RDFHandlerException
	{
		for (RDFHandler rdfHandler : rdfHandlers) {
			rdfHandler.handleComment(comment);
		}
	}
}
