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
package org.openrdf.sail.lucene;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A Map like structure where each key maps to a set of values.
 */
public class SetMap<K, V> {

	private final Map<K, Set<V>> data;

	public SetMap() {
		data = new HashMap<K, Set<V>>();
	}

	public V put(K key, V value) {
		Set<V> set = data.get(key);
		if (set == null) {
			set = new HashSet<V>();
			data.put(key, set);
		}
		set.add(value);
		return null;
	}

	public Set<V> get(K key) {
		return data.get(key);
	}

	public boolean containsKey(K key) {
		return data.containsKey(key);
	}

	public boolean containsKeyValuePair(K key, V val) {
		Set<V> set = data.get(key);
		if (set != null && set.contains(val)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return data.toString();
	}

	public void remove(K key) {
		data.remove(key);
	}

	public Set<Entry<K, Set<V>>> entrySet() {
		return data.entrySet();
	}
}