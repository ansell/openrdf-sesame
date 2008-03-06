/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * 
 */
public interface QueryModelVisitor<X extends Exception> {

	public void meet(And node)
		throws X;

	public void meet(BNodeGenerator node)
		throws X;

	public void meet(Compare node)
		throws X;

	public void meet(CompareAll node)
		throws X;

	public void meet(CompareAny node)
		throws X;

	public void meet(Count node)
		throws X;

	public void meet(DatatypeFunc node)
		throws X;

	public void meet(Difference node)
		throws X;

	public void meet(Distinct node)
		throws X;

	public void meet(EmptySet node)
		throws X;

	public void meet(Exists node)
		throws X;

	public void meet(Extension node)
		throws X;

	public void meet(ExtensionElem node)
		throws X;

	public void meet(Group node)
		throws X;

	public void meet(In node)
		throws X;

	public void meet(Intersection node)
		throws X;

	public void meet(IsBNode node)
		throws X;

	public void meet(IsLiteral node)
		throws X;

	public void meet(IsResource node)
		throws X;

	public void meet(IsURI node)
		throws X;

	public void meet(Join node)
		throws X;

	public void meet(LabelFunc node)
		throws X;

	public void meet(LangFunc node)
		throws X;

	public void meet(Like node)
		throws X;

	public void meet(LocalNameFunc node)
		throws X;

	public void meet(MathExpr node)
		throws X;

	public void meet(Max node)
		throws X;

	public void meet(Min node)
		throws X;

	public void meet(MultiProjection node)
		throws X;

	public void meet(NamespaceFunc node)
		throws X;

	public void meet(Not node)
		throws X;

	public void meet(Null node)
		throws X;

	public void meet(OptionalJoin node)
		throws X;

	public void meet(Or node)
		throws X;

	public void meet(Projection node)
		throws X;

	public void meet(ProjectionElem node)
		throws X;

	public void meet(Regex node)
		throws X;

	public void meet(RowSelection node)
		throws X;

	public void meet(Selection node)
		throws X;

	public void meet(SingletonSet node)
		throws X;

	public void meet(StatementPattern node)
		throws X;

	public void meet(StrFunc node)
		throws X;

	public void meet(Union node)
		throws X;

	public void meet(ValueConstant node)
		throws X;

	public void meet(Var node)
		throws X;
}
