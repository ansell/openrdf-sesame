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
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.openrdf.sail.lucene.BulkUpdater;
import org.openrdf.sail.lucene.SearchDocument;

public class SolrBulkUpdater implements BulkUpdater {
	private final SolrClient client;
	private final List<SolrInputDocument> addOrUpdateList = new ArrayList<SolrInputDocument>();
	private final List<String> deleteList = new ArrayList<String>();

	public SolrBulkUpdater(SolrClient client) {
		this.client = client;
	}

	@Override
	public void add(SearchDocument doc) throws IOException {
		SolrDocument document = ((SolrSearchDocument)doc).getDocument();
		addOrUpdateList.add(ClientUtils.toSolrInputDocument(document));
	}

	@Override
	public void update(SearchDocument doc) throws IOException {
		add(doc);
	}

	@Override
	public void delete(SearchDocument doc) throws IOException {
		deleteList.add(doc.getId());
	}

	@Override
	public void end() throws IOException {
		try {
			if(!deleteList.isEmpty()) {
				client.deleteById(deleteList);
			}
			if(!addOrUpdateList.isEmpty()) {
				client.add(addOrUpdateList);
			}
		} catch(SolrServerException e) {
			throw new IOException(e);
		}
	}
}
