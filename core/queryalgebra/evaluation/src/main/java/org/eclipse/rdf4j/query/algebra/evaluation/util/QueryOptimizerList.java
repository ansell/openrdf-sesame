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
package org.eclipse.rdf4j.query.algebra.evaluation.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryOptimizer;

/**
 * A query optimizer that contains a list of other query optimizers, which are
 * called consecutively when the list's {@link #optimize(TupleExpr, Dataset, BindingSet)}
 * method is called.
 * 
 * @author Arjohn Kampman
 */
public class QueryOptimizerList implements QueryOptimizer {

	protected List<QueryOptimizer> optimizers;

	public QueryOptimizerList() {
		this.optimizers = new ArrayList<QueryOptimizer>(8);
	}

	public QueryOptimizerList(List<QueryOptimizer> optimizers) {
		this.optimizers = new ArrayList<QueryOptimizer>(optimizers);
	}

	public QueryOptimizerList(QueryOptimizer... optimizers) {
		this.optimizers = new ArrayList<QueryOptimizer>(optimizers.length);
		for (QueryOptimizer optimizer : optimizers) {
			this.optimizers.add(optimizer);
		}
	}

	public void add(QueryOptimizer optimizer) {
		optimizers.add(optimizer);
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		for (QueryOptimizer optimizer : optimizers) {
			optimizer.optimize(tupleExpr, dataset, bindings);
		}
	}
}
