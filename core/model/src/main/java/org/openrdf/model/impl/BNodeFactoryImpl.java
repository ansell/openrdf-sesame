/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.openrdf.model.BNode;
import org.openrdf.model.BNodeFactory;
import org.openrdf.model.ValueFactory;

/**
 * Helper class for {@link ValueFactory} implementations
 * {@link ValueFactory#createBNode()}.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class BNodeFactoryImpl implements BNodeFactory {

	private static AtomicInteger seq = new AtomicInteger(new Random().nextInt());

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
		bnodePrefix = "node" + Integer.toString(seq.incrementAndGet(), 32) + "x";
		nextBNodeID = new AtomicLong(0);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public BNode createBNode(String nodeID) {
		return new BNodeImpl(nodeID);
	}

	public BNode createBNode() {
		return createBNode(bnodePrefix + nextBNodeID.incrementAndGet());
	}

	/**
	 * If nodeID was created in this instance's {@link #createBNode()}.
	 */
	public boolean isInternalBNode(BNode node) {
		return node.getID().startsWith(bnodePrefix);
	}

	public boolean isUsed() {
		return nextBNodeID.get() != 0;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + bnodePrefix;
	}
}
