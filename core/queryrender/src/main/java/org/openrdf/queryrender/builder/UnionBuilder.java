/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryrender.builder;

import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.parser.ParsedQuery;

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
