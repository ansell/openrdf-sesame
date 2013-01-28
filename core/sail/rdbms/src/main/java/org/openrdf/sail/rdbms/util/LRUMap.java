/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
