/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.members;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.cursor.EmptyCursor;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeFactoryImpl;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.result.ModelResult;
import org.openrdf.result.impl.ModelResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class MemberConnection extends RepositoryConnectionWrapper {

	private BNodeFactoryImpl bf;

	/** bf to delegate */
	private Map<BNode, BNode> in = new ConcurrentHashMap<BNode, BNode>();

	/** delegate to bf */
	private Map<BNode, BNode> out = new ConcurrentHashMap<BNode, BNode>();

	/** Every BNodes returned by this connection or added to this connection */
	private Set<BNode> contains = Collections.synchronizedSet(new HashSet<BNode>(512));

	public MemberConnection(RepositoryConnection delegate, BNodeFactoryImpl bf) {
		super(delegate.getRepository(), delegate);
		this.bf = bf;
	}

	@Override
	protected boolean isDelegatingImport()
		throws StoreException
	{
		return true;
	}

	@Override
	protected boolean isDelegatingAdd()
		throws StoreException
	{
		return false;
	}

	@Override
	protected boolean isDelegatingRead()
		throws StoreException
	{
		return false;
	}

	@Override
	protected boolean isDelegatingRemove()
		throws StoreException
	{
		return false;
	}

	@Override
	protected void addWithoutCommit(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		Resource s = isInternalBNode(subj) ? internal((BNode)subj) : subj;
		Value o = isInternalBNode(obj) ? internal((BNode)obj) : obj;
		Resource[] c = contexts;
		if (c != null) {
			for (int i = 0; i < c.length; i++) {
				c[i] = isInternalBNode(c[i]) ? internal((BNode)c[i]) : c[i];
			}
		}
		super.addWithoutCommit(s, pred, o, c);
	}

	@Override
	protected void removeWithoutCommit(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		if (!noMatch(subj, pred, obj, contexts))
			super.removeWithoutCommit(s(subj), pred, o(obj), c(contexts));
	}

	@Override
	public boolean hasMatch(Resource subj, URI pred, Value obj, boolean inf, Resource... contexts)
		throws StoreException
	{
		if (noMatch(subj, pred, obj, contexts))
			return false;
		return super.hasMatch(s(subj), pred, o(obj), inf, c(contexts));
	}

	@Override
	public ModelResult match(Resource subj, URI pred, Value obj, boolean inf, Resource... contexts)
		throws StoreException
	{
		if (noMatch(subj, pred, obj, contexts))
			return new ModelResultImpl(new EmptyCursor<Statement>());
		ModelResult result = super.match(s(subj), pred, o(obj), inf, c(contexts));
		return new MemberModelResult(result, out, contains);
	}

	@Override
	public long sizeMatch(Resource subj, URI pred, Value obj, boolean inf, Resource... contexts)
		throws StoreException
	{
		if (noMatch(subj, pred, obj, contexts))
			return 0;
		return super.sizeMatch(s(subj), pred, o(obj), inf, c(contexts));
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return new MemberBooleanQuery(super.prepareBooleanQuery(ql, query, baseURI), in, out);
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return new MemberGraphQuery(super.prepareGraphQuery(ql, query, baseURI), in, out, contains);
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return new MemberTupleQuery(super.prepareTupleQuery(ql, query, baseURI), in, out, contains);
	}

	@Override
	public Query prepareQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return MemberQuery.create(super.prepareQuery(ql, query, baseURI), in, out, contains);
	}

	/**
	 * If this pattern contains a BNode that has not been added or did not come
	 * from this connection.
	 */
	private boolean noMatch(Resource subj, URI pred, Value obj, Resource... contexts) {
		if (subj instanceof BNode && !contains.contains(subj))
			return true;
		if (obj instanceof BNode && !contains.contains(obj))
			return true;
		if (contexts != null) {
			for (Resource ctx : contexts) {
				if (ctx instanceof BNode && !contains.contains(ctx))
					return true;
			}
		}
		return false;
	}

	private Resource s(Resource subj) {
		if (isInternalBNode(subj)) {
			BNode b = in.get(subj);
			return b == null ? subj : b;
		}
		return subj;
	}

	private Value o(Value obj) {
		if (isInternalBNode(obj)) {
			BNode b = in.get(obj);
			return b == null ? obj : b;
		}
		return obj;
	}

	private Resource[] c(Resource... contexts) {
		if (bf.isUsed() && contexts != null) {
			for (Resource ctx : contexts) {
				if (isInternalBNode(ctx)) {
					Resource[] bnodes = new Resource[contexts.length];
					for (int i = 0; i < contexts.length; i++) {
						if (isInternalBNode(contexts[i])) {
							BNode b = in.get(contexts[i]);
							bnodes[i] = b == null ? contexts[i] : b;
						}
						else {
							bnodes[i] = contexts[i];
						}
					}
				}
			}
		}
		return contexts;
	}

	/**
	 * If value is a BNode created by the BNodeFactoryImpl.
	 */
	private boolean isInternalBNode(Value value) {
		return bf.isUsed() && value instanceof BNode && bf.isInternalBNode((BNode)value);
	}

	/**
	 * Map this BNode into a BNode created by this member.
	 */
	private BNode internal(BNode node) {
		BNode b = getValueFactory().createBNode(node.getID());
		in.put(node, b);
		out.put(b, node);
		contains.add(node);
		return b;
	}

}
