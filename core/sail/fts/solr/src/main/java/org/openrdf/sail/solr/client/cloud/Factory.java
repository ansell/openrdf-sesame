package org.openrdf.sail.solr.client.cloud;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.openrdf.sail.solr.SolrClientFactory;

public class Factory implements SolrClientFactory
{
	@Override
	public SolrClient create(String spec) {
		return new CloudSolrClient(spec.substring("cloud:".length()));
	}
}
