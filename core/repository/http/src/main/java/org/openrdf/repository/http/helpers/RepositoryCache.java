/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.helpers;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.http.client.RepositoryClient;
import org.openrdf.http.client.SizeClient;
import org.openrdf.http.client.StatementClient;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.store.StoreException;

/**
 *
 * @author James Leigh
 */
public class RepositoryCache {

	/*-----------*
	 * Variables *
	 *-----------*/

	private HTTPValueFactory vf = new HTTPValueFactory();

	/**
	 * The HTTP client that takes care of the client-server communication.
	 */
	private RepositoryClient client;

	private PrefixHashSet subjectSpace;

	private Map<StatementPattern, CachedSize> cachedSizes = new ConcurrentHashMap<StatementPattern, CachedSize>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public RepositoryCache(RepositoryClient client, HTTPValueFactory vf) {
		super();
		this.client = client;
		this.vf = vf;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setSubjectSpace(Set<String> uriSpace) {
		this.subjectSpace = new PrefixHashSet(uriSpace);
	}

	/**
	 * Indicates that the cache needs validation.
	 */
	public void modified() {
		for (CachedSize cached : cachedSizes.values()) {
			cached.stale();
		}
	}

	public boolean isIllegal(Resource subj, URI pred, Value obj, Resource... contexts) {
		return noSubject(subj);
	}

	/**
	 * Will never connect to the remote server.
	 * 
	 * @return if it is known that this pattern (or super set) has no matches.
	 */
	public boolean noMatch(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		if (!vf.member(subj) || !vf.member(obj) || !vf.member(contexts))
			return true;
		if (isIllegal(subj, pred, obj, contexts))
			return true;
		long now = System.currentTimeMillis();
		if (noExactMatch(now, subj, pred, obj, includeInferred, contexts))
			return true;
		if (noExactMatch(now, null, pred, null, true))
			return true;
		if (noExactMatch(now, null, null, null, true, contexts))
			return true;
		return false; // don't know, maybe
	}

	/**
	 * Uses cache of given pattern or super patterns before asking server.
	 */
	public boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource[] contexts)
		throws StoreException
	{
		if (!vf.member(subj) || !vf.member(obj) || !vf.member(contexts))
			return false;
		if (isIllegal(subj, pred, obj, contexts))
			return false;
		long now = System.currentTimeMillis();
		if (noExactMatchRefreshable(now, subj, pred, obj, includeInferred, contexts))
			return false;
		if (noExactMatchRefreshable(now, null, pred, null, true))
			return false;
		if (noExactMatchRefreshable(now, null, null, null, true, contexts))
			return false;
		return !loadExactAbsent(now, subj, pred, obj, includeInferred, contexts);
	}

