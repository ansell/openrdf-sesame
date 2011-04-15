/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.DelayedIteration;
import info.aduna.iteration.DistinctIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.IntersectIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.LimitIteration;
import info.aduna.iteration.LookAheadIteration;
import info.aduna.iteration.OffsetIteration;
import info.aduna.iteration.ReducedIteration;
import info.aduna.iteration.SingletonIteration;
import info.aduna.iteration.UnionIteration;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.ArbitraryLengthPath;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Coalesce;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.CompareAll;
import org.openrdf.query.algebra.CompareAny;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.IRIFunction;
import org.openrdf.query.algebra.If;
import org.openrdf.query.algebra.In;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsNumeric;
import org.openrdf.query.algebra.IsResource;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Label;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.LangMatches;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Like;
import org.openrdf.query.algebra.LocalName;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Namespace;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.StrDt;
import org.openrdf.query.algebra.StrLang;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.ZeroLengthPath;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.evaluation.iterator.BadlyDesignedLeftJoinIterator;
import org.openrdf.query.algebra.evaluation.iterator.ExtensionIterator;
import org.openrdf.query.algebra.evaluation.iterator.FilterIterator;
import org.openrdf.query.algebra.evaluation.iterator.GroupIterator;
import org.openrdf.query.algebra.evaluation.iterator.JoinIterator;
import org.openrdf.query.algebra.evaluation.iterator.LeftJoinIterator;
import org.openrdf.query.algebra.evaluation.iterator.MultiProjectionIterator;
import org.openrdf.query.algebra.evaluation.iterator.OrderIterator;
import org.openrdf.query.algebra.evaluation.iterator.ProjectionIterator;
import org.openrdf.query.algebra.evaluation.iterator.SPARQLMinusIteration;
import org.openrdf.query.algebra.evaluation.util.MathUtil;
import org.openrdf.query.algebra.evaluation.util.OrderComparator;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;
import org.openrdf.query.algebra.evaluation.util.ValueComparator;
import org.openrdf.query.algebra.helpers.VarNameCollector;
import org.openrdf.query.impl.MapBindingSet;

/**
 * Evaluates the TupleExpr and ValueExpr using Iterators and common tripleSource
 * API.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 * @author David Huynh
 */
public class EvaluationStrategyImpl implements EvaluationStrategy {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final TripleSource tripleSource;

	protected final Dataset dataset;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public EvaluationStrategyImpl(TripleSource tripleSource) {
		this(tripleSource, null);
	}

