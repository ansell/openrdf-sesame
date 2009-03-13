/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public abstract class StatementBase implements Statement {

	private static final long serialVersionUID = 8159033701345168571L;

	/**
	 * Compares a statement object to another object.
	 * 
	 * @param other
	 *        The object to compare this statement to.
	 * @return <tt>true</tt> if the other object is an instance of
	 *         {@link Statement} and if their subjects, predicates and objects
	 *         are equal.
	 */
	public final boolean equalsIgnoreContext(Object other) {
		if (other instanceof Statement) {
			return equalsIgnoreContext((Statement)other);
		}

		return false;
	}

	/**
	 * Compares a statement object to another statement object.
	 * 
	 * @param other
	 *        The object to compare this statement to.
	 * @return <tt>true</tt> if the other object is an instance of
	 *         {@link Statement} and if their subjects, predicates and objects
	 *         are equal.
	 */
	public final boolean equalsIgnoreContext(Statement other) {
		return this == other || spoEquals(other);
	}

	/**
	 * Compares a statement object to another object.
	 * 
	 * @param other
	 *        The object to compare this statement to.
	 * @return <tt>true</tt> if the other object is an instance of
	 *         {@link Statement} and if their subjects, predicates, objects, and
	 *         contexts are equal.
	 */
	@Override
	public final boolean equals(Object other) {
		if (other instanceof Statement) {
			return equals((Statement)other);
		}

		return false;
	}

	/**
	 * Compares a statement object to another statement object.
	 * 
	 * @param other
	 *        The object to compare this statement to.
	 * @return <tt>true</tt> if the other object is an instance of
	 *         {@link Statement} and if their subjects, predicates and objects
	 *         are equal.
	 */
	public final boolean equals(Statement other) {
		return this == other || spoEquals(other) && contextEquals(other);
	}

	private final boolean spoEquals(Statement other) {
		// The object is potentially the cheapest to check so we start with
		// that. The number of different predicates in sets of statements is
		// commonly the smallest, so predicate equality is checked last.
		return getObject().equals(other.getObject()) && getSubject().equals(other.getSubject())
				&& getPredicate().equals(other.getPredicate());
	}

	private final boolean contextEquals(Statement other) {
		if (getContext() == null) {
			return other.getContext() == null;
		}
		else {
			return getContext().equals(other.getContext());
		}
	}

	/**
	 * The hash code of a statement is defined as:
	 * 
	 * <tt>961 * subject.hashCode() + 31 * predicate.hashCode() + object.hashCode() + 29791 * context.hashCode()</tt>
	 * This is similar to how {@link String#hashCode String.hashCode()} is
	 * defined.
	 * 
	 * @return A hash code for the statement.
	 */
	@Override
	public final int hashCode() {
		final int prime = 31;
		Resource ctx = getContext();
		int result = (ctx == null) ? 0 : ctx.hashCode();
		result = prime * result + getSubject().hashCode();
		result = prime * result + getPredicate().hashCode();
		result = prime * result + getObject().hashCode();
		return result;
	}

	/**
	 * Gives a String-representation of this Statement that can be used for
	 * debugging.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(256);

		sb.append("(");
		sb.append(getSubject());
		sb.append(", ");
		sb.append(getPredicate());
		sb.append(", ");
		sb.append(getObject());
		sb.append(")");
		Resource ctx = getContext();
		if (ctx != null) {
			sb.append(super.toString());
			sb.append(" [").append(ctx).append("]");
		}

		return sb.toString();
	}
}
