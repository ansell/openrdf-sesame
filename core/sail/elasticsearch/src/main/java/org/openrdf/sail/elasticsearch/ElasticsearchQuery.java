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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.sail.lucene.DocumentScore;
import org.openrdf.sail.lucene.SearchQuery;

public class ElasticsearchQuery implements SearchQuery {
	private final SearchRequestBuilder request;
	private ElasticsearchIndex index;

	public ElasticsearchQuery(SearchRequestBuilder request, ElasticsearchIndex index) {
		this.request = request;
		this.index = index;
	}

	@Override
	public Iterable<? extends DocumentScore> query(Resource subject)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Highlights the given field or all fields if null.
	 */
	@Override
	public void highlight(URI property) {
		List<String> fields;
		if(property != null) {
			fields = Collections.singletonList(property.toString());
		}
		else {
			try {
				fields = ElasticsearchIndex.getPropertyFields(index.getFieldMappings().keySet());
			}
			catch(IOException ioe) {
				throw new ElasticsearchException("Failed to retrieve field mappings", ioe);
			}
		}
		for(String field : fields) {
			request.addHighlightedField(field);
		}
		request.setHighlighterPreTags(ElasticsearchIndex.HIGHLIGHTER_PRE_TAG);
		request.setHighlighterPostTags(ElasticsearchIndex.HIGHLIGHTER_POST_TAG);
		// Elastic Search doesn't really have the same support for fragments as Lucene.
		// So, we have to get back the whole highlighted value (comma-separated if it is a list)
		// and then post-process it into fragments ourselves.
		request.setHighlighterNumOfFragments(0);
	}

}
