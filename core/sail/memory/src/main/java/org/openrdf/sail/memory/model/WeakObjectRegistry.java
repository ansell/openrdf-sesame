/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
	public Iterator<E> iterator()
	{
		return objectMap.keySet().iterator();
	}

	@Override
	public int size()
	{
		return objectMap.size();
	}

	@Override
	public boolean contains(Object o)
	{
		return get(o) != null;
	}

	@Override
	public boolean add(E object)
	{
		WeakReference<E> ref = new WeakReference<E>(object);

		ref = objectMap.put(object, ref);

		if (ref != null && ref.get() != null) {
			// A duplicate was added which replaced the existing object. Undo this
			// operation.
			objectMap.put(ref.get(), ref);
			return false;
		}

		return true;
	}

	@Override
	public boolean remove(Object o)
	{
		WeakReference<E> ref = objectMap.remove(o);
		return ref != null && ref.get() != null;
	}

	@Override
	public void clear()
	{
		objectMap.clear();
	}
}
