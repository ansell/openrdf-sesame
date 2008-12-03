/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.model.impl.BNodeImpl;

/**
 * @author James Leigh
 */
public class HTTPBNode extends BNodeImpl {

	private static final long serialVersionUID = 3398787114953801437L;

	private int key;

	public HTTPBNode(String id, int key) {
		super(id);
		this.key = key;
	}

	public int getKey() {
		return key;
	}

}
