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

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.openrdf.sail.lucene.BulkUpdater;
import org.openrdf.sail.lucene.SearchDocument;

public class ElasticsearchBulkUpdater implements BulkUpdater {
	private final Client client;
	private final BulkRequestBuilder bulkRequest;

	public ElasticsearchBulkUpdater(Client client) {
		this.client = client;
		this.bulkRequest = client.prepareBulk();
	}

	@Override
	public void add(SearchDocument doc) throws IOException {
		ElasticsearchDocument esDoc = (ElasticsearchDocument) doc;
		bulkRequest.add(client.prepareIndex(esDoc.getIndex(), esDoc.getType(), esDoc.getId()).setSource(esDoc.getSource()));
	}

	@Override
	public void update(SearchDocument doc) throws IOException {
		ElasticsearchDocument esDoc = (ElasticsearchDocument) doc;
		bulkRequest.add(client.prepareUpdate(esDoc.getIndex(), esDoc.getType(), esDoc.getId()).setVersion(esDoc.getVersion()).setDoc(esDoc.getSource()));
	}

	@Override
	public void delete(SearchDocument doc) throws IOException {
		ElasticsearchDocument esDoc = (ElasticsearchDocument) doc;
		bulkRequest.add(client.prepareDelete(esDoc.getIndex(), esDoc.getType(), esDoc.getId()).setVersion(esDoc.getVersion()));
	}

	@Override
	public void end() throws IOException {
		if(bulkRequest.numberOfActions() > 0) {
			BulkResponse response = bulkRequest.execute().actionGet();
			if(response.hasFailures())
			{
				throw new IOException(response.buildFailureMessage());
			}
		}
	}
}
