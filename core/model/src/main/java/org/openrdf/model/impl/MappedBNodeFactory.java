/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.model.BNode;
import org.openrdf.model.BNodeFactory;

/**
 * @author James Leigh
 */
public class MappedBNodeFactory implements BNodeFactory {

	/**
	 * Mapping from blank node identifiers as used in the RDF document to the
	 * object created for it by the ValueFactory. This mapping is used to return
	 * identical BNode objects for recurring blank node identifiers.
	 */
	private ConcurrentMap<String, BNode> map = new ConcurrentHashMap<String, BNode>(16);

	private BNodeFactory bnodes;

	public MappedBNodeFactory(BNodeFactory bnodes) {
		this.bnodes = bnodes;
	}

	public BNode createBNode() {
		return bnodes.createBNode();
	}

	public BNode createBNode(String nodeID) {
		// Maybe the node ID has been used before:
		BNode bnode = map.get(nodeID);
		if (bnode == null) {
			// This is a new node ID, create a new BNode object for it
			bnode = bnodes.createBNode();
			// Remember it, the nodeID might occur again.
			BNode o = map.putIfAbsent(nodeID, bnode);
			if (o != null) {
				bnode = o;
			}
		}
		return bnode;
	}

}
