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
