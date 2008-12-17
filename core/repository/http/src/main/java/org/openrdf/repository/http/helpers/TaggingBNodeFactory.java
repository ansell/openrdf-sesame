/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.helpers;

import org.openrdf.model.BNode;
import org.openrdf.model.BNodeFactory;
import org.openrdf.model.impl.BNodeImpl;

/**
 * @author James Leigh
 */
public class TaggingBNodeFactory implements BNodeFactory {

	private static class TaggedBNode extends BNodeImpl {

		private static final long serialVersionUID = 4817607480806752980L;

		private transient TaggingBNodeFactory factory;

		public TaggedBNode(TaggingBNodeFactory factory, BNode node) {
			super(node.getID());
			this.factory = factory;
		}

		public boolean isTagged() {
			return factory != null;
		}

		public boolean isTaggedBy(TaggingBNodeFactory factory) {
			return this.factory == factory;
		}
	}

	private BNodeFactory delegate;

	public TaggingBNodeFactory(BNodeFactory delegate) {
		this.delegate = delegate;
	}

	public BNode createBNode()
		throws UnsupportedOperationException
	{
		return new TaggedBNode(this, delegate.createBNode());
	}

	public BNode createBNode(String nodeID)
		throws IllegalArgumentException
	{
		return new TaggedBNode(this, delegate.createBNode(nodeID));
	}

	/**
	 * If BNode was tagged by a TaggingBNodeFactory instance other than this one.
	 */
	public boolean isAlreadyTagged(BNode node) {
		if (node instanceof TaggedBNode) {
			TaggedBNode tagged = (TaggedBNode)node;
			return tagged.isTagged() && !tagged.isTaggedBy(this);
		}
		return false;
	}

}
