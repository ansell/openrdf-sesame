/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.BNode;
import org.openrdf.model.BNodeFactory;

/**
 * @author James Leigh
 */
public class MappedIllegalBNodeFactory implements BNodeFactory {

	private BNodeFactory map;

	private BNodeFactory plain;

	public MappedIllegalBNodeFactory(BNodeFactory bf) {
		this.plain = bf;
		this.map = new MappedBNodeFactory(bf);
	}

	public BNode createBNode() {
		return plain.createBNode();
	}

	public BNode createBNode(String nodeID) {
		try {
			return plain.createBNode(nodeID);
		}
		catch (IllegalArgumentException e) {
			return map.createBNode(nodeID);
		}
	}

	public String toString() {
		return "MappedIllegal" + plain.toString();
	}

}
