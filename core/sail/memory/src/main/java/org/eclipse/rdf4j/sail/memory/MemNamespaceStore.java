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
package org.eclipse.rdf4j.sail.memory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.impl.SimpleNamespace;

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