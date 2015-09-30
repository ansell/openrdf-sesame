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
package org.openrdf.sail.federation.optimizers;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;

import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.AbstractQueryModelVisitor;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Remove StatementPatterns that have no statements.
 * 
 * @author James Leigh
 */
public class EmptyPatternOptimizer extends AbstractQueryModelVisitor<RepositoryException> implements
		QueryOptimizer
{

	private final Collection<? extends RepositoryConnection> members;

	public EmptyPatternOptimizer(Collection<? extends RepositoryConnection> members) {
		super();
		this.members = members;
	}

	public void optimize(TupleExpr query, Dataset dataset, BindingSet bindings) {
		try {
			query.visit(this);
		}
		catch (RepositoryException e) {
			throw new UndeclaredThrowableException(e);
		}
	}

	@Override
	public void meet(StatementPattern node)
		throws RepositoryException
	{
		Resource subj = (Resource)node.getSubjectVar().getValue();
		IRI pred = (IRI)node.getPredicateVar().getValue();
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
		return (var == null || !var.hasValue()) ? new Resource[0] : new Resource[] { (Resource)var.getValue() };
	}

}
