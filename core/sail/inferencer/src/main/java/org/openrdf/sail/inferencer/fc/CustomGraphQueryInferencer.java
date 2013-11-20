/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.inferencer.fc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailWrapper;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.InferencerConnectionWrapper;
import org.openrdf.sail.inferencer.fc.config.CustomGraphQueryInferencerConfig;

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
		customQuery.getTupleExpr().visit(new QueryModelVisitorBase<SailException>() {

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
					if (subj instanceof Resource && pred instanceof URI && obj != null) {
						statements.add(factory.createStatement((Resource)subj, (URI)pred, obj));
					}
				}
			}
			finally {
				bindingsIter.close();
			}
		}
	}
}
