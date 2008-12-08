/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.optimizers;

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
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.helpers.PrefixHashSet;
import org.openrdf.sail.federation.algebra.OwnedTupleExpr;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class FederationJoinOptimizer extends QueryModelVisitorBase<StoreException> implements QueryOptimizer {

	Collection<RepositoryConnection> members;

	PrefixHashSet localSpace;

	public FederationJoinOptimizer(Collection<RepositoryConnection> members, PrefixHashSet localSpace) {
		this.members = members;
		this.localSpace = localSpace;
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
		Map<Var, Join> vars = new LinkedHashMap<Var, Join>();
		Scanner scanner = new Scanner();
		for (TupleExpr arg : node.getArgs()) {
			RepositoryConnection member = scanner.getSingleOwner(arg);
			if (map.containsKey(member)) {
				map.get(member).addArg(arg.clone());
			}
			else {
				map.put(member, new Join(arg.clone()));
			}
		}
		for (TupleExpr arg : node.getArgs()) {
			Var subj = scanner.getLocalSubject(arg);
			if (vars.containsKey(subj)) {
				vars.get(subj).addArg(arg.clone());
			}
			else {
				vars.put(subj, new Join(arg.clone()));
			}
		}
		addOwners(node, map, vars);
	}

	@Override
	public void meet(LeftJoin node)
		throws StoreException
	{
		Scanner scanner = new Scanner();
		Var leftSubject = scanner.getLocalSubject(node.getLeftArg());
		Var rightSubject = scanner.getLocalSubject(node.getRightArg());
		// if local then left and right can be combined
		boolean local = leftSubject != null && leftSubject.equals(rightSubject);
		RepositoryConnection leftOwner = scanner.getSingleOwner(node.getLeftArg());
		RepositoryConnection rightOwner = scanner.getSingleOwner(node.getRightArg());
		addOwners(node, leftOwner, rightOwner, local);
	}

	@Override
	public void meet(Union node)
		throws StoreException
	{
		Scanner scanner = new Scanner();
		Map<RepositoryConnection, Union> map = new LinkedHashMap<RepositoryConnection, Union>();
		for (TupleExpr arg : node.getArgs()) {
			RepositoryConnection member = scanner.getSingleOwner(arg);
			if (map.containsKey(member)) {
				map.get(member).addArg(arg.clone());
			}
			else {
				map.put(member, new Union(arg.clone()));
			}
		}
		addOwners(node, map);
	}

	class Scanner extends QueryModelVisitorBase<StoreException> {

		private boolean local;

		private Var relative;

		private boolean shared;

		private RepositoryConnection owner;

		/**
		 * If the argument can be sent to a single member.
		 */
		public RepositoryConnection getSingleOwner(TupleExpr arg)
			throws StoreException
		{
			boolean pre_shared = shared;
			RepositoryConnection pre_owner = owner;
			try {
				shared = false;
				owner = null;
				arg.visit(this);
				return owner;
			}
			finally {
				// restore
				shared = pre_shared;
				owner = pre_owner;
			}
		}

		/**
		 * If the argument can be sent as a group to the members.
		 */
		public Var getLocalSubject(TupleExpr arg)
			throws StoreException
		{
			boolean local_stack = local;
			Var relative_stack = relative;
			try {
				local = true;
				relative = null;
				arg.visit(this);
				return relative;
			}
			finally {
				// restore
				local = local_stack;
				relative = relative_stack;
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
			}
			else {
				usedBy(member);
			}
			if (pred != null && localSpace != null && localSpace.match(pred.stringValue())) {
				local(node.getSubjectVar());
			}
			else {
				notLocal();
			}
		}

		private Resource[] getContexts(Var var) {
			if (var == null || !var.hasValue())
				return new Resource[0];
			return new Resource[] { (Resource)var.getValue() };
		}

		private RepositoryConnection getSingleOwner(Resource subj, URI pred, Value obj, Resource[] ctx)
			throws StoreException
		{
			RepositoryConnection o = null;
			for (RepositoryConnection member : members) {
				if (member.hasStatement(subj, pred, obj, true, ctx)) {
					if (o == null) {
						o = member;
					}
					else if (o != member) {
						return null;
					}
				}
			}
			return o;
		}

		private void local(Var subj) {
			if (local && relative == null) {
				relative = subj;
			}
			else if (!subj.equals(relative)) {
				notLocal();
			}
		}

		private void notLocal() {
			local = false;
			relative = null;
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

	}

	private void addOwners(Join node, Map<RepositoryConnection, Join> map, Map<Var, Join> vars)
		throws StoreException
	{
		boolean local = false;
		if (vars.size() > 1 || !vars.containsKey(null)) {
			for (Map.Entry<Var, Join> e : vars.entrySet()) {
				if (e.getKey() != null && e.getValue().getNumberOfArguments() > 1) {
					local = true;
					break;
				}
			}
		}
		if (map.size() == 1) {
			RepositoryConnection o = map.keySet().iterator().next();
			if (o == null) {
				// every element has multiple owners
				if (local) {
					Join replacement = new Join();
					for (Join j : vars.values()) {
						Union union = new Union();
						for (RepositoryConnection member : members) {
							union.addArg(new OwnedTupleExpr(member, j.clone()));
						}
						replacement.addArg(union);
					}
					node.replaceWith(replacement);
				}
			}
			else {
				// every element is used by the same owner
				node.replaceWith(new OwnedTupleExpr(o, node.clone()));
			}
		}
		else if (local) {
			Scanner scanner = new Scanner();
			Join replacement = new Join();
			for (Join j : vars.values()) {
				RepositoryConnection owner = scanner.getSingleOwner(j);
				if (owner == null) {
					Union union = new Union();
					for (RepositoryConnection member : members) {
						union.addArg(new OwnedTupleExpr(member, j.clone()));
					}
					replacement.addArg(union);
				}
				else {
					replacement.addArg(new OwnedTupleExpr(owner, j));
				}
			}
			node.replaceWith(replacement);
		}
		else {
			Join replacement = new Join();
			for (Entry<RepositoryConnection, Join> e : map.entrySet()) {
				RepositoryConnection o = e.getKey();
				Join join = e.getValue();
				if (o == null) {
					// multiple owners
					for (TupleExpr arg : join.getArgs()) {
						replacement.addArg(arg);
					}
				}
				else {
					replacement.addArg(new OwnedTupleExpr(o, join));
				}
			}
			node.replaceWith(replacement);
		}
	}

	private void addOwners(LeftJoin node, RepositoryConnection leftOwner, RepositoryConnection rightOwner,
			boolean local)
	{
		if (leftOwner == null && rightOwner == null) {
			if (local) {
				Union union = new Union();
				for (RepositoryConnection member : members) {
					union.addArg(new OwnedTupleExpr(member, node.clone()));
				}
				node.replaceWith(union);
			}
		}
		else if (leftOwner == rightOwner) {
			node.replaceWith(new OwnedTupleExpr(leftOwner, node.clone()));
		}
		else {
			if (local) {
				if (rightOwner == null) {
					node.replaceWith(new OwnedTupleExpr(leftOwner, node.clone()));
				}
				else if (leftOwner == null) {
					Union union = new Union();
					for (RepositoryConnection member : members) {
						if (rightOwner == member) {
							union.addArg(new OwnedTupleExpr(member, node.clone()));
						}
						else {
							union.addArg(new OwnedTupleExpr(member, node.getLeftArg().clone()));
						}
					}
					node.replaceWith(union);
				}
				else {
					node.replaceWith(new OwnedTupleExpr(leftOwner, node.getLeftArg().clone()));
				}
			}
			else {
				if (leftOwner != null) {
					node.getLeftArg().replaceWith(new OwnedTupleExpr(leftOwner, node.getLeftArg().clone()));
				}
				if (rightOwner != null) {
					node.getRightArg().replaceWith(new OwnedTupleExpr(rightOwner, node.getRightArg().clone()));
				}
			}
		}
	}

	private void addOwners(Union node, Map<RepositoryConnection, Union> map) {
		if (map.size() == 1) {
			RepositoryConnection o = map.keySet().iterator().next();
			if (o != null) {
				// every element is used by the same owner
				node.replaceWith(new OwnedTupleExpr(o, node.clone()));
			}
		}
		else {
			Union replacement = new Union();
			for (Entry<RepositoryConnection, Union> e : map.entrySet()) {
				RepositoryConnection o = e.getKey();
				Union union = e.getValue();
				if (o == null) {
					// multiple owners
					for (TupleExpr arg : union.getArgs()) {
						replacement.addArg(arg.clone());
					}
				}
				else {
					replacement.addArg(new OwnedTupleExpr(o, union));
				}
			}
			node.replaceWith(replacement);
		}
	}

}
