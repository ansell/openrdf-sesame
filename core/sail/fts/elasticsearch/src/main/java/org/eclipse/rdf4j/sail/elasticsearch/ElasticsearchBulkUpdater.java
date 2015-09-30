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
package org.eclipse.rdf4j.sail.elasticsearch;

import java.io.IOException;

import org.eclipse.rdf4j.sail.lucene.BulkUpdater;
import org.eclipse.rdf4j.sail.lucene.SearchDocument;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;

public class ElasticsearchBulkUpdater implements BulkUpdater {

	private final Client client;

	private final BulkRequestBuilder bulkRequest;

	public ElasticsearchBulkUpdater(Client client) {
		this.client = client;
		this.bulkRequest = client.prepareBulk();
	}

	@Override
	public void add(SearchDocument doc)
		throws IOException
	{
		ElasticsearchDocument esDoc = (ElasticsearchDocument)doc;
		bulkRequest.add(client.prepareIndex(esDoc.getIndex(), esDoc.getType(), esDoc.getId()).setSource(
				esDoc.getSource()));
	}

	@Override
	public void update(SearchDocument doc)
		throws IOException
	{
		ElasticsearchDocument esDoc = (ElasticsearchDocument)doc;
		bulkRequest.add(client.prepareUpdate(esDoc.getIndex(), esDoc.getType(), esDoc.getId()).setVersion(
				esDoc.getVersion()).setDoc(esDoc.getSource()));
	}

	@Override
	public void delete(SearchDocument doc)
		throws IOException
	{
		ElasticsearchDocument esDoc = (ElasticsearchDocument)doc;
		bulkRequest.add(client.prepareDelete(esDoc.getIndex(), esDoc.getType(), esDoc.getId()).setVersion(
				esDoc.getVersion()));
	}

	@Override
	public void end()
		throws IOException
	{
		if (bulkRequest.numberOfActions() > 0) {
			BulkResponse response = bulkRequest.execute().actionGet();
			if (response.hasFailures()) {
				throw new IOException(response.buildFailureMessage());
			}
		}
	}
}
