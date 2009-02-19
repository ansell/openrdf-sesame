/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.signatures;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.openrdf.model.BNode;
import org.openrdf.model.BNodeFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeFactoryImpl;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.Query;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.ModelResult;

/**
 * @author James Leigh
 */
public class BNodeSigner {

	private static AtomicInteger seq = new AtomicInteger(new Random().nextInt());

	private String suffix = "i" + Integer.toHexString(seq.incrementAndGet());

	/** This will only accept external BNodes created by this BNodeFactory. */
	private BNodeFactoryImpl external;

	/**
	 * External BNode should be mapped to new BNodes created by this
	 * BNodeFactory.
	 */
	private BNodeFactory internal;

	/** external to internal */
	private ConcurrentMap<BNode, BNode> in = new ConcurrentHashMap<BNode, BNode>();

	/** internal to external */
	private Map<BNode, BNode> out = new ConcurrentHashMap<BNode, BNode>();

	public BNodeSigner(BNodeFactoryImpl external, BNodeFactory internal) {
		this.external = external;
		this.internal = internal;
	}

	public Value internalize(Value obj) {
		if (obj instanceof BNode) {
			BNode node = (BNode)obj;
			if (external.isInternalBNode(node) && !in.containsKey(node)) {
				BNode b = internal.createBNode(node.getID());
				BNode o = in.putIfAbsent(node, b);
				if (o != null) {
					return o;
				}
				out.put(b, node);
				return b;
			}
		}
		return removeSignature(obj);
	}

	public Resource internalize(Resource subj) {
		return (Resource)internalize((Value)subj);
	}

	public Resource[] internalize(Resource... contexts) {
		Resource[] c = contexts;
		if (c != null) {
			for (int i = 0; i < c.length; i++) {
				c[i] = internalize(c[i]);
			}
		}
		return c;
	}

	public boolean isNotSignedBNode(Value o) {
		if (o instanceof BNode) {
			BNode node = (BNode)o;
			if (external.isInternalBNode(node) && in.containsKey(o)) {
				return false;
			}
			if (node.getID().endsWith(suffix)) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * If this pattern contains a BNode that has not been added or did not come
	 * from this connection.
	 */
	public boolean isNotSignedBNode(Resource subj, URI pred, Value obj, Resource... contexts) {
		if (isNotSignedBNode(subj)) {
			return true;
		}
		if (isNotSignedBNode(obj)) {
			return true;
		}
		if (contexts != null) {
			for (Resource ctx : contexts) {
				if (isNotSignedBNode(ctx)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isSignedBNode(Value o) {
		if (o instanceof BNode) {
			BNode node = (BNode)o;
			if (external.isInternalBNode(node) && in.containsKey(o)) {
				return true;
			}
			if (node.getID().endsWith(suffix)) {
				return true;
			}
			return false;
		}
		return false;
	}

	/**
	 * If this pattern contains a BNode that has been added or did come from this
	 * connection.
	 */
	public boolean isSignedBNode(Resource subj, URI pred, Value obj, Resource... contexts) {
		if (isSignedBNode(subj)) {
			return true;
		}
		if (isSignedBNode(obj)) {
			return true;
		}
		if (contexts != null) {
			for (Resource ctx : contexts) {
				if (isSignedBNode(ctx)) {
					return true;
				}
			}
		}
		return false;
	}

	public Value removeSignature(Value subj) {
		if (subj instanceof BNode) {
			BNode node = (BNode)subj;
			String nodeID = node.getID();
			if (nodeID.endsWith(suffix)) {
				nodeID = nodeID.substring(0, nodeID.length() - suffix.length());
				return new BNodeImpl(nodeID);
			}
			if (external.isInternalBNode(node)) {
				BNode b = in.get(node);
				return b == null ? node : b;
			}
		}
		return subj;
	}

	public Resource removeSignature(Resource subj) {
		return (Resource)removeSignature((Value)subj);
	}

	public Resource[] removeSignature(Resource... contexts) {
		if (contexts != null) {
			for (Resource ctx : contexts) {
				if (ctx instanceof BNode) {
					Resource[] bnodes = new Resource[contexts.length];
					for (int i = 0; i < contexts.length; i++) {
						bnodes[i] = removeSignature(contexts[i]);
					}
					return bnodes;
				}
			}
		}
		return contexts;
	}

	public Value sign(Value value) {
		if (value == null) {
			return null;
		}
		Value v = out.get(value);
		if (v == null && value instanceof BNode) {
			return new BNodeImpl(((BNode)value).getID() + suffix);
		}
		if (v == null) {
			return value;
		}
		return v;
	}

	public Resource sign(Resource resource) {
		return (Resource)sign((Value)resource);
	}

	public BindingSet sign(BindingSet bindings) {
		if (bindings == null) {
			return null;
		}
		MapBindingSet signed = new MapBindingSet(bindings.size());
		for (Binding binding : bindings) {
			signed.addBinding(binding.getName(), sign(binding.getValue()));
		}
		return signed;
	}

	public Statement sign(Statement st) {
		if (st == null) {
			return null;
		}
		Resource subj = sign(st.getSubject());
		Value obj = sign(st.getObject());
		Resource ctx = sign(st.getContext());
		return new StatementImpl(subj, st.getPredicate(), obj, ctx);
	}

	public ModelResult sign(ModelResult result) {
		return new SignedModelResult(result, this);
	}

	public BooleanQuery sign(BooleanQuery query) {
		return new SignedBooleanQuery(query, this);
	}

	public GraphQuery sign(GraphQuery query) {
		return new SignedGraphQuery(query, this);
	}

	public TupleQuery sign(TupleQuery query) {
		return new SignedTupleQuery(query, this);
	}

	public Query sign(Query query) {
		if (query instanceof TupleQuery) {
			return sign((TupleQuery)query);
		}
		if (query instanceof GraphQuery) {
			return sign((GraphQuery)query);
		}
		if (query instanceof BooleanQuery) {
			return sign((BooleanQuery)query);
		}
		throw new AssertionError(query.getClass().getName());
	}

	public SignedConnection sign(RepositoryConnection con) {
		return new SignedConnection(con, this);
	}

}
