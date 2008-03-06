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
 * Base class for {@link RDFHandler}s with dummy implementations of all
 * methods.
 */
public class RDFHandlerBase implements RDFHandler {

	// Implements RDFHandler.startRDF()
	public void startRDF()
		throws RDFHandlerException
	{
	}

	// Implements RDFHandler.endRDF()
	public void endRDF()
		throws RDFHandlerException
	{
	}

	// Implements RDFHandler.handleNamespace(...)
	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException
	{
	}

	// Implements RDFHandler.handleStatement(...)
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
	}

	// Implements RDFHandler.handleComment(...)
	public void handleComment(String comment)
		throws RDFHandlerException
	{
	}
}
