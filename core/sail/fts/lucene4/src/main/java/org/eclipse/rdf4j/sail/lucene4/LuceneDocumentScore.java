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
package org.eclipse.rdf4j.sail.lucene4;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.eclipse.rdf4j.sail.lucene.DocumentScore;
import org.eclipse.rdf4j.sail.lucene.SearchFields;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class LuceneDocumentScore extends LuceneDocumentResult implements DocumentScore {

	private final Highlighter highlighter;

	private static Set<String> requiredFields(boolean all) {
		return all ? null : Collections.singleton(SearchFields.URI_FIELD_NAME);
	}

	public LuceneDocumentScore(ScoreDoc doc, Highlighter highlighter, LuceneIndex index) {
		super(doc, index, requiredFields(highlighter != null));
		this.highlighter = highlighter;
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
		if (values == null) {
			return null;
		}
		return Iterables.transform(values, new Function<String, String>() {

			@Override
			public String apply(String text) {
				return index.getSnippet(field, text, highlighter);
			}
		});
	}
}
