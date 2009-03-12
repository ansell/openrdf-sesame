/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.interceptors;

import static org.openrdf.http.protocol.Protocol.MAX_TIME_OUT;
import static org.openrdf.http.protocol.Protocol.TIME_OUT_UNITS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.openrdf.http.server.helpers.RequestAtt;
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
public class RepositoryInterceptor implements HandlerInterceptor, DisposableBean {

	/*---------*
	 * Statics *
	 *---------*/

	private static final String BASE = RepositoryInterceptor.class.getName() + "#";

	private static final String CON_CREATE_KEY = BASE + "create-connection";

	private static final String CON_CLOSED_KEY = BASE + "close-connection";

	private static final String QUERY_CREATE_KEY = BASE + "create-query";

	private static final String QUERY_CLOSED_KEY = BASE + "close-query";

	private static final String SELF_KEY = BASE + "self";

	// FIXME: use a random identifier to prevent guessing?
	private static final AtomicInteger seq = new AtomicInteger(new Random().nextInt());

	public static RepositoryConnection getModifyingConnection(HttpServletRequest request) {
		ConditionalRequestInterceptor.repositoryModified(request);
		return RequestAtt.getRepositoryConnection(request);
	}

	public static RepositoryConnection getRepositoryConnection(HttpServletRequest request)
		throws StoreException
	{
		ConditionalRequestInterceptor.notSafe(request);
		RepositoryConnection con = RequestAtt.getRepositoryConnection(request);
		if (con.isAutoCommit()) {
			ConditionalRequestInterceptor.repositoryModified(request);
		}
		return con;
	}

	public static RepositoryConnection getReadOnlyConnection(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection con = RequestAtt.getRepositoryConnection(request);
		if (!con.isAutoCommit()) {
			ConditionalRequestInterceptor.notSafe(request);
		}
		return con;
	}

	public static String createConnection(HttpServletRequest request) {
		ConditionalRequestInterceptor.notSafe(request);
		String id = Integer.toHexString(seq.getAndIncrement());
		request.setAttribute(CON_CREATE_KEY, id);
		return id;
	}

	public static void closeConnection(HttpServletRequest request) {
		ConditionalRequestInterceptor.notSafe(request);
		request.setAttribute(CON_CLOSED_KEY, Boolean.TRUE);
	}

	public static String saveQuery(HttpServletRequest request, Query query) {
		ConditionalRequestInterceptor.notSafe(request);
		String id = Integer.toHexString(seq.getAndIncrement());
		request.setAttribute(QUERY_CREATE_KEY, id);
		request.setAttribute(QUERY_CREATE_KEY + id, query);
		return id;
	}

	public static Query getQuery(HttpServletRequest request, String id)
		throws NotFound
	{
		ActiveConnection activeCon = RequestAtt.getActiveConnection(request);
		if (activeCon != null) {
			Query query = activeCon.getQuery(id);
			if (query != null) {
				return query;
			}
		}

		throw new NotFound(id);
	}

	public static void deleteQuery(HttpServletRequest request, String id) {
		ConditionalRequestInterceptor.notSafe(request);
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

	private final Logger logger = LoggerFactory.getLogger(RepositoryInterceptor.class);

	private final Map<String, ActiveConnection> activeConnections = new ConcurrentHashMap<String, ActiveConnection>();

	private final Map<ActiveConnection, HttpServletRequest> singleConnections = new ConcurrentHashMap<ActiveConnection, HttpServletRequest>();

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	private RepositoryManager repositoryManager;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public RepositoryInterceptor() {
		Runnable gc = new Runnable() {

			public void run() {
				closeExpiredConnections();
			}
		};

		executor.scheduleWithFixedDelay(gc, MAX_TIME_OUT, MAX_TIME_OUT, TIME_OUT_UNITS);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setRepositoryManager(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}

	public void destroy() {
		executor.shutdown();
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		throws HTTPException
	{
		ProtocolUtil.logRequestParameters(request);

		RequestAtt.setRepositoryManager(request, repositoryManager);

		String repositoryID = ProtocolUtil.getRepositoryID(request);
		if (repositoryID != null) {
			try {
				Repository repository = repositoryManager.getRepository(repositoryID);

				if (repository == null) {
					throw new NotFound("Unknown repository: " + repositoryID);
				}

				ActiveConnection activeCon;
				String connectionID = ProtocolUtil.getConnectionID(request);
				if (connectionID == null) {
					activeCon = new ActiveConnection(repository.getConnection());
					singleConnections.put(activeCon, request);
				}
				else {
					activeCon = activeConnections.get(connectionID);
					if (activeCon == null) {
						throw new NotFound("Unknown connection: " + connectionID);
					}
				}

				activeCon.open(request);
				RequestAtt.setRepository(request, repository);
				RequestAtt.setRepositoryConnection(request, activeCon.getConnection());
				RequestAtt.setActiveConnection(request, activeCon);
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
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception exception)
		throws ServerHTTPException
	{
		boolean close = request.getAttribute(CON_CLOSED_KEY) != null;
		String newId = (String)request.getAttribute(CON_CREATE_KEY);
		String queryId = (String)request.getAttribute(QUERY_CREATE_KEY);

		String id = ProtocolUtil.getConnectionID(request);
		RepositoryConnection repositoryCon = RequestAtt.getRepositoryConnection(request);
		ActiveConnection activeConnection = RequestAtt.getActiveConnection(request);

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

	private void closeExpiredConnections() {
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
}
