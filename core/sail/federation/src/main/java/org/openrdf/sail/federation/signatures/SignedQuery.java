/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.signatures;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.Query;

/**
 * @author James Leigh
 */
public abstract class SignedQuery implements Query {

	private Query query;

	BNodeSigner signer;

	public SignedQuery(Query query, BNodeSigner signer) {
		this.query = query;
		this.signer = signer;
	}

	public BindingSet getBindings() {
		BindingSet internal = query.getBindings();
		return signer.sign(internal);
	}

	public void setBinding(String name, Value value) {
		query.setBinding(name, signer.removeSignature(value));
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

}
