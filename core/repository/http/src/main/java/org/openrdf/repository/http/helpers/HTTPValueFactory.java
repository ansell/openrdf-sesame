/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.helpers;

import static org.openrdf.http.protocol.Protocol.BNODE;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import org.openrdf.http.client.BNodeClient;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.repository.http.HTTPBNode;
import org.openrdf.results.TupleResult;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class HTTPValueFactory extends ValueFactoryImpl {

	private int key;

	private int amount = 1;

	private Queue<BNode> queue;

	private BNodeClient client;

	public HTTPValueFactory() {
		this.key = new Random().nextInt();
	}

	public HTTPValueFactory(int key, BNodeClient client) {
		this.key = key;
		this.client = client;
		this.queue = new LinkedList<BNode>();
	}

	public HTTPValueFactory fork(BNodeClient client) {
		return new HTTPValueFactory(key, client);
	}

	@Override
	public HTTPBNode createBNode() {
		BNode bnode;
		if (queue == null) {
			bnode = super.createBNode();
		}
		else {
			synchronized (queue) {
				bnode = queue.poll();
				if (bnode == null) {
					try {
						bnode = loadBNodes();
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
		}
		return new HTTPBNode(bnode.getID(), key);
	}

	@Override
	public HTTPBNode createBNode(String nodeID) {
		return new HTTPBNode(nodeID, key);
	}

	HTTPBNode createBNode(BNode bnode) {
		return new HTTPBNode(bnode.getID(), key);
	}

	boolean member(Resource... values) {
		if (values == null)
			return true;
		for (Resource value : values) {
			if (!member(value))
				return false;
		}
		return true;
	}

	boolean member(Value value) {
		if (value instanceof BNode) {
			if (value instanceof HTTPBNode) {
				HTTPBNode bnode = (HTTPBNode)value;
				return key == bnode.getKey();
			}
			return false;
		}
		return true;
	}

	private BNode loadBNodes()
		throws StoreException, QueryResultParseException, NoCompatibleMediaType
	{
		BNode bnode;
		TupleResult result = client.post(amount *= 2);
		try {
			if (!result.hasNext())
				throw new StoreException("No BNodes");
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
