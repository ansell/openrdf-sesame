/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.members;

import java.util.Map;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
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

	public static Query create(Query query, Map<BNode, BNode> in, Map<BNode, BNode> out, Set<BNode> contains) {
		if (query instanceof TupleQuery)
			return new MemberTupleQuery((TupleQuery)query, in, out, contains);
		if (query instanceof GraphQuery)
			return new MemberGraphQuery((GraphQuery)query, in, out, contains);
		if (query instanceof BooleanQuery)
			return new MemberBooleanQuery((BooleanQuery)query, in, out);
		throw new AssertionError(query.getClass().getName());
	}

	private Query query;

	private Map<BNode, BNode> in;

	Map<BNode, BNode> out;

	public MemberQuery(Query query, Map<BNode, BNode> in, Map<BNode, BNode> out) {
		this.query = query;
		this.in = in;
		this.out = out;
	}

	public BindingSet getBindings() {
		BindingSet internal = query.getBindings();
		return export(internal, null);
	}

	public void setBinding(String name, Value value) {
		if (value instanceof BNode) {
			BNode v = in.get(value);
			query.setBinding(name, v == null ? value : v);
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

	BindingSet export(BindingSet internal, Set<BNode> contains) {
		if (internal == null)
			return null;
		MapBindingSet external = new MapBindingSet(internal.size());
		for (Binding binding : internal) {
			Value v = out.get(binding.getValue());
			if (v == null && contains != null && binding.getValue() instanceof BNode) {
				contains.add((BNode)binding.getValue());
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

}
