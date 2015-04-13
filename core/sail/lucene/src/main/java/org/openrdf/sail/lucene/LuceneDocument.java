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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexableField;


/**
 *
 * @author MJAHale
 */
public class LuceneDocument implements SearchDocument
{
	private final Document doc;

	public LuceneDocument()
	{
		this(new Document());
	}

	public LuceneDocument(Document doc)
	{
		this.doc = doc;
	}

	public LuceneDocument(String id, String resourceId, String context)
	{
		this();
		setId(id);
		setURIField(resourceId);
		setContextField(context);
	}

	private void setId(String id) {
		doc.add(new StringField(SearchFields.ID_FIELD_NAME, id, Store.YES));
	}

	private void setURIField(String resourceId) {
		doc.add(new StringField(SearchFields.URI_FIELD_NAME, resourceId, Store.YES));
	}

	private void setContextField(String context) {
		if (context != null) {
			doc.add(new StringField(SearchFields.CONTEXT_FIELD_NAME, context, Store.YES));
		}
	}

	public Document getDocument() {
		return doc;
	}

	@Override
	public String getId() {
		return doc.get(SearchFields.ID_FIELD_NAME);
	}

	public String getResource() {
		return doc.get(SearchFields.URI_FIELD_NAME);
	}

	public String getContext() {
		return doc.get(SearchFields.CONTEXT_FIELD_NAME);
	}

	@Override
	public List<String> getPropertyNames() {
		List<IndexableField> fields = doc.getFields();
		List<String> names = new ArrayList<String>(fields.size());
		for(IndexableField field : fields) {
			String name = field.name();
			if (SearchFields.isPropertyField(name))
				names.add(name);
		}
		return names;
	}

	@Override
	public void addProperty(String name, String text) {
		doc.add(new TextField(name, text, Store.YES));
		doc.add(new TextField(SearchFields.TEXT_FIELD_NAME, text, Store.YES));
	}

	@Override
	public boolean hasProperty(String fieldName, String value) {
		String[] fields = doc.getValues(fieldName);
		if (fields != null) {
			for (String field : fields) {
				if (value.equals(field)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public List<String> getProperty(String name) {
		return Arrays.asList(doc.getValues(name));
	}

}
