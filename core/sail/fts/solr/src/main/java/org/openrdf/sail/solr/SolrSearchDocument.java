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
package org.openrdf.sail.solr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrDocument;
import org.openrdf.sail.lucene.SearchDocument;
import org.openrdf.sail.lucene.SearchFields;

public class SolrSearchDocument implements SearchDocument {
	private final SolrDocument doc;

	public SolrSearchDocument(){
		this(new SolrDocument());
	}

	public SolrSearchDocument(SolrDocument doc)
	{
		this.doc = doc;
	}

	public SolrSearchDocument(String id, String resourceId, String context)
	{
		this();
		doc.put(SearchFields.ID_FIELD_NAME, id);
		doc.put(SearchFields.URI_FIELD_NAME, resourceId);
		if(context != null) {
			doc.put(SearchFields.CONTEXT_FIELD_NAME, context);
		}
	}

	public SolrDocument getDocument() {
		return doc;
	}

	@Override
	public String getId() {
		return (String) doc.get(SearchFields.ID_FIELD_NAME);
	}

	@Override
	public String getResource() {
		return (String) doc.get(SearchFields.URI_FIELD_NAME);
	}

	@Override
	public String getContext() {
		return (String) doc.get(SearchFields.CONTEXT_FIELD_NAME);
	}

	@Override
	public Set<String> getPropertyNames() {
		return SolrIndex.getPropertyFields(doc.keySet());
	}

	@Override
	public void addProperty(String name) {
		// don't need to do anything
	}

	@Override
	public void addProperty(String name, String text) {
		addField(name, text, doc);
		addField(SearchFields.TEXT_FIELD_NAME, text, doc);
	}

	@Override
	public void addGeoProperty(String name, String text) {
		addField(name, text, doc);
	}

	@Override
	public boolean hasProperty(String name, String value) {
		List<String> fieldValues = asStringList(doc.get(name));
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
		return asStringList(doc.get(name));
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
