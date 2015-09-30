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
package org.openrdf.sail.lucene;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to cache SearchDocument.hasProperty() calls.
 */
class PropertyCache {
	private final SearchDocument doc;
	private Map<String, Set<String>> cachedProperties;

	PropertyCache(SearchDocument doc) {
		this.doc = doc;
	}

	boolean hasProperty(String name, String value) {
		boolean found;
		Set<String> cachedValues = getCachedValues(name);
		if(cachedValues != null) {
			found = cachedValues.contains(value);
		}
		else {
			found = false;
			List<String> docValues = doc.getProperty(name);
			if(docValues != null) {
				cachedValues = new HashSet<String>(docValues.size());
				for(String docValue : docValues) {
					cachedValues.add(docValue);
					if(docValue.equals(value)) {
						found = true;
						// don't break - cache all docValues
					}
				}
			}
			else {
				cachedValues = Collections.emptySet();
			}
			setCachedValues(name, cachedValues);
		}
		return found;
	}

	private Set<String> getCachedValues(String name) {
		return (cachedProperties != null) ? cachedProperties.get(name) : null;
	}

	private void setCachedValues(String name, Set<String> values) {
		if(cachedProperties == null) {
			cachedProperties = new HashMap<String,Set<String>>();
		}
		cachedProperties.put(name, values);
	}
}
