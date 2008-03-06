/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.helpers.QueryModelNodeReplacer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.impl.MapBindingSet;

/**
 * A query optimizer that in-lines Vars.
 * 
 * @author James Leigh <james@leighnet.ca>
 */
public class BindingsFinder extends QueryModelVisitorBase<RuntimeException> {

	/*----------------*
	 * Static methods *
	 *----------------*/

	/**
	 * Applies generally applicable optimizations to the supplied query: variable
	 * assignments are inlined.
	 * 
	 * @param tupleExpr
	 * @return optimized TupleExpr
	 */
	public static BindingSet findBindings(QueryModelNode qmNode, BindingSet bindings) {
		BindingsFinder bindingsFinder = new BindingsFinder(bindings);
		qmNode.visit(bindingsFinder);
		return bindingsFinder.getBindings();
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean _optional;

	private MapBindingSet _bindings;

	private QueryModelNodeReplacer _replacer;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BindingsFinder(BindingSet bindings) {
		_optional = false;
		_bindings = new MapBindingSet();
		_replacer = new QueryModelNodeReplacer();

		for (Binding binding : bindings) {
			_bindings.addBinding(binding);
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	public BindingSet getBindings() {
		return _bindings;
	}

	@Override
	public void meet(Or node)
	{
		boolean optimizeConstraints = _optional;
		_optional = true;
		super.meet(node);
		_optional = optimizeConstraints;
	}

	@Override
	public void meet(Not node)
	{
		boolean optimizeConstraints = _optional;
		_optional = true;
		super.meet(node);
		_optional = optimizeConstraints;
	}

	@Override
	public void meet(Var node)
	{
		if (node.getValue() != null) {
			_bindings.addBinding(node.getName(), node.getValue());
		}
	}

	@Override
	public void meet(Compare node)
	{
		super.meet(node);
		if (!_optional && node.getOperator() == CompareOp.EQ) {
			ValueExpr arg1 = node.getLeftArg();
			ValueExpr arg2 = node.getRightArg();

			Value value1 = _getConstantValue(arg1);
			Value value2 = _getConstantValue(arg2);

			Var varArg = null;
			Value value = null;

			if (arg1 instanceof Var && value1 == null && value2 != null) {
				// arg2 has a value
				varArg = (Var)arg1;
				value = value2;
			}
			else if (arg2 instanceof Var && value2 == null && value1 != null) {
				// arg1 has a value
				varArg = (Var)arg2;
				value = value1;
			}

			// Inline variable assignment, unless the value is a datatyped literal;
			// datatyped literals need value processing
			if (varArg != null && (!(value instanceof Literal) || ((Literal)value).getDatatype() == null)) {
				varArg.setValue(value);
				_bindings.addBinding(varArg.getName(), value);

				// Remove the (now redundant) constraint
				_replacer.removeNode(node);
			}
		}
	}

	private Value _getConstantValue(ValueExpr v) {
		if (v instanceof ValueConstant) {
			return ((ValueConstant)v).getValue();
		}
		if (v instanceof Var) {
			return ((Var)v).getValue();
		}
		return null;
	}
}
