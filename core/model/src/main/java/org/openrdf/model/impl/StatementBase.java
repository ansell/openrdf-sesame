/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;


public abstract class StatementBase implements Statement {

	/**
	 * Compares a statement object to another object.
	 * 
	 * @param other
	 *        The object to compare this statement to.
	 * @return <tt>true</tt> if the other object is an instance of
	 *         {@link Statement} and if their subjects, predicates and objects
	 *         are equal.
	 */
	public final boolean equalsIgnoreContext(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Statement))
			return false;
		final Statement other = (Statement)obj;
		// The object is potentially the cheapest to check, as types
		// of these references might be different.

		// In general the number of different predicates in sets of
		// statements is the smallest, so predicate equality is checked
		// last.
		if (!this.getObject().equals(other.getObject()))
			return false;
		if (!getSubject().equals(other.getSubject()))
			return false;
		if (!getPredicate().equals(other.getPredicate()))
			return false;
		return true;
	}

	/**
	 * Compares a statement object to another object.
	 * 
	 * @param other
	 *        The object to compare this statement to.
	 * @return <tt>true</tt> if the other object is an instance of
	 *         {@link Statement} and if their subjects, predicates, objects,
	 *         and contexts are equal.
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Statement))
			return false;
		final Statement other = (Statement)obj;
		// The object is potentially the cheapest to check, as types
		// of these references might be different.

		// In general the number of different predicates in sets of
		// statements is the smallest, so predicate equality is checked
		// last.
		if (!this.getObject().equals(other.getObject()))
			return false;
		if (!getSubject().equals(other.getSubject()))
			return false;
		if (!getPredicate().equals(other.getPredicate()))
			return false;
		if (getContext() == null && other.getContext() != null)
			return false;
		if (getContext() != null && !getContext().equals(other.getContext()))
			return false;
		return true;
	}

	/**
	 * The hash code of a statement is defined as:
	 * <tt>961 * subject.hashCode() + 31 * predicate.hashCode() + object.hashCode() + 29791 * context.hashCode()</tt>.
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
