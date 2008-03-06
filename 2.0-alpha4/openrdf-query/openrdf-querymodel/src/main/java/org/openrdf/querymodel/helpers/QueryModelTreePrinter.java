/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel.helpers;

import org.openrdf.querymodel.QueryModelNode;

/**
 * QueryModelVisitor implementation that "prints" a tree representation of a
 * query model. The tree representations is printed to an internal character
 * buffer and can be retrieved using {@link #getTreeString()}. As an
 * alternative, the static utility method {@link #printTree(QueryModelNode)} can
 * be used.
 */
public class QueryModelTreePrinter extends QueryModelVisitorBase {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/*-----------*
	 * Constants *
	 *-----------*/

	public static String printTree(QueryModelNode node) {
		QueryModelTreePrinter treePrinter = new QueryModelTreePrinter();
		node.visit(treePrinter);
		return treePrinter.getTreeString();
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private String _indentString = "   ";

	private StringBuilder _buf;

	private int _indentLevel = 0;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public QueryModelTreePrinter() {
		_buf = new StringBuilder(256);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getTreeString() {
		return _buf.toString();
	}

	@Override
	protected void meetDefault(QueryModelNode node)
	{
		for (int i = 0; i < _indentLevel; i++) {
			_buf.append(_indentString);
		}

		_buf.append(node.toString());
		_buf.append(LINE_SEPARATOR);

		_indentLevel++;

		super.meetDefault(node);

		_indentLevel--;
	}
}
