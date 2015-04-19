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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.sail.lucene.DocumentScore;
import org.openrdf.sail.lucene.SearchFields;
import org.openrdf.sail.lucene.SearchQuery;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class SolrSearchQuery implements SearchQuery {
	private final SolrQuery query;
	private SolrIndex index;

	public SolrSearchQuery(SolrQuery q, SolrIndex index) {
		this.query = q;
		this.index = index;
	}

	@Override
	public Iterable<? extends DocumentScore> query(Resource resource)
			throws IOException {
		QueryResponse response;
		if(query.getHighlight()) {
			query.addField("*");
		}
		else {
			query.addField(SearchFields.URI_FIELD_NAME);
		}
		query.addField("score");
		try {
			if(resource != null) {
				response = index.search(resource, query);
			}
			else {
				response = index.search(query);
			}
		} catch(SolrServerException e) {
			throw new IOException(e);
		}
		SolrDocumentList results = response.getResults();
		final Map<String,Map<String,List<String>>> highlighting = response.getHighlighting();
		return Iterables.transform(results, new Function<SolrDocument,DocumentScore>()
		{
			@Override
			public DocumentScore apply(SolrDocument document) {
				SolrSearchDocument doc = new SolrSearchDocument(document);
				Map<String,List<String>> docHighlighting = (highlighting != null) ? highlighting.get(doc.getId()) : null;
				return new SolrDocumentScore(doc, docHighlighting);
			}
		});
	}

	/**
	 * Highlights the given field or all fields if null.
	 */
	@Override
	public void highlight(URI property) {
		query.setHighlight(true);
		String field = (property != null) ? property.toString() : "*";
		query.addHighlightField(field);
		query.setHighlightSimplePre(SearchFields.HIGHLIGHTER_PRE_TAG);
		query.setHighlightSimplePost(SearchFields.HIGHLIGHTER_POST_TAG);
		query.setHighlightSnippets(2);
	}

}
