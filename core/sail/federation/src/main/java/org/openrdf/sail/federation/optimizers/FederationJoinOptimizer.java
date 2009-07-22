/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.optimizers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.openrdf.sail.federation.signatures.SignedConnection;
import org.openrdf.store.StoreException;

/**
 * Search for Join, LeftJoin, and Union arguments that can be evaluated in a
 * single member.
 * 
 * @author James Leigh
 */
public class FederationJoinOptimizer extends QueryModelVisitorBase<StoreException> implements QueryOptimizer {

	private final List<SignedConnection> members;

	private final PrefixHashSet localSpace;

	private final boolean distinct;

	public FederationJoinOptimizer(List<SignedConnection> members, boolean distinct,
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
	public void meet(Join join)
		throws StoreException
	{
		super.meet(join);

		// Determine the "owners" of each join argument
		Map<TupleExpr, Set<SignedConnection>> exprOwnerMap = new HashMap<TupleExpr, Set<SignedConnection>>();

		for (TupleExpr joinArg : join.getArgs()) {
			exprOwnerMap.put(joinArg, getOwners(joinArg));
		}

		// Check for possible local joins and process these
		for (Set<TupleExpr> joinArgs : getLocalJoins(join)) {
			// Calculate the intersection of all owners
			Iterator<TupleExpr> iter = joinArgs.iterator();
			Set<SignedConnection> joinOwners = exprOwnerMap.remove(iter.next());
			while (iter.hasNext()) {
				joinOwners.retainAll(exprOwnerMap.remove(iter.next()));
			}

			Join localJoin = new Join(joinArgs);
			exprOwnerMap.put(localJoin, joinOwners);
		}

		// Join any join arguments that have the same, unique owner
		for (Set<TupleExpr> joinArgs : getColocatedJoins(exprOwnerMap)) {
			// FIXME: handle args that are not connected
			Set<SignedConnection> owners = null;
			for (TupleExpr joinArg : joinArgs) {
				owners = exprOwnerMap.remove(joinArg);
				assert owners.size() == 1;
			}

			Join localJoin = new Join(joinArgs);
			exprOwnerMap.put(localJoin, owners);
		}

		Join replacement = new Join();

		for (Map.Entry<TupleExpr, Set<SignedConnection>> entry : exprOwnerMap.entrySet()) {
			TupleExpr joinArg = entry.getKey();
			Set<SignedConnection> owners = entry.getValue();

			if (owners.isEmpty()) {
				// No results for this expression and thus for the entire join
				join.replaceWith(new EmptySet());
				return;
			}
			else if (owners.size() == 1) {
				SignedConnection owner = owners.iterator().next();
				replacement.addArg(new OwnedTupleExpr(owner, joinArg));
			}
			else if (joinArg instanceof Join || distinct) {
				// Local join with multiple owners or distinct federation members
				Union union = new Union();
				for (SignedConnection owner : owners) {
					union.addArg(new OwnedTupleExpr(owner, joinArg.clone()));
				}
				replacement.addArg(union);
			}
			else {
				replacement.addArg(joinArg);
			}
		}

		join.replaceWith(replacement);
		// addOwners(join, exprOwnerMap, localExprs);
	}

	private Collection<Set<TupleExpr>> getLocalJoins(Join join)
		throws StoreException
	{
		Map<String, Set<TupleExpr>> localJoins = new HashMap<String, Set<TupleExpr>>();

		for (TupleExpr joinArg : join.getArgs()) {
			Var subj = getLocalSubject(joinArg);

			if (subj != null) {
				Set<TupleExpr> localJoin = localJoins.get(subj.getName());
				if (localJoin == null) {
					localJoin = new HashSet<TupleExpr>();
					localJoins.put(subj.getName(), localJoin);
				}
				localJoin.add(joinArg);
			}
		}

		// Remove "joins" that exist of less than two expressions
		for (Iterator<Set<TupleExpr>> iter = localJoins.values().iterator(); iter.hasNext();) {
			if (iter.next().size() <= 1) {
				iter.remove();
			}
		}

		return localJoins.values();
	}

	/**
	 * Gets sets of join arguments that have the same, unique owner.
	 */
	private Collection<Set<TupleExpr>> getColocatedJoins(Map<TupleExpr, Set<SignedConnection>> exprOwnerMap)
	{
		Map<RepositoryConnection, Set<TupleExpr>> ownerExprMap = new HashMap<RepositoryConnection, Set<TupleExpr>>();

		for (Map.Entry<TupleExpr, Set<SignedConnection>> entry : exprOwnerMap.entrySet()) {
			if (entry.getValue().size() == 1) {
				RepositoryConnection owner = entry.getValue().iterator().next();

				Set<TupleExpr> joinArgs = ownerExprMap.get(owner);
				if (joinArgs == null) {
					joinArgs = new HashSet<TupleExpr>();
					ownerExprMap.put(owner, joinArgs = new HashSet<TupleExpr>());
				}
				joinArgs.add(entry.getKey());
			}
		}

		// Remove "joins" that exist of less than two expressions
		for (Iterator<Set<TupleExpr>> iter = ownerExprMap.values().iterator(); iter.hasNext();) {
			if (iter.next().size() <= 1) {
				iter.remove();
			}
		}

		return ownerExprMap.values();
	}

	@Override
	public void meet(LeftJoin leftJoin)
		throws StoreException
	{
		super.meet(leftJoin);

		Var leftSubject = getLocalSubject(leftJoin.getLeftArg());
		Var rightSubject = getLocalSubject(leftJoin.getRightArg());
		// if local then left and right can be combined
		boolean local = leftSubject != null && leftSubject.equals(rightSubject);
		SignedConnection leftOwner = getSingleOwner(leftJoin.getLeftArg());
		SignedConnection rightOwner = getSingleOwner(leftJoin.getRightArg());
		addOwners(leftJoin, leftOwner, rightOwner, local);
	}

	@Override
	public void meet(Union union)
		throws StoreException
	{
		super.meet(union);

		List<Owned<Union>> ownedJoins = new ArrayList<Owned<Union>>();
		for (TupleExpr arg : union.getArgs()) {
			SignedConnection member = getSingleOwner(arg);
			if (ownedJoins.size() > 0 && ownedJoins.get(ownedJoins.size() - 1).getOwner() == member) {
				ownedJoins.get(ownedJoins.size() - 1).getOperation().addArg(arg.clone());
			}
			else {
				ownedJoins.add(new Owned<Union>(member, new Union(arg.clone())));
			}
		}
		addOwners(union, ownedJoins);
	}

	@Override
	protected void meetUnaryTupleOperator(UnaryTupleOperator node)
		throws StoreException
	{
		super.meetUnaryTupleOperator(node);
		SignedConnection owner = getSingleOwner(node.getArg());
		if (owner != null) {
			node.replaceWith(new OwnedTupleExpr(owner, node.clone()));
		}
	}

	private static class Owned<O> {

		private SignedConnection owner;

		private O operation;

		public Owned(SignedConnection owner, O operation) {
			this.owner = owner;
			this.operation = operation;
		}

		public SignedConnection getOwner() {
			return owner;
		}

		public O getOperation() {
			return operation;
		}

		@Override
		public String toString() {
			return owner + "=" + operation;
		}
	}

	private class OwnerScanner extends QueryModelVisitorBase<StoreException> {

		private Set<SignedConnection> owners;

		/**
		 * If the argument can be sent to a single member.
		 */
		public Set<SignedConnection> getOwners(TupleExpr arg)
			throws StoreException
		{
			Set<SignedConnection> pre_owners = owners;
			try {
				owners = new HashSet<SignedConnection>();
				arg.visit(this);
				return owners;
			}
			finally {
				// restore
				owners = pre_owners;
			}
		}

		@Override
		public void meet(StatementPattern sp)
			throws StoreException
		{
			super.meet(sp);

			Resource subj = (Resource)sp.getSubjectVar().getValue();
			URI pred = (URI)sp.getPredicateVar().getValue();
			Value obj = sp.getObjectVar().getValue();

			Resource[] ctx;
			Var contextVar = sp.getContextVar();
			if (contextVar == null || !contextVar.hasValue()) {
				ctx = new Resource[0];
			}
			else {
				ctx = new Resource[] { (Resource)contextVar.getValue() };
			}

			for (SignedConnection member : members) {
				if (member.hasMatch(subj, pred, obj, true, ctx)) {
					owners.add(member);
				}
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
			owners.add(node.getOwner());
		}
	}

	private class LocalScanner extends QueryModelVisitorBase<StoreException> {

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
	private SignedConnection getSingleOwner(TupleExpr arg)
		throws StoreException
	{
		Set<SignedConnection> owners = getOwners(arg);
		if (owners.size() == 1) {
			return owners.iterator().next();
		}
		return null;
	}

	/**
	 * If the argument can be sent to a single member.
	 */
	private Set<SignedConnection> getOwners(TupleExpr arg)
		throws StoreException
	{
		return new OwnerScanner().getOwners(arg);
	}

	/**
	 * If the argument can be sent as a group to the members.
	 */
	private Var getLocalSubject(TupleExpr arg)
		throws StoreException
	{
		return new LocalScanner().getLocalSubject(arg);
	}

	private void addOwners(LeftJoin node, SignedConnection leftOwner, SignedConnection rightOwner,
			boolean local)
	{
		if (leftOwner == null && rightOwner == null) {
			if (local) {
				Union union = new Union();
				for (SignedConnection member : members) {
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
					for (SignedConnection member : members) {
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

	private void addOwners(Union node, List<Owned<Union>> ownedJoins) {
		if (ownedJoins.size() == 1) {
			SignedConnection o = ownedJoins.get(0).getOwner();
			if (o != null) {
				// every element is used by the same owner
				node.replaceWith(new OwnedTupleExpr(o, node.clone()));
			}
		}
		else {
			Union replacement = new Union();
			for (Owned<Union> e : ownedJoins) {
				SignedConnection o = e.getOwner();
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
