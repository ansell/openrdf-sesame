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

import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.openrdf.sail.lucene.DocumentResult;
import org.openrdf.sail.lucene.SearchDocument;

public class LuceneDocumentResult implements DocumentResult {

	protected final ScoreDoc scoreDoc;

	protected final LuceneIndex index;

	private final Set<String> fields;

	private LuceneDocument fullDoc;

	public LuceneDocumentResult(ScoreDoc doc, LuceneIndex index, Set<String> fields)
	{
		this.scoreDoc = doc;
		this.index = index;
		this.fields = fields;
	}

	@Override
	public SearchDocument getDocument() {
		if (fullDoc == null) {
			Document doc = index.getDocument(scoreDoc.doc, fields);
			fullDoc = new LuceneDocument(doc, index.getSpatialStrategyMapper());
		}
		return fullDoc;
	}
}
