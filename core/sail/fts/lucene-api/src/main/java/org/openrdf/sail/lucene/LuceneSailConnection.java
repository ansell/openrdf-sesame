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
package org.openrdf.sail.lucene;

import info.aduna.iteration.CloseableIteration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.SimpleLiteral;
import org.openrdf.model.impl.SimpleIRI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.helpers.AbstractQueryModelVisitor;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailConnectionWrapper;
import org.openrdf.sail.lucene.LuceneSailBuffer.AddRemoveOperation;
import org.openrdf.sail.lucene.LuceneSailBuffer.ClearContextOperation;
import org.openrdf.sail.lucene.LuceneSailBuffer.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h2><a name="whySailConnectionListener">Sail Connection Listener instead of
 * implementing add/remove</a></h2> Using SailConnectionListener, see <a
 * href="#whySailConnectionListener">above</a> The LuceneIndex is adapted based
 * on events coming from the wrapped Sail, rather than by overriding the
 * addStatement and removeStatements methods. This approach has two benefits:
 * (1) when the wrapped Sail only reports statements that were not stored
 * before, the LuceneIndex does not have to do the check on the skipped
 * statemements and (2) the method for removing Statements from the Lucene index
 * does not have to take wildcards into account, making its implementation
 * simpler. <h2>Synchronized Methods</h2> LuceneSailConnection uses a listener
 * to collect removed statements. The listener should not be active during the
 * removal of contexts, as this is not needed (context removal is implemented
 * differently). To realize this, all methods that can do changes are
 * synchronized and during context removal, the listener is disabled. Thus, all
 * methods of this connection that can change data are synchronized. <h2>
 * Evaluating Queries - possible optimizations</h2> Arjohn has answered this
 * question in the sesame-dev mailinglist on 13.8.2007: <b>Is there a
 * QueryModelNode that can contain a fixed (perhaps very long) list of Query
 * result bindings?</b> There is currently no such node, but there are two
 * options to get similar behaviour: 1) Include the result bindings as OR-ed
 * constraints in the query model. E.g. if you have a result binding like
 * {{x=1,y=1},{x=2,y=2}}, this translates to the constraints (x=1 and y=1) or
 * (x=2 and y=2). 2) The LuceneSail could iterate over the LuceneQueryResult and
 * supply the various results as query input parameters to the underlying Sail.
 * This is similar to using PreparedStatement's in JDBC.
 * 
 * @author sauermann
 * @author christian.huetter
 */
public class LuceneSailConnection extends NotifyingSailConnectionWrapper {

	final private Logger logger = LoggerFactory.getLogger(this.getClass());

	final private SearchIndex luceneIndex;

	final private LuceneSail sail;

	/**
	 * the buffer that collects operations
	 */
	final private LuceneSailBuffer buffer = new LuceneSailBuffer();

	/**
	 * The listener that listens to the underlying connection. It is disabled
	 * during clearContext operations.
	 */
	protected final SailConnectionListener connectionListener = new SailConnectionListener() {

		@Override
		public void statementAdded(Statement statement) {
			// we only consider statements that contain literals
			if (statement.getObject() instanceof Literal) {
				statement = sail.mapStatement(statement);
				if (statement == null)
					return;
				// we further only index statements where the Literal's datatype is
				// accepted
				Literal literal = (Literal)statement.getObject();
				if (luceneIndex.accept(literal))
					buffer.add(statement);
			}
		}

		@Override
		public void statementRemoved(Statement statement) {
			// we only consider statements that contain literals
			if (statement.getObject() instanceof Literal) {
				statement = sail.mapStatement(statement);
				if (statement == null)
					return;
				// we further only indexed statements where the Literal's datatype
				// is accepted
				Literal literal = (Literal)statement.getObject();
				if (luceneIndex.accept(literal))
					buffer.remove(statement);
			}
		}
	};

	/**
	 * To remember if the iterator was already closed and only free resources
	 * once
	 */
	private boolean mustclose = false;

