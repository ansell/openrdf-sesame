/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.dawg;

import static org.openrdf.query.dawg.DAWGTestResultSetSchema.BOOLEAN;
import static org.openrdf.query.dawg.DAWGTestResultSetSchema.RESULTSET;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * @author Arjohn Kampman
 */
public class DAWGTestBooleanParser extends RDFHandlerBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Graph graph = new GraphImpl();

	private boolean value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public DAWGTestBooleanParser() {
	}

	/*---------*
	 * Methods *
	 *---------*/

	public boolean getValue() {
		return value;
	}

	@Override
	public void startRDF()
		throws RDFHandlerException
	{
		graph.clear();
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		graph.add(st);
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		try {
			Resource resultSetNode = GraphUtil.getUniqueSubject(graph, RDF.TYPE, RESULTSET);
			Literal booleanLit = GraphUtil.getUniqueObjectLiteral(graph, resultSetNode, BOOLEAN);

			if (booleanLit.equals(DAWGTestResultSetSchema.TRUE)) {
				value = true;
			}
			else if (booleanLit.equals(DAWGTestResultSetSchema.FALSE)) {
				value = false;
			}
			else {
				new RDFHandlerException("Invalid boolean value: " + booleanLit);
			}
		}
		catch (GraphUtilException e) {
			throw new RDFHandlerException(e.getMessage(), e);
		}
	}
}
