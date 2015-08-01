package org.openrdf.sail.solr.client.http;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.openrdf.sail.solr.SolrClientFactory;

public class Factory implements SolrClientFactory {

	@Override
	public SolrClient create(String spec) {
		return new HttpSolrClient(spec);
	}
}
