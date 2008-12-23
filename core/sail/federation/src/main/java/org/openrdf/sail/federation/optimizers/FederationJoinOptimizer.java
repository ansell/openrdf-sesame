/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.optimizers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.helpers.PrefixHashSet;
import org.openrdf.sail.federation.algebra.OwnedTupleExpr;
import org.openrdf.store.StoreException;

/**
 * Search for Join, LeftJoin, and Union arguments that can be evaluated in a
 * single member.
 * 
 * @author James Leigh
 */
public class FederationJoinOptimizer extends QueryModelVisitorBase<StoreException> implements QueryOptimizer {

	Collection<? extends RepositoryConnection> members;

	PrefixHashSet localSpace;

	boolean distinct;

	public FederationJoinOptimizer(Collection<? extends RepositoryConnection> members, boolean distinct,
			PrefixHashSet localSpace)
	{
		this.members = members;
		this.localSpace = localSpace;
		this.distinct = distinct;
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
		super.meet(node);
		List<Owned<Join>> ows = new ArrayList<Owned<Join>>();
		List<LocalJoin> vars = new ArrayList<LocalJoin>();
		for (TupleExpr arg : node.getArgs()) {
			RepositoryConnection member = getSingleOwner(arg);
			if (ows.size() > 0 && ows.get(ows.size() - 1).getOwner() == member) {
				ows.get(ows.size() - 1).getOperation().addArg(arg.clone());
			}
			else {
				ows.add(new Owned<Join>(member, new Join(arg.clone())));
			}
		}
		for (TupleExpr arg : node.getArgs()) {
			Var subj = getLocalSubject(arg);
			if (vars.size() > 0 && vars.get(vars.size() - 1).getVar() == subj) {
				vars.get(vars.size() - 1).getJoin().addArg(arg.clone());
			}
			else {
				vars.add(new LocalJoin(subj, new Join(arg.clone())));
			}
		}
		addOwners(node, ows, vars);
	}

	@Override
	public void meet(LeftJoin node)
		throws StoreException
	{
		super.meet(node);
		Var leftSubject = getLocalSubject(node.getLeftArg());
		Var rightSubject = getLocalSubject(node.getRightArg());
		// if local then left and right can be combined
		boolean local = leftSubject != null && leftSubject.equals(rightSubject);
		RepositoryConnection leftOwner = getSingleOwner(node.getLeftArg());
		RepositoryConnection rightOwner = getSingleOwner(node.getRightArg());
		addOwners(node, leftOwner, rightOwner, local);
	}

	@Override
	public void meet(Union node)
		throws StoreException
	{
		super.meet(node);
		List<Owned<Union>> ows = new ArrayList<Owned<Union>>();
		for (TupleExpr arg : node.getArgs()) {
			RepositoryConnection member = getSingleOwner(arg);
			if (ows.size() > 0 && ows.get(ows.size() - 1).getOwner() == member) {
				ows.get(ows.size() - 1).getOperation().addArg(arg.clone());
			}
			else {
				ows.add(new Owned<Union>(member, new Union(arg.clone())));
			}
		}
		addOwners(node, ows);
	}

	@Override
	protected void meetUnaryTupleOperator(UnaryTupleOperator node)
		throws StoreException
	{
		super.meetUnaryTupleOperator(node);
		RepositoryConnection owner = getSingleOwner(node.getArg());
		if (owner != null) {
			node.replaceWith(new OwnedTupleExpr(owner, node.clone()));
		}
	}

	class Owned<O> {

		private RepositoryConnection owner;

		private O operation;

		public Owned(RepositoryConnection owner, O operation) {
			this.owner = owner;
			this.operation = operation;
		}

		public RepositoryConnection getOwner() {
			return owner;
		}

		public O getOperation() {
			return operation;
		}
	}

	class LocalJoin {

		private Var var;

		private Join join;

		public LocalJoin(Var key, Join value) {
			this.var = key;
			this.join = value;
		}

		public Var getVar() {
			return var;
		}

		public Join getJoin() {
			return join;
		}
	}

