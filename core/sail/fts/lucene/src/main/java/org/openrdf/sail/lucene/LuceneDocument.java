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
import java.util.Collection;
import java.util.List;

import com.spatial4j.core.shape.Shape;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;


public class LuceneDocument implements SearchDocument
{
	private final Document doc;
	private final SpatialPrefixTree grid;

	public LuceneDocument(SpatialPrefixTree tree)
	{
		this(new Document(), tree);
	}

	public LuceneDocument(Document doc, SpatialPrefixTree grid)
	{
		this.doc = doc;
		this.grid = grid;
	}

	public LuceneDocument(String id, String resourceId, String context, SpatialPrefixTree grid)
	{
		this(grid);
		setId(id);
		setResource(resourceId);
		setContext(context);
	}

	private void setId(String id) {
		LuceneIndex.addIDField(id, doc);
	}

	private void setContext(String context) {
		LuceneIndex.addContextField(context, doc);
	}

	private void setResource(String resourceId) {
		LuceneIndex.addResourceField(resourceId, doc);
	}

	public Document getDocument() {
		return doc;
	}

	@Override
	public String getId() {
		return doc.get(SearchFields.ID_FIELD_NAME);
	}

	@Override
	public String getResource() {
		return doc.get(SearchFields.URI_FIELD_NAME);
	}

	@Override
	public String getContext() {
		return doc.get(SearchFields.CONTEXT_FIELD_NAME);
	}

	@Override
	public Collection<String> getPropertyNames() {
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
	public void addProperty(String name) {
		// don't need to do anything
	}

	/**
	 * Stores and indexes a property in a Document. We don't have to recalculate
	 * the concatenated text: just add another TEXT field and Lucene will take
	 * care of this. Additional advantage: Lucene may be able to handle the
	 * invididual strings in a way that may affect e.g. phrase and proximity
	 * searches (concatenation basically means loss of information). NOTE: The
	 * TEXT_FIELD_NAME has to be stored, see in LuceneSail
	 * 
	 * @see LuceneSail
	 */
	@Override
	public void addProperty(String name, String text) {
		LuceneIndex.addPredicateField(name, text, doc);
		LuceneIndex.addTextField(text, doc);
	}

	/**
	 * Checks whether a field occurs with a specified value in a Document.
	 */
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

	@Override
	public void addShape(String field, Shape shape) {
		SpatialStrategy geoStrategy = new RecursivePrefixTreeStrategy(grid, field);
		for(IndexableField f : geoStrategy.createIndexableFields(shape)) {
			doc.add(f);
		}
	}
}
