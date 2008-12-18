package org.openrdf.sail.federation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.IteratorCursor;
import org.openrdf.model.LiteralFactory;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.URIFactory;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeFactoryImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.cursors.DistinctCursor;
import org.openrdf.query.algebra.evaluation.cursors.UnionCursor;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryModelPruner;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.helpers.PrefixHashSet;
import org.openrdf.result.ContextResult;
import org.openrdf.result.ModelResult;
import org.openrdf.result.NamespaceResult;
import org.openrdf.result.Result;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.federation.evaluation.FederationStatistics;
import org.openrdf.sail.federation.evaluation.FederationStrategy;
import org.openrdf.sail.federation.members.MemberConnection;
import org.openrdf.sail.federation.optimizers.EmptyPatternOptimizer;
import org.openrdf.sail.federation.optimizers.FederationJoinOptimizer;
import org.openrdf.sail.federation.optimizers.OwnedTupleExprPruner;
import org.openrdf.sail.federation.optimizers.PrepareOwnedTupleExpr;
import org.openrdf.store.StoreException;

/**
 * Unions the results from multiple {@link RepositoryConnection} into one
 * {@link SailConnection}.
 * 
 * @author James Leigh
 */
abstract class FederationConnection implements SailConnection, TripleSource {

	private Logger logger = LoggerFactory.getLogger(FederationConnection.class);

	private Federation federation;

	private ValueFactory vf;

	List<RepositoryConnection> members;

	public FederationConnection(Federation federation, List<RepositoryConnection> members) {
		BNodeFactoryImpl bf = new BNodeFactoryImpl();
		List<RepositoryConnection> result = new ArrayList<RepositoryConnection>(members.size());
		for (RepositoryConnection member : members) {
			result.add(new MemberConnection(member, bf));
		}
		this.federation = federation;
		this.members = result;
		URIFactory uf = federation.getURIFactory();
		LiteralFactory lf = federation.getLiteralFactory();
		vf = new ValueFactoryImpl(bf, uf, lf);
	}

	public ValueFactory getValueFactory() {
		return vf;
	}

	public void close()
		throws StoreException
	{
		excute(new Procedure() {

			public void run(RepositoryConnection member)
				throws StoreException
			{
				member.close();
			}
		});
	}

	public Cursor<? extends Resource> getContextIDs()
		throws StoreException
	{
		return new DistinctCursor<Resource>(union(new Function<Resource>() {

			public ContextResult call(RepositoryConnection member)
				throws StoreException
			{
				return member.getContextIDs();
			}
		}));
	}

	public String getNamespace(String prefix)
		throws StoreException
	{
		String namespace = null;
		for (RepositoryConnection member : members) {
			String ns = member.getNamespace(prefix);
			if (namespace == null) {
				namespace = ns;
			}
			else if (ns != null && !ns.equals(namespace)) {
				return null;
			}
		}
		return namespace;
	}

	public Cursor<? extends Namespace> getNamespaces()
		throws StoreException
	{
		Map<String, Namespace> namespaces = new HashMap<String, Namespace>();
		Set<String> prefixes = new HashSet<String>();
		for (RepositoryConnection member : members) {
			NamespaceResult ns = member.getNamespaces();
			while (ns.hasNext()) {
				Namespace next = ns.next();
				String prefix = next.getPrefix();
				if (prefixes.add(prefix)) {
					namespaces.put(prefix, next);
				}
				else if (!next.equals(namespaces.get(prefix))) {
					namespaces.remove(prefix);
				}
			}
		}
		return new IteratorCursor<Namespace>(namespaces.values().iterator());
	}

