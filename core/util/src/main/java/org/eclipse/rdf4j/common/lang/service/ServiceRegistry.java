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
package org.eclipse.rdf4j.common.lang.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry that stores services by some key. Upon initialization, the
 * registry searches for service description files at
 * <tt>META-INF/services/&lt;service class name&gt;</tt> and initializes itself
 * accordingly.
 * 
 * @see javax.imageio.spi.ServiceRegistry
 * @author Arjohn Kampman
 */
public abstract class ServiceRegistry<K, S> {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected Map<K, S> services = new ConcurrentHashMap<K, S>(16, 0.75f, 1);

	protected ServiceRegistry(Class<S> serviceClass) {
		ServiceLoader<S> loader = java.util.ServiceLoader.load(serviceClass, serviceClass.getClassLoader());

		Iterator<S> services = loader.iterator();
		while (true) {
			try {
				if (services.hasNext()) {
					S service = services.next();

					Optional<S> oldService = add(service);

					if (oldService.isPresent()) {
						logger.warn("New service {} replaces existing service {}", service.getClass(),
								oldService.get().getClass());
					}

					logger.debug("Registered service class {}", service.getClass().getName());
				}
				else {
					break;
				}
			}
			catch (Error e) {
				logger.error("Failed to instantiate service", e);
			}
		}
	}

	/**
	 * Adds a service to the registry. Any service that is currently registered
	 * for the same key (as specified by {@link #getKey(Object)}) will be
	 * replaced with the new service.
	 * 
	 * @param service
	 *        The service that should be added to the registry.
	 * @return The previous service that was registered for the same key, or
	 *         {@link Optional#empty()} if there was no such service.
	 */
	public Optional<S> add(S service) {
		return Optional.ofNullable(services.put(getKey(service), service));
	}

	/**
	 * Removes a service from the registry.
	 * 
	 * @param service
	 *        The service be removed from the registry.
	 */
	public void remove(S service) {
		services.remove(getKey(service));
	}

	/**
	 * Gets the service for the specified key, if any.
	 * 
	 * @param key
	 *        The key identifying which service to get.
	 * @return The service for the specified key, or {@link Optional#empty()} if
	 *         no such service is avaiable.
	 */
	public Optional<S> get(K key) {
		return Optional.ofNullable(services.get(key));
	}

	/**
	 * Checks whether a service for the specified key is available.
	 * 
	 * @param key
	 *        The key identifying which service to search for.
	 * @return <tt>true</tt> if a service for the specific key is available,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean has(K key) {
		return services.containsKey(key);
	}

	/**
	 * Gets all registered services.
	 * 
	 * @return An unmodifiable collection containing all registered servivces.
	 */
	public Collection<S> getAll() {
		return Collections.unmodifiableCollection(services.values());
	}

	/**
	 * Gets the set of registered keys.
	 * 
	 * @return An unmodifiable set containing all registered keys.
	 */
	public Set<K> getKeys() {
		return Collections.unmodifiableSet(services.keySet());
	}

	/**
	 * Gets the key for the specified service.
	 * 
	 * @param service
	 *        The service to get the key for.
	 * @return The key for the specified service.
	 */
	protected abstract K getKey(S service);
}
