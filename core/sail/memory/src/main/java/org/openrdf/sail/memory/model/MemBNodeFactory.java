/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import java.util.Collections;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeFactoryImpl;
import org.openrdf.model.impl.BNodeImpl;

/**
 * A factory for MemBNode objects that keeps track of created objects to prevent
 * the creation of duplicate objects, minimizing memory usage as a result.
 * 
 * @author Arjohn Kampman
 * @author David Huynh
 * @author James Leigh
 */
public class MemBNodeFactory extends BNodeFactoryImpl {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Registry containing the set of MemBNode objects as used by a MemoryStore.
	 * This registry enables the reuse of objects, minimizing the number of
	 * objects in main memory.
	 */
	private final WeakObjectRegistry<MemBNode> bnodeRegistry = new WeakObjectRegistry<MemBNode>();

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * See getMemValue() for description.
	 */
	public synchronized MemBNode getMemBNode(BNode bnode) {
		if (isOwnMemValue(bnode)) {
			return (MemBNode)bnode;
		}
		else {
			if (!isInternalBNode(bnode))
				throw new IllegalArgumentException("BNode not created from this ValueFactory");
			return bnodeRegistry.get(bnode);
		}
	}

	/**
	 * Checks whether the supplied value is an instance of <tt>MemValue</tt>
	 * and whether it has been created by this MemValueFactory.
	 */
	private boolean isOwnMemValue(Value value) {
		return value instanceof MemValue && ((MemValue)value).getCreator() == this;
	}

	/**
	 * Gets all bnodes that are managed by this value factory.
	 * <p>
	 * <b>Warning:</b> This method is not synchronized. To iterate over the
	 * returned set in a thread-safe way, this method should only be called
	 * while synchronizing on this object.
	 * 
	 * @return An unmodifiable Set of MemBNode objects.
	 */
	public Set<MemBNode> getMemBNodes() {
		return Collections.unmodifiableSet(bnodeRegistry);
	}

	/**
	 * See createMemValue() for description.
	 */
	public synchronized MemBNode createMemBNode(BNode bnode) {
		if (!isInternalBNode(bnode))
			throw new IllegalArgumentException("BNode not created from this ValueFactory");
		MemBNode memBNode = new MemBNode(this, bnode.getID());

		boolean wasNew = bnodeRegistry.add(memBNode);
		assert wasNew : "Created a duplicate MemBNode for bnode " + bnode;

		return memBNode;
	}

	@Override
	public synchronized BNode createBNode(String nodeID) {
		BNode tempBNode = new BNodeImpl(nodeID);
		MemBNode memBNode = getMemBNode(tempBNode);

		if (memBNode == null) {
			memBNode = createMemBNode(tempBNode);
		}

		return memBNode;
	}
}
