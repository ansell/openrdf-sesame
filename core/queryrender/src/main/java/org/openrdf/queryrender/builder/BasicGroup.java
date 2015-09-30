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
package org.openrdf.queryrender.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;

/**
 * <p>
 * Internal class for representing a group within a query.
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
public class BasicGroup implements Group {

	private boolean mIsOptional = false;

	private Collection<TupleExpr> mExpressions = new LinkedHashSet<TupleExpr>();

	private List<Group> mChildren = new ArrayList<Group>();

	private Collection<ValueExpr> mFilters = new LinkedHashSet<ValueExpr>();

	/**
	 * Create a new BasicGroup
	 * 
	 * @param theOptional
	 *        whether or not the patterns and filters in this group are optional
	 */
	public BasicGroup(final boolean theOptional) {
		mIsOptional = theOptional;
	}

	/**
	 * @inheritDoc
	 */
	public int size() {
		int aSize = mExpressions.size();

		for (Group aChild : mChildren) {
			aSize += aChild.size();
		}

		return aSize;
	}

	/**
	 * @inheritDoc
	 */
	public void addChild(Group theGroup) {
		mChildren.add(theGroup);
	}

	/**
	 * Remove a child from this group
	 * 
	 * @param theGroup
	 *        the child to remove
	 */
	public void removeChild(Group theGroup) {
		mChildren.remove(theGroup);
	}

	/**
	 * Add a Filter to this group
	 * 
	 * @param theExpr
	 *        the value filter to add
	 */
	public void addFilter(ValueExpr theExpr) {
		mFilters.add(theExpr);
	}

	public boolean isEmpty() {
		return mFilters.isEmpty() && mExpressions.isEmpty() && mChildren.isEmpty();
	}

	/**
	 * @inheritDoc
	 */
	public boolean isOptional() {
		return mIsOptional;
	}

	/**
	 * @inheritDoc
	 */
	public TupleExpr expr() {
		return expr(true);
	}

	private TupleExpr expr(boolean filterExpr) {
		TupleExpr aExpr = null;

		if (mExpressions.isEmpty() && mFilters.isEmpty()) {
			if (mChildren.isEmpty()) {
				return null;
			}
		}
		else if (mExpressions.isEmpty() && !mFilters.isEmpty()) {
			if (mChildren.isEmpty()) {
				aExpr = new Filter(new EmptySet(), filtersAsAnd());
			}
		}
		else {
			aExpr = asJoin(mExpressions);

			if (filterExpr) {
				aExpr = filteredTuple(aExpr);
			}
		}

		if (!mChildren.isEmpty()) {
			for (Group aGroup : mChildren) {
				if (aExpr == null) {
					if (mExpressions.isEmpty() && !mFilters.isEmpty()) {
						aExpr = new Filter(aGroup.expr(), filtersAsAnd());
					}
					else {
						aExpr = aGroup.expr();
					}
				}
				else {
					BinaryTupleOperator aJoin = aGroup.isOptional() ? new LeftJoin() : new Join();

					aJoin.setLeftArg(aExpr);

					if (aGroup.isOptional() && aJoin instanceof LeftJoin && aGroup instanceof BasicGroup
							&& !((BasicGroup)aGroup).mFilters.isEmpty())
					{

						BasicGroup aBasicGroup = (BasicGroup)aGroup;

						aJoin.setRightArg(aBasicGroup.expr(false));

						((LeftJoin)aJoin).setCondition(aBasicGroup.filtersAsAnd());
					}
					else {
						aJoin.setRightArg(aGroup.expr());
					}

					aExpr = aJoin;
				}

			}
		}

		return aExpr;
	}

	private TupleExpr filteredTuple(TupleExpr theExpr) {
		TupleExpr aExpr = theExpr;

		for (ValueExpr aValEx : mFilters) {
			Filter aFilter = new Filter();
			aFilter.setCondition(aValEx);
			aFilter.setArg(aExpr);
			aExpr = aFilter;
		}

		return aExpr;
	}

	private ValueExpr filtersAsAnd() {
		ValueExpr aExpr = null;

		for (ValueExpr aValEx : mFilters) {
			if (aExpr == null) {
				aExpr = aValEx;
			}
			else {
				And aAnd = new And();
				aAnd.setLeftArg(aValEx);
				aAnd.setRightArg(aExpr);
				aExpr = aAnd;
			}
		}

		return aExpr;
	}

	public void add(final TupleExpr theExpr) {
		mExpressions.add(theExpr);
	}

	public void addAll(final Collection<? extends TupleExpr> theTupleExprs) {
		mExpressions.addAll(theTupleExprs);
	}

	private TupleExpr asJoin(Collection<TupleExpr> theList) {
		Join aJoin = new Join();

		if (theList.isEmpty()) {
			throw new RuntimeException("Can't have an empty or missing join.");
		}
		else if (theList.size() == 1) {
			return theList.iterator().next();
		}

		for (TupleExpr aExpr : theList) {
			if (aJoin.getLeftArg() == null) {
				aJoin.setLeftArg(aExpr);
			}
			else if (aJoin.getRightArg() == null) {
				aJoin.setRightArg(aExpr);
			}
			else {
				Join aNewJoin = new Join();

				aNewJoin.setLeftArg(aJoin);
				aNewJoin.setRightArg(aExpr);

				aJoin = aNewJoin;
			}
		}

		return aJoin;
	}

	public Collection<StatementPattern> getPatterns() {
		Set<StatementPattern> aPatternSet = new HashSet<StatementPattern>();
		for (TupleExpr aExpr : mExpressions) {
			if (aExpr instanceof StatementPattern) {
				aPatternSet.add((StatementPattern)aExpr);
			}
		}
		return aPatternSet;
	}
}
