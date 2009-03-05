/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.model;

import org.openrdf.model.Value;

/**
 * Provides an internal id and version for values.
 * 
 * @author James Leigh
 */
public abstract class RdbmsValue implements Value {

	private transient Number id;

	private transient Integer version;

	public RdbmsValue() {
	}

	public RdbmsValue(Number id, Integer version) {
		this.id = id;
		this.version = version;
	}

	public Number getInternalId() {
		return id;
	}

	public void setInternalId(Number id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public boolean isExpired(int v) {
		if (id == null) {
			return true;
		}
		if (version == null) {
			return true;
		}
		return version.intValue() != v;
	}
}
