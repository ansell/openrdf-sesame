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
package org.eclipse.rdf4j.queryrender.sparql;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.query.algebra.Difference;
import org.eclipse.rdf4j.query.algebra.Filter;
import org.eclipse.rdf4j.query.algebra.Intersection;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.LeftJoin;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Union;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;

/**
 * <p>
 * Visitor implementation for the sesame query algebra which walks the tree and
 * figures out the context for nodes in the algebra. The context for a node is
 * set on the highest node in the tree. That is, everything below it shares the
 * same context.
 * </p>
 * 
 * @author Blazej Bulka
 * @since 2.7.0
 */
public class ContextCollector extends AbstractQueryModelVisitor<Exception> {

	/**
	 * Maps TupleExpr to contexts. This map contains only top-level expression
	 * elements that share the given context (i.e., all elements below share the
	 * same context) -- this is because of where contexts are being introduced
	 * into a SPARQL query -- all elements sharing the same contexts are grouped
	 * together with a "GRAPH <ctx> { ... }" clause.
	 */
	private Map<TupleExpr, Var> mContexts = new HashMap<TupleExpr, Var>();

	private ContextCollector() {
	}

	static Map<TupleExpr, Var> collectContexts(TupleExpr theTupleExpr)
		throws Exception
	{
		ContextCollector aContextVisitor = new ContextCollector();

		theTupleExpr.visit(aContextVisitor);

		return aContextVisitor.mContexts;
	}

	public void meet(Join theJoin)
		throws Exception
	{
		binaryOpMeet(theJoin, theJoin.getLeftArg(), theJoin.getRightArg());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(LeftJoin theJoin)
		throws Exception
	{
		binaryOpMeet(theJoin, theJoin.getLeftArg(), theJoin.getRightArg());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Union theOp)
		throws Exception
	{
		binaryOpMeet(theOp, theOp.getLeftArg(), theOp.getRightArg());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Difference theOp)
		throws Exception
	{
		binaryOpMeet(theOp, theOp.getLeftArg(), theOp.getRightArg());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Intersection theOp)
		throws Exception
	{
		binaryOpMeet(theOp, theOp.getLeftArg(), theOp.getRightArg());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final Filter theFilter)
		throws Exception
	{
		theFilter.getArg().visit(this);

		if (mContexts.containsKey(theFilter.getArg())) {
			Var aCtx = mContexts.get(theFilter.getArg());
			mContexts.remove(theFilter.getArg());
			mContexts.put(theFilter, aCtx);
		}
	}

	private void binaryOpMeet(TupleExpr theCurrentExpr, TupleExpr theLeftExpr, TupleExpr theRightExpr)
		throws Exception
	{
		theLeftExpr.visit(this);

		Var aLeftCtx = mContexts.get(theLeftExpr);

		theRightExpr.visit(this);

		Var aRightCtx = mContexts.get(theRightExpr);

		sameCtxCheck(theCurrentExpr, theLeftExpr, aLeftCtx, theRightExpr, aRightCtx);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(StatementPattern thePattern)
		throws Exception
	{
		Var aCtxVar = thePattern.getContextVar();

		if (aCtxVar != null) {
			mContexts.put(thePattern, aCtxVar);
		}
	}

	private void sameCtxCheck(TupleExpr theCurrentExpr, TupleExpr theLeftExpr, Var theLeftCtx,
			TupleExpr theRightExpr, Var theRightCtx)
	{
		if ((theLeftCtx != null) && (theRightCtx != null) && isSameCtx(theLeftCtx, theRightCtx)) {
			mContexts.remove(theLeftExpr);
			mContexts.remove(theRightExpr);
			mContexts.put(theCurrentExpr, theLeftCtx);
		}
	}

	private boolean isSameCtx(Var v1, Var v2) {
		if ((v1 != null && v1.getValue() != null) && (v2 != null && v2.getValue() != null)) {
			return v1.getValue().equals(v2.getValue());
		}
		else if ((v1 != null && v1.getName() != null) && (v2 != null && v2.getName() != null)) {
			return v1.getName().equals(v2.getName());
		}

		return false;
	}
}
