/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;
import org.openrdf.sail.helpers.SailWrapper;

/**
 * A VirtualPropertyInferencer is a stackable sail that infers the properties
 * <tt>sesame:directSubClassOf</tt>, <tt>sesame:directSubPropertyOf</tt>
 * and <tt>sesame:directType</tt>.
 * <p>
 * 
 * <pre>
 *    Class A is a direct subclass of B iff:
 *       1. A is a subclass of B and;
 *       2. A and B are not equa and;
 *       3. there is no class C (unequal A and B) such that 
 *          A is a subclass of C and C of B.
 *   
 *    Property P is a direct subproperty of Q iff:
 *       1. P is a subproperty of Q and;
 *       2. P and Q are not equal and;
 *       3. there is no property R (unequal P and Q) such that
 *          P is a subproperty of R and R of Q.
 *   
 *    Resource I is of direct type T iff:
 *       1. I is of type T and
 *       2. There is no class U (unequal T) such that:
 *           a. U is a subclass of T and;
 *           b. I is of type U.
 * </pre>
 */
public class VirtualPropertyInferencer extends SailWrapper {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Constants * 
	 *-----------*/

	private static final ParsedGraphQuery DIRECT_SUBCLASSOF_MATCHER;

	private static final ParsedGraphQuery DIRECT_SUBCLASSOF_QUERY;

	private static final ParsedGraphQuery DIRECT_SUBPROPERTYOF_MATCHER;

	private static final ParsedGraphQuery DIRECT_SUBPROPERTYOF_QUERY;

	private static final ParsedGraphQuery DIRECT_TYPE_MATCHER;

	private static final ParsedGraphQuery DIRECT_TYPE_QUERY;

	static {
		try {
			DIRECT_SUBCLASSOF_MATCHER = QueryParserUtil.parseGraphQuery(QueryLanguage.SERQL,
					"CONSTRUCT * FROM {X} sesame:directSubClassOf {Y} ", null);

			DIRECT_SUBPROPERTYOF_MATCHER = QueryParserUtil.parseGraphQuery(QueryLanguage.SERQL,
					"CONSTRUCT * FROM {X} sesame:directType {Y}", null);

			DIRECT_TYPE_MATCHER = QueryParserUtil.parseGraphQuery(QueryLanguage.SERQL,
					"CONSTRUCT * FROM {X} sesame:directSubPropertyOf {Y}", null);

			DIRECT_SUBCLASSOF_QUERY = QueryParserUtil.parseGraphQuery(QueryLanguage.SERQL,
					"CONSTRUCT {X} sesame:directSubClassOf {Y} " + "FROM {X} rdfs:subClassOf {Y} "
							+ "WHERE X != Y AND NOT EXISTS (" + "SELECT Z "
							+ "FROM {X} rdfs:subClassOf {Z} rdfs:subClassOf {Y} " + "WHERE X != Z AND Z != Y)", null);

			DIRECT_SUBPROPERTYOF_QUERY = QueryParserUtil.parseGraphQuery(QueryLanguage.SERQL,
					"CONSTRUCT {X} sesame:directSubPropertyOf {Y} " + "FROM {X} rdfs:subPropertyOf {Y} "
							+ "WHERE X != Y AND NOT EXISTS (" + "SELECT Z "
							+ "FROM {X} rdfs:subPropertyOf {Z} rdfs:subPropertyOf {Y} " + "WHERE X != Z AND Z != Y)", null);

			DIRECT_TYPE_QUERY = QueryParserUtil.parseGraphQuery(QueryLanguage.SERQL,
					"CONSTRUCT {X} sesame:directType {Y} " + "FROM {X} rdf:type {Y} " + "WHERE NOT EXISTS ("
							+ "SELECT Z " + "FROM {X} rdf:type {Z} rdfs:subClassOf {Y} " + "WHERE Z != Y)", null);
		}
		catch (MalformedQueryException e) {
			// Can only occur due to a bug in this code
			throw new RuntimeException(e);
		}
	}

	/*--------------*
	 * Constructors * 
	 *--------------*/

