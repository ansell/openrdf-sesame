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
package org.openrdf.query.algebra.helpers;

import org.openrdf.query.algebra.QueryModelNode;

/**
 * QueryModelVisitor implementation that "prints" a tree representation of a
 * query model. The tree representations is printed to an internal character
 * buffer and can be retrieved using {@link #getTreeString()}. As an
 * alternative, the static utility method {@link #printTree(QueryModelNode)} can
 * be used.
 */
public class QueryModelTreePrinter extends AbstractQueryModelVisitor<RuntimeException> {

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

	private String indentString = "   ";

	private StringBuilder buf;

	private int indentLevel = 0;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public QueryModelTreePrinter() {
		buf = new StringBuilder(256);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getTreeString() {
		return buf.toString();
	}

	@Override
	protected void meetNode(QueryModelNode node)
	{
		for (int i = 0; i < indentLevel; i++) {
			buf.append(indentString);
		}

		buf.append(node.getSignature());
		buf.append(LINE_SEPARATOR);

		indentLevel++;

		super.meetNode(node);

		indentLevel--;
	}
}
