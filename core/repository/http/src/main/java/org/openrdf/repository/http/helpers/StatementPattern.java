/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.helpers;

import java.util.Arrays;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;


/**
 *
 * @author James Leigh
 */
public class StatementPattern {
	private Resource subj;
	private URI pred;
	private Value obj;
	private boolean includeInferred;
	private Resource[] contexts;

	public StatementPattern(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts) {
		this.contexts = OpenRDFUtil.notNull(contexts);
		this.includeInferred = includeInferred;
		this.obj = obj;
		this.pred = pred;
		this.subj = subj;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(contexts);
		result = prime * result + (includeInferred ? 1231 : 1237);
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		result = prime * result + ((pred == null) ? 0 : pred.hashCode());
		result = prime * result + ((subj == null) ? 0 : subj.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatementPattern other = (StatementPattern)obj;
		if (!Arrays.equals(contexts, other.contexts))
			return false;
		if (includeInferred != other.includeInferred)
			return false;
		if (this.obj == null) {
			if (other.obj != null)
				return false;
		}
		else if (!this.obj.equals(other.obj))
			return false;
		if (pred == null) {
			if (other.pred != null)
				return false;
		}
		else if (!pred.equals(other.pred))
			return false;
		if (subj == null) {
			if (other.subj != null)
				return false;
		}
		else if (!subj.equals(other.subj))
			return false;
		return true;
	}

}
