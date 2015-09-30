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
package org.openrdf.sail.elasticsearch;

import java.io.IOException;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
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
public class ElasticsearchQuery implements SearchQuery {
	private final SearchRequestBuilder request;
	private final QueryBuilder qb;
	private ElasticsearchIndex index;

	public ElasticsearchQuery(SearchRequestBuilder request, QueryBuilder qb, ElasticsearchIndex index) {
		this.request = request;
		this.qb = qb;
		this.index = index;
	}

	@Override
	public Iterable<? extends DocumentScore> query(Resource resource)
			throws IOException {
		SearchHits hits;
		if(resource != null) {
			hits = index.search(resource, request, qb);
		}
		else {
			hits = index.search(request, qb);
		}
		return Iterables.transform(hits, new Function<SearchHit,DocumentScore>()
		{
			@Override
			public DocumentScore apply(SearchHit hit) {
				return new ElasticsearchDocumentScore(hit, null);
			}
		});
	}

	/**
	 * Highlights the given field or all fields if null.
	 */
	@Override
	public void highlight(URI property) {
		String field = (property != null) ? SearchFields.getPropertyField(property) : "*";
		request.addHighlightedField(field);
		request.setHighlighterPreTags(SearchFields.HIGHLIGHTER_PRE_TAG);
		request.setHighlighterPostTags(SearchFields.HIGHLIGHTER_POST_TAG);
		// Elastic Search doesn't really have the same support for fragments as Lucene.
		// So, we have to get back the whole highlighted value (comma-separated if it is a list)
		// and then post-process it into fragments ourselves.
		request.setHighlighterNumOfFragments(0);
	}
}
