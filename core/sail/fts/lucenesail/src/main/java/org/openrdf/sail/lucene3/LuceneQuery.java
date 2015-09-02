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
