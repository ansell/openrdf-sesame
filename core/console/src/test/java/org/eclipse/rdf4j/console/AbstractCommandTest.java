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
package org.eclipse.rdf4j.console;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.common.io.IOUtil;
import org.eclipse.rdf4j.console.ConsoleIO;
import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.GraphUtil;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.config.RepositoryConfigUtil;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

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
