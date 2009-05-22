/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import static org.openrdf.http.protocol.Protocol.BEGIN;
import static org.openrdf.http.protocol.Protocol.BNODES;
import static org.openrdf.http.protocol.Protocol.COMMIT;
import static org.openrdf.http.protocol.Protocol.CONFIGURATIONS;
import static org.openrdf.http.protocol.Protocol.CONNECTIONS;
import static org.openrdf.http.protocol.Protocol.CONTEXTS;
import static org.openrdf.http.protocol.Protocol.METADATA;
import static org.openrdf.http.protocol.Protocol.NAMESPACES;
import static org.openrdf.http.protocol.Protocol.PING;
import static org.openrdf.http.protocol.Protocol.PROTOCOL;
import static org.openrdf.http.protocol.Protocol.QUERIES;
import static org.openrdf.http.protocol.Protocol.REPOSITORIES;
import static org.openrdf.http.protocol.Protocol.ROLLBACK;
import static org.openrdf.http.protocol.Protocol.SCHEMAS;
import static org.openrdf.http.protocol.Protocol.SIZE;
import static org.openrdf.http.protocol.Protocol.STATEMENTS;
import static org.openrdf.http.protocol.Protocol.TEMPLATES;
import static org.openrdf.http.server.filters.ConnectionResolver.CONNECTION_ID_PARAM;
import static org.openrdf.http.server.filters.PreparedQueryResolver.QUERY_ID_PARAM;
import static org.openrdf.http.server.filters.RepositoryResolver.REPOSITORY_ID_PARAM;
import static org.openrdf.http.server.resources.ConfigurationResource.CONFIGURATION_ID_PARAM;
import static org.openrdf.http.server.resources.NamespaceResource.NS_PREFIX_PARAM;
import static org.openrdf.http.server.resources.TemplateResource.TEMPLATE_ID_PARAM;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;

import info.aduna.io.MavenUtil;

import org.openrdf.http.server.filters.AcceptParamFilter;
import org.openrdf.http.server.filters.ConnectionResolver;
import org.openrdf.http.server.filters.PreparedQueryResolver;
import org.openrdf.http.server.filters.QueryParser;
import org.openrdf.http.server.filters.QueryTypeRouter;
import org.openrdf.http.server.filters.RepositoryResolver;
import org.openrdf.http.server.filters.ScopedConnectionTagger;
import org.openrdf.http.server.helpers.ServerRepositoryManager;
import org.openrdf.http.server.resources.BNodesResource;
import org.openrdf.http.server.resources.BeginTxnResource;
import org.openrdf.http.server.resources.CommitTxnResource;
import org.openrdf.http.server.resources.ConfigurationListResource;
import org.openrdf.http.server.resources.ConfigurationResource;
import org.openrdf.http.server.resources.ConnectionListResource;
import org.openrdf.http.server.resources.ConnectionResource;
import org.openrdf.http.server.resources.ContextsResource;
import org.openrdf.http.server.resources.MetaDataResource;
import org.openrdf.http.server.resources.NamespaceListResource;
import org.openrdf.http.server.resources.NamespaceResource;
import org.openrdf.http.server.resources.PingConnectionResource;
import org.openrdf.http.server.resources.PreparedQueryResource;
import org.openrdf.http.server.resources.ProtocolResource;
import org.openrdf.http.server.resources.QueryListResource;
import org.openrdf.http.server.resources.RepositoryListResource;
import org.openrdf.http.server.resources.RollbackTxnResource;
import org.openrdf.http.server.resources.SchemaResource;
import org.openrdf.http.server.resources.SizeResource;
import org.openrdf.http.server.resources.StatementsResource;
import org.openrdf.http.server.resources.TemplateListResource;
import org.openrdf.http.server.resources.TemplateResource;
import org.openrdf.repository.manager.RepositoryManager;

/**
 * @author Arjohn Kampman
 */
public class SesameApplication extends Application {

	public static String getServerName() {
		return "OpenRDF Sesame server " + getServerVersion();
	}

	public static String getServerVersion() {
		return MavenUtil.loadVersion("org.openrdf.sesame", "sesame-http-server-restlet", "devel");
	}

	private final ServerRepositoryManager serverRepoManager;

	private final Context c;
	
	private int maxCacheAge;

