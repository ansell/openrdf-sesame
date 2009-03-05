/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.model;

import org.openrdf.model.BNode;
import org.openrdf.model.impl.BNodeImpl;

/**
 * Wraps a {@link BNodeImpl} providing an internal id and version.
 * 
 * @author James Leigh
 */
public class RdbmsBNode extends RdbmsResource implements BNode {

	private static final long serialVersionUID = 861142250999359435L;

	private BNode bnode;

	public RdbmsBNode(BNode bnode) {
		this.bnode = bnode;
	}

	public RdbmsBNode(Number id, Integer version, BNode bnode) {
		super(id, version);
		this.bnode = bnode;
	}

	public String getID() {
		return bnode.getID();
	}

	public String stringValue() {
		return bnode.stringValue();
	}

	@Override
	public String toString() {
		return bnode.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		return bnode.equals(o);
	}

	@Override
	public int hashCode() {
		return bnode.hashCode();
	}

}
