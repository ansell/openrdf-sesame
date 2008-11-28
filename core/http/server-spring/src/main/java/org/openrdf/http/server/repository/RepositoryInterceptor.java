/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import static org.openrdf.http.protocol.Protocol.IF_NONE_MATCH;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.NotFound;
import org.openrdf.http.protocol.exceptions.ServerHTTPException;
import org.openrdf.http.server.helpers.ProtocolUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * Interceptor for repository requests. Handles the opening and closing of
 * connections to the repository specified in the request.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class RepositoryInterceptor implements HandlerInterceptor {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String DATE = "Date";

	private static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

	private static final String IF_MATCH = "If-Match";

	private static final String VARY = "Vary";

	private static final String ETAG = "ETag";

	private static final String IF_MODIFIED_SINCE = "If-Modified-Since";

	private static final String LAST_MODIFIED = "Last-Modified";

	private static final String REPOSITORIES = "/repositories/";

	private static final String REPOSITORY_MANAGER = "repositoryManager";

	private static final String REPOSITORY_KEY = "repository";

	private static final String REPOSITORY_CONNECTION_KEY = "repositoryConnection";

	private static final String REPOSITORY_MODIFIED_KEY = RepositoryInterceptor.class.getName()
			+ "#repository-modified";

	private static final String MANAGER_MODIFIED_KEY = RepositoryInterceptor.class.getName()
			+ "#manager-modified";

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
		if (id.length() == 0)
			return null;
		return id;
	}

	public static Repository getRepository(HttpServletRequest request) {
		return (Repository)request.getAttribute(REPOSITORY_KEY);
	}

	public static RepositoryConnection getRepositoryConnection(HttpServletRequest request) {
		request.setAttribute(REPOSITORY_MODIFIED_KEY, Boolean.TRUE);
		return (RepositoryConnection)request.getAttribute(REPOSITORY_CONNECTION_KEY);
	}

	public static RepositoryConnection getReadOnlyConnection(HttpServletRequest request) {
		return (RepositoryConnection)request.getAttribute(REPOSITORY_CONNECTION_KEY);
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private RepositoryManager repositoryManager;

	private int maxCacheAge;

	private volatile long managerLastModified = System.currentTimeMillis();

	/** Sequential counter for more accurate Not-Modified responses. */
	private AtomicLong managerVersion = new AtomicLong((long)(Long.MAX_VALUE * Math.random()));

	private Map<String, Long> repositoriesLastModified = new ConcurrentHashMap<String, Long>();

	private ConcurrentMap<String, AtomicLong> repositoriesVersion = new ConcurrentHashMap<String, AtomicLong>();

	/*---------*
	 * Methods *
	 *---------*/

	public void setRepositoryManager(RepositoryManager repMan) {
		repositoryManager = repMan;
	}

	public void setMaxCacheAge(int maxCacheAge) {
		this.maxCacheAge = maxCacheAge;
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
			postHandle(request, response, null, null);
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

				RepositoryConnection repositoryCon = repository.getConnection();
				request.setAttribute(REPOSITORY_KEY, repository);
				request.setAttribute(REPOSITORY_CONNECTION_KEY, repositoryCon);
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
		if (isSafe(request)) {
			response.setDateHeader(LAST_MODIFIED, lastModified);
			response.setHeader(ETAG, eTag);
			response.setHeader("Cache-Control", getCacheControl(now, lastModified));
		}
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception exception)
		throws ServerHTTPException
	{
		RepositoryConnection repositoryCon = getReadOnlyConnection(request);
		if (repositoryCon != null) {
			try {
				repositoryCon.close();
			}
			catch (StoreException e) {
				throw new ServerHTTPException(e.getMessage(), e);
			}
		}
	}

	private boolean notModified(HttpServletRequest request, HttpServletResponse response) {
		RequestMethod method = RequestMethod.valueOf(request.getMethod());
		if (RequestMethod.GET.equals(method) || RequestMethod.HEAD.equals(method)) {
			long since = request.getDateHeader(IF_MODIFIED_SINCE);
			if (since != -1) {
				response.addHeader(VARY, IF_MODIFIED_SINCE);
				if (since >= lastModified(request))
					return true;
			}
			String etag = request.getHeader(IF_NONE_MATCH);
			if (etag != null) {
				response.addHeader(VARY, IF_NONE_MATCH);
				if (etag.equals(eTag(request)))
					return true;
			}
		}
		return false;
	}

	private boolean precondition(HttpServletRequest request, HttpServletResponse response) {
		String etag = request.getHeader(IF_MATCH);
		if (etag != null) {
			response.addHeader(VARY, IF_MATCH);
			if (!etag.equals(eTag(request)))
				return false;
		}
		etag = request.getHeader(IF_NONE_MATCH);
		if (etag != null) {
			response.addHeader(VARY, IF_NONE_MATCH);
			if (etag.equals(eTag(request)))
				return false;
		}
		long since = request.getDateHeader(IF_UNMODIFIED_SINCE);
		if (since != -1) {
			response.addHeader(VARY, IF_UNMODIFIED_SINCE);
			if (since < lastModified(request))
				return false;
		}
		return true;
	}

	private boolean isSafe(HttpServletRequest request) {
		if (request.getAttribute(MANAGER_MODIFIED_KEY) != null)
			return false;
		if (request.getAttribute(REPOSITORY_MODIFIED_KEY) != null)
			return false;
		return true;
	}

	private String getCacheControl(long now, long lastModified) {
		long age = (now - lastModified) / 1000;
		if (maxCacheAge < 1 || age < 1)
			return "no-cache";
		// Modifications naturally occur near each other
		if (age < maxCacheAge)
			return "max-age=" + age;
		return "max-age=" + maxCacheAge;
	}

	private long lastModified(HttpServletRequest request) {
		long modified = managerLastModified(request);

		String id = getRepositoryID(request);
		if (id == null)
			return modified;

		long repositoryModified = repositoryLastModified(id, request);
		if (modified < repositoryModified) {
			return repositoryModified;
		}
		else {
			return modified;
		}
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
		if (id != null) {
			version = version + repositoryVersion(id, request);
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
