/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.helpers;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * Abstract superclass for RDF handlers that wrap another RDF handler. The
 * abstract class <tt>RDFHandlerWrapper</tt> itself provides default methods
 * that forward method calls to the wrapped RDF handler. Subclasses of
 * <tt>RDFHandlerWrapper</tt> should override some of these methods and may also
 * provide additional methods and fields.
 */
public abstract class RDFHandlerWrapper implements RDFHandler {
	
	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The wrapped RDF handler.
	 */
	private RDFHandler _rdfHandler;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFHandlerWrapper that wraps the supplied RDF handler.
	 *
	 * @param rdfHandler The wrapped RDF handler for this
	 * <tt>RDFHandlerWrapper</tt>, must not be <tt>null</tt>.
	 */
	public RDFHandlerWrapper(RDFHandler rdfHandler) {
		assert rdfHandler != null;
		_rdfHandler = rdfHandler;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Implements RDFHandler.startRDF()
	public void startRDF()
		throws RDFHandlerException
	{
		_rdfHandler.startRDF();
	}

	// Implements RDFHandler.endRDF()
	public void endRDF()
		throws RDFHandlerException
	{
		_rdfHandler.endRDF();
	}

	// Implements RDFHandler.handleNamespace(...)
	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException
	{
		_rdfHandler.handleNamespace(prefix, uri);
	}

	// Implements RDFHandler.handleStatement(...)
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		_rdfHandler.handleStatement(st);
	}
}
