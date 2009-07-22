/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.signatures;

import org.openrdf.cursor.EmptyCursor;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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
public class SignedConnection extends RepositoryConnectionWrapper {

	private final BNodeSigner signer;

	public SignedConnection(RepositoryConnection delegate, BNodeSigner signer) {
		super(delegate.getRepository(), delegate);
		this.signer = signer;
	}

	public boolean isSignedBNode(Resource subj, URI pred, Value obj, Resource... contexts) {
		return signer.isSignedBNode(subj, pred, obj, contexts);
	}

	public Resource removeSignature(Resource subj) {
		return signer.removeSignature(subj);
	}

	@Override
	public boolean isReadOnly()
		throws StoreException
	{
		return getDelegate().getRepository().getMetaData().isReadOnly();
	}

	@Override
	public boolean hasMatch(Resource subj, URI pred, Value obj, boolean inf, Resource... contexts)
		throws StoreException
	{
		if (signer.isNotSignedBNode(subj, pred, obj, contexts)) {
			return false;
		}
		return getDelegate().hasMatch(s(subj), pred, o(obj), inf, c(contexts));
	}

	@Override
	public ModelResult match(Resource subj, URI pred, Value obj, boolean inf, Resource... contexts)
		throws StoreException
	{
		if (signer.isNotSignedBNode(subj, pred, obj, contexts)) {
			return new ModelResultImpl(new EmptyCursor<Statement>());
		}
		return signer.sign(getDelegate().match(s(subj), pred, o(obj), inf, c(contexts)));
	}

	@Override
	public long sizeMatch(Resource subj, URI pred, Value obj, boolean inf, Resource... contexts)
		throws StoreException
	{
		if (signer.isNotSignedBNode(subj, pred, obj, contexts)) {
			return 0;
		}
		return getDelegate().sizeMatch(s(subj), pred, o(obj), inf, c(contexts));
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return signer.sign(getDelegate().prepareBooleanQuery(ql, query, baseURI));
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return signer.sign(getDelegate().prepareGraphQuery(ql, query, baseURI));
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return signer.sign(getDelegate().prepareTupleQuery(ql, query, baseURI));
	}

	@Override
	public Query prepareQuery(QueryLanguage ql, String query, String baseURI)
		throws MalformedQueryException, StoreException
	{
		return signer.sign(getDelegate().prepareQuery(ql, query, baseURI));
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
	public void add(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		Resource s = signer.internalize(subj);
		Value o = signer.internalize(obj);
		Resource[] c = signer.internalize(contexts);
		super.add(s, pred, o, c);
	}

	@Override
	public void removeMatch(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		if (!signer.isNotSignedBNode(subj, pred, obj, contexts)) {
			super.removeMatch(s(subj), pred, o(obj), c(contexts));
		}
	}

	private Resource s(Resource subj) {
		return signer.removeSignature(subj);
	}

	private Value o(Value obj) {
		return signer.removeSignature(obj);
	}

	private Resource[] c(Resource... contexts) {
		return signer.removeSignature(contexts);
	}

}
