/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.helpers;

import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.CompareAll;
import org.openrdf.query.algebra.CompareAny;
import org.openrdf.query.algebra.CompareSubQueryValueOperator;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.In;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
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
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Namespace;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.SubQueryValueOperator;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.UnaryValueOperator;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.Var;

/**
 * Base class for {@link QueryModelVisitor}s. This class implements all
 * <tt>meet(... node)</tt> methods from the visitor interface, forwarding the
 * call to a method for the node's supertype. This is done recursively until
 * {@link #meetNode} is reached. This allows subclasses to easily define default
 * behaviour for visited nodes of a certain type. The default implementation of
 * {@link #meetDefault} is to visit the node's children.
 */
public abstract class QueryModelVisitorBase<X extends Exception> implements QueryModelVisitor<X> {

	public void meet(And node)
		throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(BNodeGenerator node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Bound node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Compare node)
		throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(CompareAll node)
		throws X
	{
		meetCompareSubQueryValueOperator(node);
	}

	public void meet(CompareAny node)
		throws X
	{
		meetCompareSubQueryValueOperator(node);
	}

	public void meet(Count node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Datatype node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Difference node)
		throws X
	{
		meetBinaryTupleOperator(node);
	}

	public void meet(Distinct node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(EmptySet node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Exists node)
		throws X
	{
		meetSubQueryValueOperator(node);
	}

	public void meet(Extension node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(ExtensionElem node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Filter node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(FunctionCall node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Group node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(GroupElem node)
		throws X
	{
		meetNode(node);
	}

	public void meet(In node)
		throws X
	{
		meetCompareSubQueryValueOperator(node);
	}

	public void meet(Intersection node)
		throws X
	{
		meetBinaryTupleOperator(node);
	}

	public void meet(IsBNode node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(IsLiteral node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(IsResource node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(IsURI node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Join node)
		throws X
	{
		meetBinaryTupleOperator(node);
	}

	public void meet(Label node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Lang node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(LangMatches node)
		throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(LeftJoin node)
		throws X
	{
		meetBinaryTupleOperator(node);
	}

	public void meet(LocalName node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Like node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(MathExpr node)
		throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(Max node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Min node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(MultiProjection node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(Namespace node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Not node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Or node)
		throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(Order node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(OrderElem node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Projection node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(ProjectionElem node)
		throws X
	{
		meetNode(node);
	}

	public void meet(ProjectionElemList node)
		throws X
	{
		meetNode(node);
	}

	public void meet(QueryRoot node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Regex node)
		throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(Slice node)
		throws X
	{
		meetUnaryTupleOperator(node);
	}

	public void meet(SameTerm node)
		throws X
	{
		meetBinaryValueOperator(node);
	}

	public void meet(SingletonSet node)
		throws X
	{
		meetNode(node);
	}

	public void meet(StatementPattern node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Str node)
		throws X
	{
		meetUnaryValueOperator(node);
	}

	public void meet(Union node)
		throws X
	{
		meetBinaryTupleOperator(node);
	}

	public void meet(ValueConstant node)
		throws X
	{
		meetNode(node);
	}

	public void meet(Var node)
		throws X
	{
		meetNode(node);
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a
	 * {@link UnaryTupleOperator} node as argument. Forwards the call to
	 * {@link #meetTupleExpr} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetUnaryTupleOperator(UnaryTupleOperator node)
		throws X
	{
		meetNode(node);
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a
	 * {@link BinaryTupleOperator} node as argument. Forwards the call to
	 * {@link #meetTupleExpr} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetBinaryTupleOperator(BinaryTupleOperator node)
		throws X
	{
		meetNode(node);
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a
	 * {@link CompareSubQueryValueOperator} node as argument. Forwards the call
	 * to {@link #meetSubQueryValueOperator} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetCompareSubQueryValueOperator(CompareSubQueryValueOperator node)
		throws X
	{
		meetSubQueryValueOperator(node);
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a
	 * {@link SubQueryValueOperator} node as argument. Forwards the call to
	 * {@link #meetValueExpr} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetSubQueryValueOperator(SubQueryValueOperator node)
		throws X
	{
		meetNode(node);
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a
	 * {@link UnaryValueOperator} node as argument. Forwards the call to
	 * {@link #meetValueExpr} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetUnaryValueOperator(UnaryValueOperator node)
		throws X
	{
		meetNode(node);
	}

	/**
	 * Method called by all <tt>meet</tt> methods with a
	 * {@link BinaryValueOperator} node as argument. Forwards the call to
	 * {@link #meetValueExpr} by default.
	 * 
	 * @param node
	 *        The node that is being visited.
	 * @throws
	 */
	protected void meetBinaryValueOperator(BinaryValueOperator node)
		throws X
	{
		meetNode(node);
	}

	/**
	 * Method called by all of the other <tt>meet</tt> methods that are not
	 * overridden in subclasses. This method can be overridden in subclasses to
	 * define default behaviour when visiting nodes. The default behaviour of
	 * this method is to visit the node's children.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetNode(QueryModelNode node)
		throws X
	{
		node.visitChildren(this);
	}
}
