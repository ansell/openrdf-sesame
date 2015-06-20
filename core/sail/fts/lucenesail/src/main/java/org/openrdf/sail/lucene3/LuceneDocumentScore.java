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

import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.openrdf.sail.lucene.DocumentScore;
import org.openrdf.sail.lucene.SearchDocument;
import org.openrdf.sail.lucene.SearchFields;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class LuceneDocumentScore implements DocumentScore
{
	private final ScoreDoc scoreDoc;
	private final Highlighter highlighter;
	private final LuceneIndex index;
	private LuceneDocument fullDoc;

	public LuceneDocumentScore(ScoreDoc doc, Highlighter highlighter, LuceneIndex index) {
		this.scoreDoc = doc;
		this.highlighter = highlighter;
		this.index = index;
	}

	@Override
	public SearchDocument getDocument() {
		if(fullDoc == null)
		{
			Document doc;
			if(highlighter != null) {
				doc = index.getDocument(scoreDoc.doc, null);
			}
			else {
				// don't require all fields
				doc = index.getDocument(scoreDoc.doc, Collections.singleton(SearchFields.URI_FIELD_NAME));
			}
			fullDoc = new LuceneDocument(doc, index.getCartesianTiers());
		}
		return fullDoc;
	}

	@Override
	public float getScore() {
		return scoreDoc.score;
	}

	@Override
	public boolean isHighlighted() {
		return (highlighter != null);
	}

	@Override
	public Iterable<String> getSnippets(final String field) {
		List<String> values = getDocument().getProperty(field);
		if(values == null) {
			return null;
		}
		return Iterables.transform(values, new Function<String,String>()
		{
			@Override
			public String apply(String text) {
				return index.getSnippet(field, text, highlighter);
			}
		});
	}
}
