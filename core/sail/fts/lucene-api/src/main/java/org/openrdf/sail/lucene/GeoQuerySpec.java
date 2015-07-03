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
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.sail.SailException;

public class GeoQuerySpec implements SearchQueryEvaluator {
	private QueryModelNode functionParent;
	private Literal from;
	private String to;
	private URI units;
	private double distance;
	private String distanceVar;
	private StatementPattern geoStatement;
	private Filter filter;

	public void setFunctionParent(QueryModelNode functionParent) {
		this.functionParent = functionParent;
	}

	public void setFrom(Literal from) {
		this.from = from;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setUnits(URI units) {
		this.units = units;
	}

	public void setDistance(double d) {
		this.distance = d;
	}

	public void setDistanceVar(String varName) {
		this.distanceVar = varName;
	}

	public void setGeometryPattern(StatementPattern sp) {
		this.geoStatement = sp;
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
	public Collection<BindingSet> evaluate(SearchIndex searchIndex)
			throws SailException {
		// TODO Auto-generated method stub
		return null;
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
