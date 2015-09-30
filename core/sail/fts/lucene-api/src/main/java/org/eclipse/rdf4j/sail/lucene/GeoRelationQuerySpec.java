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

import java.util.List;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.query.algebra.EmptySet;
import org.eclipse.rdf4j.query.algebra.Extension;
import org.eclipse.rdf4j.query.algebra.ExtensionElem;
import org.eclipse.rdf4j.query.algebra.Filter;
import org.eclipse.rdf4j.query.algebra.QueryModelNode;
import org.eclipse.rdf4j.query.algebra.SingletonSet;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.Var;

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
