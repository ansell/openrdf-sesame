/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.dawg;

import static org.openrdf.query.dawg.DAWGTestResultSetSchema.BOOLEAN;
import static org.openrdf.query.dawg.DAWGTestResultSetSchema.RESULTSET;

import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ModelImpl;
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

	private Model model = new ModelImpl();

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
		model.clear();
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		model.add(st);
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		Set<Resource> resultSetNodes = model.filter(null, RDF.TYPE, RESULTSET).subjects();
		if (resultSetNodes.size() != 1) {
			throw new RDFHandlerException("Expected 1 subject of type ResultSet, found: "
					+ resultSetNodes.size());
		}

		Resource resultSetNode = resultSetNodes.iterator().next();

		Set<Value> resultValues = model.filter(resultSetNode, BOOLEAN, null).objects();
		if (resultValues.size() != 1) {
			throw new RDFHandlerException("Expected 1 boolean result value, found: " + resultValues.size());
		}

		Value booleanLit = resultValues.iterator().next();

		if (booleanLit.equals(DAWGTestResultSetSchema.TRUE)) {
			value = true;
		}
		else if (booleanLit.equals(DAWGTestResultSetSchema.FALSE)) {
			value = false;
		}
		else {
			throw new RDFHandlerException("Invalid boolean value: " + booleanLit);
		}
	}
}
