/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.signatures;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.BNode;
import org.openrdf.model.BNodeFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeFactoryImpl;
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
 *
 * @author James Leigh
 */
public class BNodeSigner {

	/** This will only accept external BNodes created by this BNodeFactory. */
	private BNodeFactoryImpl external;

	/** External BNode should be mapped to new BNodes created by this BNodeFactory. */
	private BNodeFactory internal;

	/** external to internal */
	private Map<BNode, BNode> in = new ConcurrentHashMap<BNode, BNode>();

	/** internal to external */
	private Map<BNode, BNode> out = new ConcurrentHashMap<BNode, BNode>();

	/** Every BNodes returned by this connection or added to this connection */
	private Set<BNode> contains = Collections.synchronizedSet(new HashSet<BNode>(512));

	public BNodeSigner(BNodeFactoryImpl external, BNodeFactory internal) {
		this.external = external;
		this.internal = internal;
	}

	public Value internalize(Value obj) {
		return isExternalBNode(obj) ? internalize((BNode)obj) : obj;
	}

	public Resource internalize(Resource subj) {
		return isExternalBNode(subj) ? internalize((BNode)subj) : subj;
	}

	public Resource[] internalize(Resource... contexts) {
		Resource[] c = contexts;
		if (c != null) {
			for (int i = 0; i < c.length; i++) {
				c[i] = isExternalBNode(c[i]) ? internalize((BNode)c[i]) : c[i];
			}
		}
		return c;
	}

	public boolean isNotSignedBNode(Value o) {
		return o instanceof BNode && !contains.contains(o);
	}

	/**
	 * If this pattern contains a BNode that has not been added or did not come
	 * from this connection.
	 */
	public boolean isNotSignedBNode(Resource subj, URI pred, Value obj, Resource... contexts) {
		if (isNotSignedBNode(subj))
			return true;
		if (isNotSignedBNode(obj))
			return true;
		if (contexts != null) {
			for (Resource ctx : contexts) {
				if (isNotSignedBNode(ctx))
					return true;
			}
		}
		return false;
	}

	public Value removeSignature(Value subj) {
		if (isExternalBNode(subj)) {
			BNode b = in.get(subj);
			return b == null ? subj : b;
		}
		return subj;
	}

	public Resource removeSignature(Resource subj) {
		if (isExternalBNode(subj)) {
			BNode b = in.get(subj);
			return b == null ? subj : b;
		}
		return subj;
	}

	public Resource[] removeSignature(Resource... contexts) {
		if (external.isUsed() && contexts != null) {
			for (Resource ctx : contexts) {
				if (isExternalBNode(ctx)) {
					Resource[] bnodes = new Resource[contexts.length];
					for (int i = 0; i < contexts.length; i++) {
						if (isExternalBNode(contexts[i])) {
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

	public BindingSet sign(BindingSet internal) {
		if (internal == null)
			return null;
		MapBindingSet external = new MapBindingSet(internal.size());
		for (Binding binding : internal) {
			Value v = out.get(binding.getValue());
			if (v == null && binding.getValue() instanceof BNode) {
				contains.add(((BNode)binding.getValue()));
				external.addBinding(binding);
			}
			else if (v == null) {
				external.addBinding(binding);
			}
			else {
				external.addBinding(binding.getName(), v);
			}
		}
		return external;
	}

	public Statement sign(Statement st) {
		if (st == null)
			return null;
		Resource subj = out.get(st.getSubject());
		Value obj = out.get(st.getObject());
		Resource ctx = st.getContext();
		if (ctx != null) {
			ctx = out.get(ctx);
		}
		if (subj == null && st.getSubject() instanceof BNode) {
			contains.add(((BNode)st.getSubject()));
		}
		if (obj == null && st.getObject() instanceof BNode) {
			contains.add(((BNode)st.getObject()));
		}
		if (ctx == null && st.getContext() instanceof BNode) {
			contains.add(((BNode)st.getContext()));
		}
		if (subj == null && obj == null && ctx == null)
			return st;
		if (subj == null) {
			subj = st.getSubject();
		}
		if (obj == null) {
			obj = st.getObject();
		}
		if (ctx == null) {
			ctx = st.getContext();
		}
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
		if (query instanceof TupleQuery)
			return sign((TupleQuery)query);
		if (query instanceof GraphQuery)
			return sign((GraphQuery)query);
		if (query instanceof BooleanQuery)
			return sign((BooleanQuery)query);
		throw new AssertionError(query.getClass().getName());
	}

	public RepositoryConnection sign(RepositoryConnection con) {
		return new SignedConnection(con, this);
	}

	/**
	 * Map this external BNode into an internal BNode.
	 */
	private BNode internalize(BNode node) {
		BNode b = internal.createBNode(node.getID());
		in.put(node, b);
		out.put(b, node);
		contains.add(node);
		return b;
	}

	/**
	 * If value is a BNode created by the BNodeFactoryImpl.
	 */
	private boolean isExternalBNode(Value value) {
		return external.isUsed() && value instanceof BNode && external.isInternalBNode(((BNode)value));
	}

}
