/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.impl.NamespaceImpl;

/**
 * An in-memory store for namespace prefix information.
 * 
 * @author Arjohn Kampman
 */
class MemNamespaceStore implements Iterable<NamespaceImpl> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Map storing namespace information by their prefix.
	 */
	private Map<String, NamespaceImpl> namespacesMap;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MemNamespaceStore() {
		namespacesMap = new LinkedHashMap<String, NamespaceImpl>(16);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getNamespace(String prefix) {
		String result = null;
		NamespaceImpl namespace = namespacesMap.get(prefix);
		if (namespace != null) {
			result = namespace.getName();
		}
		return result;
	}

	public void setNamespace(String prefix, String name) {
		namespacesMap.put(prefix, new NamespaceImpl(prefix, name));
	}

	public void removeNamespace(String prefix) {
		namespacesMap.remove(prefix);
	}

	public Iterator<NamespaceImpl> iterator() {
		return namespacesMap.values().iterator();
	}

	public void clear() {
		namespacesMap.clear();
	}
}