	public LuceneSailConnection(NotifyingSailConnection wrappedConnection, SearchIndex luceneIndex,
			LuceneSail sail)
	{
		super(wrappedConnection);
		this.luceneIndex = luceneIndex;
		this.sail = sail;

		/*
		 * Using SailConnectionListener, see <a href="#whySailConnectionListener">above</a>

		 */

		wrappedConnection.addConnectionListener(connectionListener);
	}

	@Override
	public synchronized void addStatement(Resource arg0, IRI arg1, Value arg2, Resource... arg3)
		throws SailException
	{
		super.addStatement(arg0, arg1, arg2, arg3);
	}

	@Override
	public void close()
		throws SailException
	{
		// remember if you were closed before, some sloppy programmers
		// may call close() twice.
		if (mustclose) {
			mustclose = false;
			try {
				luceneIndex.endReading();
			}
			catch (IOException e) {
				logger.warn("could not close IndexReader or IndexSearcher " + e, e);
			}
		}

		super.close();
	}

	// //////////////////////////////// Methods related to indexing

	@Override
	public synchronized void clear(Resource... arg0)
		throws SailException
	{
		// remove the connection listener, this is safe as the changing methods
		// are synchronized
		// during the clear(), no other operation can be invoked
		getWrappedConnection().removeConnectionListener(connectionListener);
		try {
			super.clear(arg0);
			buffer.clear(arg0);
		}
		finally {
			getWrappedConnection().addConnectionListener(connectionListener);
		}
	}

