/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylogic.impl;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.querylogic.BooleanExprEvaluationException;
import org.openrdf.querylogic.EvaluationStrategy;
import org.openrdf.querylogic.QuerySolution;
import org.openrdf.querylogic.TripleSource;
import org.openrdf.querylogic.iterator.ExtensionIterator;
import org.openrdf.querylogic.iterator.JoinIterator;
import org.openrdf.querylogic.iterator.MultiProjectionIterator;
import org.openrdf.querylogic.iterator.OptionalJoinIterator;
import org.openrdf.querylogic.iterator.ProjectionIterator;
import org.openrdf.querylogic.iterator.SelectionIterator;
import org.openrdf.querymodel.And;
import org.openrdf.querymodel.BNodeGenerator;
import org.openrdf.querymodel.BooleanConstant;
import org.openrdf.querymodel.BooleanExpr;
import org.openrdf.querymodel.Compare;
import org.openrdf.querymodel.CompareAll;
import org.openrdf.querymodel.CompareAny;
import org.openrdf.querymodel.Datatype;
import org.openrdf.querymodel.Difference;
import org.openrdf.querymodel.Distinct;
import org.openrdf.querymodel.EffectiveBooleanValue;
import org.openrdf.querymodel.EmptySet;
import org.openrdf.querymodel.Exists;
import org.openrdf.querymodel.Extension;
import org.openrdf.querymodel.ExtensionElem;
import org.openrdf.querymodel.In;
import org.openrdf.querymodel.Intersection;
import org.openrdf.querymodel.IsBNode;
import org.openrdf.querymodel.IsLiteral;
import org.openrdf.querymodel.IsResource;
import org.openrdf.querymodel.IsURI;
import org.openrdf.querymodel.Join;
import org.openrdf.querymodel.Label;
import org.openrdf.querymodel.Lang;
import org.openrdf.querymodel.Like;
import org.openrdf.querymodel.LocalName;
import org.openrdf.querymodel.MathExpr;
import org.openrdf.querymodel.MultiProjection;
import org.openrdf.querymodel.Namespace;
import org.openrdf.querymodel.Not;
import org.openrdf.querymodel.Null;
import org.openrdf.querymodel.OptionalJoin;
import org.openrdf.querymodel.Or;
import org.openrdf.querymodel.Projection;
import org.openrdf.querymodel.ProjectionElem;
import org.openrdf.querymodel.QueryModelVisitor;
import org.openrdf.querymodel.RowSelection;
import org.openrdf.querymodel.Selection;
import org.openrdf.querymodel.SingletonSet;
import org.openrdf.querymodel.StatementPattern;
import org.openrdf.querymodel.TupleExpr;
import org.openrdf.querymodel.Union;
import org.openrdf.querymodel.ValueConstant;
import org.openrdf.querymodel.ValueExpr;
import org.openrdf.querymodel.Var;
import org.openrdf.querymodel.MathExpr.Operator;
import org.openrdf.querymodel.StatementPattern.Scope;
import org.openrdf.queryresult.Solution;
import org.openrdf.util.iterator.CloseableIterator;
import org.openrdf.util.iterator.ConvertingIterator;
import org.openrdf.util.iterator.DelayedIterator;
import org.openrdf.util.iterator.DistinctIterator;
import org.openrdf.util.iterator.EmptyIterator;
import org.openrdf.util.iterator.FilterIterator;
import org.openrdf.util.iterator.IntersectIterator;
import org.openrdf.util.iterator.LimitIterator;
import org.openrdf.util.iterator.MinusIterator;
import org.openrdf.util.iterator.OffsetIterator;
import org.openrdf.util.iterator.SingletonIterator;
import org.openrdf.util.iterator.UnionIterator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

/**
 * Evaluates the TupleExpr and ValueExpr using Iterators and common tripleSounce
 * API.
 */
public class EvaluationStrategyImpl implements QueryModelVisitor, EvaluationStrategy {

	private static final Solution EMPTY_SOLUTION = new QuerySolution(0);

	private TripleSource _tripleSource;

	private Solution _bindings;

	private CloseableIterator<Solution> _evaluate;

