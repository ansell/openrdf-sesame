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

/**
 * To be removed, no longer used.
 */
@Deprecated
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
		String field = (property != null) ? SearchFields.getPropertyField(property) : "*";
		query.addHighlightField(field);
		query.setHighlightSimplePre(SearchFields.HIGHLIGHTER_PRE_TAG);
		query.setHighlightSimplePost(SearchFields.HIGHLIGHTER_POST_TAG);
		query.setHighlightSnippets(2);
	}

}
