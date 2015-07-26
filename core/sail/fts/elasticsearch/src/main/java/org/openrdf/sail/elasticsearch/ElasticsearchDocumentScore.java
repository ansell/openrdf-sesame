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

import java.util.Arrays;

import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;
import org.openrdf.sail.lucene.DocumentScore;
import org.openrdf.sail.lucene.SearchDocument;
import org.openrdf.sail.lucene.SearchFields;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.spatial4j.core.context.SpatialContext;

public class ElasticsearchDocumentScore implements DocumentScore {

	private final SearchHit hit;

	private final SpatialContext geoContext;

	private ElasticsearchDocument fullDoc;

	public ElasticsearchDocumentScore(SearchHit hit, SpatialContext geoContext) {
		this.hit = hit;
		this.geoContext = geoContext;
	}

	@Override
	public SearchDocument getDocument() {
		if (fullDoc == null) {
			fullDoc = new ElasticsearchDocument(hit, geoContext);
		}
		return fullDoc;
	}

	@Override
	public float getScore() {
		return hit.getScore();
	}

	@Override
	public boolean isHighlighted() {
		return (hit.getHighlightFields() != null);
	}

	@Override
	public Iterable<String> getSnippets(String field) {
		HighlightField highlightField = hit.getHighlightFields().get(field);
		if (highlightField == null) {
			return null;
		}
		return Iterables.transform(Arrays.asList(highlightField.getFragments()), new Function<Text, String>() {

			@Override
			public String apply(Text fragment) {
				return SearchFields.getSnippet(fragment.string());
			}
		});
	}
}
