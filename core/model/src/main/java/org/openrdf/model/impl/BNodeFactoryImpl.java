/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.BNode;
import org.openrdf.model.BNodeFactory;
import org.openrdf.model.ValueFactory;

/**
 * Helper class for {@link ValueFactory} implementations
 * {@link ValueFactoyr#createBNode()}.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class BNodeFactoryImpl implements BNodeFactory {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The ID for the next bnode that is created.
	 */
	private int nextBNodeID;

	/**
	 * The prefix for any new bnode IDs.
	 */
	private String bnodePrefix;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BNodeFactoryImpl() {
		initBNodeParams();
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected void initBNodeParams() {
		// BNode prefix is based on currentTimeMillis(). Combined with a
		// sequential number per session, this gives a unique identifier.
		bnodePrefix = "node" + Long.toString(System.currentTimeMillis(), 32) + "x";
		nextBNodeID = 1;
	}

	public BNode createBNode() {
		if (nextBNodeID == Integer.MAX_VALUE) {
			// Start with a new bnode prefix
			initBNodeParams();
		}

		return createBNode(bnodePrefix + nextBNodeID++);
	}

	public BNode createBNode(String nodeID) {
		return new BNodeImpl(nodeID);
	}
}
