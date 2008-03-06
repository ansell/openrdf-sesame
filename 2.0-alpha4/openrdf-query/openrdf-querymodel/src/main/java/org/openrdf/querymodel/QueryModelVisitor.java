/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

/**
 * 
 */
public interface QueryModelVisitor {

	public void meet(And node);

	public void meet(BNodeGenerator node);

	public void meet(BooleanConstant node);

	public void meet(Compare node);

	public void meet(CompareAll node);

	public void meet(CompareAny node);

	public void meet(Datatype node);

	public void meet(MultiProjection node);

	public void meet(Difference node);

	public void meet(Distinct node);

	public void meet(EffectiveBooleanValue node);

	public void meet(EmptySet node);

	public void meet(Exists node);

	public void meet(Extension node);

	public void meet(ExtensionElem node);

	public void meet(In node);

	public void meet(Intersection node);

	public void meet(IsBNode node);

	public void meet(IsLiteral node);

	public void meet(IsResource node);

	public void meet(IsURI node);

	public void meet(Join node);

	public void meet(Label node);

	public void meet(Lang node);

	public void meet(Like node);

	public void meet(LocalName node);

	public void meet(MathExpr node);

	public void meet(Namespace node);

	public void meet(Not node);

	public void meet(Null node);

	public void meet(OptionalJoin node);

	public void meet(Or node);

	public void meet(Projection node);

	public void meet(ProjectionElem node);

	public void meet(RowSelection node);

	public void meet(Selection node);

	public void meet(SingletonSet node);

	public void meet(StatementPattern node);

	public void meet(Union node);

	public void meet(ValueConstant node);

	public void meet(Var node);
}
