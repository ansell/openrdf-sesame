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
package org.openrdf.sail.memory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.impl.SimpleNamespace;

/**
 * An in-memory store for namespace prefix information.
 * 
 * @author Arjohn Kampman
 */
class MemNamespaceStore implements Iterable<SimpleNamespace> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Map storing namespace information by their prefix.
	 */
	private final Map<String, SimpleNamespace> namespacesMap = new LinkedHashMap<String, SimpleNamespace>(16);

	/*---------*
	 * Methods *
	 *---------*/

	public String getNamespace(String prefix) {
		String result = null;
		SimpleNamespace namespace = namespacesMap.get(prefix);
		if (namespace != null) {
			result = namespace.getName();
		}
		return result;
	}

	public void setNamespace(String prefix, String name) {
		SimpleNamespace ns = namespacesMap.get(prefix);

		if (ns != null) {
			ns.setName(name);
		}
		else {
			namespacesMap.put(prefix, new SimpleNamespace(prefix, name));
		}
	}

	public void removeNamespace(String prefix) {
		namespacesMap.remove(prefix);
	}

	public Iterator<SimpleNamespace> iterator() {
		return namespacesMap.values().iterator();
	}

	public void clear() {
		namespacesMap.clear();
	}
}