	public EvaluationStrategyImpl(TripleSource tripleSource, Dataset dataset) {
		this.tripleSource = tripleSource;
		this.dataset = dataset;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(TupleExpr expr,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		if (expr instanceof StatementPattern) {
			return evaluate((StatementPattern)expr, bindings);
		}
		else if (expr instanceof UnaryTupleOperator) {
			return evaluate((UnaryTupleOperator)expr, bindings);
		}
		else if (expr instanceof BinaryTupleOperator) {
			return evaluate((BinaryTupleOperator)expr, bindings);
		}
		else if (expr instanceof SingletonSet) {
			return evaluate((SingletonSet)expr, bindings);
		}
		else if (expr instanceof EmptySet) {
			return evaluate((EmptySet)expr, bindings);
		}
		else if (expr instanceof ExternalSet) {
			return evaluate((ExternalSet)expr, bindings);
		}
		else if (expr instanceof ZeroLengthPath) {
			return evaluate((ZeroLengthPath)expr, bindings);
		}
		else if (expr instanceof ArbitraryLengthPath) {
			return evaluate((ArbitraryLengthPath)expr, bindings);
		}
		else if (expr == null) {
			throw new IllegalArgumentException("expr must not be null");
		}
		else {
			throw new QueryEvaluationException("Unsupported tuple expr type: " + expr.getClass());
		}
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(ArbitraryLengthPath alp,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		final Scope scope = alp.getScope();
		final Var subjectVar = alp.getSubjectVar();
		final Var predVar = alp.getPredicateVar();
		final Var objVar = alp.getObjectVar();
		final Var contextVar = alp.getContextVar();
		final long minLength = alp.getMinLength();

		StatementPattern sp = new StatementPattern(scope, subjectVar, predVar, objVar, contextVar);

		return new PathIteration(sp, minLength, bindings);
	}

	private class PathIteration extends LookAheadIteration<BindingSet, QueryEvaluationException> {


		private long currentLength;

		private StatementPattern pattern;

		private CloseableIteration<BindingSet, QueryEvaluationException> currentIter;

		private BindingSet bindings;

		private boolean currentIterNotEmpty;
		
		public PathIteration(StatementPattern sp, long minLength, BindingSet bindings)
			throws QueryEvaluationException {
			this.currentLength = minLength;
			this.pattern = sp;
			this.bindings = bindings;
			
			createIteration();
		}

		@Override
		protected BindingSet getNextElement()
			throws QueryEvaluationException
		{
			if (currentIter.hasNext()) {
				currentIterNotEmpty = true;
				return currentIter.next();
			}
			else {
				if (currentIterNotEmpty) {
					currentLength++;
					createIteration();
					currentIterNotEmpty = false;
					
					if (currentIter.hasNext()) {
						currentIterNotEmpty = true;
						return currentIter.next();
					}
				}
			}
			
			return null;
		}

		private void createIteration()
			throws QueryEvaluationException
		{
			if (currentLength == 0L) {
				ZeroLengthPath zlp = new ZeroLengthPath(pattern.getScope(), pattern.getSubjectVar(),
						pattern.getPredicateVar(), pattern.getObjectVar(), pattern.getContextVar());
				currentIter = evaluate(zlp, bindings);
			}
			else if (currentLength == 1) {
				currentIter = evaluate(pattern, bindings);
			}
			else {
				// length greater than zero, create join with filter for cycle-detection.
				long numberOfJoins = currentLength - 1;
				Join join = createMultiJoin(pattern, numberOfJoins);
				
				// if the outcome of the join has the start of the path equal to the end, we are in a loop.
				Compare loopCheck = new Compare(pattern.getSubjectVar(), pattern.getObjectVar(), CompareOp.NE);
				Filter filter = new Filter(join, loopCheck);
				
				currentIter = evaluate(filter, bindings);
			}
		}

		private Join createMultiJoin(StatementPattern sp, long numberOfJoins) {

			Join join = new Join();
			Join currentJoin = join;
		
			Var subjectJoinVar = pattern.getSubjectVar();
			
			for (long i = 0L; i < numberOfJoins; i++) {
				Var joinVar = createAnonVar("path-join-" + numberOfJoins + "-" + i);

				currentJoin.setLeftArg(new StatementPattern(pattern.getScope(), subjectJoinVar,
						pattern.getPredicateVar(), joinVar, pattern.getContextVar()));

				if (i == numberOfJoins - 1L) {
					currentJoin.setRightArg(new StatementPattern(pattern.getScope(), joinVar,
							pattern.getPredicateVar(), pattern.getObjectVar(), pattern.getContextVar()));
				}
				else {
					Join newJoin = new Join();
					currentJoin.setRightArg(newJoin);
					currentJoin = newJoin;
				}
				subjectJoinVar = joinVar;
			}
			
			return join;
		}
	}

	private Var createAnonVar(String varName) {
		Var var = new Var(varName);
		var.setAnonymous(true);
		return var;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(ZeroLengthPath zlp,
			final BindingSet bindings)
	{

		final Var subjectVar = zlp.getSubjectVar();
		final Var objVar = zlp.getObjectVar();

		Value subj = null;
		try {
			subj = evaluate(subjectVar, bindings);
		}
		catch (QueryEvaluationException e) {
		}

		Value obj = null;
		try {
			obj = evaluate(objVar, bindings);
		}
		catch (QueryEvaluationException e) {
		}

		if (subj != null && obj != null) {
			if (!subj.equals(obj)) {
				return new EmptyIteration<BindingSet, QueryEvaluationException>();
			}
		}

		return new ZeroLengthPathIteration(subjectVar, objVar, subj, obj, bindings);
	}

	private class ZeroLengthPathIteration extends LookAheadIteration<BindingSet, QueryEvaluationException> {

		private QueryBindingSet result;

		public ZeroLengthPathIteration(Var subjectVar, Var objVar, Value subj, Value obj, BindingSet bindings) {
			result = new QueryBindingSet(bindings);

			if (subj != null && obj == null) {
				result.addBinding(objVar.getName(), subj);
			}

			if (obj != null && subj == null) {
				result.addBinding(subjectVar.getName(), obj);
			}
		}

		@Override
		protected BindingSet getNextElement()
			throws QueryEvaluationException
		{
			BindingSet next = result;
			result = null;
			return next;
		}

	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(StatementPattern sp,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		final Var subjVar = sp.getSubjectVar();
		final Var predVar = sp.getPredicateVar();
		final Var objVar = sp.getObjectVar();
		final Var conVar = sp.getContextVar();

		final Value subjValue = getVarValue(subjVar, bindings);
		final Value predValue = getVarValue(predVar, bindings);
		final Value objValue = getVarValue(objVar, bindings);
		final Value contextValue = getVarValue(conVar, bindings);

		CloseableIteration<? extends Statement, QueryEvaluationException> stIter = null;

		try {
			Resource[] contexts;

			if (dataset != null) {
				Set<URI> graphs;
				if (sp.getScope() == Scope.DEFAULT_CONTEXTS) {
					graphs = dataset.getDefaultGraphs();
				}
				else {
					graphs = dataset.getNamedGraphs();
				}

				if (graphs.isEmpty()) {
					// Search zero contexts
					return new EmptyIteration<BindingSet, QueryEvaluationException>();
				}
				else if (contextValue != null) {
					if (graphs.contains(contextValue)) {
						contexts = new Resource[] { (Resource)contextValue };
					}
					else {
						// Statement pattern specifies a context that is not part of
						// the dataset
						return new EmptyIteration<BindingSet, QueryEvaluationException>();
					}
				}
				else {
					contexts = graphs.toArray(new Resource[graphs.size()]);
				}
			}
			else if (contextValue != null) {
				contexts = new Resource[] { (Resource)contextValue };
			}
			else {
				contexts = new Resource[0];
			}

			stIter = tripleSource.getStatements((Resource)subjValue, (URI)predValue, objValue, contexts);

			if (contexts.length == 0 && sp.getScope() == Scope.NAMED_CONTEXTS) {
				// Named contexts are matched by retrieving all statements from
				// the store and filtering out the statements that do not have a
				// context.
				stIter = new FilterIteration<Statement, QueryEvaluationException>(stIter) {

					@Override
					protected boolean accept(Statement st) {
						return st.getContext() != null;
					}

				}; // end anonymous class
			}
		}
		catch (ClassCastException e) {
			// Invalid value type for subject, predicate and/or context
			return new EmptyIteration<BindingSet, QueryEvaluationException>();
		}

		// The same variable might have been used multiple times in this
		// StatementPattern, verify value equality in those cases.
		// TODO: skip this filter if not necessary
		stIter = new FilterIteration<Statement, QueryEvaluationException>(stIter) {

			@Override
			protected boolean accept(Statement st) {
				Resource subj = st.getSubject();
				URI pred = st.getPredicate();
				Value obj = st.getObject();
				Resource context = st.getContext();

				if (subjVar != null && subjValue == null) {
					if (subjVar.equals(predVar) && !subj.equals(pred)) {
						return false;
					}
					if (subjVar.equals(objVar) && !subj.equals(obj)) {
						return false;
					}
					if (subjVar.equals(conVar) && !subj.equals(context)) {
						return false;
					}
				}

				if (predVar != null && predValue == null) {
					if (predVar.equals(objVar) && !pred.equals(obj)) {
						return false;
					}
					if (predVar.equals(conVar) && !pred.equals(context)) {
						return false;
					}
				}

				if (objVar != null && objValue == null) {
					if (objVar.equals(conVar) && !obj.equals(context)) {
						return false;
					}
				}

				return true;
			}
		};

		// Return an iterator that converts the statements to var bindings
		return new ConvertingIteration<Statement, BindingSet, QueryEvaluationException>(stIter) {

			@Override
			protected BindingSet convert(Statement st) {
				QueryBindingSet result = new QueryBindingSet(bindings);

				if (subjVar != null && !result.hasBinding(subjVar.getName())) {
					result.addBinding(subjVar.getName(), st.getSubject());
				}
				if (predVar != null && !result.hasBinding(predVar.getName())) {
					result.addBinding(predVar.getName(), st.getPredicate());
				}
				if (objVar != null && !result.hasBinding(objVar.getName())) {
					result.addBinding(objVar.getName(), st.getObject());
				}
				if (conVar != null && !result.hasBinding(conVar.getName()) && st.getContext() != null) {
					result.addBinding(conVar.getName(), st.getContext());
				}

				return result;
			}
		};
	}

	protected Value getVarValue(Var var, BindingSet bindings) {
		if (var == null) {
			return null;
		}
		else if (var.hasValue()) {
			return var.getValue();
		}
		else {
			return bindings.getValue(var.getName());
		}
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(UnaryTupleOperator expr,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		if (expr instanceof Projection) {
			return evaluate((Projection)expr, bindings);
		}
		else if (expr instanceof MultiProjection) {
			return evaluate((MultiProjection)expr, bindings);
		}
		else if (expr instanceof Filter) {
			return evaluate((Filter)expr, bindings);
		}
		else if (expr instanceof Slice) {
			return evaluate((Slice)expr, bindings);
		}
		else if (expr instanceof Extension) {
			return evaluate((Extension)expr, bindings);
		}
		else if (expr instanceof Distinct) {
			return evaluate((Distinct)expr, bindings);
		}
		else if (expr instanceof Reduced) {
			return evaluate((Reduced)expr, bindings);
		}
		else if (expr instanceof Group) {
			return evaluate((Group)expr, bindings);
		}
		else if (expr instanceof Order) {
			return evaluate((Order)expr, bindings);
		}
		else if (expr instanceof QueryRoot) {
			return evaluate(((QueryRoot)expr).getArg(), bindings);
		}
		else if (expr == null) {
			throw new IllegalArgumentException("expr must not be null");
		}
		else {
			throw new QueryEvaluationException("Unknown unary tuple operator type: " + expr.getClass());
		}
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Projection projection,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> result;
		result = this.evaluate(projection.getArg(), bindings);
		result = new ProjectionIterator(projection, result, bindings);
		return result;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(MultiProjection multiProjection,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> result;
		result = this.evaluate(multiProjection.getArg(), bindings);
		result = new MultiProjectionIterator(multiProjection, result, bindings);
		return result;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Filter filter, BindingSet bindings)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> result;
		result = this.evaluate(filter.getArg(), bindings);
		result = new FilterIterator(filter, result, this);
		return result;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Slice slice, BindingSet bindings)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> result = evaluate(slice.getArg(), bindings);

		if (slice.hasOffset()) {
			result = new OffsetIteration<BindingSet, QueryEvaluationException>(result, slice.getOffset());
		}

		if (slice.hasLimit()) {
			result = new LimitIteration<BindingSet, QueryEvaluationException>(result, slice.getLimit());
		}

		return result;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Extension extension,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> result;
		try {
			result = this.evaluate(extension.getArg(), bindings);
		}
		catch (ValueExprEvaluationException e) {
			// a type error in an extension argument should be silently ignored and
			// result in zero bindings.
			result = new EmptyIteration<BindingSet, QueryEvaluationException>();
		}

		result = new ExtensionIterator(extension, result, this);
		return result;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Distinct distinct,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		return new DistinctIteration<BindingSet, QueryEvaluationException>(
				evaluate(distinct.getArg(), bindings));
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Reduced reduced,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		return new ReducedIteration<BindingSet, QueryEvaluationException>(evaluate(reduced.getArg(), bindings));
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Group node, BindingSet bindings)
		throws QueryEvaluationException
	{
		return new GroupIterator(this, node, bindings);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Order node, BindingSet bindings)
		throws QueryEvaluationException
	{
		ValueComparator vcmp = new ValueComparator();
		OrderComparator cmp = new OrderComparator(this, node, vcmp);
		boolean reduced = isReduced(node);
		long limit = getLimit(node);
		return new OrderIterator(evaluate(node.getArg(), bindings), cmp, limit, reduced);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(BinaryTupleOperator expr,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		if (expr instanceof Join) {
			return evaluate((Join)expr, bindings);
		}
		else if (expr instanceof LeftJoin) {
			return evaluate((LeftJoin)expr, bindings);
		}
		else if (expr instanceof Union) {
			return evaluate((Union)expr, bindings);
		}
		else if (expr instanceof Intersection) {
			return evaluate((Intersection)expr, bindings);
		}
		else if (expr instanceof Difference) {
			return evaluate((Difference)expr, bindings);
		}
		else if (expr == null) {
			throw new IllegalArgumentException("expr must not be null");
		}
		else {
			throw new QueryEvaluationException("Unsupported binary tuple operator type: " + expr.getClass());
		}
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Join join, BindingSet bindings)
		throws QueryEvaluationException
	{
		return new JoinIterator(this, join, bindings);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(LeftJoin leftJoin,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		// Check whether optional join is "well designed" as defined in section
		// 4.2 of "Semantics and Complexity of SPARQL", 2006, Jorge Pérez et al.
		VarNameCollector optionalVarCollector = new VarNameCollector();
		leftJoin.getRightArg().visit(optionalVarCollector);
		if (leftJoin.hasCondition()) {
			leftJoin.getCondition().visit(optionalVarCollector);
		}

		Set<String> problemVars = optionalVarCollector.getVarNames();
		problemVars.removeAll(leftJoin.getLeftArg().getBindingNames());
		problemVars.retainAll(bindings.getBindingNames());

		if (problemVars.isEmpty()) {
			// left join is "well designed"
			return new LeftJoinIterator(this, leftJoin, bindings);
		}
		else {
			return new BadlyDesignedLeftJoinIterator(this, leftJoin, bindings, problemVars);
		}
	}

	@SuppressWarnings("unchecked")
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(final Union union,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		Iteration<BindingSet, QueryEvaluationException> leftArg, rightArg;

		leftArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(union.getLeftArg(), bindings);
			}
		};

		rightArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(union.getRightArg(), bindings);
			}
		};

		return new UnionIteration<BindingSet, QueryEvaluationException>(leftArg, rightArg);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(final Intersection intersection,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		Iteration<BindingSet, QueryEvaluationException> leftArg, rightArg;

		leftArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(intersection.getLeftArg(), bindings);
			}
		};

		rightArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(intersection.getRightArg(), bindings);
			}
		};

