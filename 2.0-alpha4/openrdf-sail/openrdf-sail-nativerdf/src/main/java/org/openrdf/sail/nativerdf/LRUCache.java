/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility subclass of {@link LinkedHashMap} the makes it a fixed-size LRU
 * cache.
 */
class LRUCache<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = -8180282377977820910L;

	private int _capacity;

	public LRUCache(int capacity) {
		this(capacity, 0.75f);
	}

	public LRUCache(int capacity, float loadFactor) {
		super((int)(capacity / loadFactor), loadFactor, true);
		_capacity = capacity;
	}

	public int getCapacity() {
		return _capacity;
	}

	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > _capacity;
	}
}
