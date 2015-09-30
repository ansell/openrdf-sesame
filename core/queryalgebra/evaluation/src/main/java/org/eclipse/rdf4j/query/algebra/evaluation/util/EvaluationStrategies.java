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
package org.eclipse.rdf4j.query.algebra.evaluation.util;

import java.util.Map;
import java.util.UUID;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;

/**
 * Registry for currently active {@link EvaluationStrategy} objects. The
 * internal registry uses soft references to allow entries to be
 * garbage-collected when no longer used. Currently, the primary purpose of this
 * is to support (de)serialization of objects (over the lifetime of the VM) that
 * depend on an EvaluationStrategy
 * 
 * @author Jeen Broekstra
 */
public class EvaluationStrategies {

	private static final Cache<UUID, EvaluationStrategy> registry = CacheBuilder.newBuilder().weakValues().build();

	/**
	 * Retrieve the EvaluationStrategy registered with the supplied key.
	 * 
	 * @param key
	 *        the key
	 * @return the registered EvaluationStrategy, or <code>null</code> if no
	 *         matching EvaluationStrategy can be found.
	 */
	public static final EvaluationStrategy get(UUID key) {
		return registry.getIfPresent(key);
	}

	/**
	 * Retrieve the registry key for the given EvaluationStrategy
	 * 
	 * @param strategy
	 *        the EvaluationStrategy for which to retrieve the registry key
	 * @return the registry key with which the supplied strategy can be
	 *         retrieved, or <code>null</code> if the supplied strategy is not in
	 *         the registry.
	 */
	public static final UUID getKey(EvaluationStrategy strategy) {
		final Map<UUID, EvaluationStrategy> map = registry.asMap();

		// we could make this lookup more efficient with a WeakHashMap-based
		// reverse index, but we currently prefer this slower but more robust
		// approach (less chance of accidental lingering references that prevent
		// GC)
		for (UUID key : map.keySet()) {
			// we use identity comparison in line with how guava caches behave
			// when softValues are used.
			if (strategy == map.get(key)) {
				return key;
			}
		}
		return null;
	}

	/**
	 * Add a strategy to the registry and returns the registry key. If the
	 * strategy is already present, the operation simply returns the key with
	 * which it is currently registered.
	 * 
	 * @param strategy
	 *        the EvaluationStrategy to register
	 * @return the key with which the strategy is registered.
	 */
	public static final UUID register(EvaluationStrategy strategy) {
		UUID key = getKey(strategy);
		if (key == null) {
			key = UUID.randomUUID();
			registry.put(key, strategy);
		}
		return key;
	}

	/**
	 * Prevent instantiation: util class
	 */
	private EvaluationStrategies() {
	}
}
