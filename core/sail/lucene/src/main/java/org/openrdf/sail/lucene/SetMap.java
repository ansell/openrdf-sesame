/**
 * 
 */
package org.openrdf.sail.lucene;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * A Map like structure where each key maps to a map of values. 
 */
public class SetMap<K,V> {

	private final HashMap<K, Set<V>> data;

	public SetMap() { 
		data=new HashMap<K,Set<V>>();
	}
	
	public V put(K key, V value) {
		Set<V> set;
		if (data.containsKey(key)) {
			set=data.get(key);
		} else { 
			set=new HashSet<V>();
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
		Set<V> set;
		if (data.containsKey(key)) {
			set=data.get(key);
			if(set.contains(val))
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