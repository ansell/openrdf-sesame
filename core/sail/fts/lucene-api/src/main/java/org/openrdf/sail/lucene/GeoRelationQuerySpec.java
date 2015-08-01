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

import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

public class GeoRelationQuerySpec implements SearchQueryEvaluator {
	private String relation;
	private QueryModelNode functionParent;
	private Literal qshape;
	private String valueVar;
	private StatementPattern geoStatement;
	private Filter filter;

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getRelation() {
		return relation;
	}

	public void setFunctionParent(QueryModelNode functionParent) {
		this.functionParent = functionParent;
	}

	public void setQueryGeometry(Literal shape) {
		this.qshape = shape;
	}

	public Literal getQueryGeometry() {
		return qshape;
	}

	public void setFunctionValueVar(String varName) {
		this.valueVar = varName;
	}

	public String getFunctionValueVar() {
		return valueVar;
	}

	public void setGeometryPattern(StatementPattern sp) {
		if(sp.getSubjectVar().hasValue()) {
			throw new IllegalArgumentException("Subject cannot be bound: "+sp);
		}
		if(!sp.getPredicateVar().hasValue()) {
			throw new IllegalArgumentException("Predicate must be bound: "+sp);
		}
		if(sp.getObjectVar().hasValue()) {
			throw new IllegalArgumentException("Object cannot be bound: "+sp);
		}
		this.geoStatement = sp;
	}

	public String getSubjectVar() {
		return geoStatement.getSubjectVar().getName();
	}

	public Var getContextVar() {
		return geoStatement.getContextVar();
	}

	public URI getGeoProperty() {
		return (URI) geoStatement.getPredicateVar().getValue();
	}

	public String getGeoVar() {
		return geoStatement.getObjectVar().getName();
	}

	public void setFilter(Filter f) {
		this.filter = f;
	}

	public Filter getFilter() {
		return filter;
	}

	@Override
	public QueryModelNode getParentQueryModelNode() {
		return filter;
	}

	@Override
	public void updateQueryModelNodes(boolean hasResult) {
		QueryModelNode replacementNode = hasResult ? new SingletonSet() : new EmptySet();
		geoStatement.replaceWith(replacementNode);

		if(hasResult) {
			filter.replaceWith(filter.getArg());
		} else {
			filter.replaceWith(new EmptySet());
		}

		if(functionParent instanceof ExtensionElem) {
			Extension extension = (Extension) functionParent.getParentNode();
			List<ExtensionElem> elements = extension.getElements();
			if(elements.size() > 1) {
				elements.remove(functionParent);
			} else {
				extension.replaceWith(extension.getArg());
			}
		}
	}
}