	/**
	 * Uses cache of given pattern or super patterns before loading size.
	 */
	public long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		if (!vf.member(subj) || !vf.member(obj) || !vf.member(contexts))
			return 0;
		if (isIllegal(subj, pred, obj, contexts))
			return 0;
		long now = System.currentTimeMillis();
		StatementPattern pattern = new StatementPattern(subj, pred, obj, includeInferred, contexts);
		CachedSize cached = cachedSizes.get(pattern);
		if (cached != null && cached.isSizeAvailable() && cached.isFresh(now))
			return cached.getSize(); // we have the valid size in the cache
		if (noExactMatchRefreshable(now, subj, pred, obj, includeInferred, contexts))
			return 0;
		if (noExactMatchRefreshable(now, null, pred, null, true))
			return 0;
		if (noExactMatchRefreshable(now, null, null, null, true, contexts))
			return 0;
		return loadSize(now, subj, pred, obj, includeInferred, contexts);
	}

	/**
	 * If this repository cannot contain this subject.
	 */
	private boolean noSubject(Resource subj) {
		if (subj instanceof URI && subjectSpace != null) {
			return !subjectSpace.match(subj.stringValue());
		}
		return false;
	}

	/**
	 * Will never connect to the remote server.
	 * 
	 * @return if it is known that this pattern has no matches.
	 */
	private boolean noExactMatch(long now, Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws StoreException
	{
		StatementPattern pattern = new StatementPattern(subj, pred, obj, includeInferred, contexts);
		CachedSize cached = cachedSizes.get(pattern);
		if (cached == null)
			return false; // don't know
		return cached.isFresh(now) && cached.isAbsent();
	}

	/**
	 * Will connect to the remote server. If no matches, may query the server for
	 * super patterns not in cache.
	 */
	private long loadSize(long now, Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws StoreException
	{
		long size = loadExactSize(now, subj, pred, obj, includeInferred, contexts);
		if (size == 0) {
			StatementPattern orig = new StatementPattern(subj, pred, obj, includeInferred, contexts);
			StatementPattern predOnly = new StatementPattern(null, pred, null, true);
			StatementPattern ctxOnly = new StatementPattern(null, null, null, true, contexts);

			if (pred != null && !orig.equals(predOnly)) {
				// no values, does it have this predicate?
				if (!cachedSizes.containsKey(predOnly)) {
					loadExactAbsent(now, null, pred, null, true);
				}
			}
			if ((contexts == null || contexts.length > 0) && !orig.equals(ctxOnly)) {
				// no values, does it have this context?
				if (!cachedSizes.containsKey(ctxOnly)) {
					loadExactAbsent(now, null, null, null, true, contexts);
				}
			}
		}
		return size;
	}

	/**
	 * Will always connect to the remote server to ensure cache is valid (if
	 * available).
	 */
	private long loadExactSize(long now, Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws StoreException
	{
		StatementPattern pattern = new StatementPattern(subj, pred, obj, includeInferred, contexts);
		CachedSize cached = cachedSizes.get(pattern);
		SizeClient client = this.client.size();
		if (cached != null) {
			// Only calculate size if cached value is old
			client.ifNoneMatch(cached.getETag());
		}
		Long size = client.get(subj, pred, obj, includeInferred, contexts);
		if (size == null) {
			assert cached != null : "Server did not return a size value";
			cached.refreshed(now, client.getMaxAge());
		}
		else {
			cached = new CachedSize(size, client.getETag());
			cached.refreshed(now, client.getMaxAge());
			cachedSizes.put(pattern, cached);
		}
		assert cached.isSizeAvailable();
		return cached.getSize();
	}

	/**
	 * Will connect to the remote server for validation, if it is believed that
	 * there will be no match.
	 * 
	 * @return if it is known that this pattern has no matches.
	 */
	private boolean noExactMatchRefreshable(long now, Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		StatementPattern pattern = new StatementPattern(subj, pred, obj, includeInferred, contexts);
		CachedSize cached = cachedSizes.get(pattern);
		if (cached == null || !cached.isAbsent())
			return false; // might have a match
		if (cached.isFresh(now))
			return true; // no match
		return loadExactAbsent(now, subj, pred, obj, includeInferred, contexts);
	}

	/**
	 * Connects to the server.
	 * @return <code>true</code> if this pattern does not exist on the server.
	 */
	private boolean loadExactAbsent(long now, Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
		throws StoreException
	{
		StatementPattern pattern = new StatementPattern(subj, pred, obj, includeInferred, contexts);
		CachedSize cached = cachedSizes.get(pattern);
		StatementClient client = this.client.statements();
		client.setLimit(1);
		if (cached != null) {
			// Only calculate if cached value is old
			client.ifNoneMatch(cached.getETag());
		}
		GraphQueryResult result = client.get(subj, pred, obj, includeInferred, contexts);
		if (result == null) {
			assert cached != null : "Server did not return a size value";
			cached.refreshed(now, client.getMaxAge());
		}
		else {
			cached = new CachedSize(result.hasNext(), client.getETag());
			cached.refreshed(now, client.getMaxAge());
			cachedSizes.put(pattern, cached);
		}
		return cached.isAbsent();
	}
}
