/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.model;

import org.openrdf.model.Resource;

/**
 * Provides an internal id and version for URI and BNodes.
 * 
 * @author James Leigh
 * 
 */
public abstract class RdbmsResource extends RdbmsValue implements Resource {

	public RdbmsResource() {
	}

	public RdbmsResource(Number id, Integer version) {
		super(id, version);
	}
}
