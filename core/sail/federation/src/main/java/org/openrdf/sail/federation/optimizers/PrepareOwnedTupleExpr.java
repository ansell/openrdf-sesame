/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.optimizers;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ExternalSet;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryMetaData;
import org.openrdf.sail.SailMetaData;
import org.openrdf.sail.federation.algebra.OwnedTupleExpr;
import org.openrdf.sail.federation.query.QueryModelSerializer;
import org.openrdf.store.StoreException;

/**
 * Remove redundent {@link OwnedTupleExpr}.
 * 
 * @author James Leigh
 */
public class PrepareOwnedTupleExpr extends QueryModelVisitorBase<StoreException> implements QueryOptimizer {

	private SailMetaData metadata;

	private OwnedTupleExpr owned;

	public PrepareOwnedTupleExpr(SailMetaData metadata) {
		this.metadata = metadata;
	}

	public void optimize(QueryModel query, BindingSet bindings)
		throws StoreException
	{
		query.visit(this);
	}

	@Override
	public void meetOther(QueryModelNode node)
		throws StoreException
	{
		if (node instanceof OwnedTupleExpr) {
			meetOwnedTupleExpr((OwnedTupleExpr)node);
		}
		else {
			super.meetOther(node);
		}
	}

	private void meetOwnedTupleExpr(OwnedTupleExpr node)
		throws StoreException
	{
		if (isRemoteQueryModelSupported(node.getOwner(), node.getArg())) {
			owned = node;
			// continue to look for signed BNodes
			super.meetOther(node);
			node.prepare();
			owned = null;
		}
	}

	@Override
	public void meet(ValueConstant node)
		throws StoreException
	{
		Value value = node.getValue();
		if (owned != null && value instanceof BNode) {
			node.setValue(owned.getBNode((BNode) node.getValue()));
		}
	}

	@Override
	public void meet(Var node)
		throws StoreException
	{
		Value value = node.getValue();
		if (owned != null && value instanceof BNode) {
			node.setValue(owned.getBNode((BNode) node.getValue()));
		}
	}

	private boolean isRemoteQueryModelSupported(RepositoryConnection owner, TupleExpr expr)
		throws StoreException
	{
		if (expr instanceof StatementPattern) {
			return false;
		}
		if (((TupleExpr)expr).getBindingNames().size() == 0) {
			return false;
		}
		if (expr instanceof ExternalSet) {
			return false;
		}
		RepositoryMetaData md = owner.getRepository().getMetaData();
		int version = metadata.getSesameMajorVersion();
		if (version != 0 && version != md.getSesameMajorVersion()) {
			return false;
		}
		if (version != 0 && metadata.getSesameMinorVersion() != md.getSesameMinorVersion()) {
			return false;
		}
		for (QueryLanguage ql : md.getQueryLanguages()) {
			if (QueryModelSerializer.LANGUAGE.equals(ql)) {
				return true;
			}
		}
		return false;
	}

}