	public VirtualPropertyInferencer() {
		super();
	}

	public VirtualPropertyInferencer(Sail baseSail) {
		super(baseSail);
	}

	/*---------*
	 * Methods * 
	 *---------*/

	public SailConnection getConnection()
		throws SailException
	{
		return new VirtualPropertyInferencerConnection(super.getConnection());
	}

	/*-------------------------------------------------*
	 * Inner class VirtualPropertyInferencerConnection *
	 *-------------------------------------------------*/

	private class VirtualPropertyInferencerConnection extends SailConnectionWrapper {

		public VirtualPropertyInferencerConnection(SailConnection wrappedTxn) {
			super(wrappedTxn);
		}

		public void commit()
			throws SailException
		{
			// FIXME this is not quite right from a transaction-isolation point of view: either
			// the entire commit succeeds or everything fails and rolls back.
			super.commit();

			try {
				// Determine which statements should be added

				// and which should be removed
				Collection<Statement> oldStatements = new HashSet<Statement>(256);
				Collection<Statement> newStatements = new HashSet<Statement>(256);

				StatementCollector oldSC = new StatementCollector(oldStatements);
				StatementCollector newSC = new StatementCollector(newStatements);

				evaluateIntoStatements(DIRECT_SUBCLASSOF_MATCHER, oldSC);
				evaluateIntoStatements(DIRECT_SUBPROPERTYOF_MATCHER, oldSC);
				evaluateIntoStatements(DIRECT_TYPE_MATCHER, oldSC);

				evaluateIntoStatements(DIRECT_SUBCLASSOF_QUERY, newSC);
				evaluateIntoStatements(DIRECT_SUBPROPERTYOF_QUERY, newSC);
				evaluateIntoStatements(DIRECT_TYPE_QUERY, newSC);

				logger.debug("existing virtual properties: {}", oldStatements.size());
				logger.debug("new virtual properties: {}", newStatements.size());

				// Remove the statements that should be retained from both sets
				oldStatements.removeAll(newStatements);
				newStatements.removeAll(oldStatements);

				logger.debug("virtual properties to remove: {}", oldStatements.size());
				logger.debug("virtual properties to add: {}", newStatements.size());

				if (!oldStatements.isEmpty() || !newStatements.isEmpty()) {
					InferencerConnection con = (InferencerConnection)getWrappedConnection();

					try {
						for (Statement st : oldStatements) {
							con.removeInferredStatement(st.getSubject(), st.getPredicate(), st.getObject(), (Resource)null);
						}

						for (Statement st : newStatements) {
							con.addInferredStatement(st.getSubject(), st.getPredicate(), st.getObject(), (Resource)null);
						}

						con.commit();
					}
					catch (SailException e) {
						con.rollback();
						throw e;
					}
					catch (RuntimeException e) {
						con.rollback();
						throw e;
					}
				}
			}
			catch (RDFHandlerException e) {
				Throwable t = e.getCause();
				if (t instanceof SailException) {
					throw (SailException)t;
				}
				else {
					throw new SailException(t);
				}
			}
			catch (ClassCastException e) {
				throw new SailException(e);
			}
			catch (QueryEvaluationException e) {
				throw new SailException(e);
			}
		}

		private void evaluateIntoStatements(ParsedGraphQuery query, StatementCollector collector)
			throws SailException, RDFHandlerException, QueryEvaluationException
		{
			CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingsIter = super.evaluate(
					query.getTupleExpr(), new MapBindingSet(0), true);

			try {
				collector.startRDF();

				while (bindingsIter.hasNext()) {
					BindingSet bindings = bindingsIter.next();

					Value subj = bindings.getValue("subject");
					Value pred = bindings.getValue("predicate");
					Value obj = bindings.getValue("object");

					if (subj instanceof Resource && pred instanceof URI && obj != null) {
						Statement st = getValueFactory().createStatement((Resource)subj, (URI)pred, obj);
						collector.handleStatement(st);
					}
				}

				collector.endRDF();
			}
			finally {
				bindingsIter.close();
			}
		}
	} // end inner class VirtualPropertyInferencerTransaction
}
