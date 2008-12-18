/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.members;

import java.util.Map;
import java.util.Set;

import org.openrdf.cursor.EmptyCursor;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.result.ModelResult;
import org.openrdf.result.impl.ModelResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class MemberModelResult extends ModelResultImpl {

	private ModelResult result;

	private Map<BNode, BNode> map;

	private Set<BNode> contains;

	public MemberModelResult(Map<BNode, BNode> map, Set<BNode> contains) {
		super(new EmptyCursor<Statement>());
		this.map = map;
		this.contains = contains;
	}

	public MemberModelResult(ModelResult result, Map<BNode, BNode> map, Set<BNode> contains) {
		super(result);
		this.result = result;
		this.map = map;
		this.contains = contains;
	}

	@Override
	public Map<String, String> getNamespaces()
		throws StoreException
	{
		return result.getNamespaces();
	}

	@Override
	public Statement next()
		throws StoreException
	{
		return export(super.next());
	}

	Statement export(Statement st) {
		if (st == null)
			return null;
		Resource subj = map.get(st.getSubject());
		Value obj = map.get(st.getObject());
		Resource ctx = st.getContext();
		if (ctx != null) {
			ctx = map.get(ctx);
		}
		if (subj == null && st.getSubject() instanceof BNode) {
			contains.add((BNode)st.getSubject());
		}
		if (obj == null && st.getObject() instanceof BNode) {
			contains.add((BNode)st.getObject());
		}
		if (ctx == null && st.getContext() instanceof BNode) {
			contains.add((BNode)st.getContext());
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

}
