/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.MappedIllegalBNodeFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * A factory for MemValue objects that keeps track of created objects to prevent
 * the creation of duplicate objects, minimizing memory usage as a result.
 * 
 * @author Arjohn Kampman
 * @author David Huynh
 * @author James Leigh
 */
public class MemValueFactory extends ValueFactoryImpl {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final MemBNodeFactory bf;

	private final MemURIFactory uf;

	private final MemLiteralFactory lf;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MemValueFactory(MemBNodeFactory bf, MemURIFactory uf, MemLiteralFactory lf) {
		super(new MappedIllegalBNodeFactory(bf), uf, lf);
		this.bf = bf;
		this.uf = uf;
		this.lf = lf;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public MemBNodeFactory getBNodeFactory() {
		return bf;
	}

	@Override
	public MemLiteralFactory getLiteralFactory() {
		return lf;
	}

	@Override
	public MemURIFactory getURIFactory() {
		return uf;
	}

	/**
	 * Returns a previously created MemValue that is equal to the supplied value,
	 * or <tt>null</tt> if the supplied value is a new value or is equal to
	 * <tt>null</tt>.
	 * 
	 * @param value
	 *        The MemValue equivalent of the supplied value, or <tt>null</tt>.
	 * @return A previously created MemValue that is equal to <tt>value</tt>, or
	 *         <tt>null</tt> if no such value exists or if <tt>value</tt> is
	 *         equal to <tt>null</tt>.
	 */
	public MemValue getMemValue(Value value) {
		if (value instanceof Resource) {
			return getMemResource((Resource)value);
		}
		else if (value instanceof Literal) {
			return getMemLiteral((Literal)value);
		}
		else if (value == null) {
			return null;
		}
		else {
			throw new IllegalArgumentException("value is not a Resource or Literal: " + value);
		}
	}

	/**
	 * See getMemValue() for description.
	 */
	public MemResource getMemResource(Resource resource) {
		if (resource instanceof URI) {
			return getMemURI((URI)resource);
		}
		else if (resource instanceof BNode) {
			return getMemBNode((BNode)resource);
		}
		else if (resource == null) {
			return null;
		}
		else {
			throw new IllegalArgumentException("resource is not a URI or BNode: " + resource);
		}
	}

	/**
	 * See getMemValue() for description.
	 */
	public MemURI getMemURI(URI uri) {
		return uf.getMemURI(uri);
	}

	/**
	 * See getMemValue() for description.
	 */
	public MemBNode getMemBNode(BNode bnode) {
		try {
			return bf.getMemBNode(bnode);
		}
		catch (IllegalArgumentException e) {
			return (MemBNode)createBNode(bnode.getID());
		}
	}

	/**
	 * See getMemValue() for description.
	 */
	public MemLiteral getMemLiteral(Literal literal) {
		return lf.getMemLiteral(literal);
	}

	/**
	 * Gets all URIs that are managed by this value factory.
	 * <p>
	 * <b>Warning:</b> This method is not synchronized. To iterate over the
	 * returned set in a thread-safe way, this method should only be called while
	 * synchronizing on this object.
	 * 
	 * @return An unmodifiable Set of MemURI objects.
	 */
	public Set<MemURI> getMemURIs() {
		return uf.getMemURIs();
	}

	/**
	 * Gets all bnodes that are managed by this value factory.
	 * <p>
	 * <b>Warning:</b> This method is not synchronized. To iterate over the
	 * returned set in a thread-safe way, this method should only be called while
	 * synchronizing on this object.
	 * 
	 * @return An unmodifiable Set of MemBNode objects.
	 */
	public Set<MemBNode> getMemBNodes() {
		return bf.getMemBNodes();
	}

	/**
	 * Gets all literals that are managed by this value factory.
	 * <p>
	 * <b>Warning:</b> This method is not synchronized. To iterate over the
	 * returned set in a thread-safe way, this method should only be called while
	 * synchronizing on this object.
	 * 
	 * @return An unmodifiable Set of MemURI objects.
	 */
	public Set<MemLiteral> getMemLiterals() {
		return lf.getMemLiterals();
	}

	/**
	 * Creates a MemValue for the supplied Value. The supplied value should not
	 * already have an associated MemValue. The created MemValue is returned.
	 * 
	 * @param value
	 *        A Resource or Literal.
	 * @return The created MemValue.
	 */
	public MemValue createMemValue(Value value) {
		if (value instanceof Resource) {
			return createMemResource((Resource)value);
		}
		else if (value instanceof Literal) {
			return createMemLiteral((Literal)value);
		}
		else {
			throw new IllegalArgumentException("value is not a Resource or Literal: " + value);
		}
	}

	/**
	 * See createMemValue() for description.
	 */
	public MemResource createMemResource(Resource resource) {
		if (resource instanceof URI) {
			return createMemURI((URI)resource);
		}
		else if (resource instanceof BNode) {
			return createMemBNode((BNode)resource);
		}
		else {
			throw new IllegalArgumentException("resource is not a URI or BNode: " + resource);
		}
	}

	/**
	 * See createMemValue() for description.
	 */
	public MemURI createMemURI(URI uri) {
		return uf.createMemURI(uri);
	}

	/**
	 * See createMemValue() for description.
	 */
	public MemBNode createMemBNode(BNode bnode) {
		try {
			return bf.createMemBNode(bnode);
		}
		catch (IllegalArgumentException e) {
			return (MemBNode)createBNode(bnode.getID());
		}
	}

	/**
	 * See createMemValue() for description.
	 */
	public MemLiteral createMemLiteral(Literal literal) {
		return lf.createMemLiteral(literal);
	}
}
