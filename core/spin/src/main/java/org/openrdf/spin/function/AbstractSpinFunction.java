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
package org.openrdf.spin.function;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.Query;
import org.openrdf.query.algebra.evaluation.QueryPreparer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.spin.QueryContext;


public abstract class AbstractSpinFunction {
	private final String uri;

	private QueryPreparer queryPreparer;

	protected AbstractSpinFunction(String uri) {
		this.uri = uri;
	}

	public String getURI() {
		return uri;
	}

	public QueryPreparer getQueryPreparer() {
		return queryPreparer;
	}
	
	public void setQueryPreparer(QueryPreparer queryPreparer) {
		this.queryPreparer = queryPreparer;
	}

	protected QueryPreparer getCurrentQueryPreparer() {
		QueryPreparer qp = (queryPreparer != null) ? queryPreparer : QueryContext.getQueryPreparer();
		if(qp == null) {
			throw new IllegalStateException("No QueryPreparer!");
		}
		return qp;
	}

	protected static void addBindings(Query query, Value... args)
		throws ValueExprEvaluationException
	{
		for(int i=1; i<args.length; i+=2) {
			if(!(args[i] instanceof Literal)) {
				throw new ValueExprEvaluationException("Argument "+i+" must be a literal");
			}
			query.setBinding(((Literal)args[i]).getLabel(), args[i+1]);
		}
	}
}
