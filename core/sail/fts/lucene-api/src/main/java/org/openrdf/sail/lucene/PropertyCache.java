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
