package org.openrdf.sail.solr.client.embedded;

import java.io.File;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.core.SolrXmlConfig;
import org.openrdf.sail.solr.SolrClientFactory;

public class Factory implements SolrClientFactory {

	@Override
	public SolrClient create(String spec) {
		String solrHome = SolrResourceLoader.locateSolrHome();
		File configFile = new File(solrHome, SolrXmlConfig.SOLR_XML_FILE);
		return new EmbeddedSolrServer(CoreContainer.createAndLoad(solrHome, configFile), "embedded");
	}
}
