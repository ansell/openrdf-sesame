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
package org.openrdf.sail.memory.model;

import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * An object registry that uses weak references to keep track of the stored
 * objects. The registry can be used to retrieve stored objects using another,
 * equivalent object. As such, it can be used to prevent the use of duplicates
 * in another data structure, reducing memory usage. The objects that are being
 * stored should properly implement the {@link Object#equals} and
 * {@link Object#hashCode} methods.
 */
public class WeakObjectRegistry<E> extends AbstractSet<E> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The hash map that is used to store the objects.
	 */
	private final Map<E, WeakReference<E>> objectMap = new WeakHashMap<E, WeakReference<E>>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Constructs a new, empty object registry.
	 */
	public WeakObjectRegistry() {
		super();
	}

	/**
	 * Constructs a new WeakObjectRegistry containing the elements in the
	 * specified collection.
	 * 
	 * @param c
	 *        The collection whose elements are to be placed into this object
	 *        registry.
	 * @throws NullPointerException
	 *         If the specified collection is null.
	 */
	public WeakObjectRegistry(Collection<? extends E> c) {
		this();
		addAll(c);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Retrieves the stored object that is equal to the supplied <tt>key</tt>
	 * object.
	 * 
	 * @param key
	 *        The object that should be used as the search key for the operation.
	 * @return A stored object that is equal to the supplied key, or
	 *         <tt>null</tt> if no such object was found.
	 */
	public E get(Object key) {
		WeakReference<E> weakRef = objectMap.get(key);

		if (weakRef != null) {
			return weakRef.get();
		}

		return null;
	}

	@Override
	public Iterator<E> iterator() {
		return objectMap.keySet().iterator();
	}

	@Override
	public int size() {
		return objectMap.size();
	}

	@Override
	public boolean contains(Object o) {
		return get(o) != null;
	}

	@Override
	public boolean add(E object) {
		WeakReference<E> ref = new WeakReference<E>(object);

		WeakReference<E> existingRef = objectMap.put(object, ref);

		if (existingRef != null) {
			// Get a hard reference to the value to avoid GC timing issues
			E existingObject = existingRef.get();
			if (existingObject != null) {
				// A duplicate was added which replaced the existing object. Undo
				// this
				// operation.
				objectMap.put(existingObject, existingRef);
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean remove(Object o) {
		WeakReference<E> ref = objectMap.remove(o);
		return ref != null && ref.get() != null;
	}

	@Override
	public void clear() {
		objectMap.clear();
	}
}
