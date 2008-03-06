/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.DelayedIteration;
import info.aduna.iteration.DistinctIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.IntersectIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.LimitIteration;
import info.aduna.iteration.MinusIteration;
import info.aduna.iteration.OffsetIteration;
import info.aduna.iteration.SingletonIteration;
import info.aduna.iteration.UnionIteration;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.DecimalLiteralImpl;
import org.openrdf.model.impl.IntegerLiteralImpl;
import org.openrdf.model.impl.NumericLiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.CompareAll;
import org.openrdf.query.algebra.CompareAny;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.DatatypeFunc;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.In;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsResource;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LabelFunc;
import org.openrdf.query.algebra.LangFunc;
import org.openrdf.query.algebra.Like;
import org.openrdf.query.algebra.LocalNameFunc;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.NamespaceFunc;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Null;
import org.openrdf.query.algebra.OptionalJoin;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.RowSelection;
import org.openrdf.query.algebra.Selection;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StrFunc;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.evaluation.BooleanExprEvaluationException;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.iterator.ExtensionIterator;
import org.openrdf.query.algebra.evaluation.iterator.GroupIterator;
import org.openrdf.query.algebra.evaluation.iterator.JoinIterator;
import org.openrdf.query.algebra.evaluation.iterator.MultiProjectionIterator;
import org.openrdf.query.algebra.evaluation.iterator.OptionalJoinIterator;
import org.openrdf.query.algebra.evaluation.iterator.ProjectionIterator;
import org.openrdf.query.algebra.evaluation.iterator.SelectionIterator;

/**
 * Evaluates the TupleExpr and ValueExpr using Iterators and common tripleSource
 * API.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 * @author David Huynh
 */