	private Value _value;

	private Boolean _isTrue;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querylogic.ExprStrategyI#evaluate(org.openrdf.querymodel.TupleExpr,
	 *      org.openrdf.querymodel.TripleSource,
	 *      org.openrdf.queryresult.Solution)
	 */
	public CloseableIterator<Solution> evaluate(TupleExpr expr, TripleSource tripleSource, Solution bindings) {
		TripleSource stack_tripleSource = _tripleSource;
		Solution stack_bindings = _bindings;
		try {
			_tripleSource = tripleSource;
			_bindings = bindings;
			expr.visit(this);
			return _evaluate;
		}
		finally {
			_tripleSource = stack_tripleSource;
			_bindings = stack_bindings;
			_evaluate = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querylogic.ExprStrategyI#getValue(org.openrdf.querymodel.ValueExpr,
	 *      org.openrdf.querymodel.TripleSource,
	 *      org.openrdf.queryresult.Solution)
	 */
	public Value getValue(ValueExpr expr, TripleSource tripleSource, Solution bindings) {
		TripleSource stack_tripleSource = _tripleSource;
		Solution stack_bindings = _bindings;
		try {
			_tripleSource = tripleSource;
			_bindings = bindings;
			expr.visit(this);
			if (_value == null && _isTrue != null) {
				return _tripleSource.getValueFactory().createLiteral(_isTrue.booleanValue());
			}
			return _value;
		}
		catch (BooleanExprEvaluationException e) {
			return null;
		}
		finally {
			_tripleSource = stack_tripleSource;
			_bindings = stack_bindings;
			_value = null;
			_isTrue = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querylogic.ExprStrategyI#isTrue(org.openrdf.querymodel.BooleanExpr,
	 *      org.openrdf.querymodel.TripleSource,
	 *      org.openrdf.queryresult.Solution)
	 */
	public boolean isTrue(BooleanExpr expr, TripleSource tripleSource, Solution bindings)
		throws BooleanExprEvaluationException
	{
		TripleSource stack_tripleSource = _tripleSource;
		Solution stack_bindings = _bindings;
		try {
			_tripleSource = tripleSource;
			_bindings = bindings;
			expr.visit(this);
			return _isTrue;
		}
		finally {
			_tripleSource = stack_tripleSource;
			_bindings = stack_bindings;
			_isTrue = null;
		}
	}

	public void meet(ExtensionElem node) {
	}

	public void meet(ProjectionElem node) {
	}

	public void meet(final Difference difference) {
		final TripleSource tripleSource = _tripleSource;
		final Solution bindings = _bindings;
		Iterator<Solution> leftArg = new DelayedIterator<Solution>() {

			protected Iterator<Solution> createIterator() {
				return evaluate(difference.getLeftArg(), tripleSource, bindings);
			}
		};

		Iterator<Solution> rightArg = new DelayedIterator<Solution>() {

			protected Iterator<Solution> createIterator() {
				return evaluate(difference.getRightArg(), tripleSource, bindings);
			}
		};

		_evaluate = new MinusIterator<Solution>(leftArg, rightArg);
	}

	public void meet(Distinct distinct) {
		_evaluate = new DistinctIterator<Solution>(evaluate(distinct.getArg(), _tripleSource, _bindings));
	}

	public void meet(EffectiveBooleanValue ebv) {
		Value value = getValue(ebv.getArg(), _tripleSource, _bindings);
		
		if (value instanceof Literal) {
			Literal literal = (Literal)value;
			String label = literal.getLabel();
			URI datatype = literal.getDatatype();
			
			if (datatype == null || datatype.equals(XMLSchema.STRING)) {
				_isTrue = label.length() > 0;
			}
			else if (datatype.equals(XMLSchema.BOOLEAN)) {
				if ("true".equals(label) || "1".equals(label)) {
					_isTrue = Boolean.TRUE;
				}
				else if ("false".equals(label) || "0".equals(label)) {
					_isTrue = Boolean.FALSE;
				}
			}
			else if (datatype.equals(XMLSchema.DECIMAL)) {
				try {
					String normDec = XMLDatatypeUtil.normalizeDecimal(label);
					_isTrue = !normDec.equals("0.0");
				}
				catch (IllegalArgumentException e) {
					// type error, don't set _isTrue
				}
			}
			else if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
				try {
					String normInt = XMLDatatypeUtil.normalize(label, datatype);
					_isTrue = !normInt.equals("0");
				}
				catch (IllegalArgumentException e) {
					// type error, don't set _isTrue
				}
			}
			else if (XMLDatatypeUtil.isFloatingPointDatatype(datatype)) {
				try {
					String normFP = XMLDatatypeUtil.normalize(label, datatype);
					_isTrue = !normFP.equals("0.0E0") && !normFP.equals("NaN");
				}
				catch (IllegalArgumentException e) {
					// type error, don't set _isTrue
				}
			}
		}
	}

	public void meet(EmptySet emptySet) {
		_evaluate = new EmptyIterator<Solution>();
	}

	public void meet(Extension extension) {
		_evaluate = new ExtensionIterator(this, extension, _tripleSource, _bindings);
	}

	public void meet(final Intersection intersection) {
		final TripleSource tripleSource = _tripleSource;
		final Solution bindings = _bindings;
		Iterator<Solution> leftArg = new DelayedIterator<Solution>() {

			protected Iterator<Solution> createIterator() {
				return evaluate(intersection.getLeftArg(), tripleSource, bindings);
			}
		};

		Iterator<Solution> rightArg = new DelayedIterator<Solution>() {

			protected Iterator<Solution> createIterator() {
				return evaluate(intersection.getRightArg(), tripleSource, bindings);
			}
		};

		_evaluate = new IntersectIterator<Solution>(leftArg, rightArg);
	}

	public void meet(Join join) {
		_evaluate = new JoinIterator(this, join, _tripleSource, _bindings);
	}

	public void meet(MultiProjection multiProjection) {
		_evaluate = new MultiProjectionIterator(this.evaluate(multiProjection.getArg(), _tripleSource,
				_bindings), multiProjection, _tripleSource, _bindings);
	}

	public void meet(OptionalJoin optionalJoin) {
		_evaluate = new OptionalJoinIterator(this, optionalJoin, _tripleSource, _bindings);
	}

	public void meet(Projection projection) {
		_evaluate = new ProjectionIterator(this.evaluate(projection.getArg(), _tripleSource, _bindings),
				projection, _tripleSource, _bindings);
	}

	public void meet(RowSelection rowSelection) {
		CloseableIterator<Solution> result = evaluate(rowSelection.getArg(), _tripleSource, _bindings);

		if (rowSelection.hasOffset()) {
			result = new OffsetIterator<Solution>(result, rowSelection.getOffset());
		}

		if (rowSelection.hasLimit()) {
			result = new LimitIterator<Solution>(result, rowSelection.getLimit());
		}

		_evaluate = result;
	}

	public void meet(Selection selection) {
		_evaluate = new SelectionIterator(this, selection, _tripleSource, _bindings);
	}

	public void meet(SingletonSet singletonSet) {
		_evaluate = new SingletonIterator<Solution>(EMPTY_SOLUTION);
	}

	public void meet(StatementPattern sp) {
		final TripleSource tripleSource = _tripleSource;
		final Solution bindings = _bindings;

		final Var subjVar = sp.getSubjectVar();
		final Var predVar = sp.getPredicateVar();
		final Var objVar = sp.getObjectVar();
		final Var conVar = sp.getContextVar();

		Value subjValue = _getVarValue(subjVar, tripleSource, bindings);
		Value predValue = _getVarValue(predVar, tripleSource, bindings);
		Value objValue = _getVarValue(objVar, tripleSource, bindings);
		Value contextValue = _getVarValue(conVar, tripleSource, bindings);

		CloseableIterator<? extends Statement> stIter = null;

		try {
			if (sp.getScope() == Scope.ALL_CONTEXTS) {
				stIter = tripleSource.getStatements((Resource)subjValue, (URI)predValue, objValue);
			}
			else if (sp.getScope() == Scope.NULL_CONTEXT) {
				stIter = tripleSource.getNullContextStatements((Resource)subjValue, (URI)predValue, objValue);
			}
			else if (sp.getScope() == Scope.NAMED_CONTEXTS) {
				stIter = tripleSource.getNamedContextStatements((Resource)subjValue, (URI)predValue, objValue,
						(Resource)contextValue);
			}
			else {
				throw new RuntimeException("Unknown statement pattern scope " + sp.getScope());
			}
		}
		catch (ClassCastException e) {
			// Invalid value type for subject, predicate and/or context
			stIter = new EmptyIterator<Statement>();
		}

		// The same variable might have been used multiple times in this
		// StatementPattern, verify value equality in those cases.
		stIter = new FilterIterator<Statement>(stIter) {

			protected boolean accept(Statement st) {
				Resource subj = st.getSubject();
				URI pred = st.getPredicate();
				Value obj = st.getObject();
				Resource context = st.getContext();

				if (subjVar != null) {
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

				if (predVar != null) {
					if (predVar.equals(objVar) && !pred.equals(obj)) {
						return false;
					}
					if (predVar.equals(conVar) && !pred.equals(context)) {
						return false;
					}
				}

				if (objVar != null) {
					if (objVar.equals(conVar) && !obj.equals(context)) {
						return false;
					}
				}

				return true;
			}
		};

		// Return an iterator that converts the statements to var bindings
		_evaluate = new ConvertingIterator<Statement, Solution>(stIter) {

			@Override
			protected Solution convert(Statement st)
			{
				QuerySolution result = new QuerySolution(bindings);

				if (subjVar != null && !result.hasBinding(subjVar.getName())) {
					result.addBinding(subjVar.getName(), st.getSubject());
				}
				if (predVar != null && !result.hasBinding(predVar.getName())) {
					result.addBinding(predVar.getName(), st.getPredicate());
				}
				if (objVar != null && !result.hasBinding(objVar.getName())) {
					result.addBinding(objVar.getName(), st.getObject());
				}
				if (conVar != null && !result.hasBinding(conVar.getName())) {
					result.addBinding(conVar.getName(), st.getContext());
				}

				return result;
			}
		};
	}

	private Value _getVarValue(Var var, TripleSource tripleSource, Solution bindings) {
		if (var == null) {
			return null;
		}
		else {
			return getValue(var, tripleSource, bindings);
		}
	}

	public void meet(final Union union) {
		final TripleSource tripleSource = _tripleSource;
		final Solution bindings = _bindings;
		Iterator<Solution> leftArg = new DelayedIterator<Solution>() {

			protected Iterator<Solution> createIterator() {
				return evaluate(union.getLeftArg(), tripleSource, bindings);
			}
		};

		Iterator<Solution> rightArg = new DelayedIterator<Solution>() {

			protected Iterator<Solution> createIterator() {
				return evaluate(union.getRightArg(), tripleSource, bindings);
			}
		};

		_evaluate = new UnionIterator<Solution>(leftArg, rightArg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.And)
	 */
	public void meet(And node) {
		try {
			if (!isTrue(node.getLeftArg(), _tripleSource, _bindings)) {
				// Left argument evaluates to false, we don't need to look any
				// further
				_isTrue = Boolean.FALSE;
				return;
			}
		}
		catch (BooleanExprEvaluationException e) {
			// Failed to evaluate the left argument. Result is 'false' when
			// the right argument evaluates to 'false', failure otherwise.
			if (!isTrue(node.getRightArg(), _tripleSource, _bindings)) {
				_isTrue = Boolean.FALSE;
				return;
			}
			else {
				throw new BooleanExprEvaluationException();
			}
		}

		// Left argument evaluated to 'true', result is determined
		// by the evaluation of the right argument.
		_isTrue = isTrue(node.getRightArg(), _tripleSource, _bindings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.BNodeGenerator)
	 */
	public void meet(BNodeGenerator node) {
		_value = _tripleSource.getValueFactory().createBNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.BooleanConstant)
	 */
	public void meet(BooleanConstant node) {
		_isTrue = Boolean.valueOf(node.isTrue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.Compare)
	 */
	public void meet(Compare node) {
		Value leftVal = getValue(node.getLeftArg(), _tripleSource, _bindings);
		Value rightVal = getValue(node.getRightArg(), _tripleSource, _bindings);

		_isTrue = isTrue(leftVal, rightVal, node.getOperator());
	}

	public static boolean isTrue(Value leftVal, Value rightVal, Compare.Operator operator)
		throws BooleanExprEvaluationException
	{
		boolean result = false;

		if (leftVal instanceof Literal && rightVal instanceof Literal) {
			// Both left and right argument is a Literal
			result = _compareLiterals((Literal)leftVal, (Literal)rightVal, operator);
		}
		else {
			// All other value combinations
			switch (operator) {
				case EQ:
					result = _valuesEqual(leftVal, rightVal);
					break;
				case NE:
					result = !_valuesEqual(leftVal, rightVal);
					break;
				default:
					throw new BooleanExprEvaluationException(
							"Only literals with compatible, ordered datatypes can be compared using <, <=, > and >= operators");
			}
		}

		return result;
	}

	private static boolean _valuesEqual(Value leftVal, Value rightVal) {
		boolean result = false;

		if (leftVal == null) {
			result = (rightVal == null);
		}
		else {
			result = leftVal.equals(rightVal);
		}

		return result;
	}

	private static boolean _compareLiterals(Literal leftLit, Literal rightLit, Compare.Operator operator)
		throws BooleanExprEvaluationException
	{
		String leftLabel = leftLit.getLabel();
		String leftLang = leftLit.getLanguage();
		URI leftDatatype = leftLit.getDatatype();

		String rightLabel = rightLit.getLabel();
		String rightLang = rightLit.getLanguage();
		URI rightDatatype = rightLit.getDatatype();

		// apply type casting if necessary
		if (leftDatatype == null && rightDatatype != null) {
			// left argument has no datatype, assume it is
			// of the same datatype as the right argument
			leftDatatype = rightDatatype;
		}
		else if (rightDatatype == null && leftDatatype != null) {
			// right argument has no datatype, assume it is
			// of the same datatype as the left argument
			rightDatatype = leftDatatype;
		}
		else if (rightDatatype != null && leftDatatype != null && !leftDatatype.equals(rightDatatype)) {
			// left and right arguments have different datatypes,
			// try to cast them to a more general, shared datatype
			if (XMLDatatypeUtil.isDecimalDatatype(rightDatatype)
					&& XMLDatatypeUtil.isDecimalDatatype(leftDatatype))
			{
				leftDatatype = rightDatatype = XMLSchema.DECIMAL;
			}
		}

		boolean result = false;

		if (leftDatatype != null && leftDatatype.equals(rightDatatype)
				&& XMLDatatypeUtil.isOrderedDatatype(leftDatatype))
		{
			// Both arguments have the same, ordered datatype
			try {
				int compareTo = XMLDatatypeUtil.compare(leftLabel, rightLabel, leftDatatype);
				switch (operator) {
					case LT:
						result = compareTo < 0;
						break;
					case LE:
						result = compareTo <= 0;
						break;
					case EQ:
						result = compareTo == 0;
						break;
					case NE:
						result = compareTo != 0;
						break;
					case GE:
						result = compareTo >= 0;
						break;
					case GT:
						result = compareTo > 0;
						break;
					default:
						throw new BooleanExprEvaluationException("Unknown operator: " + operator);
				}
			}
			catch (IllegalArgumentException e) {
				// One or both of the arguments was invalid, probably due to an
				// invalid cast earlier in this method. Return false.
				throw new BooleanExprEvaluationException(e.getMessage());
			}
		}
		else if ((leftDatatype == null && leftLang == null || leftDatatype != null
				&& leftDatatype.equals(XMLSchema.STRING))
				&& (rightDatatype == null && rightLang == null || rightDatatype != null
						&& rightDatatype.equals(XMLSchema.STRING)))
		{
			// Both arguments are either plain literals (i.e. have no language
			// or datatype), or are of type xsd:string. Compare the labels
			int compareTo = leftLabel.compareTo(rightLabel);
			switch (operator) {
				case LT:
					result = compareTo < 0;
					break;
				case LE:
					result = compareTo <= 0;
					break;
				case EQ:
					result = compareTo == 0;
					break;
				case NE:
					result = compareTo != 0;
					break;
				case GE:
					result = compareTo >= 0;
					break;
				case GT:
					result = compareTo > 0;
					break;
				default:
					throw new BooleanExprEvaluationException("Unknown operator: " + operator);
			}
		}
		else {
			// All other cases, e.g. literals with languages, unequal or
			// unordered datatypes, etc. These arguments can only be compared
			// using the operators 'EQ' and 'NE'.
			switch (operator) {
				case EQ:
					result = leftLit.equals(rightLit);
					break;
				case NE:
					result = !leftLit.equals(rightLit);
					break;
				default:
					throw new BooleanExprEvaluationException(
							"Only literals with compatible, ordered datatypes can be compared using <, <=, > and >= operators");
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.CompareAll)
	 */
	public void meet(CompareAll node) {
		Value leftValue = getValue(node.getValueExpr(), _tripleSource, _bindings);

		// Result is true until a mismatch has been found
		boolean result = true;

		// Use first binding name from tuple expr to compare values
		String bindingName = node.getSubQuery().getBindingNames().iterator().next();

		CloseableIterator<Solution> iter = evaluate(node.getSubQuery(), _tripleSource, _bindings);
		try {
			while (result == true && iter.hasNext()) {
				Solution solution = iter.next();

				Value rightValue = solution.getValue(bindingName);

				try {
					result = isTrue(leftValue, rightValue, node.getOperator());
				}
				catch (BooleanExprEvaluationException e) {
					// Exception thrown by ValueCompare.isTrue(...)
					result = false;
				}
			}
		}
		finally {
			iter.close();
		}

		_isTrue = result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.CompareAny)
	 */
	public void meet(CompareAny node) {
		Value leftValue = getValue(node.getValueExpr(), _tripleSource, _bindings);

		// Result is false until a match has been found
		boolean result = false;

		// Use first binding name from tuple expr to compare values
		String bindingName = node.getSubQuery().getBindingNames().iterator().next();

		CloseableIterator<Solution> iter = evaluate(node.getSubQuery(), _tripleSource, _bindings);
		try {
			while (result == false && iter.hasNext()) {
				Solution solution = iter.next();

				Value rightValue = solution.getValue(bindingName);

				try {
					result = isTrue(leftValue, rightValue, node.getOperator());
				}
				catch (BooleanExprEvaluationException e) {
					// ignore, maybe next value will match
				}
			}
		}
		finally {
			iter.close();
		}

		_isTrue = result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.Datatype)
	 */
	public void meet(Datatype node) {
		Value v = getValue(node.getArg(), _tripleSource, _bindings);

		if (v instanceof Literal) {
			_value = ((Literal)v).getDatatype();
			return;
		}

		_value = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.Exists)
	 */
	public void meet(Exists node) {
		CloseableIterator<Solution> iter = evaluate(node.getSubQuery(), _tripleSource, _bindings);
		try {
			_isTrue = iter.hasNext();
		}
		finally {
			iter.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.In)
	 */
	public void meet(In node) {
		Value leftValue = getValue(node.getValueExpr(), _tripleSource, _bindings);

		// Result is false until a match has been found
		boolean result = false;

		// Use first binding name from tuple expr to compare values
		String bindingName = node.getSubQuery().getBindingNames().iterator().next();

		CloseableIterator<Solution> iter = evaluate(node.getSubQuery(), _tripleSource, _bindings);
		try {
			while (result == false && iter.hasNext()) {
				Solution solution = iter.next();

				Value rightValue = solution.getValue(bindingName);

				result = leftValue == null && rightValue == null || leftValue != null
						&& leftValue.equals(rightValue);
			}
		}
		finally {
			iter.close();
		}

		_isTrue = result;
	}

	/**
	 * Determines whether the operand (a variable) contains a BNode.
	 * 
	 * @return <tt>true</tt> if the operand contains a BNode, <tt>false</tt>
	 *         otherwise.
	 */
	public void meet(IsBNode node) {
		_isTrue = getValue(node.getArg(), _tripleSource, _bindings) instanceof BNode;
	}

	/**
	 * Determines whether the operand (a variable) contains a Literal.
	 * 
	 * @return <tt>true</tt> if the operand contains a Literal, <tt>false</tt>
	 *         otherwise.
	 */
	public void meet(IsLiteral node) {
		_isTrue = getValue(node.getArg(), _tripleSource, _bindings) instanceof Literal;
	}

	/**
	 * Determines whether the operand (a variable) contains a Resource.
	 * 
	 * @return <tt>true</tt> if the operand contains a Resource, <tt>false</tt>
	 *         otherwise.
	 */
	public void meet(IsResource node) {
		_isTrue = getValue(node.getArg(), _tripleSource, _bindings) instanceof Resource;
	}

	/**
	 * Determines whether the operand (a variable) contains a URI.
	 * 
	 * @return <tt>true</tt> if the operand contains a URI, <tt>false</tt>
	 *         otherwise.
	 */
	public void meet(IsURI node) {
		_isTrue = getValue(node.getArg(), _tripleSource, _bindings) instanceof URI;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.Label)
	 */
	public void meet(Label node) {
		Value value = getValue(node.getArg(), _tripleSource, _bindings);

		if (value instanceof Literal) {
			Literal literal = (Literal)value;

			if (literal.getLanguage() == null && literal.getDatatype() == null) {
				_value = literal;
				return;
			}
			else {
				_value = _tripleSource.getValueFactory().createLiteral(literal.getLabel());
				return;
			}
		}

		_value = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.Lang)
	 */
	public void meet(Lang node) {
		Value value = getValue(node.getArg(), _tripleSource, _bindings);

		if (value instanceof Literal) {
			Literal literal = (Literal)value;

			if (literal.getLanguage() != null) {
				_value = _tripleSource.getValueFactory().createLiteral(literal.getLanguage());
				return;
			}
		}

		_value = null;
	}

	/**
	 * Determines whether the two operands match according to the
	 * <code>like</code> operator. The operator is defined as a string
	 * comparison with the possible use of an asterisk (*) at the end and/or the
	 * start of the second operand to indicate substring matching.
	 * 
	 * @return <tt>true</tt> if the operands match according to the
	 *         <tt>like</tt> operator, <tt>false</tt> otherwise.
	 */
	public void meet(Like node) {
		Value val = getValue(node.getValueExpr(), _tripleSource, _bindings);
		String strVal = null;

		if (val instanceof URI) {
			strVal = ((URI)val).toString();
		}
		else if (val instanceof Literal) {
			strVal = ((Literal)val).getLabel();
		}

		if (strVal == null) {
			throw new BooleanExprEvaluationException();
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
			_isTrue = node.getOpPattern().equals(strVal);
			return;
		}

		String snippet;

		if (patternIndex > 0) {
			// Pattern does not start with a wildcard, first part must match
			snippet = node.getOpPattern().substring(0, patternIndex);
			if (!strVal.startsWith(snippet)) {
				_isTrue = false;
				return;
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
				_isTrue = false;
				return;
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
				_isTrue = false;
				return;
			}

			valIndex += snippet.length();

			if (valIndex < strVal.length()) {
				// Some characters were not matched
				_isTrue = false;
				return;
			}
		}

		_isTrue = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.LocalName)
	 */
	public void meet(LocalName node) {
		Value value = getValue(node.getArg(), _tripleSource, _bindings);

		if (value instanceof URI) {
			URI uri = (URI)value;
			_value = _tripleSource.getValueFactory().createLiteral(uri.getLocalName());
			return;
		}

		_value = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.MathExpr)
	 */
	public void meet(MathExpr node) {
		// Do the math
		Value leftVal = getValue(node.getLeftArg(), _tripleSource, _bindings);
		Value rightVal = getValue(node.getRightArg(), _tripleSource, _bindings);

		if (leftVal instanceof Literal && rightVal instanceof Literal) {
			_value = getValue((Literal)leftVal, (Literal)rightVal, node.getOperator());
			return;
		}

		_value = null;
	}

	public static Literal getValue(Literal leftLit, Literal rightLit, Operator op) {
		String leftLabel = leftLit.getLabel();
		URI leftDatatype = leftLit.getDatatype();

		String rightLabel = rightLit.getLabel();
		URI rightDatatype = rightLit.getDatatype();

		if (leftDatatype == null || XMLDatatypeUtil.isIntegerDatatype(leftDatatype) && rightDatatype == null
				|| XMLDatatypeUtil.isIntegerDatatype(rightDatatype))
		{
			// Both arguments are, or could be, integers. Attempt an integer
			// operation.
			try {
				BigInteger leftInt = new BigInteger(leftLabel);
				BigInteger rightInt = new BigInteger(rightLabel);

				BigInteger result = null;
				switch (op) {
					case PLUS:
						result = leftInt.add(rightInt);
						break;
					case MINUS:
						result = leftInt.subtract(rightInt);
						break;
					case MULTIPLY:
						result = leftInt.multiply(rightInt);
						break;
					case DIVIDE:
						result = leftInt.divide(rightInt);
						break;
					case REMAINDER:
						result = leftInt.remainder(rightInt);
						break;

					default:
						throw new IllegalArgumentException("Unknown operator: " + op);
				}

				// Return the result as an xsd:integer
				return new LiteralImpl(result.toString(), XMLSchema.INTEGER);
			}
			catch (NumberFormatException e) {
				if (leftDatatype != null && rightDatatype != null) {
					// Both were specified to be integer, but apparently are not
					return null;
				}
				// else: type casting of untyped literal to an integer failed,
				// attempt decimal operation
			}
		}

		// Integer operation failed or does not apply, attempt floating point
		// operation
		try {
			BigDecimal leftDec = new BigDecimal(leftLabel);
			BigDecimal rightDec = new BigDecimal(rightLabel);

			BigDecimal result = null;
			switch (op) {
				case PLUS:
					result = leftDec.add(rightDec);
					break;
				case MINUS:
					result = leftDec.subtract(rightDec);
					break;
				case MULTIPLY:
					result = leftDec.multiply(rightDec);
					break;
				case DIVIDE:
					result = leftDec.divide(rightDec, 32, BigDecimal.ROUND_DOWN);
					break;
				case REMAINDER:
					return null; // Remainder is not applicable to floating point
										// numbers

				default:
					throw new IllegalArgumentException("Unknown operator: " + op);
			}

			// Return the result as an xsd:double
			return new LiteralImpl(result.toString(), XMLSchema.DOUBLE);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.Namespace)
	 */
	public void meet(Namespace node) {
		Value value = getValue(node.getArg(), _tripleSource, _bindings);

		if (value instanceof URI) {
			URI uri = (URI)value;
			_value = _tripleSource.getValueFactory().createURI(uri.getNamespace());
			return;
		}

		_value = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.Not)
	 */
	public void meet(Not node) {
		_isTrue = !isTrue(node.getArg(), _tripleSource, _bindings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.Null)
	 */
	public void meet(Null node) {
		_value = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.Or)
	 */
	public void meet(Or node) {
		try {
			if (isTrue(node.getLeftArg(), _tripleSource, _bindings)) {
				// Left argument evaluates to true, we don't need to look any
				// further
				_isTrue = true;
				return;
			}
		}
		catch (BooleanExprEvaluationException e) {
			// Failed to evaluate the left argument. Result is 'true' when
			// the right argument evaluates to 'true', failure otherwise.
			if (isTrue(node.getRightArg(), _tripleSource, _bindings)) {
				_isTrue = true;
				return;
			}
			else {
				throw new BooleanExprEvaluationException();
			}
		}

		// Left argument evaluated to 'false', result is determined
		// by the evaluation of the right argument.
		_isTrue = isTrue(node.getRightArg(), _tripleSource, _bindings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.ValueConstant)
	 */
	public void meet(ValueConstant node) {
		_value = node.getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.querymodel.QueryModelVisitor#meet(org.openrdf.querymodel.Var)
	 */
	public void meet(Var node) {
		if (node.getValue() != null) {
			_value = node.getValue();
		}
		else {
			_value = _bindings.getValue(node.getName());
		}
	}
}