		return new IntersectIteration<BindingSet, QueryEvaluationException>(leftArg, rightArg);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(final Difference difference,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		Iteration<BindingSet, QueryEvaluationException> leftArg, rightArg;

		leftArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(difference.getLeftArg(), bindings);
			}
		};

		rightArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(difference.getRightArg(), bindings);
			}
		};

		return new SPARQLMinusIteration<QueryEvaluationException>(leftArg, rightArg);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(SingletonSet singletonSet,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		return new SingletonIteration<BindingSet, QueryEvaluationException>(bindings);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(EmptySet emptySet,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		return new EmptyIteration<BindingSet, QueryEvaluationException>();
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(ExternalSet external,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		return external.evaluate(bindings);
	}

	public Value evaluate(ValueExpr expr, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		if (expr instanceof Var) {
			return evaluate((Var)expr, bindings);
		}
		else if (expr instanceof ValueConstant) {
			return evaluate((ValueConstant)expr, bindings);
		}
		else if (expr instanceof BNodeGenerator) {
			return evaluate((BNodeGenerator)expr, bindings);
		}
		else if (expr instanceof Bound) {
			return evaluate((Bound)expr, bindings);
		}
		else if (expr instanceof Str) {
			return evaluate((Str)expr, bindings);
		}
		else if (expr instanceof Label) {
			return evaluate((Label)expr, bindings);
		}
		else if (expr instanceof Lang) {
			return evaluate((Lang)expr, bindings);
		}
		else if (expr instanceof LangMatches) {
			return evaluate((LangMatches)expr, bindings);
		}
		else if (expr instanceof Datatype) {
			return evaluate((Datatype)expr, bindings);
		}
		else if (expr instanceof Namespace) {
			return evaluate((Namespace)expr, bindings);
		}
		else if (expr instanceof LocalName) {
			return evaluate((LocalName)expr, bindings);
		}
		else if (expr instanceof IsResource) {
			return evaluate((IsResource)expr, bindings);
		}
		else if (expr instanceof IsURI) {
			return evaluate((IsURI)expr, bindings);
		}
		else if (expr instanceof IsBNode) {
			return evaluate((IsBNode)expr, bindings);
		}
		else if (expr instanceof IsLiteral) {
			return evaluate((IsLiteral)expr, bindings);
		}
		else if (expr instanceof IsNumeric) {
			return evaluate((IsNumeric)expr, bindings);
		}
		else if (expr instanceof IRIFunction) {
			return evaluate((IRIFunction)expr, bindings);
		}
		else if (expr instanceof StrDt) {
			return evaluate((StrDt)expr, bindings);
		}
		else if (expr instanceof StrLang) {
			return evaluate((StrLang)expr, bindings);
		}
		else if (expr instanceof Regex) {
			return evaluate((Regex)expr, bindings);
		}
		else if (expr instanceof Coalesce) {
			return evaluate((Coalesce)expr, bindings);
		}
		else if (expr instanceof Like) {
			return evaluate((Like)expr, bindings);
		}
		else if (expr instanceof FunctionCall) {
			return evaluate((FunctionCall)expr, bindings);
		}
		else if (expr instanceof And) {
			return evaluate((And)expr, bindings);
		}
		else if (expr instanceof Or) {
			return evaluate((Or)expr, bindings);
		}
		else if (expr instanceof Not) {
			return evaluate((Not)expr, bindings);
		}
		else if (expr instanceof SameTerm) {
			return evaluate((SameTerm)expr, bindings);
		}
		else if (expr instanceof Compare) {
			return evaluate((Compare)expr, bindings);
		}
		else if (expr instanceof MathExpr) {
			return evaluate((MathExpr)expr, bindings);
		}
		else if (expr instanceof In) {
			return evaluate((In)expr, bindings);
		}
		else if (expr instanceof CompareAny) {
			return evaluate((CompareAny)expr, bindings);
		}
		else if (expr instanceof CompareAll) {
			return evaluate((CompareAll)expr, bindings);
		}
		else if (expr instanceof Exists) {
			return evaluate((Exists)expr, bindings);
		}
		else if (expr instanceof If) {
			return evaluate((If)expr, bindings);
		}
		else if (expr == null) {
			throw new IllegalArgumentException("expr must not be null");
		}
		else {
			throw new QueryEvaluationException("Unsupported value expr type: " + expr.getClass());
		}
	}

	public Value evaluate(Var var, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value value = var.getValue();

		if (value == null) {
			value = bindings.getValue(var.getName());
		}

		if (value == null) {
			throw new ValueExprEvaluationException();
		}

		return value;
	}

	public Value evaluate(ValueConstant valueConstant, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		return valueConstant.getValue();
	}

	public Value evaluate(BNodeGenerator node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		ValueExpr nodeIdExpr = node.getNodeIdExpr();

		if (nodeIdExpr != null) {
			Value nodeId = evaluate(nodeIdExpr, bindings);

			if (nodeId instanceof Literal) {
				return tripleSource.getValueFactory().createBNode(((Literal)nodeId).getLabel());
			}
			else {
				throw new ValueExprEvaluationException("BNODE function argument must be a literal");
			}
		}
		return tripleSource.getValueFactory().createBNode();
	}

	public Value evaluate(Bound node, BindingSet bindings)
		throws QueryEvaluationException
	{
		try {
			Value argValue = evaluate(node.getArg(), bindings);
			return BooleanLiteralImpl.valueOf(argValue != null);
		}
		catch (ValueExprEvaluationException e) {
			return BooleanLiteralImpl.FALSE;
		}
	}

	public Value evaluate(Str node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof URI) {
			return tripleSource.getValueFactory().createLiteral(argValue.toString());
		}
		else if (argValue instanceof Literal) {
			Literal literal = (Literal)argValue;

			if (QueryEvaluationUtil.isSimpleLiteral(literal)) {
				return literal;
			}
			else {
				return tripleSource.getValueFactory().createLiteral(literal.getLabel());
			}
		}
		else {
			throw new ValueExprEvaluationException();
		}
	}

	public Value evaluate(Label node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		// FIXME: deprecate Label in favour of Str(?)
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof Literal) {
			Literal literal = (Literal)argValue;

			if (QueryEvaluationUtil.isSimpleLiteral(literal)) {
				return literal;
			}
			else {
				return tripleSource.getValueFactory().createLiteral(literal.getLabel());
			}
		}
		else {
			throw new ValueExprEvaluationException();
		}
	}

	public Value evaluate(Lang node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof Literal) {
			Literal literal = (Literal)argValue;

			String langTag = literal.getLanguage();
			if (langTag == null) {
				langTag = "";
			}

			return tripleSource.getValueFactory().createLiteral(langTag);
		}

		throw new ValueExprEvaluationException();
	}

	public Value evaluate(Datatype node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value v = evaluate(node.getArg(), bindings);

		if (v instanceof Literal) {
			Literal literal = (Literal)v;

			if (literal.getDatatype() != null) {
				// literal with datatype
				return literal.getDatatype();
			}
			else if (literal.getLanguage() == null) {
				// simple literal
				return XMLSchema.STRING;
			}
		}

		throw new ValueExprEvaluationException();
	}

	public Value evaluate(Namespace node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof URI) {
			URI uri = (URI)argValue;
			return tripleSource.getValueFactory().createURI(uri.getNamespace());
		}
		else {
			throw new ValueExprEvaluationException();
		}
	}

	public Value evaluate(LocalName node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof URI) {
			URI uri = (URI)argValue;
			return tripleSource.getValueFactory().createLiteral(uri.getLocalName());
		}
		else {
			throw new ValueExprEvaluationException();
		}
	}

	/**
	 * Determines whether the operand (a variable) contains a Resource.
	 * 
	 * @return <tt>true</tt> if the operand contains a Resource, <tt>false</tt>
	 *         otherwise.
	 */
	public Value evaluate(IsResource node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);
		return BooleanLiteralImpl.valueOf(argValue instanceof Resource);
	}

	/**
	 * Determines whether the operand (a variable) contains a URI.
	 * 
	 * @return <tt>true</tt> if the operand contains a URI, <tt>false</tt>
	 *         otherwise.
	 */
	public Value evaluate(IsURI node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);
		return BooleanLiteralImpl.valueOf(argValue instanceof URI);
	}

	/**
	 * Determines whether the operand (a variable) contains a BNode.
	 * 
	 * @return <tt>true</tt> if the operand contains a BNode, <tt>false</tt>
	 *         otherwise.
	 */
	public Value evaluate(IsBNode node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);
		return BooleanLiteralImpl.valueOf(argValue instanceof BNode);
	}

	/**
	 * Determines whether the operand (a variable) contains a Literal.
	 * 
	 * @return <tt>true</tt> if the operand contains a Literal, <tt>false</tt>
	 *         otherwise.
	 */
	public Value evaluate(IsLiteral node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);
		return BooleanLiteralImpl.valueOf(argValue instanceof Literal);
	}

	/**
	 * Determines whether the operand (a variable) contains a numeric datatyped
	 * literal, i.e. a literal with datatype xsd:float, xsd:double, xsd:decimal,
	 * or a derived datatype of xsd:decimal.
	 * 
	 * @return <tt>true</tt> if the operand contains a numeric datatyped literal,
	 *         <tt>false</tt> otherwise.
	 */
	public Value evaluate(IsNumeric node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof Literal) {
			Literal lit = (Literal)argValue;
			URI datatype = lit.getDatatype();

			return BooleanLiteralImpl.valueOf(datatype != null && XMLDatatypeUtil.isNumericDatatype(datatype));
		}
		else {
			return BooleanLiteralImpl.FALSE;
		}

	}

	/**
	 * Creates a URI from the operand value (a plain literal or a URI).
	 * 
	 * @param node
	 * @param bindings
	 * @return
	 * @throws ValueExprEvaluationException
	 * @throws QueryEvaluationException
	 */
	public URI evaluate(IRIFunction node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);

		if (argValue instanceof Literal) {
			Literal lit = (Literal)argValue;

			return new URIImpl(lit.getLabel());
		}
		else if (argValue instanceof URI) {
			return ((URI)argValue);
		}

		throw new ValueExprEvaluationException();
	}

	/**
	 * Creates a datatyped literal from the operand values.
	 * 
	 * @param node
	 * @param bindings
	 * @return
	 * @throws ValueExprEvaluationException
	 * @throws QueryEvaluationException
	 */
	public Literal evaluate(StrDt node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value lexicalValue = evaluate(node.getLeftArg(), bindings);
		Value datatypeValue = evaluate(node.getRightArg(), bindings);

		if (QueryEvaluationUtil.isSimpleLiteral(lexicalValue)) {
			Literal lit = (Literal)lexicalValue;
			if (datatypeValue instanceof URI) {
				return new LiteralImpl(lit.getLabel(), (URI)datatypeValue);
			}
			else {
				throw new ValueExprEvaluationException("illegal value for operand: " + datatypeValue);
			}
		}
		else {
			throw new ValueExprEvaluationException("illegal value for operand: " + lexicalValue);
		}

	}

	/**
	 * Creates a language literal from the operand values.
	 * 
	 * @param node
	 * @param bindings
	 * @return
	 * @throws ValueExprEvaluationException
	 * @throws QueryEvaluationException
	 */
	public Literal evaluate(StrLang node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value lexicalValue = evaluate(node.getLeftArg(), bindings);
		Value languageValue = evaluate(node.getRightArg(), bindings);

		if (QueryEvaluationUtil.isSimpleLiteral(lexicalValue)) {
			Literal lit = (Literal)lexicalValue;

			if (languageValue instanceof Literal) {
				return new LiteralImpl(lit.getLabel(), ((Literal)languageValue).getLabel());
			}
			else {
				throw new ValueExprEvaluationException("illegal value for operand: " + languageValue);
			}
		}
		else {
			throw new ValueExprEvaluationException("illegal value for operand: " + lexicalValue);
		}

	}

	/**
	 * Determines whether the two operands match according to the
	 * <code>regex</code> operator.
	 * 
	 * @return <tt>true</tt> if the operands match according to the
	 *         <tt>regex</tt> operator, <tt>false</tt> otherwise.
	 */
	public Value evaluate(Regex node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value arg = evaluate(node.getArg(), bindings);
		Value parg = evaluate(node.getPatternArg(), bindings);
		Value farg = null;
		ValueExpr flagsArg = node.getFlagsArg();
		if (flagsArg != null) {
			farg = evaluate(flagsArg, bindings);
		}

		if (QueryEvaluationUtil.isSimpleLiteral(arg) && QueryEvaluationUtil.isSimpleLiteral(parg)
				&& (farg == null || QueryEvaluationUtil.isSimpleLiteral(farg)))
		{
			String text = ((Literal)arg).getLabel();
			String ptn = ((Literal)parg).getLabel();
			String flags = "";
			if (farg != null) {
				flags = ((Literal)farg).getLabel();
			}
			// TODO should this Pattern be cached?
			int f = 0;
			for (char c : flags.toCharArray()) {
				switch (c) {
					case 's':
						f |= Pattern.DOTALL;
						break;
					case 'm':
						f |= Pattern.MULTILINE;
						break;
					case 'i':
						f |= Pattern.CASE_INSENSITIVE;
						break;
					case 'x':
						f |= Pattern.COMMENTS;
						break;
					case 'd':
						f |= Pattern.UNIX_LINES;
						break;
					case 'u':
						f |= Pattern.UNICODE_CASE;
						break;
					default:
						throw new ValueExprEvaluationException(flags);
				}
			}
			Pattern pattern = Pattern.compile(ptn, f);
			boolean result = pattern.matcher(text).find();
			return BooleanLiteralImpl.valueOf(result);
		}

		throw new ValueExprEvaluationException();
	}

	public Value evaluate(LangMatches node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value langTagValue = evaluate(node.getLeftArg(), bindings);
		Value langRangeValue = evaluate(node.getRightArg(), bindings);

		if (QueryEvaluationUtil.isSimpleLiteral(langTagValue)
				&& QueryEvaluationUtil.isSimpleLiteral(langRangeValue))
		{
			String langTag = ((Literal)langTagValue).getLabel();
			String langRange = ((Literal)langRangeValue).getLabel();

			boolean result = false;
			if (langRange.equals("*")) {
				result = langTag.length() > 0;
			}
			else if (langTag.length() == langRange.length()) {
				result = langTag.equalsIgnoreCase(langRange);
			}
			else if (langTag.length() > langRange.length()) {
				// check if the range is a prefix of the tag
				String prefix = langTag.substring(0, langRange.length());
				result = prefix.equalsIgnoreCase(langRange) && langTag.charAt(langRange.length()) == '-';
			}

			return BooleanLiteralImpl.valueOf(result);
		}

		throw new ValueExprEvaluationException();

	}

	/**
	 * Determines whether the two operands match according to the
	 * <code>like</code> operator. The operator is defined as a string comparison
	 * with the possible use of an asterisk (*) at the end and/or the start of
	 * the second operand to indicate substring matching.
	 * 
	 * @return <tt>true</tt> if the operands match according to the <tt>like</tt>
	 *         operator, <tt>false</tt> otherwise.
	 */
	public Value evaluate(Like node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value val = evaluate(node.getArg(), bindings);
		String strVal = null;

		if (val instanceof URI) {
			strVal = ((URI)val).toString();
		}
		else if (val instanceof Literal) {
			strVal = ((Literal)val).getLabel();
		}

		if (strVal == null) {
			throw new ValueExprEvaluationException();
		}

		if (!node.isCaseSensitive()) {
			// Convert strVal to lower case, just like the pattern has been done
			strVal = strVal.toLowerCase();
		}

		int valIndex = 0;
		int prevPatternIndex = -1;
		int patternIndex = node.getOpPattern().indexOf('*');

		if (patternIndex == -1) {
			// No wildcards
			return BooleanLiteralImpl.valueOf(node.getOpPattern().equals(strVal));
		}

		String snippet;

		if (patternIndex > 0) {
			// Pattern does not start with a wildcard, first part must match
			snippet = node.getOpPattern().substring(0, patternIndex);
			if (!strVal.startsWith(snippet)) {
				return BooleanLiteralImpl.FALSE;
			}

			valIndex += snippet.length();
			prevPatternIndex = patternIndex;
			patternIndex = node.getOpPattern().indexOf('*', patternIndex + 1);
		}

		while (patternIndex != -1) {
			// Get snippet between previous wildcard and this wildcard
			snippet = node.getOpPattern().substring(prevPatternIndex + 1, patternIndex);

			// Search for the snippet in the value
			valIndex = strVal.indexOf(snippet, valIndex);
			if (valIndex == -1) {
				return BooleanLiteralImpl.FALSE;
			}

			valIndex += snippet.length();
			prevPatternIndex = patternIndex;
			patternIndex = node.getOpPattern().indexOf('*', patternIndex + 1);
		}

		// Part after last wildcard
		snippet = node.getOpPattern().substring(prevPatternIndex + 1);

		if (snippet.length() > 0) {
			// Pattern does not end with a wildcard.

			// Search last occurence of the snippet.
			valIndex = strVal.indexOf(snippet, valIndex);
			int i;
			while ((i = strVal.indexOf(snippet, valIndex + 1)) != -1) {
				// A later occurence was found.
				valIndex = i;
			}

			if (valIndex == -1) {
				return BooleanLiteralImpl.FALSE;
			}

			valIndex += snippet.length();

			if (valIndex < strVal.length()) {
				// Some characters were not matched
				return BooleanLiteralImpl.FALSE;
			}
		}

		return BooleanLiteralImpl.TRUE;
	}

	/**
	 * Evaluates a function.
	 */
	public Value evaluate(FunctionCall node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Function function = FunctionRegistry.getInstance().get(node.getURI());

		if (function == null) {
			throw new QueryEvaluationException("Unknown function '" + node.getURI() + "'");
		}

		List<ValueExpr> args = node.getArgs();

		Value[] argValues = new Value[args.size()];

		for (int i = 0; i < args.size(); i++) {
			argValues[i] = evaluate(args.get(i), bindings);
		}

		return function.evaluate(tripleSource.getValueFactory(), argValues);
	}

	public Value evaluate(And node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		try {
			Value leftValue = evaluate(node.getLeftArg(), bindings);
			if (QueryEvaluationUtil.getEffectiveBooleanValue(leftValue) == false) {
				// Left argument evaluates to false, we don't need to look any
				// further
				return BooleanLiteralImpl.FALSE;
			}
		}
		catch (ValueExprEvaluationException e) {
			// Failed to evaluate the left argument. Result is 'false' when
			// the right argument evaluates to 'false', failure otherwise.
			Value rightValue = evaluate(node.getRightArg(), bindings);
			if (QueryEvaluationUtil.getEffectiveBooleanValue(rightValue) == false) {
				return BooleanLiteralImpl.FALSE;
			}
			else {
				throw new ValueExprEvaluationException();
			}
		}

		// Left argument evaluated to 'true', result is determined
		// by the evaluation of the right argument.
		Value rightValue = evaluate(node.getRightArg(), bindings);
		return BooleanLiteralImpl.valueOf(QueryEvaluationUtil.getEffectiveBooleanValue(rightValue));
	}

	public Value evaluate(Or node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		try {
			Value leftValue = evaluate(node.getLeftArg(), bindings);
			if (QueryEvaluationUtil.getEffectiveBooleanValue(leftValue) == true) {
				// Left argument evaluates to true, we don't need to look any
				// further
				return BooleanLiteralImpl.TRUE;
			}
		}
		catch (ValueExprEvaluationException e) {
			// Failed to evaluate the left argument. Result is 'true' when
			// the right argument evaluates to 'true', failure otherwise.
			Value rightValue = evaluate(node.getRightArg(), bindings);
			if (QueryEvaluationUtil.getEffectiveBooleanValue(rightValue) == true) {
				return BooleanLiteralImpl.TRUE;
			}
			else {
				throw new ValueExprEvaluationException();
			}
		}

		// Left argument evaluated to 'false', result is determined
		// by the evaluation of the right argument.
		Value rightValue = evaluate(node.getRightArg(), bindings);
		return BooleanLiteralImpl.valueOf(QueryEvaluationUtil.getEffectiveBooleanValue(rightValue));
	}

	public Value evaluate(Not node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value argValue = evaluate(node.getArg(), bindings);
		boolean argBoolean = QueryEvaluationUtil.getEffectiveBooleanValue(argValue);
		return BooleanLiteralImpl.valueOf(!argBoolean);
	}

	public Value evaluate(SameTerm node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value leftVal = evaluate(node.getLeftArg(), bindings);
		Value rightVal = evaluate(node.getRightArg(), bindings);

		return BooleanLiteralImpl.valueOf(leftVal != null && leftVal.equals(rightVal));
	}

	public Value evaluate(Coalesce node, BindingSet bindings)
		throws ValueExprEvaluationException
	{
		Value result = null;

		for (ValueExpr expr : node.getArguments()) {
			try {
				result = evaluate(expr, bindings);

				// return first result that does not produce an error on evaluation.
				break;
			}
			catch (ValueExprEvaluationException e) {
				continue;
			}
			catch (QueryEvaluationException e) {
				continue;
			}
		}

		if (result == null) {
			throw new ValueExprEvaluationException("COALESCE arguments do not evaluate to a value: "
					+ node.getSignature());
		}

		return result;
	}

	public Value evaluate(Compare node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value leftVal = evaluate(node.getLeftArg(), bindings);
		Value rightVal = evaluate(node.getRightArg(), bindings);

		return BooleanLiteralImpl.valueOf(QueryEvaluationUtil.compare(leftVal, rightVal, node.getOperator()));
	}

	public Value evaluate(MathExpr node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		// Do the math
		Value leftVal = evaluate(node.getLeftArg(), bindings);
		Value rightVal = evaluate(node.getRightArg(), bindings);

		if (leftVal instanceof Literal && rightVal instanceof Literal) {
			return MathUtil.compute((Literal)leftVal, (Literal)rightVal, node.getOperator());
		}

		throw new ValueExprEvaluationException("Both arguments must be numeric literals");
	}

	public Value evaluate(If node, BindingSet bindings)
		throws QueryEvaluationException
	{
		Value result = null;
		if (isTrue(node.getCondition(), bindings)) {
			result = evaluate(node.getResult(), bindings);
		}
		else {
			result = evaluate(node.getAlternative(), bindings);
		}
		return result;
	}

	public Value evaluate(In node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value leftValue = evaluate(node.getArg(), bindings);

		// Result is false until a match has been found
		boolean result = false;

		// Use first binding name from tuple expr to compare values
		String bindingName = node.getSubQuery().getBindingNames().iterator().next();

		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluate(node.getSubQuery(), bindings);
		try {
			while (result == false && iter.hasNext()) {
				BindingSet bindingSet = iter.next();

				Value rightValue = bindingSet.getValue(bindingName);

				result = leftValue == null && rightValue == null || leftValue != null
						&& leftValue.equals(rightValue);
			}
		}
		finally {
			iter.close();
		}

		return BooleanLiteralImpl.valueOf(result);
	}

	public Value evaluate(CompareAny node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value leftValue = evaluate(node.getArg(), bindings);

		// Result is false until a match has been found
		boolean result = false;

		// Use first binding name from tuple expr to compare values
		String bindingName = node.getSubQuery().getBindingNames().iterator().next();

		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluate(node.getSubQuery(), bindings);
		try {
			while (result == false && iter.hasNext()) {
				BindingSet bindingSet = iter.next();

				Value rightValue = bindingSet.getValue(bindingName);

				try {
					result = QueryEvaluationUtil.compare(leftValue, rightValue, node.getOperator());
				}
				catch (ValueExprEvaluationException e) {
					// ignore, maybe next value will match
				}
			}
		}
		finally {
			iter.close();
		}

		return BooleanLiteralImpl.valueOf(result);
	}

	public Value evaluate(CompareAll node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		Value leftValue = evaluate(node.getArg(), bindings);

		// Result is true until a mismatch has been found
		boolean result = true;

		// Use first binding name from tuple expr to compare values
		String bindingName = node.getSubQuery().getBindingNames().iterator().next();

		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluate(node.getSubQuery(), bindings);
		try {
			while (result == true && iter.hasNext()) {
				BindingSet bindingSet = iter.next();

				Value rightValue = bindingSet.getValue(bindingName);

				try {
					result = QueryEvaluationUtil.compare(leftValue, rightValue, node.getOperator());
				}
				catch (ValueExprEvaluationException e) {
					// Exception thrown by ValueCompare.isTrue(...)
					result = false;
				}
			}
		}
		finally {
			iter.close();
		}

		return BooleanLiteralImpl.valueOf(result);
	}

	public Value evaluate(Exists node, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluate(node.getSubQuery(), bindings);
		try {
			return BooleanLiteralImpl.valueOf(iter.hasNext());
		}
		finally {
			iter.close();
		}
	}

	public boolean isTrue(ValueExpr expr, BindingSet bindings)
		throws QueryEvaluationException
	{
		try {
			Value value = evaluate(expr, bindings);
			return QueryEvaluationUtil.getEffectiveBooleanValue(value);
		}
		catch (ValueExprEvaluationException e) {
			return false;
		}
	}

	private boolean isReduced(QueryModelNode node) {
		QueryModelNode parent = node.getParentNode();
		if (parent instanceof Slice) {
			return isReduced(parent);
		}
		return parent instanceof Distinct || parent instanceof Reduced;
	}

	/**
	 * Returns the limit of the current variable bindings before any further
	 * projection.
	 */
	private long getLimit(QueryModelNode node) {
		long offset = 0;
		if (node instanceof Slice) {
			Slice slice = (Slice)node;
			if (slice.hasOffset() && slice.hasLimit()) {
				return slice.getOffset() + slice.getLimit();
			}
			else if (slice.hasLimit()) {
				return slice.getLimit();
			}
			else if (slice.hasOffset()) {
				offset = slice.getOffset();
			}
		}
		QueryModelNode parent = node.getParentNode();
		if (parent instanceof Distinct || parent instanceof Reduced || parent instanceof Slice) {
			long limit = getLimit(parent);
			if (offset > 0L && limit < Long.MAX_VALUE) {
				return offset + limit;
			}
			else {
				return limit;
			}
		}
		return Long.MAX_VALUE;
	}
}
