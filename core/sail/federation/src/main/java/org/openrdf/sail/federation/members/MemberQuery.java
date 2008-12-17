/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.members;

import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeFactoryImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.Query;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.impl.MapBindingSet;

/**
 * @author James Leigh
 */
public abstract class MemberQuery implements Query {

	public static Query create(Query query, BNodeFactoryImpl bf, Map<BNode, BNode> in, Map<BNode, BNode> out) {
		if (query instanceof TupleQuery)
			return new MemberTupleQuery((TupleQuery)query, bf, in, out);
		if (query instanceof GraphQuery)
			return new MemberGraphQuery((GraphQuery)query, bf, in, out);
		if (query instanceof BooleanQuery)
			return new MemberBooleanQuery((BooleanQuery)query, bf, in, out);
		throw new AssertionError(query.getClass().getName());
	}

	private Query query;

	private BNodeFactoryImpl bf;

	private Map<BNode, BNode> in;

	Map<BNode, BNode> out;

	public MemberQuery(Query query, BNodeFactoryImpl bf, Map<BNode, BNode> in, Map<BNode, BNode> out) {
		this.query = query;
		this.bf = bf;
		this.in = in;
		this.out = out;
	}

	public BindingSet getBindings() {
		BindingSet internal = query.getBindings();
		return export(internal);
	}

	public void setBinding(String name, Value value) {
		if (value instanceof BNode && bf.isInternalBNode((BNode)value)) {
			query.setBinding(name, bnode((BNode)value));
		}
		else {
			query.setBinding(name, value);
		}
	}

	public Dataset getDataset() {
		return query.getDataset();
	}

	public boolean getIncludeInferred() {
		return query.getIncludeInferred();
	}

	public int getMaxQueryTime() {
		return query.getMaxQueryTime();
	}

	public void removeBinding(String name) {
		query.removeBinding(name);
	}

	public void setDataset(Dataset dataset) {
		query.setDataset(dataset);
	}

	public void setIncludeInferred(boolean includeInferred) {
		query.setIncludeInferred(includeInferred);
	}

	public void setMaxQueryTime(int maxQueryTime) {
		query.setMaxQueryTime(maxQueryTime);
	}

	BindingSet export(BindingSet internal) {
		if (internal == null)
			return null;
		if (out.isEmpty())
			return internal;
		MapBindingSet external = new MapBindingSet(internal.size());
		for (Binding binding : internal) {
			Value v = out.get(binding.getValue());
			if (v == null) {
				external.addBinding(binding);
			}
			else {
				external.addBinding(binding.getName(), v);
			}
		}
		return external;
	}

	private BNode bnode(BNode node) {
		BNode del = in.get(node);
		if (del == null) {
			del = bf.createBNode(node.getID());
			in.put(node, del);
			out.put(del, node);
		}
		return del;
	}

}
