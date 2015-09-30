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
package org.eclipse.rdf4j.sail.lucene;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.GEOF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.algebra.Compare;
import org.eclipse.rdf4j.query.algebra.ExtensionElem;
import org.eclipse.rdf4j.query.algebra.Filter;
import org.eclipse.rdf4j.query.algebra.FunctionCall;
import org.eclipse.rdf4j.query.algebra.QueryModelNode;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.Compare.CompareOp;
import org.eclipse.rdf4j.query.algebra.helpers.QueryModelVisitorBase;
import org.eclipse.rdf4j.sail.SailException;

public class DistanceQuerySpecBuilder implements SearchQueryInterpreter {
	private SearchIndex index;

	public DistanceQuerySpecBuilder(SearchIndex index) {
		this.index = index;
	}

	@Override
	public void process(TupleExpr tupleExpr, BindingSet bindings,
			final Collection<SearchQueryEvaluator> results) throws SailException {

		tupleExpr.visit(new QueryModelVisitorBase<SailException>() {
			final Map<String,DistanceQuerySpec> specs = new HashMap<String,DistanceQuerySpec>();

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
						if(rv == null) {
							return;
						}
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

					DistanceQuerySpec spec = new DistanceQuerySpec();
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
				if(propertyName != null && index.isGeoField(SearchFields.getPropertyField(propertyName)) && !sp.getObjectVar().hasValue()) {
					String objectVarName = sp.getObjectVar().getName();
					DistanceQuerySpec spec = specs.remove(objectVarName);
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
		Object[] rv = null;
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
				rv = new Object[] {f, dist};
			}
		}
		else if(node != null) {
			rv = getFilterAndDistance(node.getParentNode(), compareArgVarName);
		}
		return rv;
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
