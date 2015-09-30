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
package org.openrdf.util;

import java.util.Map;
import java.util.UUID;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


/**
 * A registry to support (de)serialization of objects (over the lifetime of the VM).
 * It uses weak references to allow entries to be garbage-collected when no longer used.
 * @author Mark
 */
public class NonSerializables {
	private static final Cache<UUID,Object> registry = CacheBuilder.newBuilder().weakValues().build();

	/**
	 * Retrieve the object registered with the supplied key.
	 * 
	 * @param key
	 *        the key.
	 * @return the registered object, or <code>null</code> if no
	 *         matching EvaluationStrategy can be found.
	 */
	public static final Object get(UUID key) {
		return registry.getIfPresent(key);
	}

	/**
	 * Retrieves the registry key for the given object.
	 * 
	 * @param obj
	 *        the object for which to retrieve the registry key.
	 * @return the registry key with which the supplied object can be
	 *         retrieved, or <code>null</code> if the supplied object is not in
	 *         the registry.
	 */
	public static final UUID getKey(Object obj) {
		final Map<UUID, Object> map = registry.asMap();

		// we could make this lookup more efficient with a WeakHashMap-based
		// reverse index, but we currently prefer this slower but more robust
		// approach (less chance of accidental lingering references that prevent
		// GC)
		for (UUID key : map.keySet()) {
			// we use identity comparison in line with how guava caches behave
			// when softValues are used.
			if (obj == map.get(key)) {
				return key;
			}
		}
		return null;
	}

	/**
	 * Add an object to the registry and returns the registry key. If the
	 * object is already present, the operation simply returns the key with
	 * which it is currently registered.
	 * 
	 * @param obj
	 *        the object to register
	 * @return the key with which the object is registered.
	 */
	public static final UUID register(Object obj) {
		UUID key = getKey(obj);
		if (key == null) {
			key = UUID.randomUUID();
			registry.put(key, obj);
		}
		return key;
	}

	/**
	 * Prevent instantiation: util class
	 */
	private NonSerializables() {
	}
}
