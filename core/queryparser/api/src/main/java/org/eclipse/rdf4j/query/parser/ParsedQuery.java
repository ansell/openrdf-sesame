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
package org.eclipse.rdf4j.query.parser;

import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.TupleExpr;

/**
 * Abstract super class of all query types that a query parser can generate.
 * 
 * @author Arjohn Kampman
 */
public abstract class ParsedQuery extends ParsedOperation {

	/*-----------*
	 * Variables *
	 *-----------*/

	private TupleExpr tupleExpr;

	/**
	 * The dataset that was specified in the operation, if any.
	 */
	private Dataset dataset;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new query object. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 */
	public ParsedQuery() {
		super();
	}

	/**
	 * Creates a new query object. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 */
	public ParsedQuery(String sourceString) {
		super(sourceString);
	}

	/**
	 * Creates a new query object.
	 * 
	 * @param tupleExpr
	 *        The tuple expression underlying this query.
	 */
	public ParsedQuery(String sourceString, TupleExpr tupleExpr) {
		this(sourceString);
		setTupleExpr(tupleExpr);
	}

	/**
	 * Creates a new query object.
	 * 
	 * @param tupleExpr
	 *        The tuple expression underlying this query.
	 */
	public ParsedQuery(TupleExpr tupleExpr) {
		this(null, tupleExpr);
	}

	/**
	 * Creates a new query object.
	 * 
	 * @param tupleExpr
	 *        The tuple expression underlying this query.
	 */
	public ParsedQuery(TupleExpr tupleExpr, Dataset dataset) {
		this(null, tupleExpr, dataset);
	}

	/**
	 * Creates a new query object.
	 * 
	 * @param tupleExpr
	 *        The tuple expression underlying this query.
	 */
	public ParsedQuery(String sourceString, TupleExpr tupleExpr, Dataset dataset) {
		this(sourceString, tupleExpr);
		setDataset(dataset);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}
	
	/**
	 * Gets the tuple expression underlying this operation.
	 */
	public void setTupleExpr(TupleExpr tupleExpr) {
		assert tupleExpr != null : "tupleExpr must not be null";
		this.tupleExpr = tupleExpr;
	}

	/**
	 * Gets the tuple expression underlying this operation.
	 */
	public TupleExpr getTupleExpr() {
		return tupleExpr;
	}

	@Override
	public String toString() {
		if (getDataset() != null) {
			return getDataset().toString() + getTupleExpr().toString();
		}
		else {
			return getTupleExpr().toString();
		}
 	}

}
