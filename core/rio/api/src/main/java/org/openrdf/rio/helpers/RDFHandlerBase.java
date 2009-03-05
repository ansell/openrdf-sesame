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
 * Base class for {@link RDFHandler}s with dummy implementations of all methods.
 */
public class RDFHandlerBase implements RDFHandler {

	public void startRDF()
		throws RDFHandlerException
	{
	}

	public void endRDF()
		throws RDFHandlerException
	{
	}

	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException
	{
	}

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
	}

	public void handleComment(String comment)
		throws RDFHandlerException
	{
	}
}
