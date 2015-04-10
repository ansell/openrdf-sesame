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
package org.openrdf.query.algebra.evaluation.util;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import org.openrdf.query.algebra.evaluation.EvaluationStrategy;

/**
 * Registry for currently active {@link EvaluationStrategy} objects. The
 * internal registry uses soft references to allow entries to be
 * garbage-collected when no longer used.
 * 
 * @author Jeen Broekstra
 */
public class EvaluationStrategies {

	private static final HashMap<Integer, SoftReference<EvaluationStrategy>> registry = new HashMap<Integer, SoftReference<EvaluationStrategy>>();

	/**
	 * Retrieve the EvaluationStrategy registered with the supplied key.
	 * 
	 * @param key
	 *        the key
	 * @return the registered EvaluationStrategy, or <code>null</code> if no
	 *         matching EvaluationStrategy can be found.
	 */
	public static final EvaluationStrategy get(Integer key) {
		return registry.get(key).get();
	}

	/**
	 * Add a strategy with the supplied key to the registry. Any existing
	 * registry entry with the same key will be silently overwritten.
	 * 
	 * @param key
	 *        the key
	 * @param strategy
	 *        the EvaluationStrategy to register
	 */
	public static final void register(Integer key, EvaluationStrategy strategy) {
		registry.put(key, new SoftReference<EvaluationStrategy>(strategy));
	}

	/**
	 * Prevent instantiation: util class
	 */
	private EvaluationStrategies() {
	}
}
