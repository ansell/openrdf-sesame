/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.query.algebra;

/**
 * An interface for query model visitors, implementing the Visitor pattern. Core
 * query model nodes will call their type-specific method when
 * {@link QueryModelNode#visit(QueryModelVisitor)} is called. The method
 * {@link #meetOther(QueryModelNode)} is provided as a hook for foreign query
 * model nodes.
 */
public interface QueryModelVisitor<X extends Exception> {

	public void meet(QueryRoot node)
		throws X;


	public void meet(Add add)
			throws X;


	public void meet(And node)
		throws X;

	public void meet(ArbitraryLengthPath node)
		throws X;

	public void meet(Avg node)
		throws X;

	public void meet(BindingSetAssignment node)
		throws X;

	public void meet(BNodeGenerator node)
		throws X;
	
	public void meet(Bound node)
		throws X;

	public void meet(Clear clear)
			throws X;

	public void meet(Coalesce node)
		throws X;

	public void meet(Compare node)
		throws X;

	public void meet(CompareAll node)
		throws X;

	public void meet(CompareAny node)
		throws X;

	public void meet(DescribeOperator node)
			throws X;

	public void meet(Copy copy)
			throws X;

	public void meet(Count node)
		throws X;

	public void meet(Create create)
			throws X;

	public void meet(Datatype node)
		throws X;

	public void meet(DeleteData deleteData)
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

	public void meet(Filter node)
		throws X;

	public void meet(FunctionCall node)
		throws X;

	public void meet(Group node)
		throws X;

	public void meet(GroupConcat node)
		throws X;

	public void meet(GroupElem node)
		throws X;

	public void meet(If node)
		throws X;

	public void meet(In node)
		throws X;

	public void meet(InsertData insertData)
			throws X;

	public void meet(Intersection node)
		throws X;

	public void meet(IRIFunction node)
		throws X;

	public void meet(IsBNode node)
		throws X;

	public void meet(IsLiteral node)
		throws X;

	public void meet(IsNumeric node)
		throws X;

	public void meet(IsResource node)
		throws X;

	public void meet(IsURI node)
		throws X;

	public void meet(Join node)
		throws X;

	public void meet(Label node)
		throws X;

	public void meet(Lang node)
		throws X;

	public void meet(LangMatches node)
		throws X;

	public void meet(LeftJoin node)
		throws X;

	public void meet(Like node)
		throws X;

	public void meet(Load load)
			throws X;

	public void meet(LocalName node)
		throws X;

	public void meet(MathExpr node)
		throws X;

	public void meet(Max node)
		throws X;

	public void meet(Min node)
		throws X;

	public void meet(Modify modify)
			throws X;

	public void meet(Move move)
			throws X;

	public void meet(MultiProjection node)
		throws X;

	public void meet(Namespace node)
		throws X;

	public void meet(Not node)
		throws X;

	public void meet(Or node)
		throws X;

	public void meet(Order node)
		throws X;

	public void meet(OrderElem node)
		throws X;

	public void meet(Projection node)
		throws X;
	
	public void meet(ProjectionElem node)
		throws X;

	public void meet(ProjectionElemList node)
		throws X;

	public void meet(Reduced node)
		throws X;

	public void meet(Regex node)
		throws X;

	public void meet(SameTerm node)
		throws X;

	public void meet(Sample node)
		throws X;

	public void meet(Service node)
		throws X;

	public void meet(SingletonSet node)
		throws X;

	public void meet(Slice node)
		throws X;

	public void meet(StatementPattern node)
		throws X;

	public void meet(Str node)
		throws X;

	public void meet(Sum node)
		throws X;

	public void meet(Union node)
		throws X;

	public void meet(ValueConstant node)
		throws X;

	/**
	 * @since 2.7.4
	 */
	public void meet(ListMemberOperator node)
			throws X;

	public void meet(Var node)
		throws X;

	public void meet(ZeroLengthPath node)
		throws X;

	public void meetOther(QueryModelNode node)
		throws X;
}
