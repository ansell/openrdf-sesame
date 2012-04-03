/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
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
}
