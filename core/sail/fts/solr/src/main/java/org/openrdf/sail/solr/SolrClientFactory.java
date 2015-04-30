package org.openrdf.sail.solr;

import org.apache.solr.client.solrj.SolrClient;

public interface SolrClientFactory
{
	SolrClient create(String spec);
}
