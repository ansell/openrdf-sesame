/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.inferencer.fc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.UnsupportedQueryLanguageException;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.sail.NotifyingSail;
import org.eclipse.rdf4j.sail.SailConnectionListener;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.NotifyingSailWrapper;
import org.eclipse.rdf4j.sail.inferencer.InferencerConnection;
import org.eclipse.rdf4j.sail.inferencer.InferencerConnectionWrapper;
import org.eclipse.rdf4j.sail.inferencer.fc.config.CustomGraphQueryInferencerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A forward-chaining inferencer that infers new statements using a SPARQL or
 * SeRQL graph query.
 * 
 * @author Dale Visser
 */
public class CustomGraphQueryInferencer extends NotifyingSailWrapper {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ParsedGraphQuery customQuery;

	private ParsedGraphQuery customMatcher;

	private final Collection<Value> watchPredicates = new HashSet<Value>();

	private final Collection<Value> watchSubjects = new HashSet<Value>();

	private final Collection<Value> watchObjects = new HashSet<Value>();

	private boolean hasWatchValues;

	public CustomGraphQueryInferencer() {
		super();
	}

	/**
	 * Create a new custom inferencer.
	 * 
	 * @param language
	 *        language that <tt>queryText</tt> and <tt>matcherText</tt> are
	 *        expressed in
	 * @param queryText
	 *        a query that returns an RDF graph of inferred statements to be
	 *        added to the underlying Sail
	 * @param matcherText
	 *        a query that returns an RDF graph of existing inferred statements
	 *        already added previously
	 * @throws MalformedQueryException
	 *         if there is a problem parsing either of the given queries
	 * @throws UnsupportedQueryLanguageException
	 *         if an unsupported query language is specified
	 * @throws SailException
	 *         if a problem occurs interpreting the rule pattern
	 */
	public CustomGraphQueryInferencer(QueryLanguage language, String queryText, String matcherText)
		throws MalformedQueryException, UnsupportedQueryLanguageException, SailException
	{
		super();
		setFields(language, queryText, matcherText);
	}

	/**
	 * Create a new custom inferencer.
	 * 
	 * @param baseSail
	 *        an underlying Sail, such as another inferencer or a SailRepository
	 * @param language
	 *        language that <tt>queryText</tt> and <tt>matcherText</tt> are
	 *        expressed in
	 * @param queryText
	 *        a query that returns an RDF graph of inferred statements to be
	 *        added to the underlying Sail
	 * @param matcherText
	 *        a query that returns an RDF graph of existing inferred statements
	 *        already added previously
	 * @throws MalformedQueryException
	 *         if there is a problem parsing either of the given queries
	 * @throws UnsupportedQueryLanguageException
	 * @throws SailException
	 *         if a problem occurs interpreting the rule pattern
	 */
	public CustomGraphQueryInferencer(NotifyingSail baseSail, QueryLanguage language, String queryText,
			String matcherText)
		throws MalformedQueryException, UnsupportedQueryLanguageException, SailException
	{
		super(baseSail);
		setFields(language, queryText, matcherText);
	}

	/**
	 * Called in order to set all the fields needed for the inferencer to
	 * function.
	 * 
	 * @param language
	 *        language that <tt>queryText</tt> and <tt>matcherText</tt> are
	 *        expressed in
	 * @param queryText
	 *        a query that returns an RDF graph of inferred statements to be
	 *        added to the underlying Sail
	 * @param matcherText
	 *        a query that returns an RDF graph of existing inferred statements
	 *        already added previously
	 * @throws MalformedQueryException
	 *         if there is a problem parsing either of the given queries
	 * @throws SailException
	 *         if a problem occurs interpreting the rule pattern
	 */
	public final void setFields(QueryLanguage language, String queryText, String matcherText)
		throws MalformedQueryException, SailException
	{
		customQuery = QueryParserUtil.parseGraphQuery(language, queryText, null);
		String matcherQuery = matcherText;
		if (matcherText.trim().isEmpty()) {
			matcherQuery = CustomGraphQueryInferencerConfig.buildMatcherQueryFromRuleQuery(language, queryText);
		}
		customMatcher = QueryParserUtil.parseGraphQuery(language, matcherQuery, null);
		customQuery.getTupleExpr().visit(new AbstractQueryModelVisitor<SailException>() {

			@Override
			public void meet(StatementPattern statement)
				throws SailException
			{
				Var var = statement.getSubjectVar();
				if (var.hasValue()) {
					watchSubjects.add(var.getValue());
				}
				var = statement.getPredicateVar();
				if (var.hasValue()) {
					watchPredicates.add(var.getValue());
				}
				var = statement.getObjectVar();
				if (var.hasValue()) {
					watchObjects.add(var.getValue());
				}
			}
		});
		hasWatchValues = !(watchSubjects.isEmpty() && watchPredicates.isEmpty() && watchObjects.isEmpty());
	}

