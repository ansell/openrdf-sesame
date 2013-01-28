/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.parser;

import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;

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
		return tupleExpr.toString();
	}

}