public class EvaluationStrategyImpl implements QueryModelVisitor<QueryEvaluationException>,
		EvaluationStrategy
{

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final boolean USE_TYPED_LITERALS = true;

	public static Literal getValue(Literal leftLit, Literal rightLit, MathOp op) {
		URI leftDatatype = leftLit.getDatatype();
		URI rightDatatype = rightLit.getDatatype();

		// Only numeric value can be compared
		if (leftDatatype != null && rightDatatype != null && XMLDatatypeUtil.isNumericDatatype(leftDatatype)
				&& XMLDatatypeUtil.isNumericDatatype(rightDatatype))
		{
			// Determine most specific datatype that the arguments have in common,
			// choosing from xsd:integer, xsd:decimal, xsd:float and xsd:double as
			// per the SPARQL/XPATH spec
			URI commonDatatype;

			if (leftDatatype.equals(XMLSchema.DOUBLE) || rightDatatype.equals(XMLSchema.DOUBLE)) {
				commonDatatype = XMLSchema.DOUBLE;
			}
			else if (leftDatatype.equals(XMLSchema.FLOAT) || rightDatatype.equals(XMLSchema.FLOAT)) {
				commonDatatype = XMLSchema.FLOAT;
			}
			else if (leftDatatype.equals(XMLSchema.DECIMAL) || rightDatatype.equals(XMLSchema.DECIMAL)) {
				commonDatatype = XMLSchema.DECIMAL;
			}
			else if (op == MathOp.DIVIDE) {
				// Result of integer divide is decimal and requires the arguments to
				// be handled as such, see for details:
				// http://www.w3.org/TR/xpath-functions/#func-numeric-divide
				commonDatatype = XMLSchema.DECIMAL;
			}
			else {
				commonDatatype = XMLSchema.INTEGER;
			}

			// Note: Java already handles cases like divide-by-zero appropriately
			// for floats and doubles, see:
			// http://www.particle.kth.se/~lindsey/JavaCourse/Book/Part1/Tech/Chapter02/floatingPt2.html

			try {
				if (commonDatatype.equals(XMLSchema.DOUBLE)) {
					double left = leftLit.doubleValue();
					double right = rightLit.doubleValue();

					switch (op) {
						case PLUS:
							return new NumericLiteralImpl(left + right);
						case MINUS:
							return new NumericLiteralImpl(left - right);
						case MULTIPLY:
							return new NumericLiteralImpl(left * right);
						case DIVIDE:
							return new NumericLiteralImpl(left / right);
						default:
							throw new IllegalArgumentException("Unknown operator: " + op);
					}
				}
				else if (commonDatatype.equals(XMLSchema.FLOAT)) {
					float left = leftLit.floatValue();
					float right = rightLit.floatValue();

					switch (op) {
						case PLUS:
							return new NumericLiteralImpl(left + right);
						case MINUS:
							return new NumericLiteralImpl(left - right);
						case MULTIPLY:
							return new NumericLiteralImpl(left * right);
						case DIVIDE:
							return new NumericLiteralImpl(left / right);
						default:
							throw new IllegalArgumentException("Unknown operator: " + op);
					}
				}
				else if (commonDatatype.equals(XMLSchema.DECIMAL)) {
					BigDecimal left = leftLit.decimalValue();
					BigDecimal right = rightLit.decimalValue();

					switch (op) {
						case PLUS:
							return new DecimalLiteralImpl(left.add(right));
						case MINUS:
							return new DecimalLiteralImpl(left.subtract(right));
						case MULTIPLY:
							return new DecimalLiteralImpl(left.multiply(right));
						case DIVIDE:
							// Divide by zero handled through NumberFormatException
							return new DecimalLiteralImpl(left.divide(right, RoundingMode.HALF_UP));
						default:
							throw new IllegalArgumentException("Unknown operator: " + op);
					}
				}
				else { // XMLSchema.INTEGER
					BigInteger left = leftLit.integerValue();
					BigInteger right = rightLit.integerValue();

					switch (op) {
						case PLUS:
							return new IntegerLiteralImpl(left.add(right));
						case MINUS:
							return new IntegerLiteralImpl(left.subtract(right));
						case MULTIPLY:
							return new IntegerLiteralImpl(left.multiply(right));
						case DIVIDE:
							throw new RuntimeException("Integer divisions should be processed as decimal divisions");
						default:
							throw new IllegalArgumentException("Unknown operator: " + op);
					}
				}
			}
			catch (NumberFormatException e) {
				return null;
			}
			catch (ArithmeticException e) {
				return null;
			}
		}

		return null;
	}

	private static boolean _compare(Value leftVal, Value rightVal, CompareOp operator)
		throws BooleanExprEvaluationException
	{
		if (leftVal instanceof Literal && rightVal instanceof Literal) {
			// Both left and right argument is a Literal
			return _compareLiterals((Literal)leftVal, (Literal)rightVal, operator);
		}
		else {
			// All other value combinations
			switch (operator) {
				case EQ:
					return _valuesEqual(leftVal, rightVal);
				case NE:
					return !_valuesEqual(leftVal, rightVal);
				default:
					throw new BooleanExprEvaluationException(
							"Only literals with compatible, ordered datatypes can be compared using <, <=, > and >= operators");
			}
		}
	}

	private static boolean _compareLiterals(Literal leftLit, Literal rightLit, CompareOp operator)
		throws BooleanExprEvaluationException
	{
		URI leftDatatype = leftLit.getDatatype();
		URI rightDatatype = rightLit.getDatatype();
		URI commonDatatype = null;

		// FIXME: remove type casting for untyped literals for proper SPARQL
		// support when type casting has been added to SeRQL

		// apply type casting if necessary
		if (leftDatatype == null && rightDatatype != null) {
			// left argument has no datatype, assume it is of the same datatype as
			// the right argument
			commonDatatype = rightDatatype;
		}
		else if (rightDatatype == null && leftDatatype != null) {
			// right argument has no datatype, assume it is of the same datatype as
			// the left argument
			commonDatatype = leftDatatype;
		}
		else if (leftDatatype != null && rightDatatype != null) {
			if (leftDatatype.equals(rightDatatype)) {
				commonDatatype = leftDatatype;
			}
			else {
				// left and right arguments have different datatypes, try to find a
				// more general, shared datatype
				if (leftDatatype != null && rightDatatype != null
						&& XMLDatatypeUtil.isNumericDatatype(leftDatatype)
						&& XMLDatatypeUtil.isNumericDatatype(rightDatatype))
				{
					if (leftDatatype.equals(XMLSchema.DOUBLE) || rightDatatype.equals(XMLSchema.DOUBLE)) {
						commonDatatype = XMLSchema.DOUBLE;
					}
					else if (leftDatatype.equals(XMLSchema.FLOAT) || leftDatatype.equals(XMLSchema.FLOAT)) {
						commonDatatype = XMLSchema.FLOAT;
					}
					else if (leftDatatype.equals(XMLSchema.DECIMAL) || leftDatatype.equals(XMLSchema.DECIMAL)) {
						commonDatatype = XMLSchema.DECIMAL;
					}
					else {
						commonDatatype = XMLSchema.INTEGER;
					}
				}
			}
		}

		Integer compareResult = null;

		if ((leftLit.getLanguage() == null && (leftDatatype == null || leftDatatype.equals(XMLSchema.STRING)))
				&& (rightLit.getLanguage() == null && (rightDatatype == null || rightDatatype.equals(XMLSchema.STRING))))
		{
			// Both arguments are either plain literals (i.e. have no language
			// or datatype), or are of type xsd:string. Compare the labels

			// FIXME: this compares plain literals to xsd:string literals, which is
			// not allowed according to SPARQL
			compareResult = leftLit.getLabel().compareTo(rightLit.getLabel());
		}
		else if (commonDatatype != null) {
			if (USE_TYPED_LITERALS) {
				try {
					if (commonDatatype.equals(XMLSchema.DOUBLE)) {
						compareResult = Double.compare(leftLit.doubleValue(), rightLit.doubleValue());
					}
					else if (commonDatatype.equals(XMLSchema.FLOAT)) {
						compareResult = Float.compare(leftLit.floatValue(), rightLit.floatValue());
					}
					else if (commonDatatype.equals(XMLSchema.DECIMAL)) {
						compareResult = leftLit.decimalValue().compareTo(rightLit.decimalValue());
					}
					else if (XMLDatatypeUtil.isIntegerDatatype(commonDatatype)) {
						compareResult = leftLit.integerValue().compareTo(rightLit.integerValue());
					}
					else if (commonDatatype.equals(XMLSchema.BOOLEAN)) {
						Boolean leftBool = Boolean.valueOf(leftLit.booleanValue());
						Boolean rightBool = Boolean.valueOf(rightLit.booleanValue());
						compareResult = leftBool.compareTo(rightBool);
					}
					else if (commonDatatype.equals(XMLSchema.DATETIME)) {
						XMLGregorianCalendar left = leftLit.calendarValue();
						XMLGregorianCalendar right = rightLit.calendarValue();

						compareResult = left.compare(right);

						// Note: XMLGregorianCalendar.compare() returns compatible
						// values
						// (-1, 0, 1) but INDETERMINATE needs special treatment
						if (compareResult == DatatypeConstants.INDETERMINATE) {
							return false;
						}
					}
				}
				catch (IllegalArgumentException e) {
					// One of the basic-type method calls failed
					//throw new BooleanExprEvaluationException(e);
				}
			}
			else {
				compareResult = XMLDatatypeUtil.compare(leftLit.getLabel(), rightLit.getLabel(), commonDatatype);
			}
		}

		if (compareResult != null) {
			// Literals have compatible ordered datatypes
			switch (operator) {
				case LT:
					return compareResult.intValue() < 0;
				case LE:
					return compareResult.intValue() <= 0;
				case EQ:
					return compareResult.intValue() == 0;
				case NE:
					return compareResult.intValue() != 0;
				case GE:
					return compareResult.intValue() >= 0;
				case GT:
					return compareResult.intValue() > 0;
				default:
					throw new IllegalArgumentException("Unknown operator: " + operator);
			}
		}
		else {
			// All other cases, e.g. literals with languages, unequal or
			// unordered datatypes, etc. These arguments can only be compared
			// using the operators 'EQ' and 'NE'. See SPARQL's RDFterm-equal
			// operator
			boolean literalsEqual = leftLit.equals(rightLit);

			if (!literalsEqual && leftDatatype != null && rightDatatype != null) {
				// Literals with unsupported datatypes, we don't know if their
				// values are equal
				throw new BooleanExprEvaluationException("Unable to compare literals with unsupported types");
			}
			else {
				switch (operator) {
					case EQ:
						return literalsEqual;
					case NE:
						return !literalsEqual;
					case LT:
					case LE:
					case GE:
					case GT:
						throw new BooleanExprEvaluationException(
								"Only literals with compatible, ordered datatypes can be compared using <, <=, > and >= operators");
					default:
						throw new IllegalArgumentException("Unknown operator: " + operator);
				}
			}
		}
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

	private BindingSet _bindings;

	private CloseableIteration<BindingSet, QueryEvaluationException> _evaluate;

	private Boolean _isTrue;

	private TripleSource _tripleSource;

	private Value _value;

	public EvaluationStrategyImpl(TripleSource tripleSource) {
		_tripleSource = tripleSource;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(TupleExpr expr,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		BindingSet stack_bindings = _bindings;
		try {
			_bindings = bindings;
			expr.visit(this);
			return _evaluate;
		}
		finally {
			_bindings = stack_bindings;
			_evaluate = null;
		}
	}

	public Value getValue(ValueExpr expr, BindingSet bindings) {
		BindingSet stack_bindings = _bindings;
		try {
			_bindings = bindings;
			expr.visit(this);
			if (_value == null && _isTrue != null) {
				ValueFactory vf = _tripleSource.getValueFactory();
				_value = vf.createLiteral(_isTrue.booleanValue());
			}
			return _value;
		}
		catch (BooleanExprEvaluationException e) {
			logger.debug("Value could not be determined due to a problem in evaluating a boolean expression", e);
			return null;
		}
		catch (QueryEvaluationException e) {
			logger.debug("Value could not be determined due to an error in an internal query", e);
			return null;
		}
		finally {
			_bindings = stack_bindings;
			_value = null;
			_isTrue = null;
		}
	}

	public boolean isTrue(ValueExpr expr, BindingSet bindings)
		throws BooleanExprEvaluationException
	{
		BindingSet stack_bindings = _bindings;
		try {
			_bindings = bindings;
			expr.visit(this);
			if (_value != null && _isTrue == null) {
				_isTrue = _getEffectiveBooleanValue(_value);
			}
			return _isTrue;
		}
		catch (QueryEvaluationException e) {
			logger.debug("Expression could not be evaluated due to an error in an internal query", e);
			throw new BooleanExprEvaluationException(e);
		}
		finally {
			_bindings = stack_bindings;
			_value = null;
			_isTrue = null;
		}
	}

	public void meet(And node)
		throws QueryEvaluationException
	{
		try {
			if (!isTrue(node.getLeftArg(), _bindings)) {
				// Left argument evaluates to false, we don't need to look any
				// further
				_isTrue = Boolean.FALSE;
				return;
			}
		}
		catch (BooleanExprEvaluationException e) {
			// Failed to evaluate the left argument. Result is 'false' when
			// the right argument evaluates to 'false', failure otherwise.
			if (!isTrue(node.getRightArg(), _bindings)) {
				_isTrue = Boolean.FALSE;
				return;
			}
			else {
				throw new BooleanExprEvaluationException();
			}
		}

		// Left argument evaluated to 'true', result is determined
		// by the evaluation of the right argument.
		_isTrue = isTrue(node.getRightArg(), _bindings);
	}

	public void meet(BNodeGenerator node)
		throws QueryEvaluationException
	{
		_value = _tripleSource.getValueFactory().createBNode();
	}

	public void meet(Compare node)
		throws QueryEvaluationException
	{
		Value leftVal = getValue(node.getLeftArg(), _bindings);
		Value rightVal = getValue(node.getRightArg(), _bindings);

		_isTrue = _compare(leftVal, rightVal, node.getOperator());
	}

	public void meet(CompareAll node)
		throws QueryEvaluationException
	{
		Value leftValue = getValue(node.getArg(), _bindings);

		// Result is true until a mismatch has been found
		boolean result = true;

		// Use first binding name from tuple expr to compare values
		String bindingName = node.getSubQuery().getBindingNames().iterator().next();

		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluate(node.getSubQuery(), _bindings);
		try {
			while (result == true && iter.hasNext()) {
				BindingSet bindingSet = iter.next();

				Value rightValue = bindingSet.getValue(bindingName);

				try {
					result = _compare(leftValue, rightValue, node.getOperator());
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

	public void meet(CompareAny node)
		throws QueryEvaluationException
	{
		Value leftValue = getValue(node.getArg(), _bindings);

		// Result is false until a match has been found
		boolean result = false;

		// Use first binding name from tuple expr to compare values
		String bindingName = node.getSubQuery().getBindingNames().iterator().next();

		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluate(node.getSubQuery(), _bindings);
		try {
			while (result == false && iter.hasNext()) {
				BindingSet bindingSet = iter.next();

				Value rightValue = bindingSet.getValue(bindingName);

				try {
					result = _compare(leftValue, rightValue, node.getOperator());
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

	public void meet(Count node)
		throws QueryEvaluationException
	{
		// do nothing
	}

	public void meet(Max node)
		throws QueryEvaluationException
	{
		// do nothing
	}

	public void meet(Min node)
		throws QueryEvaluationException
	{
		// do nothing
	}

	public void meet(final Difference difference) {
		final BindingSet bindings = _bindings;
		Iteration<BindingSet, QueryEvaluationException> leftArg, rightArg;

		leftArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(difference.getLeftArg(), bindings);
			}
		};

		rightArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(difference.getRightArg(), bindings);
			}
		};

		_evaluate = new MinusIteration<BindingSet, QueryEvaluationException>(leftArg, rightArg);
	}

	public void meet(Distinct distinct)
		throws QueryEvaluationException
	{
		_evaluate = new DistinctIteration<BindingSet, QueryEvaluationException>(evaluate(distinct.getArg(),
				_bindings));
	}

	public void meet(EmptySet emptySet) {
		_evaluate = new EmptyIteration<BindingSet, QueryEvaluationException>();
	}

	public void meet(Exists node)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluate(node.getSubQuery(), _bindings);
		try {
			_isTrue = iter.hasNext();
		}
		finally {
			iter.close();
		}
	}

	public void meet(Extension extension)
		throws QueryEvaluationException
	{
		_evaluate = new ExtensionIterator(this, extension, _bindings);
	}

	public void meet(ExtensionElem node)
		throws QueryEvaluationException
	{
	}

	public void meet(Group node)
		throws QueryEvaluationException
	{
		_evaluate = new GroupIterator(this, node, _bindings);
	}

	public void meet(In node)
		throws QueryEvaluationException
	{
		Value leftValue = getValue(node.getArg(), _bindings);

		// Result is false until a match has been found
		boolean result = false;

		// Use first binding name from tuple expr to compare values
		String bindingName = node.getSubQuery().getBindingNames().iterator().next();

		CloseableIteration<BindingSet, QueryEvaluationException> iter = evaluate(node.getSubQuery(), _bindings);
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

		_isTrue = result;
	}

	public void meet(final Intersection intersection) {
		Iteration<BindingSet, QueryEvaluationException> leftArg, rightArg;

		leftArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(intersection.getLeftArg(), _bindings);
			}
		};

		rightArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(intersection.getRightArg(), _bindings);
			}
		};

		_evaluate = new IntersectIteration<BindingSet, QueryEvaluationException>(leftArg, rightArg);
	}

	/**
	 * Determines whether the operand (a variable) contains a BNode.
	 * 
	 * @return <tt>true</tt> if the operand contains a BNode, <tt>false</tt>
	 *         otherwise.
	 */
	public void meet(IsBNode node)
		throws QueryEvaluationException
	{
		_isTrue = getValue(node.getArg(), _bindings) instanceof BNode;
	}

	/**
	 * Determines whether the operand (a variable) contains a Literal.
	 * 
	 * @return <tt>true</tt> if the operand contains a Literal, <tt>false</tt>
	 *         otherwise.
	 */
	public void meet(IsLiteral node)
		throws QueryEvaluationException
	{
		_isTrue = getValue(node.getArg(), _bindings) instanceof Literal;
	}

	/**
	 * Determines whether the operand (a variable) contains a Resource.
	 * 
	 * @return <tt>true</tt> if the operand contains a Resource, <tt>false</tt>
	 *         otherwise.
	 */
	public void meet(IsResource node)
		throws QueryEvaluationException
	{
		_isTrue = getValue(node.getArg(), _bindings) instanceof Resource;
	}

	/**
	 * Determines whether the operand (a variable) contains a URI.
	 * 
	 * @return <tt>true</tt> if the operand contains a URI, <tt>false</tt>
	 *         otherwise.
	 */
	public void meet(IsURI node)
		throws QueryEvaluationException
	{
		_isTrue = getValue(node.getArg(), _bindings) instanceof URI;
	}

	public void meet(Join join)
		throws QueryEvaluationException
	{
		_evaluate = new JoinIterator(this, join, _bindings);
	}

	/**
	 * Determines whether the two operands match according to the
	 * <code>regex</code> operator.
	 * 
	 * @return <tt>true</tt> if the operands match according to the
	 *         <tt>regex</tt> operator, <tt>false</tt> otherwise.
	 */
	public void meet(Regex node)
		throws QueryEvaluationException
	{
		Value val = getValue(node.getArg(), _bindings);
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

		_isTrue = node.getOpPattern().matcher(strVal).find();
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
	public void meet(Like node)
		throws QueryEvaluationException
	{
		Value val = getValue(node.getArg(), _bindings);
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

	public void meet(MathExpr node)
		throws QueryEvaluationException
	{
		// Do the math
		Value leftVal = getValue(node.getLeftArg(), _bindings);
		Value rightVal = getValue(node.getRightArg(), _bindings);

		if (leftVal instanceof Literal && rightVal instanceof Literal) {
			_value = getValue((Literal)leftVal, (Literal)rightVal, node.getOperator());
			return;
		}

		_value = null;
	}

	public void meet(MultiProjection multiProjection)
		throws QueryEvaluationException
	{
		_evaluate = new MultiProjectionIterator(this.evaluate(multiProjection.getArg(), _bindings),
				multiProjection, _bindings);
	}

	public void meet(Not node)
		throws QueryEvaluationException
	{
		_isTrue = !isTrue(node.getArg(), _bindings);
	}

	public void meet(Null node)
		throws QueryEvaluationException
	{
		_value = null;
	}

	public void meet(OptionalJoin optionalJoin)
		throws QueryEvaluationException
	{
		_evaluate = new OptionalJoinIterator(this, optionalJoin, _bindings);
	}

	public void meet(Or node)
		throws QueryEvaluationException
	{
		try {
			if (isTrue(node.getLeftArg(), _bindings)) {
				// Left argument evaluates to true, we don't need to look any
				// further
				_isTrue = true;
				return;
			}
		}
		catch (BooleanExprEvaluationException e) {
			// Failed to evaluate the left argument. Result is 'true' when
			// the right argument evaluates to 'true', failure otherwise.
			if (isTrue(node.getRightArg(), _bindings)) {
				_isTrue = true;
				return;
			}
			else {
				throw new BooleanExprEvaluationException();
			}
		}

		// Left argument evaluated to 'false', result is determined
		// by the evaluation of the right argument.
		_isTrue = isTrue(node.getRightArg(), _bindings);
	}

	public void meet(Projection projection)
		throws QueryEvaluationException
	{
		_evaluate = new ProjectionIterator(this, projection, _bindings);
	}

	public void meet(ProjectionElem node)
		throws QueryEvaluationException
	{
	}

	public void meet(RowSelection rowSelection)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> result = evaluate(rowSelection.getArg(),
				_bindings);

		if (rowSelection.hasOffset()) {
			result = new OffsetIteration<BindingSet, QueryEvaluationException>(result, rowSelection.getOffset());
		}

		if (rowSelection.hasLimit()) {
			result = new LimitIteration<BindingSet, QueryEvaluationException>(result, rowSelection.getLimit());
		}

		_evaluate = result;
	}

	public void meet(Selection selection)
		throws QueryEvaluationException
	{
		_evaluate = new SelectionIterator(this, selection, _bindings);
	}

	public void meet(SingletonSet singletonSet) {
		_evaluate = new SingletonIteration<BindingSet, QueryEvaluationException>(_bindings);
	}

	public void meet(StatementPattern sp)
		throws QueryEvaluationException
	{
		final BindingSet bindings = _bindings;

		final Var subjVar = sp.getSubjectVar();
		final Var predVar = sp.getPredicateVar();
		final Var objVar = sp.getObjectVar();
		final Var conVar = sp.getContextVar();

		Value subjValue = _getVarValue(subjVar, bindings);
		Value predValue = _getVarValue(predVar, bindings);
		Value objValue = _getVarValue(objVar, bindings);
		Value contextValue = _getVarValue(conVar, bindings);

		CloseableIteration<? extends Statement, QueryEvaluationException> stIter = null;

		try {
			if (sp.getScope() == Scope.ALL_CONTEXTS) {
				stIter = _tripleSource.getStatements((Resource)subjValue, (URI)predValue, objValue);
			}
			else if (sp.getScope() == Scope.NULL_CONTEXT) {
				stIter = _tripleSource.getStatements((Resource)subjValue, (URI)predValue, objValue,
						(Resource)null);
			}
			else if (sp.getScope() == Scope.NAMED_CONTEXTS) {
				if (contextValue == null) {
					// we match all named contexts by simply retrieving all
					// statements from the store and filtering out the
					// statements that do not have a context.
					stIter = new FilterIteration<Statement, QueryEvaluationException>(_tripleSource.getStatements(
							(Resource)subjValue, (URI)predValue, objValue))
					{

						@Override
						protected boolean accept(Statement st)
						{
							return st.getContext() != null;
						}

					}; // end anonymous class
				}
				else { // a named context is specified
					stIter = _tripleSource.getStatements((Resource)subjValue, (URI)predValue, objValue,
							(Resource)contextValue);
				}
			}
			else {
				throw new RuntimeException("Unknown statement pattern scope " + sp.getScope());
			}
		}
		catch (ClassCastException e) {
			// Invalid value type for subject, predicate and/or context
			stIter = new EmptyIteration<Statement, QueryEvaluationException>();
		}

		// The same variable might have been used multiple times in this
		// StatementPattern, verify value equality in those cases.
		stIter = new FilterIteration<Statement, QueryEvaluationException>(stIter) {

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
		_evaluate = new ConvertingIteration<Statement, BindingSet, QueryEvaluationException>(stIter) {

			@Override
			protected BindingSet convert(Statement st)
			{
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

	public void meet(DatatypeFunc node)
		throws QueryEvaluationException
	{
		Value v = getValue(node.getArg(), _bindings);

		if (v instanceof Literal) {
			_value = ((Literal)v).getDatatype();
			return;
		}

		_value = null;
	}

	public void meet(StrFunc node)
		throws QueryEvaluationException
	{
		Value value = getValue(node.getArg(), _bindings);

		if (value instanceof URI) {
			_value = _tripleSource.getValueFactory().createLiteral(value.toString());
		}
		else if (value instanceof Literal) {
			Literal literal = (Literal)value;

			if (literal.getLanguage() == null && literal.getDatatype() == null) {
				_value = literal;
			}
			else {
				_value = _tripleSource.getValueFactory().createLiteral(literal.getLabel());
			}
		}
		else {
			_value = null;
		}
	}

	public void meet(LabelFunc node)
		throws QueryEvaluationException
	{
		Value value = getValue(node.getArg(), _bindings);

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

	public void meet(LangFunc node)
		throws QueryEvaluationException
	{
		Value value = getValue(node.getArg(), _bindings);

		if (value instanceof Literal) {
			Literal literal = (Literal)value;

			if (literal.getLanguage() != null) {
				_value = _tripleSource.getValueFactory().createLiteral(literal.getLanguage());
				return;
			}
		}

		_value = null;
	}

	public void meet(LocalNameFunc node)
		throws QueryEvaluationException
	{
		Value value = getValue(node.getArg(), _bindings);

		if (value instanceof URI) {
			URI uri = (URI)value;
			_value = _tripleSource.getValueFactory().createLiteral(uri.getLocalName());
			return;
		}

		_value = null;
	}

	public void meet(NamespaceFunc node)
		throws QueryEvaluationException
	{
		Value value = getValue(node.getArg(), _bindings);

		if (value instanceof URI) {
			URI uri = (URI)value;
			_value = _tripleSource.getValueFactory().createURI(uri.getNamespace());
			return;
		}

		_value = null;
	}

	@SuppressWarnings("unchecked")
	public void meet(final Union union)
	{
		final BindingSet bindings = _bindings;
		Iteration<BindingSet, QueryEvaluationException> leftArg, rightArg;

		leftArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(union.getLeftArg(), bindings);
			}
		};

		rightArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(union.getRightArg(), bindings);
			}
		};

		_evaluate = new UnionIteration<BindingSet, QueryEvaluationException>(leftArg, rightArg);
	}

	public void meet(ValueConstant node)
		throws QueryEvaluationException
	{
		_value = node.getValue();
	}

	public void meet(Var node)
		throws QueryEvaluationException
	{
		if (node.getValue() != null) {
			_value = node.getValue();
		}
		else {
			_value = _bindings.getValue(node.getName());
		}
	}

	private Boolean _getEffectiveBooleanValue(Value value) {
		if (value instanceof Literal) {
			Literal literal = (Literal)value;
			String label = literal.getLabel();
			URI datatype = literal.getDatatype();

			if (datatype == null || datatype.equals(XMLSchema.STRING)) {
				return label.length() > 0;
			}
			else if (datatype.equals(XMLSchema.BOOLEAN)) {
				if ("true".equals(label) || "1".equals(label)) {
					return Boolean.TRUE;
				}
				else if ("false".equals(label) || "0".equals(label)) {
					return Boolean.FALSE;
				}
			}
			else if (datatype.equals(XMLSchema.DECIMAL)) {
				try {
					String normDec = XMLDatatypeUtil.normalizeDecimal(label);
					return !normDec.equals("0.0");
				}
				catch (IllegalArgumentException e) {
					// type error, don't set _isTrue
				}
			}
			else if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
				try {
					String normInt = XMLDatatypeUtil.normalize(label, datatype);
					return !normInt.equals("0");
				}
				catch (IllegalArgumentException e) {
					// type error, don't set _isTrue
				}
			}
			else if (XMLDatatypeUtil.isFloatingPointDatatype(datatype)) {
				try {
					String normFP = XMLDatatypeUtil.normalize(label, datatype);
					return !normFP.equals("0.0E0") && !normFP.equals("NaN");
				}
				catch (IllegalArgumentException e) {
					// type error, don't set _isTrue
				}
			}
		}
		return null;
	}

	private Value _getVarValue(Var var, BindingSet bindings)
		throws QueryEvaluationException
	{
		if (var == null) {
			return null;
		}
		else {
			return getValue(var, bindings);
		}
	}
}
