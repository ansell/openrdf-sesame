/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */

package org.openrdf.sail.rdbms.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map implementation providing an LRU algorithm and an optional maximum size.
 * Based on java.util.LinkedHashMap.
 * 
 * @author Herko ter Horst
 * 
 * @param <K>
 *            the type of the keys in the mapping
 * @param <V>
 *            the type of the values in the mapping
 */
public class LRUMap<K, V> extends LinkedHashMap<K, V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1102573423774831657L;

	private int maxSize;

	public LRUMap() {
		this(Integer.MAX_VALUE);
	}

	public LRUMap(int maxSize) {
		this(maxSize, 16, 0.75f);
	}

	public LRUMap(int maxSize, int initialCapacity) {
		this(maxSize, initialCapacity, 0.75f);
	}

	public LRUMap(int maxSize, int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor, true);
		this.maxSize = maxSize;
	}

	public LRUMap(Map<? extends K, ? extends V> m) {
		this(m.size());
		this.putAll(m);
	}

	protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
		return this.size() > this.getMaxSize();
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int i) {
		maxSize = i;
	}
}
