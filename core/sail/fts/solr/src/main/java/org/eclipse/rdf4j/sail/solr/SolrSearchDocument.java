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
package org.eclipse.rdf4j.sail.solr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrDocument;
import org.eclipse.rdf4j.sail.lucene.SearchDocument;
import org.eclipse.rdf4j.sail.lucene.SearchFields;

public class SolrSearchDocument implements SearchDocument {

	private final SolrDocument doc;

	public SolrSearchDocument() {
		this(new SolrDocument());
	}

	public SolrSearchDocument(SolrDocument doc) {
		this.doc = doc;
	}

	public SolrSearchDocument(String id, String resourceId, String context) {
		this();
		doc.put(SearchFields.ID_FIELD_NAME, id);
		doc.put(SearchFields.URI_FIELD_NAME, resourceId);
		if (context != null) {
			doc.put(SearchFields.CONTEXT_FIELD_NAME, context);
		}
	}

	public SolrDocument getDocument() {
		return doc;
	}

	@Override
	public String getId() {
		return (String)doc.get(SearchFields.ID_FIELD_NAME);
	}

	@Override
	public String getResource() {
		return (String)doc.get(SearchFields.URI_FIELD_NAME);
	}

	@Override
	public String getContext() {
		return (String)doc.get(SearchFields.CONTEXT_FIELD_NAME);
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

	private static void addField(String name, String value, Map<String, Object> document) {
		Object oldValue = document.get(name);
		Object newValue;
		if (oldValue != null) {
			List<String> newList = makeModifiable(asStringList(oldValue));
			newList.add(value);
			newValue = newList;
		}
		else {
			newValue = value;
		}
		document.put(name, newValue);
	}

	private static List<String> makeModifiable(List<String> l) {
		List<String> modList;
		if (!(l instanceof ArrayList<?>)) {
			modList = new ArrayList<String>(l.size() + 1);
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
		if (value == null) {
			l = null;
		}
		else if (value instanceof List<?>) {
			l = (List<String>)value;
		}
		else {
			l = Collections.singletonList((String)value);
		}
		return l;
	}
}
