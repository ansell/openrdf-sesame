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
package org.openrdf.sail.lucene3;

import java.io.IOException;
import java.util.Arrays;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.sail.lucene.DocumentScore;
import org.openrdf.sail.lucene.SearchFields;
import org.openrdf.sail.lucene.SearchQuery;

/**
 * To be removed, no longer used.
 */
@Deprecated
public class LuceneQuery implements SearchQuery
{
	private final Query query;
	private final LuceneIndex index;
	private Highlighter highlighter;

	public LuceneQuery(Query q, LuceneIndex index) {
		this.query = q;
		this.index = index;
	}

	@Override
	public Iterable<? extends DocumentScore> query(Resource resource) throws IOException {
		TopDocs docs;
		if(resource != null) {
			docs = index.search(resource, query);
		}
		else {
			docs = index.search(query);
		}
		return Iterables.transform(Arrays.asList(docs.scoreDocs), new Function<ScoreDoc,DocumentScore>()
		{
			@Override
			public DocumentScore apply(ScoreDoc doc) {
				return new LuceneDocumentScore(doc, highlighter, index);
			}
		});
	}

	@Override
	public void highlight(URI property) {
		Formatter formatter = new SimpleHTMLFormatter(SearchFields.HIGHLIGHTER_PRE_TAG, SearchFields.HIGHLIGHTER_POST_TAG);
		highlighter = new Highlighter(formatter, new QueryScorer(query));
	}
}
