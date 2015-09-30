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
package org.eclipse.rdf4j.sail.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.eclipse.rdf4j.sail.lucene.BulkUpdater;
import org.eclipse.rdf4j.sail.lucene.SearchDocument;

public class SolrBulkUpdater implements BulkUpdater {

	private final SolrClient client;

	private final List<SolrInputDocument> addOrUpdateList = new ArrayList<SolrInputDocument>();

	private final List<String> deleteList = new ArrayList<String>();

	public SolrBulkUpdater(SolrClient client) {
		this.client = client;
	}

	@Override
	public void add(SearchDocument doc)
		throws IOException
	{
		SolrDocument document = ((SolrSearchDocument)doc).getDocument();
		addOrUpdateList.add(ClientUtils.toSolrInputDocument(document));
	}

	@Override
	public void update(SearchDocument doc)
		throws IOException
	{
		add(doc);
	}

	@Override
	public void delete(SearchDocument doc)
		throws IOException
	{
		deleteList.add(doc.getId());
	}

	@Override
	public void end()
		throws IOException
	{
		try {
			if (!deleteList.isEmpty()) {
				client.deleteById(deleteList);
			}
			if (!addOrUpdateList.isEmpty()) {
				client.add(addOrUpdateList);
			}
		}
		catch (SolrServerException e) {
			throw new IOException(e);
		}
	}
}
