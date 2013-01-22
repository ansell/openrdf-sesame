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
package org.openrdf.sail.federation.optimizers;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Remove StatementPatterns that have no statements.
 * 
 * @author James Leigh
 */
public class EmptyPatternOptimizer extends
		QueryModelVisitorBase<RepositoryException> implements QueryOptimizer {

	private final Collection<? extends RepositoryConnection> members;

	public EmptyPatternOptimizer(
			Collection<? extends RepositoryConnection> members) {
		super();
		this.members = members;
	}

	public void optimize(TupleExpr query, Dataset dataset, BindingSet bindings) {
		try {
			query.visit(this);
		} catch (RepositoryException e) {
			throw new UndeclaredThrowableException(e);
		}
	}

	@Override
	public void meet(StatementPattern node) throws RepositoryException {
		Resource subj = (Resource) node.getSubjectVar().getValue();
		URI pred = (URI) node.getPredicateVar().getValue();
		Value obj = node.getObjectVar().getValue();
		Resource[] ctx = getContexts(node.getContextVar());
		for (RepositoryConnection member : members) {
			if (member.hasStatement(subj, pred, obj, true, ctx)) {
				return;
			}
		}
		node.replaceWith(new EmptySet());
	}

	private Resource[] getContexts(Var var) {
		return (var == null || !var.hasValue()) ? new Resource[0]
				: new Resource[] { (Resource) var.getValue() };
	}

}
