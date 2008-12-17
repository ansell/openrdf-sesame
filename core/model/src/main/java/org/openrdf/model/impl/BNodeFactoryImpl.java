/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import java.util.concurrent.atomic.AtomicLong;

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
	 * The prefix for any new bnode IDs.
	 */
	private String bnodePrefix;

	/**
	 * The ID for the next bnode that is created.
	 */
	private AtomicLong nextBNodeID;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BNodeFactoryImpl() {
		// BNode prefix is based on currentTimeMillis(). Combined with a
		// sequential number per session, this gives a unique identifier.
		bnodePrefix = "node" + Long.toString(System.currentTimeMillis(), 32) + "x";
		nextBNodeID = new AtomicLong(0);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public BNode createBNode() {
		return createBNode(bnodePrefix + nextBNodeID.incrementAndGet());
	}

	/**
	 * If nodeID was created in this instance's {@link createBNode()}.
	 */
	public boolean isInternalBNode(BNode node) {
		return node.getID().startsWith(bnodePrefix);
	}

	public BNode createBNode(String nodeID) {
		return new BNodeImpl(nodeID);
	}
}