	public long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		if (federation.isDisjoint()) {
			long size = 0;
			for (RepositoryConnection member : members) {
				size += member.sizeMatch(subj, pred, obj, includeInferred, contexts);
			}
			return size;
		}
		else {
			Cursor<? extends Statement> cursor;
			cursor = getStatements(subj, pred, obj, includeInferred, contexts);
			try {
				long size = 0;
				while (cursor.next() != null) {
					size++;
				}
				return size;
			}
			finally {
				cursor.close();
			}
		}
	}

	public Cursor<? extends Statement> getStatements(final Resource subj, final URI pred, final Value obj,
			final boolean includeInferred, final Resource... contexts)
		throws StoreException
	{
		Cursor<? extends Statement> cursor = union(new Function<Statement>() {

			public ModelResult call(RepositoryConnection member)
				throws StoreException
			{
				return member.match(subj, pred, obj, includeInferred, contexts);
			}
		});
		if (federation.isDisjoint() || isLocal(pred))
			return cursor;
		return new DistinctCursor<Statement>(cursor);
	}

	public Cursor<? extends Statement> getStatements(final Resource subj, final URI pred, final Value obj,
			final Resource... contexts)
		throws StoreException
	{
		Cursor<? extends Statement> cursor = union(new Function<Statement>() {

			public ModelResult call(RepositoryConnection member)
				throws StoreException
			{
				return member.match(subj, pred, obj, true, contexts);
			}
		});
		if (federation.isDisjoint() || isLocal(pred))
			return cursor;
		return new DistinctCursor<Statement>(cursor);
	}

	public Cursor<? extends BindingSet> evaluate(QueryModel query, BindingSet bindings, boolean includeInferred)
		throws StoreException
	{
		EvaluationStrategyImpl strategy;
		strategy = new FederationStrategy(this, query);
		TupleExpr qry = optimize(query, bindings, strategy);
		return strategy.evaluate(qry, EmptyBindingSet.getInstance());
	}

	interface Procedure {

		void run(RepositoryConnection member)
			throws StoreException;
	}

	void excute(Procedure operation)
		throws StoreException
	{
		StoreException store = null;
		RuntimeException runtime = null;
		for (RepositoryConnection member : members) {
			try {
				operation.run(member);
			}
			catch (StoreException e) {
				logger.error(e.toString(), e);
				if (store != null) {
					store = e;
				}
			}
			catch (RuntimeException e) {
				logger.error(e.toString(), e);
				if (runtime != null) {
					runtime = e;
				}

			}
		}
		if (store != null)
			throw store;
		if (runtime != null)
			throw runtime;
	}

	private interface Function<E> {

		public abstract Result<E> call(RepositoryConnection member)
			throws StoreException;
	}

	private boolean isLocal(URI pred) {
		if (pred == null)
			return false;
		PrefixHashSet hash = federation.getLocalPropertySpace();
		if (hash == null)
			return false;
		return hash.match(pred.stringValue());
	}

	private <E> Cursor<? extends E> union(Function<E> converter)
		throws StoreException
	{
		List<Cursor<? extends E>> cursors = new ArrayList<Cursor<? extends E>>(members.size());
		try {
			for (RepositoryConnection member : members) {
				cursors.add(converter.call(member));
			}
			return new UnionCursor<E>(cursors);
		}
		catch (StoreException e) {
			closeAll(cursors);
			throw e;
		}
		catch (RuntimeException e) {
			closeAll(cursors);
			throw e;
		}
	}

	private <E> void closeAll(Iterable<? extends Cursor<? extends E>> cursors) {
		for (Cursor<? extends E> cursor : cursors) {
			try {
				cursor.close();
			}
			catch (StoreException e) {
				logger.error(e.toString(), e);
			}
		}
	}

	private QueryModel optimize(QueryModel parsed, BindingSet bindings, EvaluationStrategyImpl strategy)
		throws StoreException
	{
		logger.trace("Incoming query model:\n{}", parsed.toString());

		// Clone the tuple expression to allow for more aggressive optimisations
		QueryModel query = parsed.clone();

		new BindingAssigner().optimize(query, bindings);
		new ConstantOptimizer(strategy).optimize(query, bindings);
		new CompareOptimizer().optimize(query, bindings);
		new ConjunctiveConstraintSplitter().optimize(query, bindings);
		new DisjunctiveConstraintOptimizer().optimize(query, bindings);
		new SameTermFilterOptimizer().optimize(query, bindings);
		new QueryModelPruner().optimize(query, bindings);

		FederationStatistics statistics = new FederationStatistics(members, query);
		new QueryJoinOptimizer(statistics).optimize(query, bindings);
		new FilterOptimizer().optimize(query, bindings);

		new EmptyPatternOptimizer(members).optimize(query, bindings);
		boolean disjoint = federation.isDisjoint();
		PrefixHashSet local = federation.getLocalPropertySpace();
		new FederationJoinOptimizer(members, disjoint, local).optimize(query, bindings);
		new OwnedTupleExprPruner().optimize(query, bindings);
		new QueryModelPruner().optimize(query, bindings);
		new QueryJoinOptimizer(statistics).optimize(query, bindings);
		statistics.await(); // let statistics throw any exceptions it has

		new PrepareOwnedTupleExpr(federation.getMetaData()).optimize(query, bindings);

		logger.trace("Optimized query model:\n{}", query.toString());
		return query;
	}

}
