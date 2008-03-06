/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.helpers;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * Convenience base class for RDF handlers that wrap another RDF handler. This
 * class provides default methods that forward method calls to the wrapped RDF
 * handler.
 * 
 * @author Arjohn Kampman
 */
public class RDFHandlerWrapper implements RDFHandler {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The wrapped RDF handler.
	 */
	private RDFHandler rdfHandler;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFHandlerWrapper that wraps the supplied RDF handler.
	 * 
	 * @param rdfHandler
	 *        The wrapped RDF handler for this <tt>RDFHandlerWrapper</tt>,
	 *        must not be <tt>null</tt>.
	 */
	public RDFHandlerWrapper(RDFHandler rdfHandler) {
		assert rdfHandler != null;
		this.rdfHandler = rdfHandler;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void startRDF()
		throws RDFHandlerException
	{
		rdfHandler.startRDF();
	}

	public void endRDF()
		throws RDFHandlerException
	{
		rdfHandler.endRDF();
	}

	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException
	{
		rdfHandler.handleNamespace(prefix, uri);
	}

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		rdfHandler.handleStatement(st);
	}

	public void handleComment(String comment)
		throws RDFHandlerException
	{
		rdfHandler.handleComment(comment);
	}
}
