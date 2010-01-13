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
import static org.openrdf.http.protocol.Protocol.SESSION;
import static org.openrdf.http.protocol.Protocol.SIZE;
import static org.openrdf.http.protocol.Protocol.STATEMENTS;
import static org.openrdf.http.protocol.Protocol.TEMPLATES;
import static org.openrdf.http.server.filters.ConnectionResolver.CONNECTION_ID_PARAM;
import static org.openrdf.http.server.filters.PreparedQueryResolver.QUERY_ID_PARAM;
import static org.openrdf.http.server.filters.RepositoryResolver.REPOSITORY_ID_PARAM;
import static org.openrdf.http.server.resources.ConfigurationResource.CONFIGURATION_ID_PARAM;
import static org.openrdf.http.server.resources.NamespaceResource.NS_PREFIX_PARAM;
import static org.openrdf.http.server.resources.TemplateResource.TEMPLATE_ID_PARAM;

import java.io.File;
import java.io.IOException;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.engine.application.Encoder;
import org.restlet.resource.Finder;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

import info.aduna.app.AppConfiguration;
import info.aduna.io.MavenUtil;

import org.openrdf.http.server.auth.cas.CasAuthFilter;
import org.openrdf.http.server.auth.cas.CasProxyCallback;
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
import org.openrdf.http.server.session.SessionFilter;
import org.openrdf.http.server.session.SessionResource;
import org.openrdf.repository.manager.LocalRepositoryManager;
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

	private static AppConfiguration getAppConfig()
		throws IOException
	{
		AppConfiguration appConfig = new AppConfiguration("OpenRDF Sesame");
		appConfig.init();
		return appConfig;
	}

	private static final int COMPRESSION_THRESHOLD = 4096;

	private final File dataDir;

	private RepositoryManager repoManager;

	private ServerRepositoryManager serverRepoManager;

	public SesameApplication()
		throws IOException
	{
		this(getAppConfig().getDataDir());
	}

	public SesameApplication(File dataDir) {
		super();
		this.dataDir = dataDir;
		setStatusService(new ErrorHandler());
	}

	public SesameApplication(Context context)
		throws IOException
	{
		this(context, getAppConfig().getDataDir());
	}

	public SesameApplication(Context context, File dataDir) {
		super(context);
		this.dataDir = dataDir;
		setStatusService(new ErrorHandler());
	}

	public RepositoryManager getRepositoryManager() {
		return repoManager;
	}

	public ServerRepositoryManager getServerRepositoryManager() {
		return serverRepoManager;
	}

	@Override
	public synchronized void start()
		throws Exception
	{
		repoManager = new LocalRepositoryManager(dataDir);
		repoManager.initialize();
		serverRepoManager = new ServerRepositoryManager(repoManager);
		super.start();
	}

	@Override
	public synchronized void stop()
		throws Exception
	{
		try {
			super.stop();
		}
		finally {
			repoManager.shutDown();
		}
	}

	@Override
	public synchronized Restlet createInboundRoot() {
		Context c = getContext();
		Restlet root = createRootRouter(c);

		// Restore sessions
		root = new SessionFilter(c, root);

		// Allow Accept-parameters to override Accept-headers:
		root = new AcceptParamFilter(c, root);

		// Compress returned entities
		Encoder encoder = new Encoder(c);
		encoder.setMinimumSize(COMPRESSION_THRESHOLD);
		encoder.setNext(root);
		root = encoder;

		// root = new RequestLogger(c, root);

		return root;
	}

	protected Restlet createRootRouter(Context c) {
		Router router = new Router(c);
		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
		router.attach("/" + PROTOCOL, ProtocolResource.class);
		router.attach("/" + SCHEMAS, SchemaResource.class);
		router.attach("/" + CONFIGURATIONS, createConfigurationsRouter(c));
		router.attach("/" + TEMPLATES, createTemplatesRouter(c));
		router.attach("/" + REPOSITORIES, createRepositoriesRouter(c));
		router.attach("/" + SESSION, createSessionPath(c));
		router.attach("/cas-proxy-callback", CasProxyCallback.class);
		return router;
	}

	protected Restlet createConfigurationsRouter(Context c) {
		Router router = new Router(c);
		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
		router.attach("/{" + CONFIGURATION_ID_PARAM + "}", ConfigurationResource.class);
		router.attach("", ConfigurationListResource.class);
		return router;
	}

	protected Restlet createTemplatesRouter(Context c) {
		Router router = new Router(c);
		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
		router.attach("/{" + TEMPLATE_ID_PARAM + "}", TemplateResource.class);
		router.attach("", TemplateListResource.class);
		return router;
	}

	protected Restlet createRepositoriesRouter(Context c) {
		Router router = new Router(c);
		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
		router.attach("/{" + REPOSITORY_ID_PARAM + "}", new RepositoryResolver(c, createRepositoryRouter(c)));
		router.attach("", RepositoryListResource.class);
		return router;
	}

	protected Restlet createRepositoryRouter(Context c) {
		Router router = new Router(c);
		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
		router.attach("/" + CONNECTIONS, createExplicitConnectionRouter(c));
		router.attachDefault(new ScopedConnectionTagger(c, createImplicitConnectionRouter(c)));
		return router;
	}

	protected Restlet createExplicitConnectionRouter(Context c) {
		Router router = new Router(c);
		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
		router.attach("/{" + CONNECTION_ID_PARAM + "}", new ConnectionResolver(c, createConnectionRouter(c)));
		router.attach("", ConnectionListResource.class);
		return router;
	}

	protected Restlet createImplicitConnectionRouter(Context c) {
		Router router = new Router(c);
		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
		router.attach("/" + STATEMENTS, StatementsResource.class);
		router.attach("/" + CONTEXTS, ContextsResource.class);
		router.attach("/" + SIZE, SizeResource.class);
		router.attach("/" + METADATA, MetaDataResource.class);
		router.attach("/" + NAMESPACES, createNamespacesRouter(c));
		router.attach("", new QueryParser(c, new QueryTypeRouter(c)));
		return router;
	}

	protected Restlet createConnectionRouter(Context c) {
		Router router = new Router(c);
		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
		router.attach("/" + STATEMENTS, StatementsResource.class);
		router.attach("/" + CONTEXTS, ContextsResource.class);
		router.attach("/" + SIZE, SizeResource.class);
		router.attach("/" + METADATA, MetaDataResource.class);
		router.attach("/" + NAMESPACES, createNamespacesRouter(c));
		router.attach("/" + BNODES, BNodesResource.class);
		router.attach("/" + QUERIES, createQueriesRouter(c));
		router.attach("/" + BEGIN, BeginTxnResource.class);
		router.attach("/" + COMMIT, CommitTxnResource.class);
		router.attach("/" + ROLLBACK, RollbackTxnResource.class);
		router.attach("/" + PING, PingConnectionResource.class);
		router.attach("", new QueryParser(c, new QueryTypeRouter(c, ConnectionResource.class)));
		return router;
	}

	protected Restlet createNamespacesRouter(Context c) {
		Router router = new Router(c);
		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
		router.attach("/{" + NS_PREFIX_PARAM + "}", NamespaceResource.class);
		router.attach("", NamespaceListResource.class);
		return router;
	}

	protected Restlet createQueriesRouter(Context c) {
		Router router = new Router(c);
		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
		router.attach("/{" + QUERY_ID_PARAM + "}", new PreparedQueryResolver(c, new QueryTypeRouter(c,
				PreparedQueryResource.class)));
		router.attach("", new QueryParser(c, QueryListResource.class));
		return router;
	}

	protected Restlet createSessionPath(Context c) {
		Restlet result = new Finder(c, SessionResource.class);
		result = new CasAuthFilter(c, result);
		return result;
	}
}
