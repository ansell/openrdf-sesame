package org.openrdf.sail.solr.client.embedded;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.ConfigSolr;
import org.apache.solr.core.CoreContainer;
import org.openrdf.sail.solr.SolrClientFactory;

public class Factory implements SolrClientFactory {

	@Override
	public SolrClient create(String spec) {
		String path = spec.substring("embedded:".length());
		if(path.isEmpty()) {
			path = "/conf/solr.xml";
		}
		ConfigSolr config;
		File configFile = new File(path);
		if(configFile.exists()) {
			config = ConfigSolr.fromFile(null, configFile);
		}
		else {
			// try classpath
			InputStream in = getClass().getResourceAsStream(path);
			try {
				config = ConfigSolr.fromInputStream(null, in);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}
		CoreContainer cc = new CoreContainer(config);
		return new EmbeddedSolrServer(cc, "embedded");
	}
}
