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
package org.openrdf.console;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import info.aduna.io.IOUtil;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Graph;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.Models;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author Dale Visser
 */
public class AbstractCommandTest {

	protected RepositoryManager manager;

	protected ConsoleIO streams = mock(ConsoleIO.class);

	protected final void addRepositories(String... identities)
		throws UnsupportedEncodingException, IOException, OpenRDFException
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (String identity : identities) {
			addRepository(classLoader.getResourceAsStream("federate/" + identity + "-config.ttl"),
					classLoader.getResource("federate/" + identity + ".ttl"));
		}
	}

	protected void addRepository(InputStream configStream, URL data)
		throws UnsupportedEncodingException, IOException, OpenRDFException
	{
		Repository systemRepo = manager.getSystemRepository();
		RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, systemRepo.getValueFactory());
		Model graph = new LinkedHashModel();
		rdfParser.setRDFHandler(new StatementCollector(graph));
		rdfParser.parse(new StringReader(IOUtil.readString(new InputStreamReader(configStream, "UTF-8"))),
				RepositoryConfigSchema.NAMESPACE);
		configStream.close();
		Resource repositoryNode = Models.subject(
				graph.filter(null, RDF.TYPE, RepositoryConfigSchema.REPOSITORY)).orElseThrow(
						() -> new RepositoryConfigException("could not find subject resource"));
		RepositoryConfig repoConfig = RepositoryConfig.create(graph, repositoryNode);
		repoConfig.validate();
		RepositoryConfigUtil.updateRepositoryConfigs(systemRepo, repoConfig);
		if (null != data) { // null if we didn't provide a data file
			final String repId = Models.objectLiteral(
					graph.filter(repositoryNode, RepositoryConfigSchema.REPOSITORYID, null)).orElseThrow(
							() -> new RepositoryConfigException("missing repository id")).stringValue();
			RepositoryConnection connection = manager.getRepository(repId).getConnection();
			try {
				connection.add(data, null, RDFFormat.TURTLE);
			}
			finally {
				connection.close();
			}
		}
	}

}
