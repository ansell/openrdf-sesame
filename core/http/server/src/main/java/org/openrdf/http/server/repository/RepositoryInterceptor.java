/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import static org.openrdf.http.protocol.Protocol.IF_NONE_MATCH;
import static org.openrdf.http.protocol.Protocol.MAX_TIME_OUT;
import static org.openrdf.http.protocol.Protocol.TIME_OUT_UNITS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.NotFound;
import org.openrdf.http.protocol.exceptions.ServerHTTPException;
import org.openrdf.http.server.helpers.ActiveConnection;
import org.openrdf.http.server.helpers.ProtocolUtil;
import org.openrdf.query.Query;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * Interceptor for repository requests. Handles the opening and closing of
 * connections to the repository specified in the request and handles the
 * caching headers.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class RepositoryInterceptor implements HandlerInterceptor, Runnable, DisposableBean {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String SERVER = "Server";

	private static final String DATE = "Date";

	private static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

	private static final String IF_MATCH = "If-Match";

	private static final String VARY = "Vary";

	private static final String ETAG = "ETag";

	private static final String IF_MODIFIED_SINCE = "If-Modified-Since";

	private static final String LAST_MODIFIED = "Last-Modified";

	private static final String REPOSITORIES = "/repositories/";

	private static final String CONNECTIONS = "/connections/";

	private static final String REPOSITORY_MANAGER = "repositoryManager";

	private static final String REPOSITORY_KEY = "repository";

	private static final String REPOSITORY_CONNECTION_KEY = "repositoryConnection";

	private static final String BASE = RepositoryInterceptor.class.getName() + "#";

	private static final String REPOSITORY_MODIFIED_KEY = BASE + "repository-modified";

	private static final String MANAGER_MODIFIED_KEY = BASE + "manager-modified";

	private static final String CONN_CREATE_KEY = BASE + "create-connection";

	private static final String CONN_CLOSED_KEY = BASE + "close-connection";

	private static final String QUERY_CREATE_KEY = BASE + "create-query";

	private static final String QUERY_MAP_KEY = BASE + "active-queries";

	private static final String QUERY_CLOSED_KEY = BASE + "close-query";

	private static final String NOT_SAFE_KEY = BASE + "not-safe";

	private static final String SELF_KEY = BASE + "self";

	// FIXME: use a random identifier to prevent guessing?
	private static AtomicInteger seq = new AtomicInteger(new Random().nextInt());

	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	public RepositoryInterceptor() {
		executor.scheduleWithFixedDelay(this, MAX_TIME_OUT, MAX_TIME_OUT, TIME_OUT_UNITS);
	}

	public static RepositoryManager getRepositoryManager(HttpServletRequest request) {
		request.setAttribute(MANAGER_MODIFIED_KEY, Boolean.TRUE);
		return (RepositoryManager)request.getAttribute(REPOSITORY_MANAGER);
	}

	public static RepositoryManager getReadOnlyManager(HttpServletRequest request) {
		return (RepositoryManager)request.getAttribute(REPOSITORY_MANAGER);
	}

	public static String getRepositoryID(HttpServletRequest request) {
		String path = request.getRequestURI();
		int start = path.indexOf(REPOSITORIES);
		if (start < 0) {
			return null;
		}
		String id = path.substring(start + REPOSITORIES.length());
		if (id.contains("/")) {
			id = id.substring(0, id.indexOf('/'));
		}
		if (id.length() == 0) {
			return null;
		}
		return id;
	}

	public static String getConnectionID(HttpServletRequest request) {
		String path = request.getRequestURI();
		int start = path.indexOf(CONNECTIONS);
		if (start < 0) {
			return null;
		}
		String id = path.substring(start + CONNECTIONS.length());
		if (id.contains("/")) {
			id = id.substring(0, id.indexOf('/'));
		}
		if (id.length() == 0) {
			return null;
		}
		return id;
	}

	public static Repository getRepository(HttpServletRequest request) {
		return (Repository)request.getAttribute(REPOSITORY_KEY);
	}

	public static RepositoryConnection getModifyingConnection(HttpServletRequest request) {
		request.setAttribute(REPOSITORY_MODIFIED_KEY, Boolean.TRUE);
		return (RepositoryConnection)request.getAttribute(REPOSITORY_CONNECTION_KEY);
	}

	public static RepositoryConnection getRepositoryConnection(HttpServletRequest request)
		throws StoreException
	{
		notSafe(request);
		Object attr = request.getAttribute(REPOSITORY_CONNECTION_KEY);
		RepositoryConnection con = (RepositoryConnection)attr;
		if (con.isAutoCommit()) {
			request.setAttribute(REPOSITORY_MODIFIED_KEY, Boolean.TRUE);
		}
		return con;
	}

	public static RepositoryConnection getReadOnlyConnection(HttpServletRequest request)
		throws StoreException
	{
		Object attr = request.getAttribute(REPOSITORY_CONNECTION_KEY);
		RepositoryConnection con = (RepositoryConnection)attr;
		if (!con.isAutoCommit()) {
			notSafe(request);
		}
		return con;
	}

	public static void notSafe(HttpServletRequest request) {
		request.setAttribute(NOT_SAFE_KEY, Boolean.TRUE);
	}

	public static String createConnection(HttpServletRequest request) {
		String id = Integer.toHexString(seq.getAndIncrement());
		request.setAttribute(CONN_CREATE_KEY, id);
		return id;
	}

	public static void closeConnection(HttpServletRequest request) {
		request.setAttribute(CONN_CLOSED_KEY, Boolean.TRUE);
	}

	public static String saveQuery(HttpServletRequest request, Query query) {
		String id = Integer.toHexString(seq.getAndIncrement());
		request.setAttribute(QUERY_CREATE_KEY, id);
		request.setAttribute(QUERY_CREATE_KEY + id, query);
		return id;
	}

	public static Query getQuery(HttpServletRequest request, String id)
		throws NotFound
	{
		ActiveConnection activeQueries = (ActiveConnection)request.getAttribute(QUERY_MAP_KEY);
		Query query = activeQueries.getQuery(id);
		if (query != null) {
			return query;
		}
		throw new NotFound(id);
	}

	public static void deleteQuery(HttpServletRequest request, String id) {
		request.setAttribute(QUERY_CLOSED_KEY, id);
	}

	/**
	 * @return Set of req.getMethod() + " " + req.getRequestURL()
	 */
	public static Collection<String> getActiveRequests(HttpServletRequest request) {
		RepositoryInterceptor self = (RepositoryInterceptor)request.getAttribute(SELF_KEY);
		List<String> result = new ArrayList<String>(self.activeConnections.size() * 2
				+ self.singleConnections.size());
		for (ActiveConnection con : self.activeConnections.values()) {
			result.addAll(con.getActiveRequests());
		}
		for (ActiveConnection con : self.singleConnections.keySet()) {
			result.addAll(con.getActiveRequests());
		}
		return result;
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private Logger logger = LoggerFactory.getLogger(RepositoryInterceptor.class);

	private String serverName;

	private RepositoryManager repositoryManager;

	private int maxCacheAge;

	private volatile long managerLastModified = System.currentTimeMillis();

	/** Sequential counter for more accurate Not-Modified responses. */
	private AtomicLong managerVersion = new AtomicLong((long)(Long.MAX_VALUE * Math.random()));

	private Map<String, Long> repositoriesLastModified = new ConcurrentHashMap<String, Long>();

	private ConcurrentMap<String, AtomicLong> repositoriesVersion = new ConcurrentHashMap<String, AtomicLong>();

	private Map<String, ActiveConnection> activeConnections = new ConcurrentHashMap<String, ActiveConnection>();

	private Map<ActiveConnection, HttpServletRequest> singleConnections = new ConcurrentHashMap<ActiveConnection, HttpServletRequest>();

	/*---------*
	 * Methods *
	 *---------*/

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public void setRepositoryManager(RepositoryManager repMan) {
		repositoryManager = repMan;
	}

	public void setMaxCacheAge(int maxCacheAge) {
		this.maxCacheAge = maxCacheAge;
	}

	public void destroy() {
		executor.shutdown();
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		throws HTTPException
	{
		ProtocolUtil.logRequestParameters(request);

		if (notModified(request, response)) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			postHandle(request, response, null, null);
			return false;
		}

		if (!precondition(request, response)) {
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
			response.setDateHeader(DATE, System.currentTimeMillis() / 1000 * 1000);
			return false;
		}

		request.setAttribute(REPOSITORY_MANAGER, repositoryManager);

		String repositoryID = getRepositoryID(request);
		if (repositoryID != null) {
			try {
				Repository repository = repositoryManager.getRepository(repositoryID);

				if (repository == null) {
					throw new NotFound("Unknown repository: " + repositoryID);
				}

				ActiveConnection repositoryCon;
				String connectionID = getConnectionID(request);
				if (connectionID == null) {
					repositoryCon = new ActiveConnection(repository.getConnection());
					singleConnections.put(repositoryCon, request);
				}
				else {
					repositoryCon = activeConnections.get(connectionID);
					if (repositoryCon == null) {
						throw new NotFound("Unknown connection: " + connectionID);
					}
				}
				repositoryCon.open(request);
				request.setAttribute(REPOSITORY_KEY, repository);
				request.setAttribute(REPOSITORY_CONNECTION_KEY, repositoryCon.getConnection());
				request.setAttribute(QUERY_MAP_KEY, repositoryCon);
				request.setAttribute(SELF_KEY, this);
			}
			catch (StoreConfigException e) {
				throw new ServerHTTPException(e.getMessage(), e);
			}
			catch (StoreException e) {
				throw new ServerHTTPException(e.getMessage(), e);
			}
		}
		return true;
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView)
	{
		long now = System.currentTimeMillis() / 1000 * 1000;
		long lastModified = lastModified(request); // update
		String eTag = eTag(request); // update
		response.setDateHeader(DATE, now);
		if (serverName != null) {
			response.setHeader(SERVER, serverName);
		}
		if (isSafe(request) && eTag != null && 0 < lastModified && lastModified < Long.MAX_VALUE) {
			response.setDateHeader(LAST_MODIFIED, lastModified);
			response.setHeader(ETAG, eTag);
			response.setHeader("Cache-Control", getCacheControl(now, lastModified));
		}
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception exception)
		throws ServerHTTPException
	{
		String id = getConnectionID(request);
		boolean close = request.getAttribute(CONN_CLOSED_KEY) != null;
		String newId = (String)request.getAttribute(CONN_CREATE_KEY);
		Object attr = request.getAttribute(REPOSITORY_CONNECTION_KEY);
		RepositoryConnection repositoryCon = (RepositoryConnection)attr;
		String queryId = (String)request.getAttribute(QUERY_CREATE_KEY);
		ActiveConnection activeConnection = (ActiveConnection)request.getAttribute(QUERY_MAP_KEY);
		if (activeConnection != null) {
			activeConnection.accessed(System.currentTimeMillis());
			activeConnection.close(request);
			if (id == null) {
				singleConnections.remove(activeConnection);
			}
		}
		if (queryId != null) {
			Query query = (Query)request.getAttribute(QUERY_CREATE_KEY + queryId);
			activeConnection.putQuery(queryId, query);
		}
		queryId = (String)request.getAttribute(QUERY_CLOSED_KEY);
		if (queryId != null) {
			activeConnection.removeQuery(queryId);
		}
		if (repositoryCon != null && (close || id == null && newId == null)) {
			try {
				repositoryCon.close();
				if (id != null) {
					activeConnections.remove(id);
				}
			}
			catch (StoreException e) {
				throw new ServerHTTPException(e.getMessage(), e);
			}
		}
		else if (newId != null) {
			activeConnections.put(newId, activeConnection);
		}
	}

	public void run() {
		long now = System.currentTimeMillis();
		long max = Protocol.TIME_OUT_UNITS.toMillis(Protocol.MAX_TIME_OUT);

		for (Entry<String, ActiveConnection> entry : activeConnections.entrySet()) {
			ActiveConnection activeCon = entry.getValue();

			long since = now - activeCon.getLastAccessed();

			if (since > max && !activeCon.isActive()) {
				String id = entry.getKey();
				logger.info("Connection {} has expired", id);
				activeConnections.remove(id);
				try {
					RepositoryConnection repCon = activeCon.getConnection();
					try {
						if (!repCon.isAutoCommit()) {
							logger.info("Rolling back transaction for expired connection {}", id);
							repCon.rollback();
						}
					}
					finally {
						repCon.close();
					}
				}
				catch (StoreException exc) {
					logger.error(exc.toString(), exc);
				}
			}
		}
	}

	private boolean notModified(HttpServletRequest request, HttpServletResponse response) {
		long since = request.getDateHeader(IF_MODIFIED_SINCE);
		if (since != -1) {
			response.addHeader(VARY, IF_MODIFIED_SINCE);
			if (since >= lastModified(request)) {
				return true;
			}
		}
		String etag = request.getHeader(IF_NONE_MATCH);
		if (etag != null) {
			response.addHeader(VARY, IF_NONE_MATCH);
			if (etag.equals(eTag(request))) {
				return true;
			}
		}
		return false;
	}

	private boolean precondition(HttpServletRequest request, HttpServletResponse response) {
		String etag = request.getHeader(IF_MATCH);
		if (etag != null) {
			response.addHeader(VARY, IF_MATCH);
			if (!etag.equals(eTag(request))) {
				return false;
			}
		}
		etag = request.getHeader(IF_NONE_MATCH);
		if (etag != null) {
			response.addHeader(VARY, IF_NONE_MATCH);
			if (etag.equals(eTag(request))) {
				return false;
			}
		}
		long since = request.getDateHeader(IF_UNMODIFIED_SINCE);
		if (since != -1) {
			response.addHeader(VARY, IF_UNMODIFIED_SINCE);
			if (since < lastModified(request)) {
				return false;
			}
		}
		return true;
	}

	private boolean isSafe(HttpServletRequest request) {
		if (request.getAttribute(MANAGER_MODIFIED_KEY) != null) {
			return false;
		}
		if (request.getAttribute(REPOSITORY_MODIFIED_KEY) != null) {
			return false;
		}
		if (request.getAttribute(CONN_CREATE_KEY) != null) {
			return false;
		}
		if (request.getAttribute(CONN_CLOSED_KEY) != null) {
			return false;
		}
		if (request.getAttribute(QUERY_CREATE_KEY) != null) {
			return false;
		}
		if (request.getAttribute(QUERY_CLOSED_KEY) != null) {
			return false;
		}
		if (request.getAttribute(NOT_SAFE_KEY) != null) {
			return false;
		}
		return true;
	}

	private String getCacheControl(long now, long lastModified) {
		long age = (now - lastModified) / 1000;
		if (maxCacheAge < 1 || age < 1) {
			return "no-cache";
		}
		// Modifications naturally occur near each other
		if (age < maxCacheAge) {
			return "max-age=" + age;
		}
		return "max-age=" + maxCacheAge;
	}

	private long lastModified(HttpServletRequest request) {
		long modified = managerLastModified(request);

		String id = getRepositoryID(request);
		if (id == null) {
			try {
				for (String i : repositoryManager.getRepositoryIDs()) {
					long repositoryModified = repositoryLastModified(i, request);
					if (modified < repositoryModified) {
						modified = repositoryModified;
					}
				}
			}
			catch (StoreConfigException e) {
				logger.error(e.toString(), e);
				return Long.MAX_VALUE;
			}
		}
		else {
			long repositoryModified = repositoryLastModified(id, request);
			if (modified < repositoryModified) {
				modified = repositoryModified;
			}
		}
		return modified;
	}

	private long managerLastModified(HttpServletRequest request) {
		if (request.getAttribute(MANAGER_MODIFIED_KEY) == null) {
			return managerLastModified;
		}
		else {
			return managerLastModified = System.currentTimeMillis() / 1000 * 1000;
		}
	}

	private long repositoryLastModified(String id, HttpServletRequest request) {
		if (request.getAttribute(REPOSITORY_MODIFIED_KEY) == null) {
			if (repositoriesLastModified.containsKey(id)) {
				return repositoriesLastModified.get(id);
			}
		}
		long now = System.currentTimeMillis() / 1000 * 1000;
		repositoriesLastModified.put(id, now);
		return now;
	}

	private String eTag(HttpServletRequest request) {
		long version = managerVersion(request);

		String id = getRepositoryID(request);
		if (id == null) {
			try {
				for (String i : repositoryManager.getRepositoryIDs()) {
					version += repositoryVersion(i, request);
				}
			}
			catch (StoreConfigException e) {
				logger.error(e.toString(), e);
				return null;
			}
		}
		else {
			version += repositoryVersion(id, request);
		}
		return "W/\"" + Long.toHexString(version) + "\"";
	}

	private long managerVersion(HttpServletRequest request) {
		if (request.getAttribute(MANAGER_MODIFIED_KEY) == null) {
			return managerVersion.longValue();
		}
		else {
			return managerVersion.incrementAndGet();
		}
	}

	private long repositoryVersion(String id, HttpServletRequest request) {
		AtomicLong seq = repositoriesVersion.get(id);
		if (seq == null) {
			long code = (long)(Long.MAX_VALUE * Math.random());
			AtomicLong o = repositoriesVersion.putIfAbsent(id, new AtomicLong(code));
			if (o == null) {
				return code;
			}
			else {
				return o.longValue();
			}
		}
		if (request.getAttribute(REPOSITORY_MODIFIED_KEY) == null) {
			return seq.longValue();
		}
		else {
			return seq.incrementAndGet();
		}
	}
}
