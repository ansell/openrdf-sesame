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
package info.aduna.lang.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

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

	protected Map<K, S> services = new HashMap<K, S>();

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
