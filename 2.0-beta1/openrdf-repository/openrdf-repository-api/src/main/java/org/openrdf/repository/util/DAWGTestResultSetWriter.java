/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.util;

import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * A TupleQueryResultWriter that converts query results to an RDF graph using
 * the Data Access Working Group Test Result Set RDF Vocabulary
 * (http://www.w3.org/2001/sw/DataAccess/tests/result-set#).
 */
class DAWGTestResultSetWriter implements TupleQueryResultHandler {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String NS_RS = "http://www.w3.org/2001/sw/DataAccess/tests/result-set#";

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * RDFHandler to report the generated statements to.
	 */
	private RDFHandler _rdfHandler;

	private ValueFactory _vf;

	private URI rsResultSet;

	private URI rsResultVariable;

	private URI rsSolution;

	private URI rsBinding;

	private URI rsValue;

	private URI rsVariable;

	private BNode resultSetNode;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public DAWGTestResultSetWriter(RDFHandler rdfHandler) {
		this(rdfHandler, new ValueFactoryImpl());
	}

	public DAWGTestResultSetWriter(RDFHandler rdfHandler, ValueFactory vf) {
		_rdfHandler = rdfHandler;
		_vf = vf;

		rsResultSet = _vf.createURI(NS_RS + "ResultSet");
		rsResultVariable = _vf.createURI(NS_RS + "resultVariable");
		rsSolution = _vf.createURI(NS_RS + "solution");
		rsBinding = _vf.createURI(NS_RS + "binding");
		rsValue = _vf.createURI(NS_RS + "value");
		rsVariable = _vf.createURI(NS_RS + "variable");
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void startQueryResult(List<String> bindingNames, boolean distinct, boolean ordered)
		throws TupleQueryResultHandlerException
	{
		try {
			_rdfHandler.startRDF();

			resultSetNode = _vf.createBNode();

			_reportStatement(resultSetNode, RDF.TYPE, rsResultSet);

			for (String bindingName : bindingNames) {
				Literal bindingNameLit = _vf.createLiteral(bindingName);
				_reportStatement(resultSetNode, rsResultVariable, bindingNameLit);
			}
		}
		catch (RDFHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		try {
			_rdfHandler.endRDF();
		}
		catch (RDFHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		try {
			BNode solutionNode = _vf.createBNode();

			_reportStatement(resultSetNode, rsSolution, solutionNode);

			for (Binding bnd : bindingSet) {
				BNode bindingNode = _vf.createBNode();

				_reportStatement(solutionNode, rsBinding, bindingNode);
				_reportStatement(bindingNode, rsVariable, _vf.createLiteral(bnd.getName()));
				_reportStatement(bindingNode, rsValue, bnd.getValue());
			}
		}
		catch (RDFHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	private void _reportStatement(Resource subject, URI predicate, Value object)
		throws RDFHandlerException
	{
		_rdfHandler.handleStatement(_vf.createStatement(subject, predicate, object));
	}
}
