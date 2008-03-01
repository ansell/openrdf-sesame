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
 * 
 */
public abstract class RdbmsValue implements Value {
	private transient Long id;
	private transient Integer version;
	private transient boolean queued;

	public RdbmsValue() {
	}

	public RdbmsValue(Long id, Integer version) {
		this.id = id;
		this.version = version;
	}

	public Long getInternalId() {
		return id;
	}

	public void setInternalId(Long id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public boolean isQueued() {
		return queued;
	}

	public void setQueued(boolean queued) {
		this.queued = queued;
	}
}
