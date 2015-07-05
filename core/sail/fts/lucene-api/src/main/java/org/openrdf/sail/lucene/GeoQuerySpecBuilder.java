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
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Compare.CompareOp;
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

public class GeoQuerySpecBuilder implements SearchQueryInterpreter {
	private SearchIndex index;

	public GeoQuerySpecBuilder(SearchIndex index) {
		this.index = index;
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
					if(args.size() != 3) {
						return;
					}

					Literal from = getLiteral(args.get(0));
					String to = getVarName(args.get(1));
					URI units = getURI(args.get(2));

					if(from == null || to == null || units == null) {
						return;
					}

					Filter filter = null;
					Literal dist = null;
					String distanceVar = null;
					QueryModelNode parent = f.getParentNode();
					if(parent instanceof ExtensionElem) {
						distanceVar = ((ExtensionElem)parent).getName();
						QueryModelNode extension = parent.getParentNode();
						Object[] rv = getFilterAndDistance(extension.getParentNode(), distanceVar);
						filter = (Filter) rv[0];
						dist = (Literal) rv[1];
					} else if(parent instanceof Compare) {
						filter = (Filter) parent.getParentNode();
						Compare compare = (Compare) parent;
						CompareOp op = compare.getOperator();
						if(op == CompareOp.LT && compare.getLeftArg() == f) {
							dist = getLiteral(compare.getRightArg());
						} else if(op == CompareOp.GT && compare.getRightArg() == f) {
							dist = getLiteral(compare.getLeftArg());
						}
					}

					if(dist == null || !XMLSchema.DOUBLE.equals(dist.getDatatype())) {
						return;
					}

					GeoQuerySpec spec = new GeoQuerySpec();
					spec.setFunctionParent(parent);
					spec.setFrom(from);
					spec.setUnits(units);
					spec.setDistance(dist.doubleValue());
					spec.setDistanceVar(distanceVar);
					spec.setFilter(filter);
					specs.put(to, spec);
				}
			}

			@Override
			public void meet(StatementPattern sp) {
				URI propertyName = (URI) sp.getPredicateVar().getValue();
				if(propertyName != null && index.isGeoProperty(propertyName.toString()) && !sp.getObjectVar().hasValue()) {
					String objectVarName = sp.getObjectVar().getName();
					GeoQuerySpec spec = specs.remove(objectVarName);
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

	private static Object[] getFilterAndDistance(QueryModelNode node, String compareArgVarName) {
		if(node instanceof Filter) {
			Filter f = (Filter) node;
			ValueExpr condition = f.getCondition();
			if(condition instanceof Compare) {
				Compare compare = (Compare) condition;
				CompareOp op = compare.getOperator();
				Literal dist = null;
				if(op == CompareOp.LT && compareArgVarName.equals(getVarName(compare.getLeftArg()))) {
					dist = getLiteral(compare.getRightArg());
				} else if(op == CompareOp.GT && compareArgVarName.equals(getVarName(compare.getRightArg()))) {
					dist = getLiteral(compare.getLeftArg());
				}
				return new Object[] {f, dist};
			}
		}
		return getFilterAndDistance(node.getParentNode(), compareArgVarName);
	}

	private static Literal getLiteral(ValueExpr v) {
		Value value = getValue(v);
		if(value instanceof Literal) {
			return (Literal) value;
		}
		return null;
	}

	private static URI getURI(ValueExpr v) {
		Value value = getValue(v);
		if(value instanceof URI) {
			return (URI) value;
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
