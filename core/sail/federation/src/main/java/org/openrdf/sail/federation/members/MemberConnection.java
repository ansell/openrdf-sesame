/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.members;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
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
		super.addWithoutCommit(s(subj), pred, o(obj), c(contexts));
	}

	@Override
	protected void removeWithoutCommit(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		super.removeWithoutCommit(s(subj), pred, o(obj), c(contexts));
	}

	@Override
	public boolean hasMatch(Resource subj, URI pred, Value obj, boolean inf, Resource... contexts)
		throws StoreException
	{
		return super.hasMatch(s(subj), pred, o(obj), inf, c(contexts));
	}

	@Override
	public ModelResult match(Resource subj, URI pred, Value obj, boolean inf, Resource... contexts)
		throws StoreException
	{
		ModelResult result = super.match(s(subj), pred, o(obj), inf, c(contexts));
		if (bf.isUsed())
			return new MemberModelResult(result, out);
		return result;
	}

	@Override
	public long sizeMatch(Resource subj, URI pred, Value obj, boolean inf, Resource... contexts)
		throws StoreException
	{
		return super.sizeMatch(s(subj), pred, o(obj), inf, c(contexts));
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return new MemberBooleanQuery(super.prepareBooleanQuery(ql, query, baseURI), bf, in, out);
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return new MemberGraphQuery(super.prepareGraphQuery(ql, query, baseURI), bf, in, out);
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return new MemberTupleQuery(super.prepareTupleQuery(ql, query, baseURI), bf, in, out);
	}

	@Override
	public Query prepareQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return MemberQuery.create(super.prepareQuery(ql, query, baseURI), bf, in, out);
	}

	private Resource s(Resource subj) {
		if (bf.isUsed() && subj instanceof BNode && bf.isInternalBNode((BNode)subj)) {
			return bnode((BNode)subj);
		}
		return subj;
	}

	private Value o(Value obj) {
		if (bf.isUsed() && obj instanceof BNode && bf.isInternalBNode((BNode)obj)) {
			return bnode((BNode)obj);
		}
		return obj;
	}

	private Resource[] c(Resource... contexts) {
		if (bf.isUsed() && contexts != null) {
			for (Resource ctx : contexts) {
				if (ctx instanceof BNode && bf.isInternalBNode((BNode)ctx)) {
					Resource[] bnodes = new Resource[contexts.length];
					for (int i = 0; i < contexts.length; i++) {
						if (contexts[i] instanceof BNode && bf.isInternalBNode((BNode)contexts[i])) {
							bnodes[i] = bnode((BNode)contexts[i]);
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

	private BNode bnode(BNode node) {
		BNode del = in.get(node);
		if (del == null) {
			del = getValueFactory().createBNode(node.getID());
			in.put(node, del);
			out.put(del, node);
		}
		return del;
	}

}
