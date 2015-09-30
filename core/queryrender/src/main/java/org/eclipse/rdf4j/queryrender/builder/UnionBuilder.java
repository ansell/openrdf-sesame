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
package org.eclipse.rdf4j.queryrender.builder;

import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Union;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

/**
 * <p>
 * Builder class for creating Unioned groups
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
public class UnionBuilder<T extends ParsedQuery, E extends SupportsGroups> implements
		SupportsGroups<UnionBuilder<T, E>>, Group
{

	/**
	 * Left operand
	 */
	private Group mLeft;

	/**
	 * Right operand
	 */
	private Group mRight;

	/**
	 * Parent builder
	 */
	private GroupBuilder<T, E> mParent;

	public UnionBuilder(final GroupBuilder<T, E> theParent) {
		mParent = theParent;
	}

	/**
	 * Return a builder for creating the left operand of the union
	 * 
	 * @return builder for left operand
	 */
	public GroupBuilder<T, UnionBuilder<T, E>> left() {
		return new GroupBuilder<T, UnionBuilder<T, E>>(this);
	}

	/**
	 * Return a builder for creating the right operand of the union
	 * 
	 * @return builder for right operand
	 */
	public GroupBuilder<T, UnionBuilder<T, E>> right() {
		return new GroupBuilder<T, UnionBuilder<T, E>>(this);
	}

	/**
	 * Close this union and return it's parent group builder.
	 * 
	 * @return the parent builder
	 */
	public GroupBuilder<T, E> closeUnion() {
		return mParent;
	}

	/**
	 * @inheritDoc
	 */
	public int size() {
		return (mLeft == null ? 0 : mLeft.size()) + (mRight == null ? 0 : mRight.size());
	}

	/**
	 * @inheritDoc
	 */
	public UnionBuilder<T, E> addGroup(final Group theGroup) {
		if (mLeft == null) {
			mLeft = theGroup;
		}
		else if (mRight == null) {
			mRight = theGroup;
		}
		else {
			throw new IllegalArgumentException("Cannot set left or right arguments of union, both already set");
		}

		return this;
	}

	/**
	 * @inheritDoc
	 */
	public UnionBuilder<T, E> removeGroup(final Group theGroup) {
		if (mLeft != null && mLeft.equals(theGroup)) {
			mLeft = null;
		}
		else if (mRight != null && mRight.equals(theGroup)) {
			mRight = null;
		}

		return this;
	}

	/**
	 * @inheritDoc
	 */
	public void addChild(final Group theGroup) {
		addGroup(theGroup);
	}

	/**
	 * @inheritDoc
	 */
	public TupleExpr expr() {
		if (mLeft != null && mRight != null) {
			return new Union(mLeft.expr(), mRight.expr());
		}
		else if (mLeft != null && mRight == null) {
			return mLeft.expr();

		}
		else if (mRight != null && mLeft == null) {
			return mRight.expr();
		}
		else {
			return null;
		}
	}

	/**
	 * @inheritDoc
	 */
	public boolean isOptional() {
		return false;
	}
}
