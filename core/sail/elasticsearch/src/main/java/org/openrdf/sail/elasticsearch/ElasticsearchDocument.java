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
package org.openrdf.sail.elasticsearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.search.SearchHit;
import org.openrdf.sail.lucene.SearchDocument;
import org.openrdf.sail.lucene.SearchFields;

public class ElasticsearchDocument implements SearchDocument {
	private final String id;
	private final String type;
	private final long version;
	private final Map<String,Object> fields;

	public ElasticsearchDocument(SearchHit hit) {
		this(hit.getId(), hit.getType(), hit.getVersion(), hit.getSource());
	}

	public ElasticsearchDocument(String id, String type, String resourceId, String context)
	{
		this(id, type, 0L, new HashMap<String,Object>());
		fields.put(SearchFields.URI_FIELD_NAME, resourceId);
		if (context != null) {
			fields.put(SearchFields.CONTEXT_FIELD_NAME, context);
		}
	}

	public ElasticsearchDocument(String id, String type, long version, Map<String,Object> fields) {
		this.id = id;
		this.type = type;
		this.version = version;
		this.fields = fields;
	}

	@Override
	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public long getVersion() {
		return version;
	}

	public Map<String,Object> getSource()
	{
		return fields;
	}

	@Override
	public String getResource() {
		return (String) fields.get(SearchFields.URI_FIELD_NAME);
	}

	@Override
	public String getContext() {
		return (String) fields.get(SearchFields.CONTEXT_FIELD_NAME);
	}

	@Override
	public Collection<String> getPropertyNames() {
		return ElasticsearchIndex.getPropertyFields(fields.keySet());
	}

	@Override
	public void addProperty(String name, String text) {
		addField(name, text, fields);
		addField(SearchFields.TEXT_FIELD_NAME, text, fields);
	}

	@Override
	public boolean hasProperty(String name, String value) {
		List<String> fieldValues = asStringList(fields.get(name));
		if (fieldValues != null) {
			for (String fieldValue : fieldValues) {
				if (value.equals(fieldValue)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public List<String> getProperty(String name) {
		return asStringList(fields.get(name));
	}

	private static void addField(String name, String value, Map<String,Object> document) {
		Object oldValue = document.get(name);
		Object newValue;
		if(oldValue != null) {
			List<String> newList = makeModifiable(asStringList(oldValue));
			newList.add(value);
			newValue = newList;
		}
		else {
			newValue = value;
		}
		document.put(name, newValue);
	}

	private static List<String> makeModifiable(List<String> l)
	{
		List<String> modList;
		if(!(l instanceof ArrayList<?>)) {
			modList = new ArrayList<String>(l.size()+1);
			modList.addAll(l);
		}
		else {
			modList = l;
		}
		return modList;
	}

	@SuppressWarnings("unchecked")
	private static List<String> asStringList(Object value) {
		List<String> l;
		if(value == null) {
			l = null;
		}
		else if(value instanceof List<?>) {
			l = (List<String>) value;
		}
		else {
			l = Collections.singletonList((String) value);
		}
		return l;
	}
}
