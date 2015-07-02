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
package org.openrdf.sail.lucene;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.vocabulary.GEOF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.SailException;

public class GeoQuerySpecBuilder implements SearchQueryInterpreter {
	private boolean failOnIncompleteQuery;

	@Override
	public void setIncompleteQueryFails(boolean f) {
		this.failOnIncompleteQuery = f;
	}

	@Override
	public void process(TupleExpr tupleExpr, BindingSet bindings,
			final Collection<SearchQueryEvaluator> results) throws SailException {

		tupleExpr.visit(new QueryModelVisitorBase<SailException>() {
			final Map<String,GeoQuerySpec> specs = new HashMap<String,GeoQuerySpec>();

			@Override
			public void meet(FunctionCall f) throws SailException {
				if(GEOF.DISTANCE.stringValue().equals(f.getURI())) {
					List<ValueExpr> args = f.getArgs();
					if(args.size() == 3 && isValue(args.get(0))
							&& isVar(args.get(1))
							&& isValue(args.get(2))) {
						String geoLiteralVarName = ((Var)args.get(1)).getName();
						GeoQuerySpec spec = new GeoQuerySpec();
						specs.put(geoLiteralVarName, spec);
					}
				}
			}

			@Override
			public void meet(StatementPattern sp) {
				String objectVarName = sp.getObjectVar().getName();
				GeoQuerySpec spec = specs.get(objectVarName);
				if(spec != null) {
					
				}
			}
		});
	}

	private static boolean isValue(ValueExpr v) {
		return (v instanceof Var) && ((Var)v).hasValue();
	}

	private static boolean isVar(ValueExpr v) {
		return (v instanceof Var) && !((Var)v).hasValue();
	}
}
