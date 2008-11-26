/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.IOException;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.templates.ConfigTemplate;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 */
public class HTTPMemServer extends EmbeddedServer {

	public static final String TEST_REPO_ID = "memory";

	public static final String TEST_INFERENCE_REPO_ID = "memory-rdfs-dt";

	public static String SERVER_URL = "http://localhost:" + DEFAULT_PORT;

	public static String REPOSITORY_URL = Protocol.getRepositoryLocation(HTTPMemServer.SERVER_URL, TEST_REPO_ID);

	public static String INFERENCE_REPOSITORY_URL = Protocol.getRepositoryLocation(HTTPMemServer.SERVER_URL,
			TEST_INFERENCE_REPO_ID);

	public HTTPMemServer() throws IOException, StoreConfigException {
		super(DEFAULT_PORT);
	}

	@Override
	public void start()
		throws Exception
	{
		super.start();

		createTestRepositories();
	}

	/**
	 * @throws StoreException
	 */
	private void createTestRepositories()
		throws StoreException, StoreConfigException
	{
		RepositoryManager manager = getRepositoryManager();
		ConfigTemplate memory = manager.getConfigTemplateManager().getTemplate("memory");
		manager.addRepositoryConfig("memory", memory.createConfig(null));
		ConfigTemplate memory_rdfs_dt = manager.getConfigTemplateManager().getTemplate("memory-rdfs-dt");
		manager.addRepositoryConfig("memory-rdfs-dt", memory_rdfs_dt.createConfig(null));
	}
}
