/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.federation.algebra.OwnedTupleExpr;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class FederationJoinOptimizer extends QueryModelVisitorBase<StoreException> implements QueryOptimizer {

	private Collection<RepositoryConnection> members;

	private boolean shared;

	private RepositoryConnection owner;

	public FederationJoinOptimizer(Collection<RepositoryConnection> members) {
		this.members = members;
	}

	public void optimize(QueryModel query, BindingSet bindings)
		throws StoreException
	{
		query.visit(this);
	}

	@Override
	public void meet(Join node)
		throws StoreException
	{
		Map<RepositoryConnection, Join> map = new LinkedHashMap<RepositoryConnection, Join>(); 
		for (TupleExpr arg : node.getArgs()) {
			RepositoryConnection member = getSingleOwner(arg);
			if (map.containsKey(member)) {
				map.get(member).addArg(arg.clone());
			} else {
				map.put(member, new Join(arg.clone()));
			}
		}
		if (map.size() == 1) {
			RepositoryConnection o = map.keySet().iterator().next();
			if (o == null) {
				// every element has multiple owners
				multipleOwners();
			} else {
				// every element is used by the same owner
				usedBy(o);
				node.replaceWith(new OwnedTupleExpr(o, node.clone()));
			}
		} else {
			Join replacement = new Join();
			for (Entry<RepositoryConnection, Join> e : map.entrySet()) {
				RepositoryConnection o = e.getKey();
				Join join = e.getValue();
				if (o == null) {
					// multiple owners
					for (TupleExpr arg : join.getArgs()) {
						replacement.addArg(arg);
					}
				} else if (1 == node.getNumberOfArguments()) {
					replacement.addArg(new OwnedTupleExpr(o, join.getArg(0)));
				} else {
					replacement.addArg(new OwnedTupleExpr(o, join));
				}
			}
			multipleOwners();
			node.replaceWith(replacement);
		}
	}

	@Override
	public void meet(LeftJoin node)
		throws StoreException
	{
		TupleExpr left = node.getLeftArg();
		TupleExpr right = node.getRightArg();
		RepositoryConnection leftOwner = getSingleOwner(left);
		RepositoryConnection rightOwner = getSingleOwner(right);
		if (leftOwner == null && rightOwner == null) {
			multipleOwners();
		} else if (leftOwner == rightOwner) {
			usedBy(leftOwner);
			node.replaceWith(new OwnedTupleExpr(leftOwner, node.clone()));
		} else {
			multipleOwners();
			if (leftOwner != null) {
				left.replaceWith(new OwnedTupleExpr(leftOwner, left.clone()));
			}
			if (rightOwner != null) {
				right.replaceWith(new OwnedTupleExpr(rightOwner, right.clone()));
			}
		}
	}

	@Override
	public void meet(Union node)
		throws StoreException
	{
		Map<RepositoryConnection, Union> map = new LinkedHashMap<RepositoryConnection, Union>(); 
		for (TupleExpr arg : node.getArgs()) {
			RepositoryConnection member = getSingleOwner(arg);
			if (map.containsKey(member)) {
				map.get(member).addArg(arg.clone());
			} else {
				map.put(member, new Union(arg.clone()));
			}
		}
		if (map.size() == 1) {
			RepositoryConnection o = map.keySet().iterator().next();
			if (o == null) {
				// every element has multiple owners
				multipleOwners();
			} else {
				// every element is used by the same owner
				usedBy(o);
				node.replaceWith(new OwnedTupleExpr(o, node.clone()));
			}
		} else {
			Union replacement = new Union();
			for (Entry<RepositoryConnection, Union> e : map.entrySet()) {
				RepositoryConnection o = e.getKey();
				Union union = e.getValue();
				if (o == null) {
					// multiple owners
					for (TupleExpr arg : union.getArgs()) {
						replacement.addArg(arg.clone());
					}
				} else if (1 == node.getNumberOfArguments()) {
					replacement.addArg(new OwnedTupleExpr(o, union.getArg(0)));
				} else {
					replacement.addArg(new OwnedTupleExpr(o, union));
				}
			}
			multipleOwners();
			node.replaceWith(replacement);
		}
	}

	@Override
	public void meetOther(QueryModelNode node)
		throws StoreException
	{
		if (node instanceof OwnedTupleExpr) {
			OwnedTupleExpr owned = (OwnedTupleExpr) node;
			// no need to go look at the children
			usedBy(owned.getOwner());
		} else {
			super.meetOther(node);
		}
	}

	@Override
	public void meet(StatementPattern node)
		throws StoreException
	{
		super.meet(node);
		Resource subj = (Resource)node.getSubjectVar().getValue();
		URI pred = (URI)node.getPredicateVar().getValue();
		Value obj = node.getObjectVar().getValue();
		Resource[] ctx = getContexts(node.getContextVar());
		RepositoryConnection member = getSingleOwner(subj, pred, obj, ctx);
		if (member == null) {
			multipleOwners();
		} else {
			usedBy(member);
			node.replaceWith(new OwnedTupleExpr(member, node.clone()));
		}
	}

	private RepositoryConnection getSingleOwner(Resource subj, URI pred, Value obj, Resource[] ctx)
		throws StoreException
	{
		RepositoryConnection o = null;
		for (RepositoryConnection member : members) {
			if (member.hasStatement(subj, pred, obj, true, ctx)) {
				if (o == null) {
					o = member;
				} else if (o != member){
					return null;
				}
			}
		}
		return o;
	}

	/**
	 * If the argument can be sent to a single member.
	 */
	private RepositoryConnection getSingleOwner(TupleExpr arg)
		throws StoreException
	{
		boolean pre_shared = shared;
		RepositoryConnection pre_owner = owner;
		try {
			shared = false;
			owner = null;
			arg.visit(this);
			return owner;
		} finally {
			// restore
			shared = pre_shared;
			owner = pre_owner;
		}
	}

	private void usedBy(RepositoryConnection member) {
		if (!shared && owner == null) {
			// first owner sensitive element
			owner = member;
		}
		else if (owner != member) {
			multipleOwners();
		}
	}

	private void multipleOwners() {
		owner = null;
		shared = true;
	}

	private Resource[] getContexts(Var var) {
		if (var == null || !var.hasValue())
			return new Resource[0];
		return new Resource[] { (Resource)var.getValue() };
	}

}
