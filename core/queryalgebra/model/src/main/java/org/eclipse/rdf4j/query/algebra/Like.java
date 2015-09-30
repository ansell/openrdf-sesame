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
package org.eclipse.rdf4j.query.algebra;

/**
 * Compares the string representation of a value expression to a pattern.
 */
public class Like extends UnaryValueOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String pattern;

	private boolean caseSensitive;

	/**
	 * Operational pattern, equal to pattern but converted to lower case when not
	 * case sensitive.
	 */
	private String opPattern;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Like() {
	}

	public Like(ValueExpr expr, String pattern, boolean caseSensitive) {
		super(expr);
		setPattern(pattern, caseSensitive);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setPattern(String pattern, boolean caseSensitive) {
		assert pattern != null : "pattern must not be null";
		this.pattern = pattern;
		this.caseSensitive = caseSensitive;
		opPattern = caseSensitive ? pattern : pattern.toLowerCase();
	}

	public String getPattern() {
		return pattern;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public String getOpPattern() {
		return opPattern;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder(128);

		sb.append(super.getSignature());
		sb.append(" \"");
		sb.append(pattern);
		sb.append("\"");

		if (caseSensitive) {
			sb.append(" IGNORE CASE");
		}

		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Like && super.equals(other)) {
			Like o = (Like)other;
			return caseSensitive == o.isCaseSensitive() && opPattern.equals(o.getOpPattern());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ opPattern.hashCode();
	}

	@Override
	public Like clone() {
		return (Like)super.clone();
	}
}
