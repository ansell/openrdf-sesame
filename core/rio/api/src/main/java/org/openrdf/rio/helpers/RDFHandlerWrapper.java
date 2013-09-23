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
package org.openrdf.rio.helpers;

import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

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

	@Override
	public void handleBaseURI(String baseURI)
		throws RDFHandlerException
	{
		for (RDFHandler rdfHandler : rdfHandlers) {
			rdfHandler.handleBaseURI(baseURI);
		}
	}
}
