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
 * The SLICE operator, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#defn_algSlice">SPARQL Query
 * Language for RDF</a>. The SLICE operator selects specific results from the
 * underlying tuple expression based on an offset and limit value (both
 * optional).
 * 
 * @author Arjohn Kampman
 */
public class Slice extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private long offset;

	private long limit;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Slice() {
	}

	public Slice(TupleExpr arg) {
		this(arg, 0, -1);
	}

	public Slice(TupleExpr arg, long offset2, long limit2) {
		super(arg);
		setOffset(offset2);
		setLimit(limit2);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	/**
	 * Checks whether the row selection has a (valid) offset.
	 * 
	 * @return <tt>true</tt> when <tt>offset &gt; 0</tt>
	 */
	public boolean hasOffset() {
		return offset > 0L;
	}

	public long getLimit() {
		return limit;
	}

	public void setLimit(long limit) {
		this.limit = limit;
	}

	/**
	 * Checks whether the row selection has a (valid) limit.
	 * 
	 * @return <tt>true</tt> when <tt>offset &gt;= 0</tt>
	 */
	public boolean hasLimit() {
		return limit >= 0L;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder(256);

		sb.append(super.getSignature());
		sb.append(" ( ");

		if (hasLimit()) {
			sb.append("limit=").append(getLimit());
		}
		if (hasOffset()) {
			sb.append("offset=").append(getOffset());
		}

		sb.append(" )");

		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Slice && super.equals(other)) {
			Slice o = (Slice)other;
			return offset == o.getOffset() && limit == o.getLimit();
		}
		return false;
	}

	@Override
	public int hashCode() {
		// casting long to int is not safe, but shouldn't matter for hashcode, should it?
		return super.hashCode() ^ (int)offset ^ (int)limit;
	}

	@Override
	public Slice clone() {
		return (Slice)super.clone();
	}
}
