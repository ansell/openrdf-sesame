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
package org.openrdf.sail.lucene.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Map like structure where each key maps to a list of values. I couldn't get
 * the generics to work the way I wanted, so it doesn't inherit anything. TODO:
 * move this to a utils project.
 * 
 * @author grimnes
 */
public class ListMap<K, V> {

	private final Map<K, List<V>> data;

	public ListMap() {
		data = new HashMap<K, List<V>>();
	}

	public V put(K key, V value) {
		List<V> list = data.get(key);
		if (list == null) {
			list = new ArrayList<V>();
			data.put(key, list);
		}
		list.add(value);
		return null;
	}

	public List<V> get(K key) {
		return data.get(key);
	}

	public boolean containsKey(K key) {
		return data.containsKey(key);
	}

	@Override
	public String toString() {
		return data.toString();
	}

	public void remove(K key) {
		data.remove(key);
	}

}
