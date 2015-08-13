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

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.GEOF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.SailException;

public class GeoRelationQuerySpecBuilder implements SearchQueryInterpreter {
	private SearchIndex index;

	public GeoRelationQuerySpecBuilder(SearchIndex index) {
		this.index = index;
	}

	@Override
	public void process(TupleExpr tupleExpr, BindingSet bindings,
			final Collection<SearchQueryEvaluator> results) throws SailException {

		tupleExpr.visit(new QueryModelVisitorBase<SailException>() {
			final Map<String,GeoRelationQuerySpec> specs = new HashMap<String,GeoRelationQuerySpec>();

			@Override
			public void meet(FunctionCall f) throws SailException {
				if(f.getURI().startsWith(GEOF.NAMESPACE)) {
					List<ValueExpr> args = f.getArgs();
					if(args.size() != 2) {
						return;
					}

					Literal qshape = getLiteral(args.get(0));
					String varShape = getVarName(args.get(1));

					if(qshape == null || varShape == null) {
						return;
					}

					Filter filter = null;
					String fVar = null;
					QueryModelNode parent = f.getParentNode();
					if(parent instanceof ExtensionElem) {
						fVar = ((ExtensionElem)parent).getName();
						QueryModelNode extension = parent.getParentNode();
						filter = getFilter(extension.getParentNode(), fVar);
					} else if(parent instanceof Filter) {
						filter = (Filter) parent;
					}

					if(filter == null) {
						return;
					}

					GeoRelationQuerySpec spec = new GeoRelationQuerySpec();
					spec.setRelation(f.getURI());
					spec.setFunctionParent(parent);
					spec.setQueryGeometry(qshape);
					spec.setFunctionValueVar(fVar);
					spec.setFilter(filter);
					specs.put(varShape, spec);
				}
			}

			@Override
			public void meet(StatementPattern sp) {
				URI propertyName = (URI) sp.getPredicateVar().getValue();
				if(propertyName != null && index.isGeoField(SearchFields.getPropertyField(propertyName)) && !sp.getObjectVar().hasValue()) {
					String objectVarName = sp.getObjectVar().getName();
					GeoRelationQuerySpec spec = specs.remove(objectVarName);
					if(spec != null && isChildOf(sp, spec.getFilter())) {
						spec.setGeometryPattern(sp);
						results.add(spec);
					}
				}
			}
		});
	}

	private static boolean isChildOf(QueryModelNode child, QueryModelNode parent) {
		if(child.getParentNode() == parent) {
			return true;
		}
		return isChildOf(child.getParentNode(), parent);
	}

	private static Filter getFilter(QueryModelNode node, String varName) {
		Filter filter = null;
		if(node instanceof Filter) {
			Filter f = (Filter) node;
			ValueExpr condition = f.getCondition();
			if(varName.equals(getVarName(condition))) {
				filter = f;
			}
		}
		else if(node != null) {
			filter = getFilter(node.getParentNode(), varName);
		}
		return filter;
	}

	private static Literal getLiteral(ValueExpr v) {
		Value value = getValue(v);
		if(value instanceof Literal) {
			return (Literal) value;
		}
		return null;
	}

	private static Value getValue(ValueExpr v) {
		Value value = null;
		if(v instanceof ValueConstant) {
			value = ((ValueConstant)v).getValue();
		}
		else if(v instanceof Var) {
			value = ((Var)v).getValue();
		}
		return value;
	}

	private static String getVarName(ValueExpr v) {
		if(v instanceof Var) {
			Var var = (Var) v;
			if(!var.isConstant()) {
				return var.getName();
			}
		}
		return null;
	}
}
