/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import java.util.Collections;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.URIFactory;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.util.URIUtil;

/**
 * A factory for MemURI objects that keeps track of created objects to prevent
 * the creation of duplicate objects, minimizing memory usage as a result.
 * 
 * @author Arjohn Kampman
 * @author David Huynh
 * @author James Leigh
 */
public class MemURIFactory implements URIFactory {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Registry containing the set of MemURI objects as used by a MemoryStore.
	 * This registry enables the reuse of objects, minimizing the number of
	 * objects in main memory.
	 */
	private final WeakObjectRegistry<MemURI> uriRegistry = new WeakObjectRegistry<MemURI>();

	/**
	 * Registry containing the set of namespce strings as used by MemURI objects
	 * in a MemoryStore. This registry enables the reuse of objects, minimizing
	 * the number of objects in main memory.
	 */
	private final WeakObjectRegistry<String> namespaceRegistry = new WeakObjectRegistry<String>();

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * See getMemValue() for description.
	 */
	public synchronized MemURI getMemURI(URI uri) {
		if (isOwnMemValue(uri)) {
			return (MemURI)uri;
		}
		else {
			return uriRegistry.get(uri);
		}
	}

	/**
	 * Checks whether the supplied value is an instance of <tt>MemValue</tt> and
	 * whether it has been created by this MemValueFactory.
	 */
	private boolean isOwnMemValue(Value value) {
		return value instanceof MemValue && ((MemValue)value).getCreator() == this;
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
		return Collections.unmodifiableSet(uriRegistry);
	}

	/**
	 * See createMemValue() for description.
	 */
	public synchronized MemURI createMemURI(URI uri) {
		// Namespace strings are relatively large objects and are shared
		// between uris
		String namespace = uri.getNamespace();
		String sharedNamespace = namespaceRegistry.get(namespace);

		if (sharedNamespace == null) {
			// New namespace, add it to the registry
			namespaceRegistry.add(namespace);
		}
		else {
			// Use the shared namespace
			namespace = sharedNamespace;
		}

		// Create a MemURI and add it to the registry
		MemURI memURI;
		if (isOwnMemValue(uri) && namespace == uri.getNamespace()) {
			// Supplied parameter is a MemURI that can be reused
			memURI = (MemURI)uri;
		}
		else {
			memURI = new MemURI(this, namespace, uri.getLocalName());
		}

		boolean wasNew = uriRegistry.add(memURI);
		assert wasNew : "Created a duplicate MemURI for URI " + uri;

		return memURI;
	}

	public synchronized URI createURI(String uri) {
		URI tempURI = new URIImpl(uri);
		MemURI memURI = getMemURI(tempURI);

		if (memURI == null) {
			memURI = createMemURI(tempURI);
		}

		return memURI;
	}

	public synchronized URI createURI(String namespace, String localName) {
		URI tempURI = null;

		// Reuse supplied namespace and local name strings if possible
		if (URIUtil.isCorrectURISplit(namespace, localName)) {
			if (namespace.indexOf(':') == -1) {
				throw new IllegalArgumentException("Not a valid (absolute) URI: " + namespace + localName);
			}

			tempURI = new MemURI(this, namespace, localName);
		}
		else {
			tempURI = new URIImpl(namespace + localName);
		}

		MemURI memURI = uriRegistry.get(tempURI);

		if (memURI == null) {
			memURI = createMemURI(tempURI);
		}

		return memURI;
	}
}