	class OwnerScanner extends QueryModelVisitorBase<StoreException> {

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
			usedBy(node.getOwner());
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
				if (member.hasMatch(subj, pred, obj, true, ctx)) {
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

	class LocalScanner extends QueryModelVisitorBase<StoreException> {

		private boolean local;

		private Var relative;

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
			URI pred = (URI)node.getPredicateVar().getValue();
			if (pred != null && localSpace != null && localSpace.match(pred.stringValue())) {
				local(node.getSubjectVar());
			}
			else {
				notLocal();
			}
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

	}

	/**
	 * If the argument can be sent to a single member.
	 */
	private RepositoryConnection getSingleOwner(TupleExpr arg)
		throws StoreException
	{
		return new OwnerScanner().getSingleOwner(arg);
	}

	/**
	 * If the argument can be sent as a group to the members.
	 */
	private Var getLocalSubject(TupleExpr arg)
		throws StoreException
	{
		return new LocalScanner().getLocalSubject(arg);
	}

	private void addOwners(Join node, List<Owned<Join>> ows, List<LocalJoin> vars)
		throws StoreException
	{
		boolean local = false;
		if (vars.size() > 1 || vars.size() == 1 && vars.get(0).getVar() != null) {
			for (LocalJoin e : vars) {
				if (e.getVar() != null && e.getJoin().getNumberOfArguments() > 1) {
					local = true;
					break;
				}
			}
		}
		if (ows.size() == 1) {
			RepositoryConnection o = ows.get(0).getOwner();
			if (o == null) {
				// every element has multiple owners
				if (local) {
					Join replacement = new Join();
					for (LocalJoin e : vars) {
						if (distinct || e.getVar() != null) {
							Union union = new Union();
							for (RepositoryConnection member : members) {
								union.addArg(new OwnedTupleExpr(member, e.getJoin().clone()));
							}
							replacement.addArg(union);
						}
						else {
							for (TupleExpr expr : e.getJoin().getArgs()) {
								replacement.addArg(expr);
							}
						}
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
			Join replacement = new Join();
			for (LocalJoin v : vars) {
				Var var = v.getVar();
				Join j = v.getJoin();
				if (var == null) {
					// each of these arguments could be anywhere
					for (TupleExpr expr : j.getArgs()) {
						replacement.addArg(expr);
					}
				}
				else {
					boolean multipleOwners = false;
					RepositoryConnection owner = null;
					for (TupleExpr expr : j.getArgs()) {
						RepositoryConnection o = getSingleOwner(expr);
						if (owner == null) {
							owner = o;
						}
						else if (o != null && owner != o) {
							multipleOwners = true;
							owner = null;
							break;
						}
					}
					if (multipleOwners) {
						// no subject exists on multiple members,
						// but in the same member
						node.replaceWith(new EmptySet());
						return;
					}
					else if (owner == null) {
						// these arguments might exist in any member,
						// but they will only join with the same member.
						Union union = new Union();
						for (RepositoryConnection member : members) {
							union.addArg(new OwnedTupleExpr(member, j.clone()));
						}
						replacement.addArg(union);
					}
					else {
						// there is only one member that can join all these arguments
						addArg(replacement, new OwnedTupleExpr(owner, j));
					}
				}
			}
			node.replaceWith(replacement);
		}
		else {
			Join replacement = new Join();
			for (Owned<Join> e : ows) {
				RepositoryConnection o = e.getOwner();
				Join join = e.getOperation();
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

	private void addArg(Join destination, OwnedTupleExpr newArg) {
		boolean found = false;
		// if the last owner is the same then combine
		int size = destination.getNumberOfArguments();
		if (size > 0) {
			TupleExpr expr = destination.getArg(size - 1);
			if (expr instanceof OwnedTupleExpr) {
				OwnedTupleExpr existing = (OwnedTupleExpr)expr;
				boolean sameOwner = newArg.getOwner() == existing.getOwner();
				if (sameOwner && existing.getArg() instanceof Join) {
					// already added this owner
					Join existingJoin = (Join)existing.getArg();
					Join newJoin = (Join)newArg.getArg();
					for (TupleExpr t : newJoin.getArgs()) {
						existingJoin.addArg(t);
					}
					found = true;
				}
			}
		}
		if (!found) {
			destination.addArg(newArg);
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

	private void addOwners(Union node, List<Owned<Union>> ows) {
		if (ows.size() == 1) {
			RepositoryConnection o = ows.get(0).getOwner();
			if (o != null) {
				// every element is used by the same owner
				node.replaceWith(new OwnedTupleExpr(o, node.clone()));
			}
		}
		else {
			Union replacement = new Union();
			for (Owned<Union> e : ows) {
				RepositoryConnection o = e.getOwner();
				Union union = e.getOperation();
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