	@Override
	public void begin()
		throws SailException
	{
		super.begin();
		buffer.reset();
		try {
			luceneIndex.begin();
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	@Override
	public void commit()
		throws SailException
	{
		super.commit();

		logger.debug("Committing Lucene transaction with {} operations.", buffer.operations().size());
		try {
			try {
				// preprocess buffer
				buffer.optimize();

				// run operations and remove them from buffer
				for (Iterator<Operation> i = buffer.operations().iterator(); i.hasNext();) {
					Operation op = i.next();
					if (op instanceof LuceneSailBuffer.AddRemoveOperation) {
						AddRemoveOperation addremove = (AddRemoveOperation)op;
						// add/remove in one call
						addRemoveStatements(addremove.getAdded(), addremove.getRemoved());
					}
					else if (op instanceof LuceneSailBuffer.ClearContextOperation) {
						// clear context
						clearContexts(((ClearContextOperation)op).getContexts());
					}
					else if (op instanceof LuceneSailBuffer.ClearOperation) {
						logger.debug("clearing index...");
						luceneIndex.clear();
					}
					else
						throw new RuntimeException("Cannot interpret operation " + op + " of type "
								+ op.getClass().getName());
					i.remove();
				}
			}
			catch (Exception e) {
				logger.error("Committing operations in lucenesail, encountered exception " + e
						+ ". Only some operations were stored, " + buffer.operations().size()
						+ " operations are discarded. Lucene Index is now corrupt.", e);
				throw new SailException(e);
			}
		}
		finally {
			buffer.reset();
		}
	}

	private void addRemoveStatements(Set<Statement> toAdd, Set<Statement> toRemove) throws IOException
	{
		logger.debug("indexing {}/removing {} statements...", toAdd.size(),
				toRemove.size());
		luceneIndex.begin();
		try {
			luceneIndex.addRemoveStatements(toAdd, toRemove);
			luceneIndex.commit();
		}
		catch(IOException e) {
			logger.error("Rolling back", e);
			luceneIndex.rollback();
			throw e;
		}
	}

	private void clearContexts(Resource... contexts) throws IOException
	{
		logger.debug("clearing contexts...");
		luceneIndex.begin();
		try {
			luceneIndex.clearContexts(contexts);
			luceneIndex.commit();
		}
		catch(IOException e) {
			logger.error("Rolling back", e);
			luceneIndex.rollback();
			throw e;
		}
	}

	// //////////////////////////////// Methods related to querying

	@Override
	public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(TupleExpr tupleExpr,
			Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		// Don't modify the original tuple expression
		tupleExpr = tupleExpr.clone();

		// Inline any externally set bindings, lucene statement patterns can also
		// use externally bound variables
		new BindingAssigner().optimize(tupleExpr, dataset, bindings);

		// lookup the Lucene queries in this TupleExpr
		QuerySpecBuilder interpreter = new QuerySpecBuilder(sail.isIncompleteQueryFails());
		Set<QuerySpec> queries = interpreter.process(tupleExpr, bindings);

		// evaluate lucene queries
		if (!queries.isEmpty()) {
			evaluateLuceneQueries(queries, tupleExpr);
		}

		// let the lower sail evaluate the remaining query
		return super.evaluate(tupleExpr, dataset, bindings, includeInferred);
	}

	/**
	 * Evaluate the given Lucene queries, generate bindings from the query
	 * result, add the bindings to the query tree, and remove the Lucene queries
	 * from the given query tree.
	 * 
	 * @param queries
	 * @param tupleExpr
	 * @throws SailException
	 */
	private void evaluateLuceneQueries(Set<QuerySpec> queries, TupleExpr tupleExpr)
		throws SailException
	{
		// TODO: optimize lucene queries here
		// - if they refer to the same subject, merge them into one lucene query
		// - multiple different property constraints can be put into the lucene
		// query string (escape colons here)

		// mark that reading is in progress
		try {
			this.luceneIndex.beginReading();
		}
		catch(IOException e) {
			throw new SailException(e);
		}
		this.mustclose = true;

		// evaluate queries, generate binding sets, and remove queries
		for (QuerySpec query : queries) {
			// evaluate the Lucene query and generate bindings
			Collection<BindingSet> bindingSets = luceneIndex.evaluate(query);

			Class<? extends QueryModelNode> replacement;

			// found something?
			if (bindingSets != null && !bindingSets.isEmpty()) {
				replacement = SingletonSet.class;

				// add bindings to the query tree
				addBindingSets(query, bindingSets);
			}
			// return an empty result set if no matches were found
			else {
				replacement = EmptySet.class;
			}

			// remove the evaluated lucene query from the query tree
			try {
				replacePatterns(query, replacement);
			}
			catch (Exception e) {
				logger.error("Could not remove search patterns", e);
				continue;
			}
		}
	}

	/**
	 * Join the given bindings and add them to the given query tree.
	 * 
	 * @param bindingSets
	 *        bindings for the search query
	 * @param tupleExpr
	 *        query tree
	 * @param query
	 *        the search query to which the bindings belong
	 */
	private void addBindingSets(QuerySpec query, Iterable<BindingSet> bindingSets) {

		// find projection for the given query
		StatementPattern matches = query.getMatchesPattern();
		final Projection projection = (Projection)getParentNodeOfType(matches, Projection.class);
		if (projection == null) {
			logger.error("Could not add bindings to the query tree because no projection was found for the matches pattern: "
					+ matches.toString());
			return;
		}

		// find existing bindings within the given (sub-)query
		final List<BindingSetAssignment> assignments = new ArrayList<BindingSetAssignment>();
		QueryModelVisitor<RuntimeException> assignmentVisitor = new AbstractQueryModelVisitor<RuntimeException>() {

			@Override
			public void meet(BindingSetAssignment node)
				throws RuntimeException
			{
				// does the node belong to the same (sub-)query?
				QueryModelNode parent = getParentNodeOfType(node, Projection.class);
				if (parent != null && parent.equals(projection))
					assignments.add(node);
			}
		};
		projection.visit(assignmentVisitor);

		// construct a list of binding sets
		List<Iterable<BindingSet>> bindingSetsList = new ArrayList<Iterable<BindingSet>>();
		bindingSetsList.add(bindingSets);

		// add existing bindings to the list of binding sets and remove them from
		// the query tree
		for (BindingSetAssignment assignment : assignments) {
			bindingSetsList.add(assignment.getBindingSets());
			assignment.replaceWith(new SingletonSet());
		}

		// join binding sets
		Iterable<BindingSet> joinedBindingSets = joinBindingSets(bindingSetsList.iterator());
		BindingSetAssignment bindings = new BindingSetAssignment();
		bindings.setBindingSets(joinedBindingSets);

		// add bindings to the projection
		TupleExpr arg = projection.getArg();

		// required to support OPTIONAL patterns (which are represented as
		// LeftJoin)
		if (arg instanceof LeftJoin) {
			LeftJoin binary = (LeftJoin)arg;
			Join join = new Join(bindings, binary.getLeftArg());
			binary.setLeftArg(join);
		}
		else {
			Join join = new Join(bindings, arg);
			projection.setArg(join);
		}
	}

	/**
	 * Returns the closest parent node of the given type.
	 */
	private QueryModelNode getParentNodeOfType(QueryModelNode node, Class<? extends QueryModelNode> type) {
		QueryModelNode parent = node.getParentNode();
		if (parent == null)
			return null;
		else if (parent.getClass().equals(type))
			return parent;
		else
			return getParentNodeOfType(parent, type);
	}

	/**
	 * Recursively join the given binding sets.
	 * 
	 * @param iterator
	 *        for the binding sets to join
	 * @return joined binding sets
	 */
	private Iterable<BindingSet> joinBindingSets(Iterator<Iterable<BindingSet>> iterator) {
		if (iterator.hasNext()) {
			Iterable<BindingSet> left = iterator.next();
			Iterable<BindingSet> right = joinBindingSets(iterator);
			if (right != null) {
				return crossJoin(left, right);
			}
			else {
				return left;
			}
		}
		else {
			return null;
		}
	}

	/**
	 * Computes the Cartesian product of the given binding sets.
	 * 
	 * @param left
	 *        binding sets
	 * @param right
	 *        binding sets
	 * @return Cartesian product TODO: implement as sort-merge join
	 */
	private Iterable<BindingSet> crossJoin(Iterable<BindingSet> left, Iterable<BindingSet> right) {
		List<BindingSet> output = new ArrayList<BindingSet>();

		for (BindingSet l : left) {
			for (BindingSet r : right) {
				QueryBindingSet bs = new QueryBindingSet();
				bs.addAll(l);
				bs.addAll(r);
				output.add(bs);
			}
		}

		return output;
	}

	/**
	 * Replace all StatementPatterns occurring in the given query with the given
	 * replacement type.
	 * 
	 * @param query
	 *        the query for replacement
	 * @param replacement
	 *        the replacement type
	 */
	private void replacePatterns(QuerySpec query, Class<? extends QueryModelNode> replacement)
		throws InstantiationException, IllegalAccessException
	{
		replace(query.getMatchesPattern(), replacement);
		replace(query.getQueryPattern(), replacement);
		replace(query.getScorePattern(), replacement);
		replace(query.getPropertyPattern(), replacement);
		replace(query.getSnippetPattern(), replacement);
		replace(query.getTypePattern(), replacement);
	}

	/**
	 * Replace the given node with a new instance of the given replacement type.
	 * 
	 * @param pattern
	 *        the pattern to remove
	 * @param replacement
	 *        the replacement type
	 */
	private void replace(QueryModelNode node, Class<? extends QueryModelNode> replacement)
		throws InstantiationException, IllegalAccessException
	{
		if (node != null) {
			node.replaceWith(replacement.newInstance());
		}
	}

	@Override
	public synchronized void removeStatements(Resource arg0, IRI arg1, Value arg2, Resource... arg3)
		throws SailException
	{
		super.removeStatements(arg0, arg1, arg2, arg3);
	}

	@Override
	public void rollback()
		throws SailException
	{
		super.rollback();
		buffer.reset();
		try {
			luceneIndex.rollback();
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}
}
