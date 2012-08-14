/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Add;
import org.openrdf.query.algebra.Clear;
import org.openrdf.query.algebra.Copy;
import org.openrdf.query.algebra.Create;
import org.openrdf.query.algebra.DeleteData;
import org.openrdf.query.algebra.InsertData;
import org.openrdf.query.algebra.Load;
import org.openrdf.query.algebra.Modify;
import org.openrdf.query.algebra.Move;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * Implementation of
 * {@link SailConnection#executeUpdate(UpdateExpr, Dataset, BindingSet, boolean)}
 * using
 * {@link SailConnection#evaluate(TupleExpr, Dataset, BindingSet, boolean)} and
 * other {@link SailConnection} methods.
 * 
 * @author jeen
 * @author james
 */
public class SailUpdateExecutor {

	private final Logger logger = LoggerFactory.getLogger(SailUpdateExecutor.class);

	private final SailConnection con;

	private final ValueFactory vf;

	private final boolean readSnapshot;

	@Deprecated
	public SailUpdateExecutor(Sail sail, SailConnection con) {
		this(con, sail.getValueFactory(), false);
	}

	/**
	 * An implementation of the
	 * {@link SailConnection#executeUpdate(UpdateExpr, Dataset, BindingSet, boolean)}
	 * using other methods like
	 * {@link SailConnection#evaluate(TupleExpr, Dataset, BindingSet, boolean)}.
	 * 
	 * @param con
	 *        Used to read data from and write data to.
	 * @param vf
	 *        Used to create {@link BNode}s
	 * @param readSnapshot
	 *        <code>true</code> if read operations do not observe concurrent
	 *        writes, <code>false</code> if read results may changes as data is
	 *        written
	 */
	public SailUpdateExecutor(SailConnection con, ValueFactory vf, boolean readSnapshot) {
		this.con = con;
		this.vf = vf;
		this.readSnapshot = readSnapshot;
	}

	public void executeUpdate(UpdateExpr updateExpr, Dataset dataset, BindingSet bindings,
			boolean includeInferred)
		throws SailException
	{
		logger.trace("Incoming update expression:\n{}", updateExpr);

		if (updateExpr instanceof Modify) {
			executeModify((Modify)updateExpr, dataset, bindings, includeInferred);
		}
		else if (updateExpr instanceof InsertData) {
			executeInsertData((InsertData)updateExpr, dataset, bindings, includeInferred);
		}
		else if (updateExpr instanceof DeleteData) {
			executeDeleteData((DeleteData)updateExpr, dataset, bindings, includeInferred);
		}
		else if (updateExpr instanceof Clear) {
			executeClear((Clear)updateExpr, dataset, bindings, includeInferred);
		}
		else if (updateExpr instanceof Create) {
			executeCreate((Create)updateExpr, dataset, bindings, includeInferred);
		}
		else if (updateExpr instanceof Copy) {
			executeCopy((Copy)updateExpr, dataset, bindings, includeInferred);
		}
		else if (updateExpr instanceof Add) {
			executeAdd((Add)updateExpr, dataset, bindings, includeInferred);
		}
		else if (updateExpr instanceof Move) {
			executeMove((Move)updateExpr, dataset, bindings, includeInferred);
		}
		else if (updateExpr instanceof Load) {
			throw new SailException("load operations can not be handled directly by the SAIL");
		}
	}

	protected void executeCreate(Create create, Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		// check if named graph exists, if so, we have to return an error.
		// Otherwise, we simply do nothing.
		Value graphValue = create.getGraph().getValue();

		if (graphValue instanceof Resource) {
			Resource namedGraph = (Resource)graphValue;

			CloseableIteration<? extends Resource, SailException> contextIDs = con.getContextIDs();
			try {
				while (contextIDs.hasNext()) {
					Resource contextID = contextIDs.next();

					if (namedGraph.equals(contextID)) {
						throw new SailException("Named graph " + namedGraph + " already exists. ");
					}
				}
			}
			finally {
				contextIDs.close();
			}
		}
	}

	/**
	 * @param updateExpr
	 * @param dataset
	 * @param bindings
	 * @param includeInferred
	 * @throws SailException
	 */
	protected void executeCopy(Copy copy, Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		ValueConstant sourceGraph = copy.getSourceGraph();
		ValueConstant destinationGraph = copy.getDestinationGraph();

		Resource source = sourceGraph != null ? (Resource)sourceGraph.getValue() : null;
		Resource destination = destinationGraph != null ? (Resource)destinationGraph.getValue() : null;

		if (source == null && destination == null || (source != null && source.equals(destination))) {
			// source and destination are the same, copy is a null-operation.
			return;
		}

		// clear destination
		con.clear((Resource)destination);

		// get all statements from source and add them to destination
		CloseableIteration<? extends Statement, SailException> statements = con.getStatements(null, null, null,
				includeInferred, (Resource)source);
		try {
			while (statements.hasNext()) {
				Statement st = statements.next();
				con.addStatement(st.getSubject(), st.getPredicate(), st.getObject(), (Resource)destination);
			}
		}
		finally {
			statements.close();
		}
	}

	/**
	 * @param updateExpr
	 * @param dataset
	 * @param bindings
	 * @param includeInferred
	 * @throws SailException
	 */
	protected void executeAdd(Add add, Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		ValueConstant sourceGraph = add.getSourceGraph();
		ValueConstant destinationGraph = add.getDestinationGraph();

		Resource source = sourceGraph != null ? (Resource)sourceGraph.getValue() : null;
		Resource destination = destinationGraph != null ? (Resource)destinationGraph.getValue() : null;

		if (source == null && destination == null || (source != null && source.equals(destination))) {
			// source and destination are the same, copy is a null-operation.
			return;
		}

		// get all statements from source and add them to destination
		CloseableIteration<? extends Statement, SailException> statements = con.getStatements(null, null, null,
				includeInferred, (Resource)source);
		try {
			while (statements.hasNext()) {
				Statement st = statements.next();
				con.addStatement(st.getSubject(), st.getPredicate(), st.getObject(), (Resource)destination);
			}
		}
		finally {
			statements.close();
		}
	}

	/**
	 * @param updateExpr
	 * @param dataset
	 * @param bindings
	 * @param includeInferred
	 * @throws SailException
	 */
	protected void executeMove(Move move, Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		ValueConstant sourceGraph = move.getSourceGraph();
		ValueConstant destinationGraph = move.getDestinationGraph();

		Resource source = sourceGraph != null ? (Resource)sourceGraph.getValue() : null;
		Resource destination = destinationGraph != null ? (Resource)destinationGraph.getValue() : null;

		if (source == null && destination == null || (source != null && source.equals(destination))) {
			// source and destination are the same, move is a null-operation.
			return;
		}

		// clear destination
		con.clear((Resource)destination);

		// remove all statements from source and add them to destination
		CloseableIteration<? extends Statement, SailException> statements = con.getStatements(null, null, null,
				includeInferred, (Resource)source);
		try {
			while (statements.hasNext()) {
				Statement st = statements.next();
				con.addStatement(st.getSubject(), st.getPredicate(), st.getObject(), (Resource)destination);
				con.removeStatements(st.getSubject(), st.getPredicate(), st.getObject(), (Resource)source);
			}
		}
		finally {
			statements.close();
		}
	}

	/**
	 * @param updateExpr
	 * @param dataset
	 * @param bindings
	 * @param includeInferred
	 * @throws SailException
	 */
	protected void executeClear(Clear clearExpr, Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		try {
			ValueConstant graph = clearExpr.getGraph();

			if (graph != null) {
				Resource context = (Resource)graph.getValue();
				con.clear(context);
			}
			else {
				Scope scope = clearExpr.getScope();
				if (Scope.NAMED_CONTEXTS.equals(scope)) {
					CloseableIteration<? extends Resource, SailException> contextIDs = con.getContextIDs();
					try {
						while (contextIDs.hasNext()) {
							con.clear(contextIDs.next());
						}
					}
					finally {
						contextIDs.close();
					}
				}
				else if (Scope.DEFAULT_CONTEXTS.equals(scope)) {
					con.clear((Resource)null);
				}
				else {
					con.clear();
				}
			}
		}
		catch (SailException e) {
			if (!clearExpr.isSilent()) {
				throw e;
			}
		}
	}

	/**
	 * @param updateExpr
	 * @param dataset
	 * @param bindings
	 * @param includeInferred
	 * @throws SailException
	 */
	protected void executeInsertData(InsertData insertDataExpr, Dataset ds, BindingSet bindings,
			boolean includeInferred)
		throws SailException
	{
		TupleExpr insertExpr = insertDataExpr.getInsertExpr();

		CloseableIteration<? extends BindingSet, QueryEvaluationException> toBeInserted = con.evaluate(
				insertExpr, ds, bindings, includeInferred);

		try {
			try {
				URI insert = ds == null ? null : ds.getDefaultInsertGraph();
				while (toBeInserted.hasNext()) {
					BindingSet bs = toBeInserted.next();
	
					Resource subject = (Resource)bs.getValue("subject");
					URI predicate = (URI)bs.getValue("predicate");
					Value object = bs.getValue("object");
					Resource context = (Resource)bs.getValue("context");
	
					if (context == null && insert == null) {
						con.addStatement(subject, predicate, object);
					}
					else if (context == null) {
						con.addStatement(subject, predicate, object, insert);
					}
					else {
						con.addStatement(subject, predicate, object, context);
					}
				}
			}
			finally {
				toBeInserted.close();
			}
		}
		catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	/**
	 * @param updateExpr
	 * @param dataset
	 * @param bindings
	 * @param includeInferred
	 * @throws SailException
	 */
	protected void executeDeleteData(DeleteData deleteDataExpr, Dataset dataset, BindingSet bindings,
			boolean includeInferred)
		throws SailException
	{
		TupleExpr deleteExpr = deleteDataExpr.getDeleteExpr();

		CloseableIteration<? extends BindingSet, QueryEvaluationException> toBeDeleted = con.evaluate(
				deleteExpr, dataset, bindings, includeInferred);

		try {
			try {
				URI[] remove = getDefaultRemoveGraphs(dataset);
				while (toBeDeleted.hasNext()) {
					BindingSet bs = toBeDeleted.next();
	
					Resource subject = (Resource)bs.getValue("subject");
					URI predicate = (URI)bs.getValue("predicate");
					Value object = bs.getValue("object");
					Resource context = (Resource)bs.getValue("context");
	
					if (context != null) {
						con.removeStatements(subject, predicate, object, context);
					}
					else if (remove != null) {
						con.removeStatements(subject, predicate, object, remove);
					}
				}
			}
			finally {
				toBeDeleted.close();
			}
		}
		catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	protected void executeModify(Modify modify, Dataset ds, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		try {
			TupleExpr whereClause = modify.getWhereExpr();

			if (!(whereClause instanceof QueryRoot)) {
				whereClause = new QueryRoot(whereClause);
			}
			
			CloseableIteration<? extends BindingSet, QueryEvaluationException> sourceBindings;
			sourceBindings = evaluateWhereClause(whereClause, ds, bindings, includeInferred);
			try {
				URI insert = ds == null ? null : ds.getDefaultInsertGraph();
				URI[] remove = getDefaultRemoveGraphs(ds);
				if (readSnapshot) {
					while (sourceBindings.hasNext()) {
						BindingSet sourceBinding = sourceBindings.next();
						deleteBoundTriples(sourceBinding, modify.getDeleteExpr(), remove);

						insertBoundTriples(sourceBinding, modify.getInsertExpr(), insert);
					}
				}
				else {
					// We fully materialize the result of evaluation of the
					// WHERE-clause. This is necessary to avoid having triples
					// produced or removed by the current INSERT/DELETE clause
					// contaminating the result, while at the same time guaranteeing
					// that results produced by previous updates in the same
					// transaction _are_ taken into account (see SES-930)
					Collection<BindingSet> cachedSourceBindings = new ArrayList<BindingSet>();
					while (sourceBindings.hasNext()) {
						cachedSourceBindings.add(sourceBindings.next());
					}

					for (BindingSet sourceBinding : cachedSourceBindings) {
						deleteBoundTriples(sourceBinding, modify.getDeleteExpr(), remove);

						insertBoundTriples(sourceBinding, modify.getInsertExpr(), insert);
					}
				}
			}
			finally {
				sourceBindings.close();
			}
		}
		catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	private URI[] getDefaultRemoveGraphs(Dataset dataset) {
		if (dataset == null)
			return new URI[0];
		Set<URI> set = dataset.getDefaultRemoveGraphs();
		if (set == null || set.isEmpty())
			return new URI[0];
		return set.toArray(new URI[set.size()]);
	}

	/**
	 * @param whereClause
	 * @param dataset
	 * @param bindings
	 * @param includeInferred
	 * @return
	 * @throws SailException
	 * @throws QueryEvaluationException
	 */
	private CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateWhereClause(final TupleExpr whereClause, Dataset dataset,
			final BindingSet bindings, boolean includeInferred)
		throws SailException, QueryEvaluationException
	{
		CloseableIteration<? extends BindingSet, QueryEvaluationException> sourceBindings;
		sourceBindings = con.evaluate(whereClause, dataset, bindings, includeInferred);

		return new ConvertingIteration<BindingSet, BindingSet, QueryEvaluationException>(sourceBindings) {

			protected BindingSet convert(BindingSet sourceBinding)
				throws QueryEvaluationException
			{
				if (whereClause instanceof SingletonSet && sourceBinding instanceof EmptyBindingSet
						&& bindings != null)
				{
					// in the case of an empty WHERE clause, we use the supplied
					// bindings to produce triples to DELETE/INSERT
					return bindings;
				}
				else {
					// check if any supplied bindings do not occur in the bindingset
					// produced by the WHERE clause. If so, merge.
					Set<String> uniqueBindings = new HashSet<String>(bindings.getBindingNames());
					uniqueBindings.removeAll(sourceBinding.getBindingNames());
					if (uniqueBindings.size() > 0) {
						MapBindingSet mergedSet = new MapBindingSet();
						for (String bindingName : sourceBinding.getBindingNames()) {
							mergedSet.addBinding(sourceBinding.getBinding(bindingName));
						}
						for (String bindingName : uniqueBindings) {
							mergedSet.addBinding(bindings.getBinding(bindingName));
						}
						return mergedSet;
					}
					return sourceBinding;
				}
			}
		};
	}

	/**
	 * @param whereBinding
	 * @param deleteClause
	 * @throws SailException
	 */
	private void deleteBoundTriples(BindingSet whereBinding, TupleExpr deleteClause, URI... remove)
		throws SailException
	{
		if (deleteClause != null) {
			List<StatementPattern> deletePatterns = StatementPatternCollector.process(deleteClause);

			for (StatementPattern deletePattern : deletePatterns) {

				Resource subject = (Resource)getValueForVar(deletePattern.getSubjectVar(), whereBinding);
				URI predicate = (URI)getValueForVar(deletePattern.getPredicateVar(), whereBinding);
				Value object = getValueForVar(deletePattern.getObjectVar(), whereBinding);

				Resource context = null;
				if (deletePattern.getContextVar() != null) {
					context = (Resource)getValueForVar(deletePattern.getContextVar(), whereBinding);
				}
				
				if (subject == null || predicate == null || object == null) {
					// skip removal of triple if any variable is unbound (may happen with optional patterns)
					// See SES-1047.
					continue;
				}

				if (context != null) {
					con.removeStatements(subject, predicate, object, context);
				}
				else if (remove != null) {
					con.removeStatements(subject, predicate, object, remove);
				}
			}
		}
	}

	/**
	 * @param whereBinding
	 * @param insertClause
	 * @throws SailException
	 */
	private void insertBoundTriples(BindingSet whereBinding, TupleExpr insertClause, URI with)
		throws SailException
	{
		if (insertClause != null) {
			List<StatementPattern> insertPatterns = StatementPatternCollector.process(insertClause);

			// bnodes in the insert pattern are locally scoped for each
			// individual source binding.
			MapBindingSet bnodeMapping = new MapBindingSet();
			for (StatementPattern insertPattern : insertPatterns) {
				Statement toBeInserted = createStatementFromPattern(insertPattern, whereBinding,
						bnodeMapping);

				if (toBeInserted != null) {
					if (with == null && toBeInserted.getContext() == null) {
						con.addStatement(toBeInserted.getSubject(), toBeInserted.getPredicate(),
								toBeInserted.getObject());
					}
					else if (toBeInserted.getContext() == null) {
						con.addStatement(toBeInserted.getSubject(), toBeInserted.getPredicate(),
								toBeInserted.getObject(), with);
					}
					else {
						con.addStatement(toBeInserted.getSubject(), toBeInserted.getPredicate(),
								toBeInserted.getObject(), toBeInserted.getContext());
					}
				}
			}
		}
	}

	/**
	 * @param pattern
	 * @param sourceBinding
	 * @return
	 * @throws SailException
	 */
	private Statement createStatementFromPattern(StatementPattern pattern, BindingSet sourceBinding,
			MapBindingSet bnodeMapping)
		throws SailException
	{

		Resource subject = null;
		URI predicate = null;
		Value object = null;
		Resource context = null;

		if (pattern.getSubjectVar().hasValue()) {
			subject = (Resource)pattern.getSubjectVar().getValue();
		}
		else {
			subject = (Resource)sourceBinding.getValue(pattern.getSubjectVar().getName());

			if (subject == null && pattern.getSubjectVar().isAnonymous()) {
				Binding mappedSubject = bnodeMapping.getBinding(pattern.getSubjectVar().getName());

				if (mappedSubject != null) {
					subject = (Resource)mappedSubject.getValue();
				}
				else {
					subject = vf.createBNode();
					bnodeMapping.addBinding(pattern.getSubjectVar().getName(), subject);
				}
			}
		}

		if (pattern.getPredicateVar().hasValue()) {
			predicate = (URI)pattern.getPredicateVar().getValue();
		}
		else {
			predicate = (URI)sourceBinding.getValue(pattern.getPredicateVar().getName());
		}

		if (pattern.getObjectVar().hasValue()) {
			object = pattern.getObjectVar().getValue();
		}
		else {
			object = sourceBinding.getValue(pattern.getObjectVar().getName());

			if (object == null && pattern.getObjectVar().isAnonymous()) {
				Binding mappedObject = bnodeMapping.getBinding(pattern.getObjectVar().getName());

				if (mappedObject != null) {
					object = (Resource)mappedObject.getValue();
				}
				else {
					object = vf.createBNode();
					bnodeMapping.addBinding(pattern.getObjectVar().getName(), object);
				}
			}
		}

		if (pattern.getContextVar() != null) {
			if (pattern.getContextVar().hasValue()) {
				context = (Resource)pattern.getContextVar().getValue();
			}
			else {
				context = (Resource)sourceBinding.getValue(pattern.getContextVar().getName());
			}
		}

		Statement st = null;
		if (subject != null && predicate != null && object != null) {
			if (context != null) {
				st = vf.createStatement(subject, predicate, object, context);
			}
			else {
				st = vf.createStatement(subject, predicate, object);
			}
		}
		return st;
	}

	private Value getValueForVar(Var var, BindingSet bindings)
		throws SailException
	{
		Value value = null;
		if (var.hasValue()) {
			value = var.getValue();
		}
		else {
			value = bindings.getValue(var.getName());
		}
		return value;
	}
}
