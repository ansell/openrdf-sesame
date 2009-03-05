/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.helpers;

import static org.openrdf.http.protocol.Protocol.BNODE;

import java.util.LinkedList;
import java.util.Queue;

import org.openrdf.http.client.BNodeClient;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.model.BNode;
import org.openrdf.model.BNodeFactory;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class HTTPBNodeFactory implements BNodeFactory {

	private int amount = 1;

	private Queue<BNode> queue;

	private BNodeClient client;

	public HTTPBNodeFactory(BNodeClient client) {
		this.client = client;
		this.queue = new LinkedList<BNode>();
	}

	public BNode createBNode() {
		if (queue == null) {
			throw new UnsupportedOperationException();
		}
		synchronized (queue) {
			BNode bnode = queue.poll();
			if (bnode != null) {
				return bnode;
			}
			try {
				return loadBNodes();
			}
			catch (StoreException e) {
				// FIXME throw StoreException
				throw new AssertionError(e);
			}
			catch (QueryResultParseException e) {
				throw new AssertionError(e);
			}
			catch (NoCompatibleMediaType e) {
				throw new AssertionError(e);
			}
		}
	}

	public BNode createBNode(String nodeID) {
		try {
			return client.post(nodeID);
		}
		catch (StoreException e) {
			// FIXME throw StoreException
			throw new AssertionError(e);
		}
		catch (QueryResultParseException e) {
			throw new AssertionError(e);
		}
		catch (NoCompatibleMediaType e) {
			throw new AssertionError(e);
		}
	}

	private BNode loadBNodes()
		throws StoreException, QueryResultParseException, NoCompatibleMediaType
	{
		BNode bnode;
		TupleResult result = client.post(amount *= 2);
		try {
			if (!result.hasNext()) {
				throw new StoreException("No BNodes");
			}
			bnode = (BNode)result.next().getValue(BNODE);
			while (result.hasNext()) {
				queue.add((BNode)result.next().getValue(BNODE));
			}
		}
		finally {
			result.close();
		}
		return bnode;
	}

}
