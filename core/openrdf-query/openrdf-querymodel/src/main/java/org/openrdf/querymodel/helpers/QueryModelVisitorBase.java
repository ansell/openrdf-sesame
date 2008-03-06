/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel.helpers;

import org.openrdf.querymodel.And;
import org.openrdf.querymodel.BNodeGenerator;
import org.openrdf.querymodel.BooleanConstant;
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
import org.openrdf.querymodel.QueryModelNode;
import org.openrdf.querymodel.QueryModelVisitor;
import org.openrdf.querymodel.RowSelection;
import org.openrdf.querymodel.Selection;
import org.openrdf.querymodel.SingletonSet;
import org.openrdf.querymodel.StatementPattern;
import org.openrdf.querymodel.Union;
import org.openrdf.querymodel.ValueConstant;
import org.openrdf.querymodel.Var;

/**
 * Base class for {@link QueryModelVisitor}s. This class implements all methods
 * from the visitor interface, forwarding the method call to
 * {@link #meetDefault}. This allows subclasses to easily define default
 * behaviour for visited nodes. The default implementation of
 * {@link #meetDefault} is to visit the node's children.
 */
public class QueryModelVisitorBase implements QueryModelVisitor {

	/**
	 * Method called by all of the other <tt>meet</tt> methods that are not
	 * overridden in subclasses. This method can be overridden in subclasses to
	 * define default behaviour when visiting nodes. The default behaviour of
	 * this method is to visit the node's children.
	 * 
	 * @param node
	 *        The node that is being visited.
	 */
	protected void meetDefault(QueryModelNode node) {
		node.visitChildren(this);
	}

	public void meet(And node) {
		meetDefault(node);
	}

	public void meet(BNodeGenerator node) {
		meetDefault(node);
	}

	public void meet(BooleanConstant node) {
		meetDefault(node);
	}

	public void meet(Compare node) {
		meetDefault(node);
	}

	public void meet(CompareAll node) {
		meetDefault(node);
	}

	public void meet(CompareAny node) {
		meetDefault(node);
	}

	public void meet(Datatype node) {
		meetDefault(node);
	}

	public void meet(MultiProjection node) {
		meetDefault(node);
	}

	public void meet(Distinct node) {
		meetDefault(node);
	}

	public void meet(EffectiveBooleanValue node) {
		meetDefault(node);
	}

	public void meet(EmptySet node) {
		meetDefault(node);
	}

	public void meet(Exists node) {
		meetDefault(node);
	}

	public void meet(Extension node) {
		meetDefault(node);
	}

	public void meet(ExtensionElem node) {
		meetDefault(node);
	}

	public void meet(In node) {
		meetDefault(node);
	}

	public void meet(Intersection node) {
		meetDefault(node);
	}

	public void meet(IsBNode node) {
		meetDefault(node);
	}

	public void meet(IsLiteral node) {
		meetDefault(node);
	}

	public void meet(IsResource node) {
		meetDefault(node);
	}

	public void meet(IsURI node) {
		meetDefault(node);
	}

	public void meet(Join node) {
		meetDefault(node);
	}

	public void meet(Label node) {
		meetDefault(node);
	}

	public void meet(Lang node) {
		meetDefault(node);
	}

	public void meet(Like node) {
		meetDefault(node);
	}

	public void meet(LocalName node) {
		meetDefault(node);
	}

	public void meet(MathExpr node) {
		meetDefault(node);
	}

	public void meet(Difference node) {
		meetDefault(node);
	}

	public void meet(Namespace node) {
		meetDefault(node);
	}

	public void meet(Not node) {
		meetDefault(node);
	}

	public void meet(Null node) {
		meetDefault(node);
	}

	public void meet(OptionalJoin node) {
		meetDefault(node);
	}

	public void meet(Or node) {
		meetDefault(node);
	}

	public void meet(Projection node) {
		meetDefault(node);
	}

	public void meet(ProjectionElem node) {
		meetDefault(node);
	}

	public void meet(ValueConstant node) {
		meetDefault(node);
	}

	public void meet(RowSelection node) {
		meetDefault(node);
	}

	public void meet(Selection node) {
		meetDefault(node);
	}

	public void meet(SingletonSet node) {
		meetDefault(node);
	}

	public void meet(StatementPattern node) {
		meetDefault(node);
	}

	public void meet(Union node) {
		meetDefault(node);
	}

	public void meet(Var node) {
		meetDefault(node);
	}
}