	public SesameApplication(Context parentContext, RepositoryManager manager) {
		super(parentContext);
		setStatusService(new ErrorHandler());
		this.serverRepoManager = new ServerRepositoryManager(manager);
		this.c = getContext();
	}

	public ServerRepositoryManager getRepositoryManager() {
		return serverRepoManager;
	}

	@Override
	public synchronized Restlet createRoot() {
		Restlet root = createRootRouter();

		// Allow Accept-parameters to override Accept-headers:
		root = new AcceptParamFilter(c, root);

		return root;
	}

	protected Restlet createRootRouter() {
		Router router = new Router(c);
		router.attach("/" + PROTOCOL, ProtocolResource.class);
		router.attach("/" + SCHEMAS, SchemaResource.class);
		router.attach("/" + CONFIGURATIONS, createConfigurationsRouter());
		router.attach("/" + TEMPLATES, createTemplatesRouter());
		router.attach("/" + REPOSITORIES, createRepositoriesRouter());
		return router;
	}

	protected Restlet createConfigurationsRouter() {
		Router router = new Router(c);
		router.attach("", ConfigurationListResource.class);
		router.attach("/{" + CONFIGURATION_ID_PARAM + "}", ConfigurationResource.class);
		return router;
	}

	protected Restlet createTemplatesRouter() {
		Router router = new Router(c);
		router.attach("", TemplateListResource.class);
		router.attach("/{" + TEMPLATE_ID_PARAM + "}", TemplateResource.class);
		return router;
	}

	protected Restlet createRepositoriesRouter() {
		Router router = new Router(c);
		router.attach("", RepositoryListResource.class);
		router.attach("/{" + REPOSITORY_ID_PARAM + "}", new RepositoryResolver(c, createRepositoryRouter()));
		return router;
	}

	protected Restlet createRepositoryRouter() {
		Router router = new Router(c);
		router.attach("/" + CONNECTIONS, createExplicitConnectionRouter());
		router.attachDefault(new ScopedConnectionTagger(c, createImplicitConnectionRouter()));
		return router;
	}

	protected Restlet createExplicitConnectionRouter() {
		Router router = new Router(c);
		router.attach("", ConnectionListResource.class);
		router.attach("/{" + CONNECTION_ID_PARAM + "}", new ConnectionResolver(c, createConnectionRouter()));
		return router;
	}

	protected Restlet createImplicitConnectionRouter() {
		Router router = new Router(c);
		router.attach("", new QueryParser(c, new QueryTypeRouter(c)));
		router.attach("/" + STATEMENTS, StatementsResource.class);
		router.attach("/" + CONTEXTS, ContextsResource.class);
		router.attach("/" + SIZE, SizeResource.class);
		router.attach("/" + METADATA, MetaDataResource.class);
		router.attach("/" + NAMESPACES, createNamespacesRouter());
		return router;
	}

	protected Restlet createConnectionRouter() {
		Router router = new Router(c);
		router.attach("", new QueryParser(c, new QueryTypeRouter(c, ConnectionResource.class)));
		router.attach("/" + STATEMENTS, StatementsResource.class);
		router.attach("/" + CONTEXTS, ContextsResource.class);
		router.attach("/" + SIZE, SizeResource.class);
		router.attach("/" + METADATA, MetaDataResource.class);
		router.attach("/" + NAMESPACES, createNamespacesRouter());
		router.attach("/" + BNODES, BNodesResource.class);
		router.attach("/" + QUERIES, createQueriesRouter());
		router.attach("/" + BEGIN, BeginTxnResource.class);
		router.attach("/" + COMMIT, CommitTxnResource.class);
		router.attach("/" + ROLLBACK, RollbackTxnResource.class);
		router.attach("/" + PING, PingConnectionResource.class);
		return router;
	}

	protected Restlet createNamespacesRouter() {
		Router router = new Router(c);
		router.attach("", NamespaceListResource.class);
		router.attach("/{" + NS_PREFIX_PARAM + "}", NamespaceResource.class);
		return router;
	}

	protected Restlet createQueriesRouter() {
		Router router = new Router(c);
		router.attach("", new QueryParser(c, QueryListResource.class));
		router.attach("/{" + QUERY_ID_PARAM + "}", new PreparedQueryResolver(c, new QueryTypeRouter(c,
				PreparedQueryResource.class)));
		return router;
	}
}