	@Override
	public InferencerConnection getConnection()
		throws SailException
	{
		try {
			InferencerConnection con = (InferencerConnection)super.getConnection();
			return new Connection(con);
		}
		catch (ClassCastException e) {
			throw new SailException(e.getMessage(), e);
		}
	}

	@Override
	public void initialize()
		throws SailException
	{
		super.initialize();
		InferencerConnection con = getConnection();
		try {
			con.begin();
			con.flushUpdates();
			con.commit();
		}
		finally {
			con.close();
		}
	}

	/**
	 * Exposed for test purposes.
	 * 
	 * @return a computed collection of the statement subjects that, when added
	 *         or removed, trigger an update of inferred statements
	 */
	public Collection<Value> getWatchSubjects() {
		return Collections.unmodifiableCollection(watchSubjects);
	}

	/**
	 * Exposed for test purposes.
	 * 
	 * @return a computed collection of the statement predicates that, when added
	 *         or removed, trigger an update of inferred statements
	 */
	public Collection<Value> getWatchPredicates() {
		return Collections.unmodifiableCollection(watchPredicates);
	}

	/**
	 * Exposed for test purposes.
	 * 
	 * @return a computed collection of the statement objects that, when added or
	 *         removed, trigger an update of inferred statements
	 */
	public Collection<Value> getWatchObjects() {
		return Collections.unmodifiableCollection(watchObjects);
	}

	private class Connection extends InferencerConnectionWrapper implements SailConnectionListener {

		/**
		 * Flag indicating whether an update of the inferred statements is needed.
		 */
		private boolean updateNeeded = false;

		private Connection(InferencerConnection con) {
			super(con);
			con.addConnectionListener(this);
		}

		@Override
		public void statementAdded(Statement statement) {
			setUpdateNeededIfMatching(statement);
		}

		@Override
		public void statementRemoved(Statement statement) {
			setUpdateNeededIfMatching(statement);
		}

		private void setUpdateNeededIfMatching(Statement statement) {
			updateNeeded = hasWatchValues ? watchPredicates.contains(statement.getPredicate())
					|| watchSubjects.contains(statement.getSubject())
					|| watchObjects.contains(statement.getObject()) : true;
		}

		@Override
		public void rollback()
			throws SailException
		{
			super.rollback();
			updateNeeded = false;
		}

		@Override
		public void flushUpdates()
			throws SailException
		{
			super.flushUpdates();
			Collection<Statement> forRemoval = new HashSet<Statement>(256);
			Collection<Statement> forAddition = new HashSet<Statement>(256);
			Resource[] contexts = new Resource[] { null };
			while (updateNeeded) {
				try {
					// Determine which statements should be added and which should be
					// removed
					forRemoval.clear();
					forAddition.clear();
					buildDeltaSets(forRemoval, forAddition);
					for (Statement st : forRemoval) {
						removeInferredStatement(st.getSubject(), st.getPredicate(), st.getObject(), contexts);
					}
					for (Statement st : forAddition) {
						addInferredStatement(st.getSubject(), st.getPredicate(), st.getObject(), contexts);
					}
					updateNeeded = false;
				}
				catch (RDFHandlerException e) {
					Throwable cause = e.getCause();
					if (cause instanceof SailException) {
						throw (SailException)cause;
					}
					else {
						throw new SailException(cause);
					}
				}
				catch (QueryEvaluationException e) {
					throw new SailException(e);
				}
				super.flushUpdates();
			}
		}

		private void buildDeltaSets(Collection<Statement> forRemoval, Collection<Statement> forAddition)
			throws SailException, RDFHandlerException, QueryEvaluationException
		{
			evaluateIntoStatements(customMatcher, forRemoval);
			evaluateIntoStatements(customQuery, forAddition);
			logger.debug("existing virtual properties: {}", forRemoval.size());
			logger.debug("new virtual properties: {}", forAddition.size());
			Collection<Statement> inCommon = new HashSet<Statement>(forRemoval);
			inCommon.retainAll(forAddition);
			forRemoval.removeAll(inCommon);
			forAddition.removeAll(inCommon);
			logger.debug("virtual properties to remove: {}", forRemoval.size());
			logger.debug("virtual properties to add: {}", forAddition.size());
		}

		private void evaluateIntoStatements(ParsedGraphQuery query, Collection<Statement> statements)
			throws SailException, RDFHandlerException, QueryEvaluationException
		{
			CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingsIter = getWrappedConnection().evaluate(
					query.getTupleExpr(), null, EmptyBindingSet.getInstance(), true);
			try {
				ValueFactory factory = getValueFactory();
				while (bindingsIter.hasNext()) {
					BindingSet bindings = bindingsIter.next();
					Value subj = bindings.getValue("subject");
					Value pred = bindings.getValue("predicate");
					Value obj = bindings.getValue("object");
					if (subj instanceof Resource && pred instanceof IRI && obj != null) {
						statements.add(factory.createStatement((Resource)subj, (IRI)pred, obj));
					}
				}
			}
			finally {
				bindingsIter.close();
			}
		}
	}
}